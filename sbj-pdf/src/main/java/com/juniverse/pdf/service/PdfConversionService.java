package com.juniverse.pdf.service;

import com.juniverse.pdf.dto.FilesDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Conversión entre PDF e imágenes. */
public interface PdfConversionService {

    /** Convierte una imagen en un PDF. */
    void imageToPdf(MultipartFile inputPdfPath, OutputStream outputPdfPath);

    /** Convierte cada página del PDF en una imagen. */
    List<FilesDTO> pdfToImage(MultipartFile inputFile, int dpi) throws IOException;
}
