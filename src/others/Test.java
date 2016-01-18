package others;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.util.Arrays;

import javax.imageio.ImageIO;

import edu.bit.eline.system.run.SQLConnection;

public class Test {
    public static void main(String[] args) throws Exception {
        String path = "E:\\testLines";
        File f = new File(path);
        String[] strs = f.list();
        for (String s : strs) {
            System.out.println(s);
        }
    }
}
