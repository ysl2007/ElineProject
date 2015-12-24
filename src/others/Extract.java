package others;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Extract {
    public static void main(String[] args) throws IOException {
        String root = "e:/train";
        String dest = "e:/posFiles";
        File f = new File(dest);
        if (!f.exists()) {
            f.mkdir();
        }
        File rootFile = new File(root);
        File[] dirFiles = rootFile.listFiles();
        int i = 0;
        for (File oneDir : dirFiles) {
            if (!oneDir.isDirectory()) {
                continue;
            }
            File[] files = oneDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith("jpg");
                }
            });
            for (File oneFile : files) {
                i += 1;
                String path = oneFile.getAbsolutePath() + ".txt";
                BufferedReader br = new BufferedReader(new FileReader(path));
                String line = br.readLine();
                while (!line.startsWith("objType")) {
                    br.readLine();
                }
                String objType = line.trim().substring(8);
                switch (objType) {
                case "crane":
                    dest += "/1/";
                    break;
                case "pump":
                    dest += "/1/";
                    break;
                case "diggerLoader":
                    dest += "/2/";
                    break;
                case "tower":
                    dest += "/1/";
                    break;
                default:
                    break;
                }
                while (!line.startsWith("LeftTop")) {
                    line = br.readLine();
                }
                String[] lineSegs = line.split(",");
                String ltx = lineSegs[0].substring(9);
                String lty = lineSegs[1].substring(0, lineSegs[1].length() - 1);
                line = br.readLine();
                lineSegs = line.split(",");
                String rbx = lineSegs[0].substring(10);
                String rby = lineSegs[1].substring(0, lineSegs[1].length() - 1);
                int lx = Integer.parseInt(ltx);
                int ly = Integer.parseInt(lty);
                int w = Integer.parseInt(rbx) - lx;
                int h = Integer.parseInt(rby) - ly;
                BufferedImage bimg = ImageIO.read(oneFile);
                BufferedImage cropped = bimg.getSubimage(lx, ly, w, h);
                ImageIO.write(cropped, "jpg", new File(dest + i + ".jpg"));
                br.close();
            }
        }
    }
}
