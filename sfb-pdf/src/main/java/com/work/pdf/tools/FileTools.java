package com.work.pdf.tools;

/**
 *
 * @author linux
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
