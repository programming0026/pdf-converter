package com.juniverse.pdf.service;

import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

/** Seguridad de un PDF: quitar la contraseña. */
public interface PdfSecurityService {

    /** Quita la contraseña del PDF. */
    void removePassword(MultipartFile inputFile, OutputStream outputFile, String password);
}
