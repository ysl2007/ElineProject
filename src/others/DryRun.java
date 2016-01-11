package others;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.DirProcesser;
import edu.bit.eline.detection.ImageConverter;

public class DryRun {

    public static void run(String path, String dest) {
        path += '/';
        dest += '/';
        File d = new File(dest);
        if (!d.exists()){
            d.mkdirs();
        }

        int subimgCount = 0;
        String[] imgList = DirProcesser.getFilenames(path, "jpg");
        Detector detector = new Detector(0, 9, false);
        BlobAnalyzer analyzer = new BlobAnalyzer(100);
        ImageConverter converter = new ImageConverter();
        int srcImgIndex = 0;
        for (int i = 0; i < imgList.length; ++i) {
            String imgName = path + imgList[i];
            BufferedImage img;
            try {
                img = ImageIO.read(new File(imgName));
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            srcImgIndex += 1;
            Mat imgMat = converter.convert2Mat(img);
            if (imgMat == null)
                continue;
            IplImage mask = detector.detect(imgMat, 0.1, true);

            if (srcImgIndex < 15)
                continue;
            List<Blob> blobList = analyzer.analyze(mask);
            for (Blob blob : blobList) {
                CvRect rect = blob.getRect();
                if (!sizeLimit(rect))
                    continue;

                int x = rect.x();
                int y = rect.y();
                int width = rect.width();
                int height = rect.height();
                BufferedImage subimg = img.getSubimage(x, y, width, height);

                try {
                    ImageIO.write(subimg, "bmp", new File(dest + imgList[i] + ".sub" + subimgCount + ".bmp"));
                    subimgCount += 1;
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                if (subimgCount % 100 == 0)
                    System.out.println(subimgCount);
            }
        }
    }

    protected static boolean sizeLimit(CvRect rect) {
        int height = rect.height();
        int width = rect.width();
        int size = height * width;
        if (size < 2000) {
            return false;
        }
        if (height < 30 || width < 30) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String path = "E:\\testLines\\北胡11(动态风险)\\2013-11-30北胡11(动态风险)";
        String dest = "E:\\testLines\\北胡11(动态风险)\\sample";
        run(path, dest);
    }
}
