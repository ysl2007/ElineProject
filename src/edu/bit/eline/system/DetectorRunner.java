package edu.bit.eline.system;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

public class DetectorRunner implements Runnable {
    private Detector            detector;
    private BlobAnalyzer        analyzer;
    private ImageConverter      converter;
    private ExtractFeature      ef;
    private ImageClassification ic;
    private Params              param;

    public DetectorRunner(Params param) {
        this.param = param;
        detector = new Detector(0, param.varThrshVal);
        analyzer = new BlobAnalyzer(param.minAreaVal);
        converter = new ImageConverter();
        ef = new ExtractFeature();
        ic = new ImageClassification();
    }

    @Override
    public void run() {
        if (!param.checkParams()) {
            JOptionPane.showMessageDialog(null, "Parameter error.");
            return;
        }

        while (true) {
            // TODO: get image filename;
            String imgFilename = "";

            BufferedImage bimg;
            try {
                bimg = ImageIO.read(new File(imgFilename));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to open image file: " + imgFilename);
                continue;
            }
            if (bimg == null)
                continue;
            Mat imgMat = converter.convert2Mat(bimg);
            IplImage mask = detector.detect(imgMat, param.alphaVal, true);
            List<Blob> blobList;
            blobList = analyzer.analyze(mask);
            for (Blob blob : blobList) {
                CvRect rect = blob.getRect();
                BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(),
                        rect.width(), rect.height());
                String feature = ef.extractIMGfeature(subimg);
                String label = ic.classifyOneImg("4 " + feature,
                        param.finalModelPath, param.scaleParamPath,
                        param.tempimgfeaturepath,
                        param.tempscaleimgfeaturepath,
                        param.tempimageresultpath);
                if (!label.equals("0.0")){
                    // TODO: write record to database
                }
            }
        }
    }
}
