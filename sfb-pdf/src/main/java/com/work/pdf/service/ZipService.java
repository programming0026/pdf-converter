package com.work.pdf.service;

import com.work.pdf.dto.FilesDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author linux
 */
public interface ZipService {

    void zip(List<FilesDTO> files, OutputStream out) throws IOException;
}
