package com.work.pdf.service;

import com.work.pdf.dto.FilesDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author linux
 */
@Service
public class ZipServiceImpl implements ZipService {

    @Override
    public void zip(List<FilesDTO> files, OutputStream out) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (FilesDTO file : files) {
                ZipEntry zipEntry = new ZipEntry(file.getFileName());
                zipOut.putNextEntry(zipEntry);
                byte[] pdf = Base64.getDecoder().decode(file.getFile());
                IOUtils.copy(new ByteArrayInputStream(pdf), zipOut);
                zipOut.closeEntry();                
            }
        }
    }
}
