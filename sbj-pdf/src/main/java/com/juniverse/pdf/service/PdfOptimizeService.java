package com.juniverse.pdf.service;

import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

/** Optimización de un PDF: comprimirlo. */
public interface PdfOptimizeService {

    /** Comprime el PDF. */
    void compress(MultipartFile inputFile, OutputStream outputFile);
}
