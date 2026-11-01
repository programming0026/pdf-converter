package com.juniverse.pdf.service;

import com.juniverse.pdf.dto.FilesDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Operaciones que cambian la estructura o el contenido de un PDF: rotar, extraer páginas, unir, dividir y poner marca de agua. */
public interface PdfEditService {

    /** Rota las páginas del rango indicado (la primera página es la 1). */
    void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle, int startPage, int endPage);

    /** Rota todas las páginas. */
    void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle);

    /** Extrae un rango de páginas a un PDF nuevo. */
    void extractPages(MultipartFile inputPdfPath, OutputStream outputPdfPath, int startPage, int endPage);

    /** Une varios PDF en uno. */
    void merge(MultipartFile[] pdfFiles, OutputStream outputPdfPath);

    /** Divide el PDF en documentos que no superan un tamaño. */
    List<FilesDTO> split(MultipartFile inputPdfPath, long maxSizePerDocument);

    /** Escribe una marca de agua en todas las páginas. */
    void watermark(MultipartFile inputFile, OutputStream output, String watermarkText, int rotationAngle) throws IOException;
}
