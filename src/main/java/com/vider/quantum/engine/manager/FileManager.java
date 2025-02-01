package com.vider.quantum.engine.manager;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.docs.v1.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.vider.quantum.engine.constants.QuantumConstants;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import com.vider.quantum.engine.util.VariableDocUtil;
import jakarta.servlet.ServletOutputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
@Slf4j
public class FileManager {

    private GoogleDriveManager googleDriveManager;

    public List<File> listEverything() throws IOException, GeneralSecurityException {
        // Print the names and IDs for up to 10 files.
        FileList result = googleDriveManager.getInstance().files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    public List<File> listFolderContent(String parentId) throws IOException, GeneralSecurityException {
        if (parentId == null) {
            parentId = "root";
        }
        String query = "'" + parentId + "' in parents";
        FileList result = googleDriveManager.getInstance().files().list()
                .setQ(query)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    public void deleteFile(String fileId) throws Exception {
        deleteCopiedDoc(fileId);
    }

    public String uploadFile(byte[] fileContent, String fileName, String contentType, String filePath) {
        try {
            String folderId = getFolderId(filePath);
            if (null != fileContent) {
                File fileMetadata = new File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(fileName);
                File uploadFile = googleDriveManager.getInstance()
                        .files()
                        .create(fileMetadata, new InputStreamContent(
                                contentType,
                                new ByteArrayInputStream(fileContent))
                        )
                        .setFields("id").execute();
                return uploadFile.getId();
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }



    public String uploadFile(byte[] fileContent, String fileName, String contentType, String folderId, boolean isFolderId) {
        try {
            if (null != fileContent) {
                File fileMetadata = new File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(fileName);
                File uploadFile = googleDriveManager.getInstance()
                        .files()
                        .create(fileMetadata, new InputStreamContent(
                                contentType,
                                new ByteArrayInputStream(fileContent))
                        )
                        .setFields("id").execute();
                return uploadFile.getId();
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }

    public String uploadFileToFolder(MultipartFile file, String folderId) {
        try {
            if (null != file) {
                File fileMetadata = new File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(file.getOriginalFilename());
                File uploadFile = googleDriveManager.getInstance()
                        .files()
                        .create(fileMetadata, new InputStreamContent(
                                file.getContentType(),
                                new ByteArrayInputStream(file.getBytes()))
                        )
                        .setFields("id").execute();
                return uploadFile.getId();
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }

    public String getFolderId(String path) throws Exception {
        String parentId = null;
        String[] folderNames = path.split("/");

        Drive driveInstance = googleDriveManager.getInstance();
        for (String name : folderNames) {
            parentId = findOrCreateFolder(parentId, name, driveInstance);
        }
        return parentId;
    }

    private String findOrCreateFolder(String parentId, String folderName, Drive driveInstance) throws Exception {
        String folderId = searchFolderId(parentId, folderName, driveInstance);
        // Folder already exists, so return id
        if (folderId != null) {
            return folderId;
        }
        //Folder dont exists, create it and return folderId
        File fileMetadata = new File();
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setName(folderName);

        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }
        return driveInstance.files().create(fileMetadata)
                .setFields("id")
                .execute()
                .getId();
    }

    private String searchFolderId(String parentId, String folderName, Drive service) throws Exception {
        String folderId = null;
        String pageToken = null;
        FileList result = null;

        File fileMetadata = new File();
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setName(folderName);

        do {
            String query = " mimeType = 'application/vnd.google-apps.folder' ";
            if (parentId == null) {
                query = query + " and 'root' in parents";
            } else {
                query = query + " and '" + parentId + "' in parents";
            }
            result = service.files().list().setQ(query)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (File file : result.getFiles()) {
                if (file.getName().equalsIgnoreCase(folderName)) {
                    folderId = file.getId();
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null && folderId == null);

        return folderId;
    }

    public void downloadFile(String id, OutputStream outputStream) throws IOException, GeneralSecurityException {
        if (id != null) {
            String fileId = id;
            googleDriveManager.getInstance().files().get(fileId).executeMediaAndDownloadTo(outputStream);
        }
    }

    public Object getFileAsJson(String fileId) throws GeneralSecurityException, IOException {
        Document response = googleDriveManager.getDocsInstance().documents().get(fileId).execute();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(response);
    }

    public void exportFile(String fileId, String exportType, ServletOutputStream outputStream) throws GeneralSecurityException, IOException {
        if (fileId != null) {
            exportUpdatedVariableDoc(exportType, outputStream, fileId);
        }
    }

    private String fetchMimeTypeFromExportType(String exportType) {
        String mimeType = "text/html";
        if ("pdf".equalsIgnoreCase(exportType)) {
            mimeType = "application/pdf";
        } else if ("docx".equalsIgnoreCase(exportType)) {
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return mimeType;
    }

    public void exportDocWithVariables(TemplateDocMetadataDto templateDocMetadataDto,
                                       String exportType,
                                       Map<String, String> docVariableMap,
                                       boolean withoutValues,
                                       boolean downloadableDocId,
                                       List<Request> dynamicSectionsUpdateRequests,
                                       List<Request> mutuallyExclusiveSectionsRequests,
                                       OutputStream outputStream, String templateName) throws GeneralSecurityException, IOException {
        String newDocumentId = copyTemplateDoc(templateDocMetadataDto.getDocumentId(), templateName);
        if (CollectionUtils.isNotEmpty(mutuallyExclusiveSectionsRequests)) {
            updateCopiedDoc(newDocumentId, mutuallyExclusiveSectionsRequests);
        }
        if (CollectionUtils.isNotEmpty(dynamicSectionsUpdateRequests)) {
            updateCopiedDoc(newDocumentId, dynamicSectionsUpdateRequests);
        }
        if (CollectionUtils.isNotEmpty(templateDocMetadataDto.getTabularSections())) {
            templateDocMetadataDto.setDocumentId(newDocumentId);
            templateDocMetadataDto.getTabularSections().forEach(tabularSection -> {
                List<Request> docWithTabularSections = VariableDocUtil.updateDocWithTabularSections(templateDocMetadataDto, tabularSection, this);
                try {
                    updateCopiedDoc(newDocumentId, docWithTabularSections);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (!withoutValues) {
            List<Request> requests = createUpdateRequests(docVariableMap, templateDocMetadataDto);
            updateCopiedDoc(newDocumentId, requests);
        } else {
            RuleBasedNumberFormat currentIndexInWords = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
            Map<String, String> indexedParameters = new HashMap<>();
            docVariableMap.forEach((key, value) -> {
                if (key.contains(QuantumConstants.CURRENT_INDEX)) {
                    indexedParameters.put(key, value);
                }
            });
            if (MapUtils.isNotEmpty(indexedParameters)) {
                int i = 1;
                while (indexedParameters.containsKey(QuantumConstants.CURRENT_INDEX + i) ||
                        indexedParameters.containsKey(QuantumConstants.CURRENT_INDEX_IN_WORDS + i)) {
                    if (indexedParameters.containsKey(QuantumConstants.CURRENT_INDEX + i)) {
                        indexedParameters.put(QuantumConstants.CURRENT_INDEX + i, String.valueOf(i));
                    }
                    if (indexedParameters.containsKey(QuantumConstants.CURRENT_INDEX_IN_WORDS + i)) {
                        indexedParameters.put(QuantumConstants.CURRENT_INDEX + i, currentIndexInWords.format(i, "%spellout-ordinal"));
                    }
                    i++;
                }
                List<Request> requests = createUpdateRequests(indexedParameters, templateDocMetadataDto);
                updateCopiedDoc(newDocumentId, requests);
            }
        }
        if (downloadableDocId) {
            outputStream.write(newDocumentId.getBytes());
            return;
        }
        exportUpdatedVariableDoc(exportType, outputStream, newDocumentId);
        deleteCopiedDoc(newDocumentId);
    }

    private static List<Request> createUpdateRequests(Map<String, String> docVariableMap, TemplateDocMetadataDto templateDocMetadataDto) {
        List<Request> requests = new ArrayList<>();
        docVariableMap.forEach((key, value) ->
                requests.add(new Request()
                        .setReplaceAllText(new ReplaceAllTextRequest()
                                .setContainsText(new SubstringMatchCriteria()
                                        .setText("{{" + key + "}}")
                                        .setMatchCase(true))
                                .setReplaceText(value))));
        updateHeaderFooterIfRequired(templateDocMetadataDto, requests);
        return requests;
    }

    private static void updateHeaderFooterIfRequired(TemplateDocMetadataDto templateDocMetadataDto, List<Request> requests) {
        if (StringUtils.isNotEmpty(templateDocMetadataDto.getHeader())) {
            requests.add(new Request()
                    .setReplaceAllText(new ReplaceAllTextRequest()
                            .setContainsText(new SubstringMatchCriteria()
                                    .setText("{{header}}")
                                    .setMatchCase(true))
                            .setReplaceText(templateDocMetadataDto.getHeader())));
        }
        if (StringUtils.isNotEmpty(templateDocMetadataDto.getFooter())) {
            requests.add(new Request()
                    .setReplaceAllText(new ReplaceAllTextRequest()
                            .setContainsText(new SubstringMatchCriteria()
                                    .setText("{{footer}}")
                                    .setMatchCase(true))
                            .setReplaceText(templateDocMetadataDto.getFooter())));
        }
    }

    public int getElementIndexFromDoc(String newDocumentId, String elementId) throws GeneralSecurityException, IOException {
        AtomicReference<ParagraphElement> e = new AtomicReference<>(null);
        if (StringUtils.isNotEmpty(elementId)) {
            Document response = googleDriveManager.getDocsInstance().documents().get(newDocumentId).execute();
            response.getBody().getContent().forEach(s -> {
                Paragraph paragraph = s.getParagraph();
                if (paragraph != null) {
                    List<ParagraphElement> elements = paragraph.getElements();
                    if (CollectionUtils.isNotEmpty(elements)) {
                        elements.forEach(e1 -> {
                            if (e1.getTextRun() != null && e1.getTextRun().getContent().contains(elementId)) {
                                e.set(e1);
                            }
                        });
                    }
                }
            });
        }
        return e.get() != null ? e.get().getStartIndex() : 0;
    }

    private void deleteCopiedDoc(String newDocumentId) throws IOException, GeneralSecurityException {
        googleDriveManager.getInstance().files().delete(newDocumentId).execute();
    }

    private void exportUpdatedVariableDoc(String exportType, OutputStream outputStream, String newDocumentId) throws IOException, GeneralSecurityException {
        String mimeType = fetchMimeTypeFromExportType(exportType);
        googleDriveManager.getInstance().files().export(newDocumentId, mimeType).executeMediaAndDownloadTo(outputStream);
    }

    public void updateCopiedDoc(String newDocumentId, List<Request> requests) throws IOException, GeneralSecurityException {
        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest();
        googleDriveManager.getDocsInstance().documents().batchUpdate(newDocumentId, body.setRequests(requests)).execute();
    }

    public String copyTemplateDoc(String documentId, String title) throws IOException, GeneralSecurityException {
        if (StringUtils.isEmpty(title)) title = "Copy Title";
        File copyMetadata = new File().setName(title);
        copyMetadata.setParents(Collections.singletonList("16Da_RN1luBLl37QjaKIFzfW0drm6YMK7"));
        File documentCopyFile = googleDriveManager.getInstance().files().copy(documentId, copyMetadata).execute();
        return documentCopyFile.getId();
    }

    public void exportSignedPdf(String exportType, String signedDocumentId, OutputStream outputStream) throws GeneralSecurityException, IOException {
        exportUpdatedVariableDoc(exportType, outputStream, signedDocumentId);
    }
}