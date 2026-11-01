package com.juniverse.pdf.service;

import com.juniverse.pdf.dto.FilesDTO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link PdfConversionService} con Apache PDFBox.
 */
@Slf4j
@Service
public class PdfConversionServiceImpl implements PdfConversionService {

    @Override
    public void imageToPdf(MultipartFile inputPdfPath, OutputStream outputPdfPath) {
        try (
                // Crear un nuevo documento PDF
                PDDocument document = new PDDocument()) {
            // Crear una nueva página PDF
            BufferedImage image = ImageIO.read(inputPdfPath.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("La imagen no es valida");
            }

            // Obtener el ancho de la imagen
            int width = image.getWidth();
            int height = image.getHeight();
            PDRectangle rectA4;
            log.debug("W: " + width + "- H: " + height);

            if (width > height) {
                rectA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            } else {
                rectA4 = new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            }

            PDPage page = new PDPage(rectA4);
            document.addPage(page);
            // Cargar la imagen como un objeto PDImageXObject
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, inputPdfPath.getBytes(), inputPdfPath.getName());
            // Obtener las dimensiones de la imagen
            float imageWidth = pdImage.getWidth();
            float imageHeight = pdImage.getHeight();

            // Obtener las dimensiones de la página A4
            PDRectangle pageSize = rectA4;
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            // Calcular la escala para ajustar la imagen dentro de la página sin deformarse
            float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);

            // Calcular el nuevo tamaño de la imagen basado en la escala
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;

            // Calcular la posición para centrar la imagen en la página
            float xPosition = (pageWidth - scaledWidth) / 2;
            float yPosition = (pageHeight - scaledHeight) / 2;

            try (
                    // Crear un ContentStream para agregar la imagen a la página
                    PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Dibujar la imagen en el PDF (centrada en la página)
                // Ajustar la posición y tamaño si es necesario
                contentStream.drawImage(pdImage, xPosition, yPosition, scaledWidth, scaledHeight);

            }
            // Guardar el documento PDF
            document.save(outputPdfPath);
            document.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }

    @Override
    public List<FilesDTO> pdfToImage(MultipartFile inputFile, int dpi) throws IOException {
        List<FilesDTO> pdfs = new ArrayList<>();
        String extension = "jpg";
        try (PDDocument document = PDDocument.load(inputFile.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                BufferedImage bim = pdfRenderer.renderImageWithDPI(
                        page, dpi, ImageType.RGB);
                String fileName = String.format("%s_image-%d.%s", inputFile.getOriginalFilename(), page + 1, extension);
                ImageIOUtil.writeImage(bim, extension, output, dpi);
                String file = Base64.getEncoder().encodeToString(output.toByteArray());
                pdfs.add(new FilesDTO(fileName, file));
            }
        }
        return pdfs;
    }

}
