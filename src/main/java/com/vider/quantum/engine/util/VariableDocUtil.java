package com.vider.quantum.engine.util;

import com.google.api.services.docs.v1.model.*;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.vider.quantum.engine.constants.QuantumConstants;
import com.vider.quantum.engine.dto.*;
import com.vider.quantum.engine.manager.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class VariableDocUtil {

    private VariableDocUtil() {

    }

    public static List<Request> updateDocWithTabularSections(TemplateDocMetadataDto templateDocMetadataDto, TabularSection tabularSection, FileManager fileManager) {
        List<TabularSection> tabularSections = templateDocMetadataDto.getTabularSections();
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.FLOOR);
        RuleBasedNumberFormat currentIndexInWords = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
        List<Request> requests = new ArrayList<>();
        if (CollectionUtils.isEmpty(tabularSections)) {
            return Collections.emptyList();
        }
        int rows = getInitialRepeatOn(tabularSection, templateDocMetadataDto) + 1;
        int columns = 0;
        int tableStartIndex = 0;
        if (CollectionUtils.isNotEmpty(tabularSection.getColumns())) {
            columns = tabularSection.getColumns().size();
        }
        tableStartIndex = createTableRequestAndGetStartIndex(templateDocMetadataDto, tabularSection, fileManager, tableStartIndex, requests, rows, columns);
        addRowsDataRequests(tabularSection, tableStartIndex, columns, requests, rows);
        // create new section
        Map<String, String> existingFieldIds = new HashMap<>();
        List<Section> sections = templateDocMetadataDto.getSections();
        List<Section> newSections = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sections)) {
            sections.forEach(section -> {
                List<MetadataField> metadataFields = section.getMetadataFields();
                if (CollectionUtils.isNotEmpty(metadataFields)) {
                    metadataFields.forEach(metadataField -> existingFieldIds.put(metadataField.getFieldName(), metadataField.getFieldValue()));
                }
            });
            sections.stream()
                    .filter(section -> !tabularSection.getSectionId().equalsIgnoreCase(section.getSectionId()))
                    .forEach(newSections::add);
        }
        List<MetadataField> finalFields = createNewMetadataFieldsForTableSection(tabularSection, rows, existingFieldIds, currentIndexInWords, df);
        if (CollectionUtils.isNotEmpty(newSections)) {
            Section newTabularSection = Section.builder()
                    .metadataFields(finalFields)
                    .sectionId(tabularSection.getSectionId())
                    .build();
            newSections.add(newTabularSection);
            templateDocMetadataDto.setSections(newSections);
        }
        return requests;
    }

    private static List<MetadataField> createNewMetadataFieldsForTableSection(TabularSection tabularSection, int rows, Map<String, String> existingFieldIds, RuleBasedNumberFormat currentIndexInWords, DecimalFormat df) {
        List<MetadataField> finalFields = new ArrayList<>();
        Set<TableColumnMetadata> currentIndexRelatedColumns = tabularSection.getColumns().stream()
                .filter(column -> column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX) ||
                        column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX_IN_WORDS) ||
                        column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX_PERCENTAGE))
                .collect(Collectors.toSet());
        for (int i = 1; i < rows; i++) {
            int finalI = i;
            tabularSection.getMetadataFields().forEach(field -> {
                String fieldName = field.getFieldName() + finalI;
                String fieldValue = existingFieldIds.getOrDefault(fieldName, null);
                finalFields.add(MetadataField.builder()
                        .id(field.getId())
                        .type(field.getType())
                        .question(field.getQuestion())
                        .fieldValue(fieldValue)
                        .fieldName(fieldName)
                        .placeHolder(field.getPlaceHolder())
                        .hideInUi(field.isHideInUi())
                        .drivesConditionalContent(field.isDrivesConditionalContent())
                        .autoFillValue(captureAutoFillValue(field))
                        .tips(field.getTips())
                        .build());
            });
            currentIndexRelatedColumns.forEach(column -> {
                if (column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX)) {
                    finalFields.add(MetadataField.builder()
                            .fieldName(QuantumConstants.CURRENT_INDEX + finalI)
                            .fieldValue(String.valueOf(finalI))
                            .build());
                } else if (column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX_IN_WORDS)) {
                    finalFields.add(MetadataField.builder()
                            .fieldName(QuantumConstants.CURRENT_INDEX_IN_WORDS + finalI)
                            .fieldValue(currentIndexInWords.format(finalI, "%spellout-ordinal"))
                            .build());
                } else if (column.getValue().equalsIgnoreCase(QuantumConstants.CURRENT_INDEX_PERCENTAGE)) {
                    finalFields.add(MetadataField.builder()
                            .fieldName(QuantumConstants.CURRENT_INDEX_PERCENTAGE + finalI)
                            .fieldValue(df.format(100.00 / (rows - 1)))
                            .build());
                }
            });
        }
        return finalFields;
    }

    private static void addRowsDataRequests(TabularSection tabularSection, int tableStartIndex, int columns, List<Request> requests, int rows) {
        // Adding first row with table headers
        int cellIndex = tableStartIndex + 4;
        for (int j = 1; j <= columns; j++) {
            Map<Integer, String> columnWiseRowData = new HashMap<>();
            tabularSection.getColumns().forEach(c -> columnWiseRowData.put(c.getIndex(), c.getHeader()));
            requests.add(new Request().setInsertText(new InsertTextRequest()
                    .setText(columnWiseRowData.get(j))
                    .setLocation(new Location().setIndex(cellIndex))));
            cellIndex = cellIndex + columnWiseRowData.get(j).length() + 2;
        }
        // From second row onwards, put metadata element key
        Map<Integer, String> columnWiseRowData = new HashMap<>();
        tabularSection.getColumns().forEach(c -> columnWiseRowData.put(c.getIndex(), c.getValue()));
        for (int i = 2; i <= rows; i++) {
            cellIndex = cellIndex + 1; // add 1 to move to next row
            for (int j = 1; j <= columns; j++) {
                String cellText = "{{" + columnWiseRowData.get(j) + (i - 1) + "}}";
                requests.add(new Request().setInsertText(new InsertTextRequest()
                        .setText(cellText)
                        .setLocation(new Location().setIndex(cellIndex))));
                cellIndex = cellIndex + cellText.length() + 2;
            }
        }
        requests.add(new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria()
                                .setText("{{" + tabularSection.getSectionId() + "}}")
                                .setMatchCase(true))
                        .setReplaceText("")));
    }

    private static int createTableRequestAndGetStartIndex(TemplateDocMetadataDto templateDocMetadataDto, TabularSection tabularSection, FileManager fileManager, int tableStartIndex, List<Request> requests, int rows, int columns) {
        try {
            tableStartIndex = fileManager.getElementIndexFromDoc(templateDocMetadataDto.getDocumentId(), "{{" + tabularSection.getSectionId() + "}}");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        requests.add(
                new Request()
                        .setInsertTable(
                                new InsertTableRequest()
                                        .setLocation(new Location()
                                                .setIndex(tableStartIndex))
                                        .setRows(rows)
                                        .setColumns(columns)));
        return tableStartIndex;
    }

    public static List<Request> updateDocWithDynamicSections(TemplateDocMetadataDto templateDocMetadataDto) {
        List<Section> newSections = new ArrayList<>();
        List<DynamicSection> dynamicSections = templateDocMetadataDto.getDynamicSections();
        Set<String> alreadyCapturedMetadataFields = new HashSet<>();
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.FLOOR);
        List<Request> requests = new ArrayList<>();
        if (CollectionUtils.isEmpty(dynamicSections)) {
            return Collections.emptyList();
        }
        dynamicSections
                .forEach(dynamicSection -> {
                    String initialContent = dynamicSection.getContent();
                    StringBuilder content = new StringBuilder();
                    int initialRepeatOn = 0;
                    int deltaIndex = 0;
                    int totalNumberOfEffectiveElements;
                    initialRepeatOn = getInitialRepeatOn(dynamicSections, dynamicSection);
                    deltaIndex = getDeltaIndex(dynamicSections, dynamicSection, deltaIndex);
                    totalNumberOfEffectiveElements = getTotalNumberOfEffectiveElements(dynamicSections, dynamicSection, initialRepeatOn);
                    List<MetadataField> fields = new ArrayList<>();
                    for (int i = 1; i <= initialRepeatOn; i++) {
                        int currentIndex = i;
                        AtomicReference<String> tempContent = new AtomicReference<>(initialContent);
                        setCurrentContentUsingContentMapper(tempContent, dynamicSection, templateDocMetadataDto, currentIndex);
                        resolveCurrentIndexSupportedSystemVariables(df, deltaIndex, totalNumberOfEffectiveElements, i, tempContent);
                        if (CollectionUtils.isNotEmpty(dynamicSection.getMetadataFields())) {
                            dynamicSection.getMetadataFields().forEach(metadataField -> {
                                String fieldName = metadataField.getFieldName();
                                String indexedFieldName = fieldName + currentIndex;
                                resolveAutoFillFields(df, totalNumberOfEffectiveElements, tempContent, metadataField, currentIndex, templateDocMetadataDto);
                                tempContent.set(StringUtils.replaceAll(tempContent.get(), fieldName, indexedFieldName));
                                captureField(alreadyCapturedMetadataFields, fields, metadataField, indexedFieldName);
                            });
                        }
                        formatContentDependingOnTypeOfJoiner(dynamicSection, content, initialRepeatOn, deltaIndex, i, tempContent);
                    }
                    captureUpdateGoogleDocRequest(requests, dynamicSection, content);
                    captureSection(newSections, dynamicSection, fields);
                });
        updateTemplateDocSectionsIfNeeded(templateDocMetadataDto, newSections);
        return requests;
    }

    private static void setCurrentContentUsingContentMapper(AtomicReference<String> tempContent, DynamicSection dynamicSection, TemplateDocMetadataDto templateDocMetadataDto, int currentIndex) {
        StringBuilder content = new StringBuilder();
        List<Section> sections = templateDocMetadataDto.getSections();
        Map<String, String> fieldValues = new HashMap<>();
        if (CollectionUtils.isNotEmpty(sections)) {
            sections.forEach(section -> {
                List<MetadataField> metadataFields = section.getMetadataFields();
                if (CollectionUtils.isNotEmpty(metadataFields)) {
                    metadataFields.forEach(metadataField -> fieldValues.put(metadataField.getFieldName(), metadataField.getFieldValue()));
                }
            });
        }
        Map<String, ContentMetadata> contentMapper = dynamicSection.getContentMapper();
        if (MapUtils.isNotEmpty(contentMapper)) {
            List<ContentMetadata> contentMetadatas = new ArrayList<>(contentMapper.values().stream().toList());
            if (CollectionUtils.isNotEmpty(contentMetadatas)) {
                contentMetadatas.sort(Comparator.comparingInt(ContentMetadata::getIndex));
                contentMetadatas.forEach(contentMetadata -> {
                    String condition = contentMetadata.getCondition();
                    if (StringUtils.isNotEmpty(condition)) {
                        if (condition.contains("|")) {
                            String[] conditions = condition.split("\\|");
                            boolean allMatch = true;
                            for (String c : conditions) {
                                String[] conditionParameters = c.split(":");
                                if (!(fieldValues.containsKey(conditionParameters[0] + currentIndex)
                                        && conditionParameters[1].equalsIgnoreCase(fieldValues.get(conditionParameters[0] + currentIndex)))) {
                                    allMatch = false;
                                }
                            }
                            if (allMatch) {
                                content.append(contentMetadata.getContent()).append("\n");
                            }
                        } else {
                            String[] conditionParameters = condition.split(":");
                            if (fieldValues.containsKey(conditionParameters[0] + currentIndex)
                                    && conditionParameters[1].equalsIgnoreCase(fieldValues.get(conditionParameters[0] + currentIndex))) {
                                content.append(contentMetadata.getContent()).append("\n");
                            }
                        }
                    } else {
                        content.append(contentMetadata.getContent()).append("\n");
                    }
                });
            }
        }
        if (StringUtils.isNotEmpty(content.toString())) tempContent.set(content.toString());
    }

    private static void updateTemplateDocSectionsIfNeeded(TemplateDocMetadataDto templateDocMetadataDto,
                                                          List<Section> newSections) {
        if (CollectionUtils.isNotEmpty(newSections)) {
            List<Section> existingSections = templateDocMetadataDto.getSections();
            Set<String> newSectionIds = newSections.stream()
                    .map(Section::getSectionId)
                    .collect(Collectors.toSet());
            List<Section> newUpdatedSections = existingSections.stream()
                    .filter(section -> !newSectionIds.contains(section.getSectionId()))
                    .collect(Collectors.toList());
            newUpdatedSections.addAll(newSections);
            templateDocMetadataDto.setSections(newUpdatedSections);
        }
    }

    private static void captureSection(List<Section> newSections,
                                       DynamicSection dynamicSection,
                                       List<MetadataField> fields) {
        newSections.add(Section.builder()
                .metadataFields(fields)
                .sectionId(dynamicSection.getSectionId())
                .build());
    }

    private static void captureUpdateGoogleDocRequest(List<Request> requests,
                                                      DynamicSection dynamicSection,
                                                      StringBuilder content) {
        requests.add(new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria()
                                .setText("{{" + dynamicSection.getSectionId() + "}}")
                                .setMatchCase(true))
                        .setReplaceText(content.toString())));
    }

    private static void formatContentDependingOnTypeOfJoiner(DynamicSection dynamicSection,
                                                             StringBuilder content,
                                                             int initialRepeatOn,
                                                             int deltaIndex,
                                                             int i,
                                                             AtomicReference<String> tempContent) {
        if ("SINGLE_LINE".equalsIgnoreCase(dynamicSection.getContentJoiner())) {
            content.append(tempContent.get());
            if (i == initialRepeatOn - 1) {
                content.append(" and ");
            } else if (i != initialRepeatOn) {
                content.append(", ");
            }
        } else {
            int spacingBetweenElements = dynamicSection.getSpacingBetweenElements();
            if (spacingBetweenElements > 0) {
                content.append("\n".repeat(spacingBetweenElements));
            } else {
                content.append("\n");
            }
            content.append(i + deltaIndex)
                    .append(". ")
                    .append(tempContent.get());
        }
    }

    private static void captureField(Set<String> alreadyCapturedMetadataFields,
                                     List<MetadataField> fields,
                                     MetadataField metadataField,
                                     String indexedFieldName) {
        if (!alreadyCapturedMetadataFields.contains(indexedFieldName)) {
            alreadyCapturedMetadataFields.add(indexedFieldName);
            fields.add(MetadataField.builder()
                    .id(metadataField.getId())
                    .type(metadataField.getType())
                    .question(metadataField.getQuestion())
                    .fieldValue(metadataField.getFieldValue())
                    .fieldName(indexedFieldName)
                    .options(metadataField.getOptions())
                    .placeHolder(metadataField.getPlaceHolder())
                    .hideInUi(metadataField.isHideInUi())
                    .drivesConditionalContent(metadataField.isDrivesConditionalContent())
                    .autoFillValue(captureAutoFillValue(metadataField))
                    .tips(metadataField.getTips())
                    .build());
        }
    }

    private static AutoFillValue captureAutoFillValue(MetadataField metadataField) {
        AutoFillValue autoFillValue = metadataField.getAutoFillValue();
        if (autoFillValue != null) {
            return AutoFillValue.builder()
                    .dynamicConversionRateFromMetaDataField(autoFillValue.getDynamicConversionRateFromMetaDataField())
                    .copyValueFrom(autoFillValue.getCopyValueFrom())
                    .autoFillFromMetadata(autoFillValue.isAutoFillFromMetadata())
                    .conversionRequired(autoFillValue.isConversionRequired())
                    .copyValueFromIndexed(autoFillValue.getCopyValueFromIndexed())
                    .fieldNames(autoFillValue.getFieldNames())
                    .fieldName(autoFillValue.getFieldName())
                    .indexCopyValueFrom(autoFillValue.isIndexCopyValueFrom())
                    .indexedFieldNames(autoFillValue.getIndexedFieldNames())
                    .needToFetchTotalOfFields(autoFillValue.isNeedToFetchTotalOfFields())
                    .build();
        }
        return autoFillValue;
    }

    private static void resolveAutoFillFields(DecimalFormat df,
                                              int totalNumberOfEffectiveElements,
                                              AtomicReference<String> tempContent,
                                              MetadataField metadataField,
                                              int currentIndex,
                                              TemplateDocMetadataDto templateDocMetadataDto) {
        AutoFillValue autoFillValue = metadataField.getAutoFillValue();
        if (autoFillValue != null) {
            if (autoFillValue.isAutoFillFromMetadata()) {
                String autoFillValueFieldName = autoFillValue.getFieldName();
                if (StringUtils.isNotEmpty(autoFillValueFieldName)
                        && QuantumConstants.CURRENT_INDEX_PERCENTAGE.equalsIgnoreCase(autoFillValueFieldName)
                        && StringUtils.isEmpty(metadataField.getFieldValue())
                ) {
                    String currentIndexElementUpdatedValue = TemplateDocMetadataUtil.getSectionMetadataFieldValue(templateDocMetadataDto,
                            metadataField.getFieldName() + currentIndex);
                    if (StringUtils.isNotEmpty(currentIndexElementUpdatedValue)
                            && NumberUtils.isCreatable(currentIndexElementUpdatedValue)) {
                        tempContent.set(StringUtils.replaceAll(tempContent.get(), "\\{\\{" + metadataField.getFieldName() + "\\}\\}",
                                df.format(Double.parseDouble(currentIndexElementUpdatedValue))));
                    } else {
                        tempContent.set(StringUtils.replaceAll(tempContent.get(), "\\{\\{" + metadataField.getFieldName() + "\\}\\}",
                                df.format(100.00 / totalNumberOfEffectiveElements)));
                    }
                }
            }
            String copyValueFrom = autoFillValue.getCopyValueFrom();
            if (autoFillValue.isIndexCopyValueFrom()
                    && StringUtils.isNotEmpty(autoFillValue.getCopyValueFrom())
            ) {
                copyValueFrom = copyValueFrom + currentIndex;
                autoFillValue.setCopyValueFromIndexed(copyValueFrom);
            } else {
                autoFillValue.setCopyValueFromIndexed(copyValueFrom);
            }
        }
    }

    private static void resolveCurrentIndexSupportedSystemVariables(DecimalFormat df,
                                                                    int deltaIndex,
                                                                    int totalNumberOfEffectiveElements,
                                                                    int i,
                                                                    AtomicReference<String> tempContent) {
        RuleBasedNumberFormat currentIndexInWords = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
        tempContent.set(StringUtils.replaceAll(tempContent.get(), QuantumConstants.CURRENT_INDEX_IN_WORDS,
                currentIndexInWords.format(i + deltaIndex, "%spellout-ordinal")));
        tempContent.set(StringUtils.replaceAll(tempContent.get(), QuantumConstants.CURRENT_INDEX_PERCENTAGE,
                df.format(100.00 / totalNumberOfEffectiveElements)));
    }

    private static int getTotalNumberOfEffectiveElements(List<DynamicSection> dynamicSections,
                                                         DynamicSection dynamicSection,
                                                         int initialRepeatOn) {
        int totalNumberOfEffectiveElements;
        if (StringUtils.isNotEmpty(dynamicSection.getTotalElementsUsingLinkedDynamicSection())) {
            totalNumberOfEffectiveElements = getInitialRepeatOnOfLinkedDynamicSection(dynamicSections,
                    dynamicSection.getTotalElementsUsingLinkedDynamicSection());
        } else if (initialRepeatOn > 0) {
            totalNumberOfEffectiveElements = initialRepeatOn;
        } else {
            totalNumberOfEffectiveElements = 1;
        }
        return totalNumberOfEffectiveElements;
    }

    private static int getDeltaIndex(List<DynamicSection> dynamicSections,
                                     DynamicSection dynamicSection,
                                     int deltaIndex) {
        if (StringUtils.isNotEmpty(dynamicSection.getStartIndexAfterLinkedDynamicSSection())) {
            deltaIndex = getInitialRepeatOnOfLinkedDynamicSection(dynamicSections,
                    dynamicSection.getStartIndexAfterLinkedDynamicSSection());
        }
        return deltaIndex;
    }

    private static int getInitialRepeatOn(List<DynamicSection> dynamicSections,
                                          DynamicSection dynamicSection) {
        int initialRepeatOn = 0;
        if (dynamicSection.getRepeatOn() != null) {
            initialRepeatOn = Integer.parseInt(dynamicSection.getRepeatOn().getFieldValue());
        } else if (StringUtils.isNotEmpty(dynamicSection.getRepeatUsingLinkedDynamicSection())) {
            initialRepeatOn = getInitialRepeatOnOfLinkedDynamicSection(dynamicSections,
                    dynamicSection.getRepeatUsingLinkedDynamicSection());
        }
        return initialRepeatOn;
    }

    private static int getInitialRepeatOn(TabularSection tabularSection, TemplateDocMetadataDto templateDocMetadataDto) {
        int initialRepeatOn = 0;
        if (tabularSection.getRepeatOn() != null) {
            initialRepeatOn = Integer.parseInt(tabularSection.getRepeatOn().getFieldValue());
        } else if (StringUtils.isNotEmpty(tabularSection.getRepeatUsingLinkedDynamicSection())) {
            initialRepeatOn = getInitialRepeatOnOfLinkedDynamicSection(templateDocMetadataDto.getDynamicSections(),
                    tabularSection.getRepeatUsingLinkedDynamicSection());
        }
        return initialRepeatOn;
    }

    private static int getInitialRepeatOnOfLinkedDynamicSection(List<DynamicSection> dynamicSections,
                                                                String linkedDynamicSectionIds) {
        int initialRepeatOn = 0;
        if (StringUtils.isNotEmpty(linkedDynamicSectionIds) && CollectionUtils.isNotEmpty(dynamicSections)) {
            String[] ids = linkedDynamicSectionIds.split(",");
            for (String linkedDynamicSectionId : ids) {
                DynamicSection linkedDynamicSection = getDynamicSection(dynamicSections, linkedDynamicSectionId);
                if (linkedDynamicSection != null && linkedDynamicSection.getRepeatOn() != null) {
                    int temp = Integer.parseInt(linkedDynamicSection.getRepeatOn().getFieldValue());
                    initialRepeatOn = initialRepeatOn + temp;
                }
            }

        }
        return initialRepeatOn;
    }

    private static DynamicSection getDynamicSection(List<DynamicSection> dynamicSections,
                                                    String linkedDynamicSection) {
        return dynamicSections.stream()
                .filter(dynamicSection -> dynamicSection.getSectionId().equalsIgnoreCase(linkedDynamicSection))
                .findFirst()
                .orElse(null);
    }

    public static void updateElementsSequencing(TemplateDocMetadataDto templateDocMetadataDto) {
        Map<String, SequenceMetadata> sequencing = new LinkedHashMap<>();
        List<ElementSequenceMetadata> initialSequence = templateDocMetadataDto.getInitialSequence();
        if (CollectionUtils.isNotEmpty(initialSequence)) {
            initialSequence.sort(
                    Comparator.comparingInt(ElementSequenceMetadata::getSequence));
            AtomicInteger currentIndex = new AtomicInteger(1);
            initialSequence.forEach(elementSequenceMetadata -> {
                if (elementSequenceMetadata.isDynamicSectionFields())
                    populateSequencingForDynamicSectionFields(templateDocMetadataDto, elementSequenceMetadata, sequencing, currentIndex);
                else if (elementSequenceMetadata.isTabularSectionFields())
                    populateSequencingForTabularSectionFields(templateDocMetadataDto, elementSequenceMetadata, sequencing, currentIndex);
                else {
                    sequencing.put(elementSequenceMetadata.getFieldName(), SequenceMetadata.builder()
                            .sequence(currentIndex.getAndIncrement())
                            .isMutuallyExclusiveSectionSelectorField(elementSequenceMetadata.isMutuallyExclusiveSelectorField())
                            .metadata(fetchMetadataField(templateDocMetadataDto, elementSequenceMetadata))
                            .build());
                }
            });
            updateSequencingAsPerMutuallyExclusiveSections(templateDocMetadataDto, sequencing);
        }
        templateDocMetadataDto.setSequencing(sequencing);
    }

    private static void populateSequencingForTabularSectionFields(TemplateDocMetadataDto templateDocMetadataDto, ElementSequenceMetadata elementSequenceMetadata, Map<String, SequenceMetadata> sequencing, AtomicInteger currentIndex) {
        TabularSection tabularSection = fetchTabularSection(templateDocMetadataDto, elementSequenceMetadata);
        if (StringUtils.isNotEmpty(elementSequenceMetadata.getRepeatOnFieldName())) {
            sequencing.put(elementSequenceMetadata.getRepeatOnFieldName(), SequenceMetadata.builder()
                    .metadata(tabularSection.getRepeatOn())
                    .isRepeatOnField(true)
                    .tabularSection(tabularSection.getSectionId())
                    .isTabularSectionField(true)
                    .sequence(currentIndex.getAndIncrement())
                    .build());
        }
        List<String> fields = elementSequenceMetadata.getFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            int repeatBy = getInitialRepeatOn(tabularSection, templateDocMetadataDto);
            for (int i = 1; i <= repeatBy; i++) {
                int finalI = i;
                fields.forEach(field ->
                        sequencing.put(field + finalI, SequenceMetadata.builder()
                                .sequence(currentIndex.getAndIncrement())
                                .isTabularSectionField(true)
                                .metadata(fetchMetadataField(templateDocMetadataDto, field + finalI))
                                .build()));
            }
        }
    }

    private static void populateSequencingForDynamicSectionFields(TemplateDocMetadataDto templateDocMetadataDto, ElementSequenceMetadata elementSequenceMetadata, Map<String, SequenceMetadata> sequencing, AtomicInteger currentIndex) {
        DynamicSection dynamicSection = fetchDynamicSection(templateDocMetadataDto, elementSequenceMetadata);
        if (StringUtils.isNotEmpty(elementSequenceMetadata.getRepeatOnFieldName())) {
            sequencing.put(elementSequenceMetadata.getRepeatOnFieldName(), SequenceMetadata.builder()
                    .metadata(dynamicSection.getRepeatOn())
                    .isRepeatOnField(true)
                    .isDynamicSectionField(true)
                    .dynamicSection(dynamicSection.getSectionId())
                    .sequence(currentIndex.getAndIncrement())
                    .build());
        }
        List<String> fields = elementSequenceMetadata.getFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            int repeatBy = getInitialRepeatOn(templateDocMetadataDto.getDynamicSections(), dynamicSection);
            for (int i = 1; i <= repeatBy; i++) {
                int finalI = i;
                fields.forEach(field ->
                        sequencing.put(field + finalI, SequenceMetadata.builder()
                                .sequence(currentIndex.getAndIncrement())
                                .isDynamicSectionField(true)
                                .metadata(fetchMetadataField(templateDocMetadataDto, field + finalI))
                                .build()));
            }
        }
        List<SequencingFieldMetadata> fieldsWithMetadata = elementSequenceMetadata.getFieldsWithMetadata();
        if (CollectionUtils.isNotEmpty(fieldsWithMetadata)) {
            Map<String, String> existingFieldsData = new HashMap<>();
            List<Section> sections = templateDocMetadataDto.getSections();
            if (CollectionUtils.isNotEmpty(sections)) {
                sections.forEach(section -> {
                    List<MetadataField> metadataFields = section.getMetadataFields();
                    if (CollectionUtils.isNotEmpty(metadataFields)) {
                        metadataFields.forEach(metadataField -> existingFieldsData.put(metadataField.getFieldName(), metadataField.getFieldValue()));
                    }
                });
            }
            int repeatBy = getInitialRepeatOn(templateDocMetadataDto.getDynamicSections(), dynamicSection);
            for (int i = 1; i <= repeatBy; i++) {
                int finalI = i;
                fieldsWithMetadata.forEach(field -> {
                            if (StringUtils.isEmpty(field.getCondition()) || fieldConditionIsMet(field, finalI, existingFieldsData)) {
                                sequencing.put(field.getFieldName() + finalI, SequenceMetadata.builder()
                                        .sequence(currentIndex.getAndIncrement())
                                        .isDynamicSectionField(true)
                                        .metadata(fetchMetadataField(templateDocMetadataDto, field.getFieldName() + finalI))
                                        .build());

                            }
                        }
                );
            }
        }
    }

    private static boolean fieldConditionIsMet(SequencingFieldMetadata field, int finalI, Map<String, String> existingFieldsData) {
        String condition = field.getCondition();
        if (StringUtils.isNotEmpty(condition)) {
            if (condition.contains("|")) {
                String[] conditions = condition.split("\\|");
                boolean allMatch = true;
                for (String c : conditions) {
                    String[] conditionParameters = c.split(":");
                    if (!(existingFieldsData.containsKey(conditionParameters[0] + finalI)
                            && conditionParameters[1].equalsIgnoreCase(existingFieldsData.get(conditionParameters[0] + finalI)))) {
                        allMatch = false;
                    }
                }
                return allMatch;
            } else {
                String[] conditionParameters = condition.split(":");
                return existingFieldsData.containsKey(conditionParameters[0] + finalI)
                        && conditionParameters[1].equalsIgnoreCase(existingFieldsData.get(conditionParameters[0] + finalI));
            }
        }
        return false;
    }

    private static MetadataField fetchMetadataField(TemplateDocMetadataDto templateDocMetadataDto, String fieldName) {
        AtomicReference<MetadataField> metadataField = new AtomicReference<>(null);
        if (StringUtils.isNotEmpty(fieldName)) {
            List<Section> sections = templateDocMetadataDto.getSections();
            if (CollectionUtils.isNotEmpty(sections)) {
                sections.forEach(section -> {
                    if (CollectionUtils.isNotEmpty(section.getMetadataFields()) && metadataField.get() == null) {
                        section.getMetadataFields().stream()
                                .filter(field -> fieldName.equalsIgnoreCase(field.getFieldName()))
                                .findFirst()
                                .ifPresent(metadataField::set);
                    }
                });
            }
        }
        return metadataField.get();
    }

    private static MetadataField fetchMetadataField(TemplateDocMetadataDto templateDocMetadataDto, ElementSequenceMetadata elementSequenceMetadata) {
        String fieldName = elementSequenceMetadata.getFieldName();
        AtomicReference<MetadataField> metadataField = new AtomicReference<>(null);
        if (StringUtils.isNotEmpty(fieldName)) {
            List<Section> sections = templateDocMetadataDto.getSections();
            if (CollectionUtils.isNotEmpty(sections)) {
                sections.forEach(section -> {
                    if (CollectionUtils.isNotEmpty(section.getMetadataFields()) && metadataField.get() == null) {
                        section.getMetadataFields().stream()
                                .filter(field -> fieldName.equalsIgnoreCase(field.getFieldName()))
                                .findFirst()
                                .ifPresent(metadataField::set);
                    }
                });
            }
        }
        if (elementSequenceMetadata.isMutuallyExclusiveSelectorField()) {
            metadataField.set(templateDocMetadataDto.getMutuallyExclusiveSections().getSelector());
        }
        return metadataField.get();
    }

    private static DynamicSection fetchDynamicSection(TemplateDocMetadataDto templateDocMetadataDto, ElementSequenceMetadata elementSequenceMetadata) {
        AtomicReference<DynamicSection> dynamicSectionAtomicReference = new AtomicReference<>(null);
        if (StringUtils.isNotEmpty(elementSequenceMetadata.getDynamicSectionId())) {
            List<DynamicSection> dynamicSections = templateDocMetadataDto.getDynamicSections();
            if (CollectionUtils.isNotEmpty(dynamicSections) && dynamicSectionAtomicReference.get() == null) {
                dynamicSections.stream()
                        .filter(section ->
                                elementSequenceMetadata.getDynamicSectionId().equalsIgnoreCase(
                                        section.getSectionId()))
                        .findFirst()
                        .ifPresent(dynamicSectionAtomicReference::set);
            }
        }
        return dynamicSectionAtomicReference.get();
    }

    private static TabularSection fetchTabularSection(TemplateDocMetadataDto templateDocMetadataDto, ElementSequenceMetadata elementSequenceMetadata) {
        AtomicReference<TabularSection> tabularSectionAtomicReference = new AtomicReference<>(null);
        if (StringUtils.isNotEmpty(elementSequenceMetadata.getTabularSectionId())) {
            List<TabularSection> tabularSections = templateDocMetadataDto.getTabularSections();
            if (CollectionUtils.isNotEmpty(tabularSections) && tabularSectionAtomicReference.get() == null) {
                tabularSections.stream()
                        .filter(section ->
                                elementSequenceMetadata.getTabularSectionId().equalsIgnoreCase(
                                        section.getSectionId()))
                        .findFirst()
                        .ifPresent(tabularSectionAtomicReference::set);
            }
        }
        return tabularSectionAtomicReference.get();
    }

    public static List<Request> updateDocWithMutuallyExclusiveSections(TemplateDocMetadataDto templateDocMetadataDto) {
        List<Request> updateRequests = new ArrayList<>();
        List<MutuallyExclusiveSectionElement> activeSections = null;
        MutuallyExclusiveSection mutuallyExclusiveSections = templateDocMetadataDto.getMutuallyExclusiveSections();
        if (mutuallyExclusiveSections != null) {
            String selectorValue = mutuallyExclusiveSections.getSelector().getFieldValue();
            List<MutuallyExclusiveSectionElement> sectionsElements = mutuallyExclusiveSections.getElements();
            if (StringUtils.isNotEmpty(selectorValue)) {
                activeSections = sectionsElements.stream()
                        .filter(sectionElement -> selectorValue.equalsIgnoreCase(sectionElement.getActiveOnSelectorValue()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(activeSections)) {
                    List<String> mutuallyExclusiveSectionIds = sectionsElements.stream()
                            .map(MutuallyExclusiveSectionElement::getSectionId)
                            .collect(Collectors.toList());
                    List<Section> sections = templateDocMetadataDto.getSections();
                    if (CollectionUtils.isNotEmpty(sections)) {
                        List<Section> updatedSections = sections.stream()
                                .filter(section -> !mutuallyExclusiveSectionIds.contains(section.getSectionId()))
                                .collect(Collectors.toList());
                        activeSections.forEach(activeSection -> {
                            updatedSections.add(Section.builder()
                                    .sectionId(activeSection.getSectionId())
                                    .metadataFields(activeSection.getMetadataFields())
                                    .build());
                            updateRequests.add(new Request()
                                    .setReplaceAllText(new ReplaceAllTextRequest()
                                            .setContainsText(new SubstringMatchCriteria()
                                                    .setText("{{" + activeSection.getSectionId() + "}}")
                                                    .setMatchCase(true))
                                            .setReplaceText(activeSection.getContent())));
                        });
                        templateDocMetadataDto.setSections(updatedSections);
                    }
                }
            }
            updateMutuallyExclusiveSectionWithEmptyData(activeSections, sectionsElements, updateRequests);
        }
        return updateRequests;
    }

    private static void updateMutuallyExclusiveSectionWithEmptyData(List<MutuallyExclusiveSectionElement> activeSections, List<MutuallyExclusiveSectionElement> sectionsElements, List<Request> updateRequests) {
        List<String> sectionIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sectionsElements)) {
            if (CollectionUtils.isNotEmpty(activeSections)) {
                List<String> activeSectionIds = activeSections.stream()
                        .map(MutuallyExclusiveSectionElement::getSectionId)
                        .collect(Collectors.toList());
                sectionIds = sectionsElements.stream()
                        .map(MutuallyExclusiveSectionElement::getSectionId)
                        .filter(sectionId -> !activeSectionIds.contains(sectionId))
                        .collect(Collectors.toList());
            } else {
                sectionIds = sectionsElements.stream()
                        .map(MutuallyExclusiveSectionElement::getSectionId)
                        .collect(Collectors.toList());
            }
        }
        sectionIds.forEach(sectionId ->
                updateRequests.add(new Request()
                        .setReplaceAllText(new ReplaceAllTextRequest()
                                .setContainsText(new SubstringMatchCriteria()
                                        .setText("{{" + sectionId + "}}")
                                        .setMatchCase(true))
                                .setReplaceText("")))
        );
    }

    private static void updateSequencingAsPerMutuallyExclusiveSections(TemplateDocMetadataDto templateDocMetadataDto, Map<String, SequenceMetadata> sequencing) {
        MutuallyExclusiveSection mutuallyExclusiveSections = templateDocMetadataDto.getMutuallyExclusiveSections();
        if (MapUtils.isNotEmpty(sequencing)
                && mutuallyExclusiveSections != null
                && CollectionUtils.isNotEmpty(mutuallyExclusiveSections.getElements())) {
            String selectorValue = mutuallyExclusiveSections.getSelector().getFieldValue();
            List<String> metadataFieldsName = new ArrayList<>();
            if (StringUtils.isNotEmpty(selectorValue)) {
                List<String> activeSectionIds = mutuallyExclusiveSections.getElements().stream()
                        .filter(element -> selectorValue.equalsIgnoreCase(element.getActiveOnSelectorValue()))
                        .map(MutuallyExclusiveSectionElement::getSectionId)
                        .collect(Collectors.toList());
                mutuallyExclusiveSections.getElements().forEach(sectionElement -> {
                    List<MetadataField> metadataFields = sectionElement.getMetadataFields();
                    if (!activeSectionIds.contains(sectionElement.getSectionId())
                            && CollectionUtils.isNotEmpty(metadataFields)) {
                        metadataFields.stream()
                                .map(MetadataField::getFieldName)
                                .forEach(metadataFieldsName::add);
                    }
                });
            } else {
                mutuallyExclusiveSections.getElements().forEach(sectionElement -> {
                    List<MetadataField> metadataFields = sectionElement.getMetadataFields();
                    if (CollectionUtils.isNotEmpty(metadataFields)) {
                        metadataFields.stream()
                                .map(MetadataField::getFieldName)
                                .forEach(metadataFieldsName::add);
                    }
                });
            }
            metadataFieldsName.forEach(sequencing::remove);
        }
    }
}
