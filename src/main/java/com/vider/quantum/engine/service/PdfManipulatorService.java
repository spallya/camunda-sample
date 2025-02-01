package com.vider.quantum.engine.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class PdfManipulatorService {

    private final S3Service s3Service;

    public static byte[] addHeaderFooterImages(InputStream pdfInputStream, InputStream headerImageInputStream, InputStream footerImageInputStream) throws IOException {
        try (PDDocument document = PDDocument.load(pdfInputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDImageXObject headerImage = PDImageXObject.createFromByteArray(document, headerImageInputStream.readAllBytes(), "header");
            PDImageXObject footerImage = PDImageXObject.createFromByteArray(document, footerImageInputStream.readAllBytes(), "footer");

            for (PDPage page : document.getPages()) {
                PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

                // Get the dimensions of the page
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // Calculate scaling for header image
                float headerImageWidth = headerImage.getWidth();
                float headerImageHeight = headerImage.getHeight();
                float headerScale = Math.min(pageWidth / headerImageWidth, pageHeight / 10 / headerImageHeight); // Scale to fit width, or 10% of page height
                float scaledHeaderWidth = headerImageWidth * headerScale;
                float scaledHeaderHeight = headerImageHeight * headerScale;

                // Calculate scaling for footer image
                float footerImageWidth = footerImage.getWidth();
                float footerImageHeight = footerImage.getHeight();
                float footerScale = Math.min(pageWidth / footerImageWidth, pageHeight / 10 / footerImageHeight); // Scale to fit width, or 10% of page height
                float scaledFooterWidth = footerImageWidth * footerScale;
                float scaledFooterHeight = footerImageHeight * footerScale;

                // Add header image
                float headerX = (pageWidth - scaledHeaderWidth) / 2;
                float headerY = pageHeight - scaledHeaderHeight - 20;
                contentStream.drawImage(headerImage, headerX, headerY, scaledHeaderWidth, scaledHeaderHeight);

                // Add footer image
                float footerX = (pageWidth - scaledFooterWidth) / 2;
                float footerY = 20;
                contentStream.drawImage(footerImage, footerX, footerY, scaledFooterWidth, scaledFooterHeight);

                contentStream.close();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] addHeaderAndFooterToExistingPdf(ByteArrayOutputStream outputStream, String headerImagePath, String footerImagePath) throws IOException {
        return addHeaderFooterImages(new ByteArrayInputStream(outputStream.toByteArray()),
                new ByteArrayInputStream(s3Service.downloadFile(headerImagePath)),
                new ByteArrayInputStream(s3Service.downloadFile(footerImagePath)));
    }
}

