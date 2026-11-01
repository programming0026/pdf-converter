package com.juniverse.pdf.service;

import org.springframework.web.multipart.MultipartFile;

/** Extracción de texto de un PDF. */
public interface PdfTextService {

    /** Extrae el texto. */
    String extractText(MultipartFile inputPdfPath);

    /** Extrae el texto. */
    String extractText(MultipartFile inputPdfPath, int startPage, int endPage);
}
