package com.juniverse.pdf.service;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link PdfTextService} con Apache PDFBox.
 */
@Slf4j
@Service
public class PdfTextServiceImpl implements PdfTextService {

    @Override
    public String extractText(MultipartFile inputPdfPath) {
        String text = "";
        try (
                PDDocument document = PDDocument.load(inputPdfPath.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            // Extraer el texto del PDF
            text = pdfStripper.getText(document);
            log.debug("Text: {}", text);
            document.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
        return text;
    }


    @Override
    public String extractText(MultipartFile inputPdfPath, int startPage, int endPage) {
        String text = "";
        try (PDDocument document = PDDocument.load(inputPdfPath.getInputStream())) {
            int totalPages = document.getNumberOfPages();
            if (startPage < 1 || endPage > totalPages || startPage > endPage) {
                document.close();
                throw new IllegalArgumentException("Rango de páginas no válido.");
            }
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(startPage);
            pdfStripper.setEndPage(endPage);
            text = pdfStripper.getText(document);
            document.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
        return text;
    }

}
