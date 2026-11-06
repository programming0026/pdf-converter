package com.juniverse.pdf.tools;

/**
 *
 * @author Java Universe
 */
public final class FileTools {
    
    private FileTools() {
        
    }

    public static String getExtension(String in, String ext) {
        if (in == null) {
            return "unnamed" + ext;
        }
        int index = in.lastIndexOf(".");
        if (index < 0) {
            return in + ext;
        }
        return in.substring(0, index) + ext;
    }
}
