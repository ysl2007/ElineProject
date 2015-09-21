package edu.bit.eline.demo;

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
import edu.bit.eline.detection.Color;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

public class Detection implements Runnable {
    private Detector            detector;
    private BlobAnalyzer        analyzer;
    private ImageConverter      converter;
    private ExtractFeature      ef;
    private ImageClassification ic;
    private Params              param;

    public Detection(Params param) {
        this.param = param;
        detector = new Detector(0, param.varThrshVal);
        analyzer = new BlobAnalyzer(param.minAreaVal);
        converter = new ImageConverter();
        ef = new ExtractFeature();
        ic = new ImageClassification();
    }

    @Override
    public void run() {
        int i = 0;
        for (String imgFilename : param.imgList) {
            BufferedImage bimg;
            try {
                bimg = ImageIO.read(new File(imgFilename));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (bimg == null)
                continue;
            System.out.println(++i);
            Mat imgMat = converter.convert2Mat(bimg);
            IplImage mask = detector.detect(imgMat, param.alphaVal, true);
            List<Blob> blobList;
            blobList = analyzer.analyze(mask);
            Color color = new Color(255, 0, 0);
            for (Blob blob : blobList) {
                CvRect rect = blob.getRect();
                BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(), rect.width(), rect.height());
                String feature = ef.extractIMGfeature(subimg);
                // String label = ic.classifyOneImg("1 " + feature);
                String label = ic.classifyOneImg("4 " + feature, ".\\config\\models\\model0.0.4.0.model", ".\\config\\models\\scale.params");
                if (!label.equals("0.0"))
                    imgMat = blob.drawRect(imgMat, color);
            }
            BufferedImage imgProcessed = converter.convert2JavaImg(imgMat);
            param.imagePanel.setImage(imgProcessed);
            param.bottomPane.setViewportView(param.imagePanel);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}