package com.juniverse.pdf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.pdf.dto.FilesDTO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** Pruebas de los servicios de PDF con documentos generados en memoria. */
class PdfServicesTest {

    private final PdfEditService edit = new PdfEditServiceImpl();
    private final PdfTextService text = new PdfTextServiceImpl();
    private final PdfConversionService conversion = new PdfConversionServiceImpl();
    private final PdfSecurityService security = new PdfSecurityServiceImpl();
    private final PdfOptimizeService optimize = new PdfOptimizeServiceImpl();

    /** Crea un PDF con una línea de texto «Pagina N» en cada página. */
    private static byte[] pdf(int pages) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 1; i <= pages; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 12);
                    content.newLineAtOffset(50, 700);
                    content.showText("Pagina " + i);
                    content.endText();
                }
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("file", "demo.pdf", "application/pdf", bytes);
    }

    private static PDDocument open(ByteArrayOutputStream out) throws IOException {
        return PDDocument.load(new ByteArrayInputStream(out.toByteArray()));
    }

    @Test
    void extraeElTextoDeTodoElDocumentoYDeUnRango() throws IOException {
        MockMultipartFile input = file(pdf(3));
        String all = text.extractText(input);
        assertTrue(all.contains("Pagina 1") && all.contains("Pagina 3"));
        String range = text.extractText(input, 2, 2);
        assertTrue(range.contains("Pagina 2"));
        assertTrue(!range.contains("Pagina 1"));
    }

    @Test
    void unRangoDeTextoInvalidoLanzaIllegalArgumentException() throws IOException {
        MockMultipartFile input = file(pdf(2));
        assertThrows(IllegalArgumentException.class, () -> text.extractText(input, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> text.extractText(input, 2, 5));
        assertThrows(IllegalArgumentException.class, () -> text.extractText(input, 2, 1));
    }

    @Test
    void rotaTodasLasPaginas() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        edit.rotate(file(pdf(2)), out, 90);
        try (PDDocument doc = open(out)) {
            assertEquals(90, doc.getPage(0).getRotation());
            assertEquals(90, doc.getPage(1).getRotation());
        }
    }

    @Test
    void rotaSoloElRangoIndicado() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        edit.rotate(file(pdf(3)), out, 180, 2, 2);
        try (PDDocument doc = open(out)) {
            assertEquals(0, doc.getPage(0).getRotation());
            assertEquals(180, doc.getPage(1).getRotation());
            assertEquals(0, doc.getPage(2).getRotation());
        }
    }

    @Test
    void extraeUnRangoDePaginas() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        edit.extractPages(file(pdf(4)), out, 2, 3);
        try (PDDocument doc = open(out)) {
            assertEquals(2, doc.getNumberOfPages());
        }
    }

    @Test
    void unePdfEnUnSoloDocumento() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MockMultipartFile[] files = {file(pdf(2)), file(pdf(3))};
        edit.merge(files, out);
        try (PDDocument doc = open(out)) {
            assertEquals(5, doc.getNumberOfPages());
        }
    }

    @Test
    void divideElPdfEnVariosDocumentos() throws IOException {
        byte[] bytes = pdf(6);
        List<FilesDTO> parts = edit.split(file(bytes), bytes.length / 3 + 1);
        assertTrue(parts.size() > 1, "debe haber más de un documento");
        assertTrue(parts.stream().allMatch(p -> p.getFileName() != null && !p.getFile().isEmpty()));
    }

    @Test
    void conviertePaginasEnImagenes() throws IOException {
        List<FilesDTO> images = conversion.pdfToImage(file(pdf(2)), 72);
        assertEquals(2, images.size());
    }

    @Test
    void convierteUnaImagenEnPdf() throws IOException {
        BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        conversion.imageToPdf(new MockMultipartFile("file", "x.png", "image/png", png.toByteArray()), out);
        try (PDDocument doc = open(out)) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    @Test
    void comprimeSinPerderPaginas() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        optimize.compress(file(pdf(3)), out);
        try (PDDocument doc = open(out)) {
            assertEquals(3, doc.getNumberOfPages());
        }
    }

    @Test
    void quitaLaContrasenaDeUnPdfCifrado() throws IOException {
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        try (PDDocument doc = PDDocument.load(pdf(2))) {
            StandardProtectionPolicy policy = new StandardProtectionPolicy("owner", "secreto", new AccessPermission());
            policy.setEncryptionKeyLength(128);
            doc.protect(policy);
            doc.save(encrypted);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        security.removePassword(file(encrypted.toByteArray()), out, "secreto");
        try (PDDocument doc = open(out)) { // se abre sin contraseña
            assertEquals(2, doc.getNumberOfPages());
        }
    }

    @Test
    void ponMarcaDeAguaEnElDocumento() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        edit.watermark(file(pdf(1)), out, "BORRADOR", 45);
        try (PDDocument doc = open(out)) {
            assertEquals(1, doc.getNumberOfPages());
        }
        assertTrue(out.size() > 0);
    }
}
