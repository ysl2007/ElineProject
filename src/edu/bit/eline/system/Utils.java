package edu.bit.eline.system;

import java.io.File;

public class Utils {
    public static boolean delete(File file){
        if (!file.exists()) {
            return false;
        }
        boolean status = true;
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (File f : subFiles) {
                status = delete(f);
                if (!status) {
                    return false;
                }
            }
            file.delete();
        } else if (file.isFile()) {
            return file.delete();
        }
        return true;
    }
}
