package com.juniverse.pdf.controller;

import com.juniverse.pdf.dto.FilesDTO;
import com.juniverse.pdf.dto.ResponseDTO;
import com.juniverse.pdf.service.PdfService;
import com.juniverse.pdf.service.ZipService;
import com.juniverse.pdf.tools.Angles;
import com.juniverse.pdf.tools.FileTools;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author linux
 */
@CrossOrigin(exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/pdf")
@Slf4j
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ZipService zipService;

    @PostMapping(path = "/extract/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<ResponseDTO<String>> extractText(@RequestParam(name = "file") MultipartFile file) {
        log.info("Type: {}", file.getContentType());
        ResponseDTO<String> response = new ResponseDTO<>();
        String text = pdfService.extractText(file);
        response.setContent(text);
        response.setMessage(HttpStatus.OK.getReasonPhrase());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/extract/text/pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<ResponseDTO<String>> extractText(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "start") Integer startPage,
            @RequestParam(name = "end") Integer endPage) {
        log.info("Type: {}", file.getContentType());
        ResponseDTO<String> response = new ResponseDTO<>();
        String text = pdfService.extractText(file, startPage, endPage);
        response.setContent(text);
        response.setMessage(HttpStatus.OK.getReasonPhrase());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/rotate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void rotate(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "angle") Angles angle,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_rotate.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.rotate(file, response.getOutputStream(), angle.getDegree());
    }

    @PostMapping(path = "/rotate/pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void rotate(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "angle") Angles angle,
            @RequestParam(name = "start") Integer startPage,
            @RequestParam(name = "end") Integer endPage,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_rotate.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.rotate(file, response.getOutputStream(), angle.getDegree(), startPage, endPage);
    }

    @PostMapping(path = "/extract/document/pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void extractDocument(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "start") Integer startPage,
            @RequestParam(name = "end") Integer endPage,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_extract.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.extractPages(file, response.getOutputStream(), startPage, endPage);
    }

    @PostMapping(path = "/converter/image-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void converterImageToPdf(@RequestParam(name = "file") MultipartFile file,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_converter.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.imageToPdf(file, response.getOutputStream());
    }

    @PostMapping(path = "/merge/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void merge(
            @Parameter(in = ParameterIn.DEFAULT, schema = @Schema(implementation = MultipartFilesRequest.class))
            @RequestPart(name = "files", required = false) MultipartFile[] files,
            HttpServletResponse response) throws IOException {
        String out = "files_merge.pdf";
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.merge(files, response.getOutputStream());
    }

    /*@PostMapping(path = "/converter/word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document"})
    @ResponseBody
    public void converterPdfToWord(
            @RequestParam(name = "file") MultipartFile file,
            HttpServletResponse response) throws IOException {
        String out = FileTools.getExtension(file.getOriginalFilename(), "_converter.docx");
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.pdfToWord(file, response.getOutputStream());
    }*/

    @PostMapping(path = "/split/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public void split(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "sizePerDocument", defaultValue = "1048576") Integer size,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        List<FilesDTO> pdfs = pdfService.split(file, size);

        String out = FileTools.getExtension(file.getOriginalFilename(), "_split.zip");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        zipService.zip(pdfs, response.getOutputStream());
    }

    @PostMapping(path = "/compress/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void compress(@RequestParam(name = "file") MultipartFile file,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_compress.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.compress(file, response.getOutputStream());
    }

    @PostMapping(path = "/remove", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void removePwd(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "password") String password,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_rm_pwd.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.removePassword(file, response.getOutputStream(), new String(Base64.getDecoder().decode(password)));
    }

    @PostMapping(path = "/converter/pdf-to-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public void converterPdfToImage(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "dpi", defaultValue = "300") Integer dpi,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        List<FilesDTO> pdfs = pdfService.pdfToImage(file, dpi);
        String out = FileTools.getExtension(file.getOriginalFilename(), "_images.zip");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        zipService.zip(pdfs, response.getOutputStream());
    }
    
    
    @PostMapping(path = "/watermark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_PDF_VALUE})
    @ResponseBody
    public void watermark(@RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "angle") Angles angle,
            @RequestParam(name = "watermark", defaultValue = "") String watermarkText,
            HttpServletResponse response) throws IOException {
        log.info("Type: {}", file.getContentType());
        String out = FileTools.getExtension(file.getOriginalFilename(), "_watermark.pdf");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + out + "\"");
        pdfService.watermark(file, response.getOutputStream(), watermarkText, angle.getDegree());
    }

    private class MultipartFilesRequest extends ArrayList<MultipartFile> {
    }

}
