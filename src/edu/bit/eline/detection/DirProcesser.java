package edu.bit.eline.detection;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * 文件夹处理
 * @author ysl
 */
public class DirProcesser {

    /**
     * 获取路径下所有文件的文件名 要获取所有文件，suffix需为空字符串
     * @param path
     *            指定路径
     * @param suffix
     *            按扩展名过滤
     */
    public static String[] getFilenames(String path, String suffix) {
        // 遍历文件夹，取出jpg文件。
        File imgDir = new File(path);
        String[] imgList = imgDir.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                File f = new File(file.getAbsoluteFile() + "/" + name);
                return (f.isFile() && name.toLowerCase().endsWith(suffix));
            }
        });
        return imgList;
    }

    /**
     * 获取路径下所有子文件夹
     * @param path
     *            指定路径
     */
    public static String[] getSubDirs(String path) {
        File imgDir = new File(path);
        File[] files = imgDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        String[] names = new String[files.length];
        for (int i = 0; i < names.length; ++i)
            names[i] = files[i].getName();
        return names;
    }

}
