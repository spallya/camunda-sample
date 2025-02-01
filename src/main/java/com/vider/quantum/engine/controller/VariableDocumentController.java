package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import com.vider.quantum.engine.service.VariableDocumentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/vider/quantum/api/variable/doc")
@RequiredArgsConstructor
@Slf4j
public class VariableDocumentController {

    private final VariableDocumentService variableDocumentService;

    @GetMapping("/{processInstanceId}/export/{exportType}")
    public void exportDocInCurrentState(@PathVariable String processInstanceId,
                                        @PathVariable String exportType,
                                        @RequestParam(required = false) boolean withoutValues,
                                        @RequestParam(required = false) boolean downloadableDocId,
                                        @RequestParam(required = false) boolean signedPdf,
                                        HttpServletResponse response) throws IOException, GeneralSecurityException {
        variableDocumentService.exportFile(processInstanceId, exportType, withoutValues, downloadableDocId, signedPdf, response.getOutputStream());
    }

    @GetMapping("/{processInstanceId}/export/org/{orgId}")
    public void exportDocWithHeaderFooter(@PathVariable String processInstanceId,
                                        @PathVariable Integer orgId,
                                        HttpServletResponse response) throws IOException, GeneralSecurityException {
        variableDocumentService.exportDocWithHeaderFooter(processInstanceId, orgId, response.getOutputStream());
    }

    @PutMapping("/{processInstanceId}/variables")
    public ResponseEntity<Boolean> updateDocVariables(@PathVariable String processInstanceId, @RequestBody TemplateDocMetadataDto templateDocMetadataDto) throws GeneralSecurityException, IOException {
        return new ResponseEntity<>(variableDocumentService.updateDocVariables(processInstanceId, templateDocMetadataDto),
                HttpStatus.OK);
    }

    @GetMapping("/{processInstanceId}/download/{exportType}")
    public ResponseEntity<Resource> downloadDocInCurrentState(@PathVariable String processInstanceId, @PathVariable String exportType, HttpServletResponse response) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        variableDocumentService.exportFile(processInstanceId, exportType, false, false, false, outputStream);
        try (ByteArrayInputStream in = new ByteArrayInputStream(outputStream.toByteArray())) {
            InputStreamResource inputStreamResource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.docx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(inputStreamResource);
        }
    }

    @PostMapping("/{processInstanceId}/upload")
    public ResponseEntity<String> handleFileUpload(@PathVariable String processInstanceId,
                                                   @RequestParam("file") MultipartFile file) {
        variableDocumentService.uploadedSignedDocument(processInstanceId, file);
        return new ResponseEntity<>("You have successfully uploaded " + file.getOriginalFilename() + "!", HttpStatus.OK);
    }

}
