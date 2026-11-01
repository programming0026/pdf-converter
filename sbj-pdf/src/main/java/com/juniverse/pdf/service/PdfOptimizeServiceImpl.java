package com.juniverse.pdf.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link PdfOptimizeService} con Apache PDFBox.
 */
@Slf4j
@Service
public class PdfOptimizeServiceImpl implements PdfOptimizeService {

    @Override
    public void compress(MultipartFile inputFile, OutputStream outputFile) {
        // Cargar el archivo PDF
        try (PDDocument document = PDDocument.load(inputFile.getInputStream())) {

            PDPageTree pages = document.getDocumentCatalog().getPages();

            // Iterar sobre las páginas del documento PDF
            for (var page : pages) {
                PDResources resources = page.getResources();

                Iterable<COSName> xObjectNames = resources.getXObjectNames();

                // Iterar sobre los objetos XObject de la página
                for (var name : xObjectNames) {
                    if (page.getResources().isImageXObject(name)) {
                        // Obtener la imagen como PDImageXObject
                        PDImageXObject imageXObject = (PDImageXObject) page.getResources().getXObject(name);

                        // Convertir la imagen en un BufferedImage
                        BufferedImage bufferedImage = imageXObject.getImage();

                        // Comprimir la imagen guardándola como JPEG con menor calidad
                        BufferedImage compressedImage = new BufferedImage(
                                bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        compressedImage.getGraphics().drawImage(bufferedImage, 0, 0, null);

                        // Crear un archivo temporal para la imagen comprimida                        
                        ByteArrayOutputStream compressedImageFile = new ByteArrayOutputStream();
                        ImageIO.write(compressedImage, "jpg", compressedImageFile);

                        // Cargar la imagen comprimida en un nuevo PDImageXObject
                        PDImageXObject newImageXObject = PDImageXObject.createFromByteArray(document, compressedImageFile.toByteArray(), inputFile.getName());

                        // Reemplazar la imagen original con la comprimida
                        resources.put(name, newImageXObject);

                    }
                }
            }

            // Guardar el archivo PDF comprimido
            document.save(outputFile);
            log.info("PDF comprimido guardado");
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }

}
