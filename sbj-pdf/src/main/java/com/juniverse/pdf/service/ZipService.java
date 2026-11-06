package com.juniverse.pdf.service;

import com.juniverse.pdf.dto.FilesDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author Java Universe
 */
public interface ZipService {

    void zip(List<FilesDTO> files, OutputStream out) throws IOException;
}
