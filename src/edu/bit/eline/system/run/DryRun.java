package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.system.Train;

public class DryRun implements Runnable {
    private String  lineName;
    private String  tgtPath;
    private String  date;
    private Train   callBack;
    private boolean runFlag = true;

    public DryRun(String lineName, String tgtPath, String date) {
        this.lineName = lineName;
        this.tgtPath = tgtPath;
        this.date = date;
    }

    public void setCallBack(Train frame) {
        this.callBack = frame;
    }

    public void stopRun() {
        runFlag = false;
    }

    @Override
    public void run() {
        if (!validateDate(date)) {
            return;
        }
        List<String> imgList = getImgList();
        Detector detector = new Detector(0, 9, false);
        BlobAnalyzer analyzer = new BlobAnalyzer(2000);
        ImageConverter converter = new ImageConverter();
        int srcImgIndex = 0;
        int subimgCount = 0;
        for (String imgPath : imgList) {
            if (!runFlag){
                return;
            }
            ++srcImgIndex;
            String fileName;
            try {
                fileName = URLDecoder.decode(imgPath, "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                continue;
            }
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            BufferedImage bimg = HttpInterface.getImage(imgPath);
            Mat imgMat = converter.convert2Mat(bimg);
            if (imgMat == null)
                continue;
            IplImage mask = detector.detect(imgMat, 0.1, true);
            if (srcImgIndex < 10) {
                continue;
            }
            List<Blob> blobList = analyzer.analyze(mask);
            for (Blob blob : blobList) {
                if (!runFlag){
                    return;
                }
                CvRect rect = blob.getRect();
                if (!sizeLimit(rect)) {
                    continue;
                }
                int x = rect.x(), y = rect.y();
                int width = rect.width(), height = rect.height();
                BufferedImage subimg = bimg.getSubimage(x, y, width, height);
                ++subimgCount;
                try {
                    ImageIO.write(subimg, "bmp", new File(tgtPath + fileName + ".sub" + subimgCount + ".bmp"));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        callBack();
    }

    private boolean validateDate(String date) {
        if (date.length() != 10) {
            return false;
        }
        if (date.charAt(4) != '-' || date.charAt(7) != '-') {
            return false;
        }
        try {
            Integer.parseInt(date.substring(0, 4));
            Integer.parseInt(date.substring(5, 7));
            Integer.parseInt(date.substring(8, 10));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private List<String> getImgList() {
        String start = date + " 00:07:30";
        String end = date + " 23:59:59";
        List<String> imgList = HttpInterface.getImageList(lineName, start, end);
        return imgList;
    }

    private void callBack() {
        callBack.dryRunCallback();
    }

    private boolean sizeLimit(CvRect rect) {
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
}
