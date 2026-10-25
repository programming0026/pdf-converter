package com.work.pdf.service;

import com.work.pdf.dto.FilesDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author linux
 */
public interface PdfService {

    void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle, int startPage, int endPage);

    void rotate(MultipartFile inputPdfPath, OutputStream outputPdfPath, int rotationAngle);

    String extractText(MultipartFile inputPdfPath);

    String extractText(MultipartFile inputPdfPath, int startPage, int endPage);

    void merge(MultipartFile[] pdfFiles, OutputStream outputPdfPath);

    void extractPages(MultipartFile inputPdfPath, OutputStream outputPdfPath, int startPage, int endPage);

    void imageToPdf(MultipartFile inputPdfPath, OutputStream outputPdfPath);

    //void pdfToWord(MultipartFile inputFilePath, OutputStream outputFilePath);

    List<FilesDTO> split(MultipartFile inputPdfPath, long maxSizePerDocument);

    void compress(MultipartFile inputFile, OutputStream outputFile);
    
    void removePassword(MultipartFile inputFile, OutputStream outputFile, String password);
    
    List<FilesDTO> pdfToImage(MultipartFile inputFile, int dpi) throws IOException;
    
    void watermark(MultipartFile inputFile, OutputStream output, String watermarkText, int rotationAngle) throws IOException;
    
    
}
