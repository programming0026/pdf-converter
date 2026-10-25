package com.work.pdf.service;

//import com.spire.pdf.FileFormat;
//import com.spire.pdf.PdfDocument;
import com.work.pdf.dto.FilesDTO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author linux
 */
@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

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

    /*@Override
    public void pdfToWord(MultipartFile inputFilePath, OutputStream outputFilePath) {
        try {
            PdfDocument doc = new PdfDocument();

            //Load a sample PDF document
            doc.loadFromStream(inputFilePath.getInputStream());

            doc.getConvertOptions().setConvertToWordUsingFlow(true);
            doc.getConvertOptions().setKeepParagraph(true);

            //Convert PDF to Docx and save it to a specified path
            doc.saveToStream(outputFilePath, FileFormat.DOCX);           
            doc.close();
        } catch (IOException ex) {
            log.error("Error: ", ex);
        }
    }*/
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
