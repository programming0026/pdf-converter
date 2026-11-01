package com.juniverse.pdf.service;

import java.io.IOException;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link PdfSecurityService} con Apache PDFBox.
 */
@Slf4j
@Service
public class PdfSecurityServiceImpl implements PdfSecurityService {

    @Override
    public void removePassword(MultipartFile inputFile, OutputStream outputFile, String password) {
        try (PDDocument document = PDDocument.load(inputFile.getInputStream(), password)) {
            // Verificar si el documento está cifrado
            if (document.isEncrypted()) {
                // Desencriptar el documento
                document.setAllSecurityToBeRemoved(true);
            }

            // Guardar el documento sin la contraseña
            document.save(outputFile);
            log.info("PDF guardado sin contraseña");
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }

}
