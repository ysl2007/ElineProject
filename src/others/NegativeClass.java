package others;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.bit.eline.detection.DirProcesser;

public class NegativeClass {
    private Random rand;
    private int totalLimit;
    
    public NegativeClass(int tl){
        rand = new Random();
        totalLimit = tl;
    }
    
    public void processDir(String dir, String dest, int picCountLimit){
        if (!dir.endsWith("\\") && !dir.endsWith("/"))
            dir += '/';
        if (!dest.endsWith("\\") && !dest.endsWith("/"))
            dest += '/';
        
        String[] subDirs = DirProcesser.getSubDirs(dir);
        int totalCount = 0;
        for(String subDir : subDirs){
            String path = dir + subDir + '/';
            String[] picNames = DirProcesser.getFilenames(path, "jpg");
            int picCount = 0;
            for(String picName : picNames){
                BufferedImage image;
                try {
                    image = ImageIO.read(new File(path + picName));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                int tx, ty, h, w, size;
                tx = ty = h = w = 0;
                int width = image.getWidth();
                int height = image.getHeight();
                for (int i = 0; i < 5; ++i){
                    boolean flag = false;
                    while (!flag){
                        tx = rand.nextInt(width);
                        ty = rand.nextInt(height);
                        h = rand.nextInt(height - ty);
                        w = rand.nextInt(width - tx);
                        size = h * w;
                        flag = true;
                        if (size < 300 || size > 50000){
                            flag = false;
                            continue;
                        }
                        if (h < 30 || w < 30){
                            flag = false;
                            continue;
                        }
                        if (h / w >= 3 || w / h >= 3){
                        	flag = false;
                        	continue;
                        }
                    }
//                    System.out.println(tx + " " + ty + ' ' + w + ' ' + h);
                    BufferedImage subImage = image.getSubimage(tx, ty, w, h);
                    String fileName = dest + totalCount + ".jpg";
//                    System.out.println(fileName);
                    try {
                        ImageIO.write(subImage, "jpg", new File(fileName));
                        totalCount += 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (totalCount % 100 == 0)
                    	System.out.println(totalCount);
                    if (totalCount >= totalLimit)
                    	return;
                }
                picCount += 1;
                if (picCount >= picCountLimit)
                    break;
            }
        }
    }

    public static void main(String[] args) {
        String dir = "E:\\train";
        String dest = "E:\\negative";
        NegativeClass nc = new NegativeClass(2000);
        nc.processDir(dir, dest, 5);
    }

}
