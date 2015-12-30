package others;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.DirProcesser;
import edu.bit.eline.detection.ImageConverter;

public class DryRun {

    public static void run(String path, String dest) {
        path += '/';
        dest += '/';

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

    protected CvRect getTag(String imgName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(imgName + ".txt")));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("LeftTop"))
                break;
        }
        String _x = line.substring(line.indexOf("(") + 1, line.indexOf(","));
        String _y = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
        line = br.readLine();
        String _m = line.substring(line.indexOf("(") + 1, line.indexOf(","));
        String _n = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
        int x = Integer.parseInt(_x);
        int y = Integer.parseInt(_y);
        int w = Integer.parseInt(_m) - x;
        int h = Integer.parseInt(_n) - y;
        Rect rect = new Rect(x, y, w, h);
        br.close();
        return rect.asCvRect();
    }

    protected boolean include(CvRect tag, CvRect rect) {
        int tagx1 = tag.x();
        int tagy1 = tag.y();
        int tagx2 = tagx1 + tag.width();
        int tagy2 = tagy1 + tag.height();

        int rctx1 = rect.x();
        int rcty1 = rect.y();
        int rctx2 = rctx1 + rect.width();
        int rcty2 = rcty1 + rect.height();

        if (rctx1 >= tagx1 && rctx1 <= tagx2) {
            if ((rcty1 >= tagy1 && rcty1 <= tagy2) || (rcty2 >= tagy1 && rcty2 <= tagy2)) {
                return false;
            }
        }
        if (rctx2 >= tagx1 && rctx2 <= tagx2) {
            if ((rcty1 >= tagy1 && rcty1 <= tagy2) || (rcty2 >= tagy1 && rcty2 <= tagy2)) {
                return false;
            }
        }
        return true;
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
        String path = "E:\\testLines\\安左一二37(环境)\\2014-05-28安左一二37(环境)";
        String dest = "E:\\testLines\\安左一二37(环境)\\samples";
        run(path, dest);
    }
}
