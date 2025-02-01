package com.vider.quantum.engine.service;

import com.google.api.services.docs.v1.model.Request;
import com.vider.quantum.engine.dto.*;
import com.vider.quantum.engine.entities.vider.Organization;
import com.vider.quantum.engine.exception.QuantumException;
import com.vider.quantum.engine.helper.AutoFillResolverHelper;
import com.vider.quantum.engine.manager.FileManager;
import com.vider.quantum.engine.resolver.AutoFillResolverType;
import com.vider.quantum.engine.util.VariableDocUtil;
import jakarta.servlet.ServletOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vider.quantum.engine.constants.QuantumConstants.COMMA;

@Component
@RequiredArgsConstructor
@Slf4j
public class VariableDocumentService {

    private static final String TEMPLATE_DOC_METADATA = "templateDocMetadata";
    public static final String MASKED_VALUE = "___________";

    private final FileManager fileManager;
    private final QuantumService quantumService;
    private final OrganizationService organizationService;
    private final PdfManipulatorService pdfManipulatorService;

    public void exportFile(String processInstanceId,
                           String exportType,
                           boolean withoutValues,
                           boolean downloadableDocId,
                           boolean signedPdf, OutputStream outputStream) {
        try {
            Map<String, String> docVariableMap = new HashMap<>();
            ProcessDetailsDto processDetails = quantumService.getProcessDetails(processInstanceId);
            Map<String, Object> processDetailsData = processDetails.getData();
            if (signedPdf) {
                String signedDocumentId = String.valueOf(processDetailsData.getOrDefault("signedDocumentId", ""));
                if (StringUtils.isEmpty(signedDocumentId)) {
                    throw new QuantumException("Signed Document is not present for Process Id: " + processInstanceId);
                }
                fileManager.downloadFile(signedDocumentId, outputStream);
                return;
            }
            if (processDetailsData.containsKey(TEMPLATE_DOC_METADATA)) {
                TemplateDocMetadataDto templateDocMetadataDto =
                        (TemplateDocMetadataDto) processDetailsData.get(TEMPLATE_DOC_METADATA);
                buildDocVariableMap(exportType, docVariableMap, templateDocMetadataDto);
                List<Request> mutuallyExclusiveSectionsRequests = VariableDocUtil.updateDocWithMutuallyExclusiveSections(templateDocMetadataDto);
                List<Request> dynamicSectionsUpdateRequests = VariableDocUtil.updateDocWithDynamicSections(templateDocMetadataDto);
                if (!withoutValues) {
                    resolveAutoFillValues(docVariableMap, templateDocMetadataDto);
                    formatNumericValues(docVariableMap, templateDocMetadataDto);
                }
                fileManager.exportDocWithVariables(templateDocMetadataDto, exportType, docVariableMap, withoutValues, downloadableDocId,
                        dynamicSectionsUpdateRequests, mutuallyExclusiveSectionsRequests, outputStream, String.valueOf(processDetailsData.getOrDefault("templateName", "")));
            } else {
                throw new QuantumException("Invalid Variable Doc Process Id: " + processInstanceId);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new QuantumException(ex.getMessage());
        }
    }


    public void exportDocWithHeaderFooter(String processInstanceId, Integer orgId, ServletOutputStream outputStream) {
        try {
            Organization organization = organizationService.findById(orgId)
                    .orElseThrow(() -> new QuantumException("Organization not found for the org id: " + orgId));
            if (StringUtils.isEmpty(organization.getHeader()) && StringUtils.isEmpty(organization.getFooter())) {
                throw new QuantumException("Both header and footer are missing for the org id: " + orgId);
            }
            ProcessDetailsDto processDetails = quantumService.getProcessDetails(processInstanceId);
            Map<String, Object> processDetailsData = processDetails.getData();
            String name = processInstanceId;
            if (processDetailsData.containsKey("templateName")) {
                name = String.valueOf(processDetailsData.getOrDefault("templateName", ""));
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            exportFile(processInstanceId, "pdf", false,
                    false, false, byteArrayOutputStream);
            byte[] bytes = pdfManipulatorService.addHeaderAndFooterToExistingPdf(byteArrayOutputStream, organization.getHeader(), organization.getFooter());
            String fileName = fileManager.uploadFile(bytes, name, "application/pdf", "16Da_RN1luBLl37QjaKIFzfW0drm6YMK7", true);
            outputStream.write(fileName.getBytes());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new QuantumException(ex.getMessage());
        }
    }


    private void formatNumericValues(Map<String, String> docVariableMap, TemplateDocMetadataDto templateDocMetadataDto) {
        DecimalFormat formatter = new DecimalFormat("##,##,##,##,##,##,##0.00");
        Map<String, String> copiedMap = Map.copyOf(docVariableMap);
        Map<String, String> keyToValueType = new HashMap<>();
        if (CollectionUtils.isNotEmpty(templateDocMetadataDto.getSections())) {
            templateDocMetadataDto.getSections().forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().forEach(metadataField ->
                            keyToValueType.put(metadataField.getFieldName(), metadataField.getType()));
                }
            });
        }
        copiedMap.forEach((key, value) -> {
            if (NumberUtils.isCreatable(value)
                    && "NUMBER".equalsIgnoreCase(keyToValueType.getOrDefault(key, ""))) {
                double amount = Double.parseDouble(value);
                docVariableMap.put(key, formatter.format(amount));
            }
        });
    }

    private void buildDocVariableMap(String exportType,
                                     Map<String, String> docVariableMap,
                                     TemplateDocMetadataDto templateDocMetadataDto) {
        List<Section> sections = templateDocMetadataDto.getSections();
        if (CollectionUtils.isNotEmpty(sections)) {
            sections.forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().forEach(field ->
                            docVariableMap.put(field.getFieldName(), getMaskIfEmpty(field, exportType)));
                }
            });
        }
    }

    private void resolveAutoFillValues(Map<String, String> docVariableMap,
                                       TemplateDocMetadataDto templateDocMetadataDto) {
        AutoFillResolverHelper.executeResolvers(
                List.of(AutoFillResolverType.TOTALING), templateDocMetadataDto, docVariableMap);
    }

    private String getMaskIfEmpty(MetadataField metadataField,
                                  String exportType) {
        if ("html".equalsIgnoreCase(exportType) && StringUtils.isBlank(metadataField.getFieldValue())) {
            return "{{" + metadataField.getFieldName() + "}}";
        }
        return StringUtils.isNotEmpty(metadataField.getFieldValue()) ? metadataField.getFieldValue() : MASKED_VALUE;
    }

    public boolean updateDocVariables(String processInstanceId,
                                      TemplateDocMetadataDto updatedTemplateDocMetadataDto) {
        ProcessDetailsDto processDetails = quantumService.getProcessDetails(processInstanceId);
        Map<String, String> updatedVariablesMap = new HashMap<>();
        Map<String, Object> processDetailsData = processDetails.getData();
        if (processDetailsData.containsKey(TEMPLATE_DOC_METADATA)) {
            TemplateDocMetadataDto currentTemplateDocMetadataDto =
                    (TemplateDocMetadataDto) processDetailsData.get(TEMPLATE_DOC_METADATA);
            populateUpdatedVariableMap(updatedTemplateDocMetadataDto, updatedVariablesMap);
            updateCurrentTemplateDocMetadata(updatedVariablesMap, currentTemplateDocMetadataDto);
            updateDynamicSectionsDataIfRequired(currentTemplateDocMetadataDto, updatedTemplateDocMetadataDto, updatedVariablesMap);
            if (CollectionUtils.isNotEmpty(updatedTemplateDocMetadataDto.getTabularSections())) {
                currentTemplateDocMetadataDto.setTabularSections(updatedTemplateDocMetadataDto.getTabularSections());
            }
            if (CollectionUtils.isNotEmpty(currentTemplateDocMetadataDto.getTabularSections())) {
                currentTemplateDocMetadataDto.getTabularSections()
                        .forEach(tabularSection ->
                                VariableDocUtil.updateDocWithTabularSections(currentTemplateDocMetadataDto, tabularSection, fileManager));
            }
            updateHeaderAndFooterIfRequired(updatedTemplateDocMetadataDto, currentTemplateDocMetadataDto);
            updateMutuallyExclusiveSectionsData(updatedTemplateDocMetadataDto, currentTemplateDocMetadataDto);
            updateDropdownValuesForCurrentTemplateMetadataDoc(currentTemplateDocMetadataDto, updatedTemplateDocMetadataDto);
            VariableDocUtil.updateElementsSequencing(currentTemplateDocMetadataDto);
            return quantumService.updateProcessVariablesForVariableDocument(processInstanceId, processDetailsData);
        } else {
            throw new QuantumException("Invalid Variable Doc Process Id: " + processInstanceId);
        }
    }

    private void updateDropdownValuesForCurrentTemplateMetadataDoc(TemplateDocMetadataDto currentTemplateDocMetadataDto, TemplateDocMetadataDto updatedTemplateDocMetadataDto) {
        Map<String, List<String>> fieldToDropdownValues = new HashMap<>();
        updatedTemplateDocMetadataDto.getSections()
                .forEach(section -> {
                    List<MetadataField> metadataFields = section.getMetadataFields();
                    if (CollectionUtils.isNotEmpty(metadataFields)) {
                        metadataFields.forEach(field -> {
                            if ("dropdown".equalsIgnoreCase(field.getType())) {
                                fieldToDropdownValues.put(field.getFieldName(), field.getOptions());
                            }
                        });
                    }
                });
        currentTemplateDocMetadataDto.getSections()
                .forEach(section -> {
                    List<MetadataField> metadataFields = section.getMetadataFields();
                    if (CollectionUtils.isNotEmpty(metadataFields)) {
                        metadataFields.forEach(field -> {
                            if (fieldToDropdownValues.containsKey(field.getFieldName())) {
                                field.setOptions(fieldToDropdownValues.get(field.getFieldName()));
                            }
                        });
                    }
                });
    }

    private void updateMutuallyExclusiveSectionsData(TemplateDocMetadataDto updatedTemplateDocMetadataDto, TemplateDocMetadataDto currentTemplateDocMetadataDto) {
        MutuallyExclusiveSection updatedMutuallyExclusiveSections = updatedTemplateDocMetadataDto.getMutuallyExclusiveSections();
        MutuallyExclusiveSection currentMutuallyExclusiveSections = currentTemplateDocMetadataDto.getMutuallyExclusiveSections();
        if (updatedMutuallyExclusiveSections != null
                && currentMutuallyExclusiveSections != null
                && (!updatedMutuallyExclusiveSections.getSelector().getFieldValue()
                .equalsIgnoreCase(currentMutuallyExclusiveSections.getSelector().getFieldValue()))) {
            currentTemplateDocMetadataDto.setMutuallyExclusiveSections(
                    updatedTemplateDocMetadataDto.getMutuallyExclusiveSections());
            VariableDocUtil.updateDocWithMutuallyExclusiveSections(currentTemplateDocMetadataDto);
        }
    }

    private void updateHeaderAndFooterIfRequired(TemplateDocMetadataDto updatedTemplateDocMetadataDto,
                                                 TemplateDocMetadataDto currentTemplateDocMetadataDto) {
        if (StringUtils.isNotEmpty(updatedTemplateDocMetadataDto.getHeader())) {
            currentTemplateDocMetadataDto.setHeader(updatedTemplateDocMetadataDto.getHeader());
        }
        if (StringUtils.isNotEmpty(updatedTemplateDocMetadataDto.getFooter())) {
            currentTemplateDocMetadataDto.setFooter(updatedTemplateDocMetadataDto.getFooter());
        }
    }

    private void updateCurrentTemplateDocMetadata(Map<String, String> updatedVariablesMap,
                                                  TemplateDocMetadataDto currentTemplateDocMetadataDto) {
        currentTemplateDocMetadataDto.getSections().forEach(section -> {
            if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                section.getMetadataFields().forEach(field -> {
                    if (updatedVariablesMap.containsKey(field.getFieldName())) {
                        field.setFieldValue(updatedVariablesMap.get(field.getFieldName()));
                    }
                });
            }
        });
        AutoFillResolverHelper.executeResolvers(
                List.of(AutoFillResolverType.CONVERSION, AutoFillResolverType.DYNAMIC_OPTIONS),
                currentTemplateDocMetadataDto,
                updatedVariablesMap);
    }

    private void populateUpdatedVariableMap(TemplateDocMetadataDto updatedTemplateDocMetadataDto,
                                            Map<String, String> updatedVariablesMap) {
        updatedTemplateDocMetadataDto.getSections().forEach(section -> {
            if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                section.getMetadataFields().forEach(field ->
                        updatedVariablesMap.put(field.getFieldName(), field.getFieldValue()));
            }
        });
    }

    private void updateDynamicSectionsDataIfRequired(TemplateDocMetadataDto currentTemplateDocMetadataDto,
                                                     TemplateDocMetadataDto updatedTemplateDocMetadataDto,
                                                     Map<String, String> updatedVariablesMap) {
        if (CollectionUtils.isNotEmpty(updatedTemplateDocMetadataDto.getDynamicSections()) &&
                CollectionUtils.isNotEmpty(currentTemplateDocMetadataDto.getDynamicSections())) {
            Map<String, Integer> updatedDynamicRepCount = getUpdatedDynamicRepCount(updatedTemplateDocMetadataDto);
            identifyDynamicSectionsWhichNeedsToBeUpdated(currentTemplateDocMetadataDto, updatedDynamicRepCount);
            VariableDocUtil.updateDocWithDynamicSections(currentTemplateDocMetadataDto);
            currentTemplateDocMetadataDto.getSections().forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().forEach(field -> {
                        if (updatedVariablesMap.containsKey(field.getFieldName())
                                && !MASKED_VALUE.equalsIgnoreCase(updatedVariablesMap.get(field.getFieldName()))) {
                            field.setFieldValue(updatedVariablesMap.get(field.getFieldName()));
                        }
                    });
                }
            });
            currentTemplateDocMetadataDto.setDynamicSections(updatedTemplateDocMetadataDto.getDynamicSections());
        }
    }

    private Map<String, Integer> getUpdatedDynamicRepCount(TemplateDocMetadataDto updatedTemplateDocMetadataDto) {
        Map<String, Integer> updatedDynamicRepCount = new HashMap<>();
        updatedTemplateDocMetadataDto.getDynamicSections().forEach(dynamicSection -> {
            if (dynamicSection.getRepeatOn() != null
                    && StringUtils.isNotEmpty(dynamicSection.getRepeatOn().getFieldName())
            ) {
                updatedDynamicRepCount.put(dynamicSection.getRepeatOn().getFieldName(),
                        Integer.parseInt(dynamicSection.getRepeatOn().getFieldValue()));
            }
        });
        return updatedDynamicRepCount;
    }

    private void identifyDynamicSectionsWhichNeedsToBeUpdated(TemplateDocMetadataDto currentTemplateDocMetadataDto,
                                                              Map<String, Integer> updatedDynamicRepCount) {
        currentTemplateDocMetadataDto.getDynamicSections().forEach(dynamicSection -> {
            if (dynamicSection.getRepeatOn() != null
                    && StringUtils.isNotEmpty(dynamicSection.getRepeatOn().getFieldName())
                    && updatedDynamicRepCount.containsKey(dynamicSection.getRepeatOn().getFieldName())
            ) {
                if (updatedDynamicRepCount.get(dynamicSection.getRepeatOn().getFieldName()) ==
                        Integer.parseInt(dynamicSection.getRepeatOn().getFieldValue())) {
                    dynamicSection.setNoNeedToUpdateGoogleDoc(true);
                } else {
                    dynamicSection.getRepeatOn().setFieldValue(
                            String.valueOf(
                                    updatedDynamicRepCount.get(dynamicSection.getRepeatOn().getFieldName()).intValue()));
                    dynamicSection.setNoNeedToUpdateGoogleDoc(false);
                }
            } else {
                dynamicSection.setNoNeedToUpdateGoogleDoc(true);
            }
        });
        sectionsWhoseLinkedSectionGotUpdatedShouldAlsoGetUpdated(currentTemplateDocMetadataDto);
    }

    private void sectionsWhoseLinkedSectionGotUpdatedShouldAlsoGetUpdated(TemplateDocMetadataDto currentTemplateDocMetadataDto) {
        Set<String> updatedSectionIds = currentTemplateDocMetadataDto.getDynamicSections().stream()
                .filter(ds -> !ds.isNoNeedToUpdateGoogleDoc())
                .map(DynamicSection::getSectionId)
                .collect(Collectors.toSet());
        currentTemplateDocMetadataDto.getDynamicSections().forEach(dynamicSection -> {
            if (StringUtils.isNotEmpty(dynamicSection.getEffectiveLinkedDynamicSectionIds())) {
                String[] linkedDynamicSectionIds = dynamicSection.getEffectiveLinkedDynamicSectionIds().split(COMMA);
                for (String sectionId : linkedDynamicSectionIds) {
                    if (updatedSectionIds.contains(sectionId)) {
                        dynamicSection.setNoNeedToUpdateGoogleDoc(false);
                        break;
                    }
                }
            }
        });
    }

    public void uploadedSignedDocument(String processInstanceId, MultipartFile file) {
        String originalFilename = StringUtils.isNotEmpty(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
        String[] parts = originalFilename.split("\\.");
        if (parts != null && !parts[parts.length - 1].equalsIgnoreCase("pdf")) {
            throw new QuantumException("Only PDF files are allowed to be uploaded");
        }
        String signedDocId = fileManager.uploadFileToFolder(file, "16Da_RN1luBLl37QjaKIFzfW0drm6YMK7");
        quantumService.updateProcessVariables(processInstanceId, Map.of("signedDocumentId", signedDocId));
    }
}
