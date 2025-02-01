package com.vider.quantum.engine.manager;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.drive.Drive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleDriveManager {

    private static final String APPLICATION_NAME = "Technicalsand.com - Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private ResourceLoader resourceLoader;

    private GoogleCredential getGoogleCredential(NetHttpTransport HTTP_TRANSPORT) throws GeneralSecurityException, IOException {
        Resource resource = resourceLoader.getResource("classpath:certs/google-drive-service-account-auth.p12");
        InputStream inputStream = resource.getInputStream();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId("vider-quantum-sa@citric-dream-391714.iam.gserviceaccount.com")
                .setServiceAccountScopes(Collections.singleton("https://www.googleapis.com/auth/drive"))
                .setServiceAccountPrivateKeyFromP12File(inputStream)
                .setServiceAccountUser("vider-quantum-sa@citric-dream-391714.iam.gserviceaccount.com")
                .build();
        return credential;
    }

    public Drive getInstance() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = getGoogleCredential(HTTP_TRANSPORT);
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;

    }

    public Docs getDocsInstance() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = getGoogleCredential(HTTP_TRANSPORT);
        Docs docsService =
                new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        return docsService;
    }

}