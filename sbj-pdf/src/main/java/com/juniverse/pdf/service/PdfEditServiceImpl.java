package com.juniverse.pdf.service;

import com.juniverse.pdf.dto.FilesDTO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link PdfEditService} con Apache PDFBox.
 */
@Slf4j
@Service
public class PdfEditServiceImpl implements PdfEditService {

    @Override
    public void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle, int startPage, int endPage) {
        try (
                // Cargar el archivo PDF original
                PDDocument document = PDDocument.load(inputPdfPath.getInputStream())) {
            // Rotar todas las páginas
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                int currentPage = i + 1;

                // Obtener la rotación actual
                int currentRotation = page.getRotation();

                if (currentPage >= startPage && currentPage <= endPage) {
                    // Establecer la nueva rotación sumando el ángulo deseado
                    page.setRotation(currentRotation + rotationAngle);
                } else {
                    page.setRotation(currentRotation);
                }
            }   // Guardar el documento con las páginas rotadas
            document.save(outputPdfPath);
            // Cerrar el documento
            document.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }


    @Override
    public void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle) {
        // Rotar todas las páginas
        try (
                // Cargar el archivo PDF original
                PDDocument document = PDDocument.load(inputPdfPath.getInputStream())) {
            // Rotar todas las páginas
            for (PDPage page : document.getPages()) {
                // Obtener la rotación actual
                int currentRotation = page.getRotation();
                // Establecer la nueva rotación sumando el ángulo deseado
                page.setRotation(currentRotation + rotationAngle);
            }   // Guardar el documento con las páginas rotadas
            document.save(outputPdfPath);

            document.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }


    @Override
    public void extractPages(MultipartFile inputPdfPath, OutputStream outputPdfPath, int startPage, int endPage) {
        PDDocument newDocument;
        // Crear un nuevo documento donde se almacenarán las páginas extraídas
        try (PDDocument originalDocument = PDDocument.load(inputPdfPath.getInputStream())) {
            // Crear un nuevo documento donde se almacenarán las páginas extraídas
            newDocument = new PDDocument();
            // Verificar que el rango esté dentro del número total de páginas
            int totalPages = originalDocument.getNumberOfPages();
            if (startPage < 1 || endPage > totalPages || startPage > endPage) {
                originalDocument.close();
                throw new IllegalArgumentException("Rango de páginas no válido.");
            }
            // Extraer las páginas del rango especificado
            for (int i = startPage - 1; i < endPage; i++) {
                PDPage page = originalDocument.getPage(i);
                newDocument.addPage(page);
            }   // Guardar el nuevo documento con las páginas extraídas
            newDocument.save(outputPdfPath);
            // Cerrar ambos documentos
            newDocument.close();
        } catch (Exception ex) {
            log.error("Error: ", ex);
        }
    }


    @Override
    public void merge(MultipartFile[] pdfFiles, OutputStream outputPdfPath) {
        try {
            // Crear una instancia de PDFMergerUtility
            PDFMergerUtility pdfMerger = new PDFMergerUtility();

            // Añadir cada archivo PDF a la lista de fuentes
            for (MultipartFile pdfFile : pdfFiles) {
                pdfMerger.addSource(pdfFile.getInputStream());
            }

            // Establecer la ruta del archivo de salida
            pdfMerger.setDestinationStream(outputPdfPath);

            // Fusionar los PDFs
            pdfMerger.mergeDocuments(null);
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }


    @Override
    public List<FilesDTO> split(MultipartFile inputPdfPath, long maxSizePerDocument) {
        // Cargar el archivo PDF original

        // Obtener el tamaño total del archivo PDF en bytes
        try (PDDocument document = PDDocument.load(inputPdfPath.getInputStream())) {
            // Obtener el tamaño total del archivo PDF en bytes
            long totalFileSize = inputPdfPath.getSize();
            log.debug("Tamaño total del archivo PDF: " + totalFileSize + " bytes");
            // Calcular el número total de páginas del PDF
            int totalPages = document.getNumberOfPages();
            // Calcular el número de documentos necesarios en función del tamaño máximo
            int numberOfDocuments = (int) Math.ceil((double) totalFileSize / maxSizePerDocument);
            log.debug("Número de documentos necesarios: " + numberOfDocuments);
            // Calcular el número de páginas por documento
            int pagesPerDocument = (int) Math.ceil((double) totalPages / numberOfDocuments);
            log.debug("Páginas por documento: " + pagesPerDocument);
            // Llamar a la función para dividir el PDF
            return splitPdf(document, pagesPerDocument);
        } catch (IOException ex) {
            log.error("Error: ", ex);
            return Collections.emptyList();
        }
    }

    // Función para dividir el PDF en documentos con un número específico de páginas

    private List<FilesDTO> splitPdf(PDDocument document, int pagesPerDocument) throws IOException {
        int totalPages = document.getNumberOfPages();
        int documentIndex = 1;

        List<FilesDTO> pdfs = new ArrayList<>();
        // Iterar sobre las páginas del PDF original
        for (int i = 0; i < totalPages; i += pagesPerDocument) {
            // Crear un nuevo documento PDF
            PDDocument newDocument = new PDDocument();

            // Obtener el rango de páginas para el nuevo documento
            int startPage = i;
            int endPage = Math.min(i + pagesPerDocument, totalPages);

            // Añadir las páginas seleccionadas al nuevo documento
            for (int j = startPage; j < endPage; j++) {
                PDPage page = document.getPage(j);
                newDocument.addPage(page);
            }

            // Guardar el nuevo documento con un nombre basado en el índice
            String outputFileName = "documento_parte_" + documentIndex + ".pdf";
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                newDocument.save(out);
                log.info("Documento creado: " + outputFileName);
                newDocument.close();
                byte[] bytes = out.toByteArray();
                pdfs.add(new FilesDTO(outputFileName, Base64.getEncoder().encodeToString(bytes)));
            } catch (IOException ex) {
                log.error("Error: ", ex);
            }

            documentIndex++;
        }

        return pdfs;
    }


    @Override
    public void watermark(MultipartFile inputFile, OutputStream output, String watermarkText, int rotationAngle) throws IOException {
        try (PDDocument document = PDDocument.load(inputFile.getInputStream())) {

            for (PDPage page : document.getPages()) {
                // Configurar transparencia de la marca de agua
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    // Configurar transparencia de la marca de agua
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setNonStrokingAlphaConstant(0.3f);  // Nivel de transparencia
                    contentStream.setGraphicsStateParameters(graphicsState);

                    // Ajustar la fuente y tamaño del texto
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 50);
                    // Color gris claro
                    contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
                    // Rotar y posicionar  
                    Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotationAngle), 300, 400);
                    contentStream.setTextMatrix(matrix);
                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }

            document.save(output);
            log.info("Marca de agua añadida correctamente.");
        }
    }

}
