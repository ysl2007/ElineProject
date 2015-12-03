package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;
import edu.bit.eline.system.Params;

public class Processer implements Runnable {
    private ConcurrentHashMap<String, Pair<Detector, Params>> detMap;
    private ImageStorage                                      store;
    private ImageConverter                                    converter;
    private BlobAnalyzer                                      analyzer;
    private ExtractFeature                                    featureExt;
    private ImageClassification                               classifier;

    public Processer(ImageStorage store) {
        this.store = store;
        detMap = new ConcurrentHashMap<String, Pair<Detector, Params>>();
        converter = new ImageConverter();
        featureExt = new ExtractFeature();
        classifier = new ImageClassification();
    }

    public List<String> getRunningLine() {
        synchronized (detMap) {
            Enumeration<String> e = detMap.keys();
            List<String> result = new ArrayList<String>();
            while (e.hasMoreElements()) {
                result.add(e.nextElement());
            }
            return result;
        }
    }

    public void runLine(String lineName, Detector det, Params param) {
        synchronized (detMap) {
            detMap.put(lineName, new Pair<Detector, Params>(det, param));
        }
    }

    public void stopLine(String lineName) {
        synchronized (detMap) {
            detMap.remove(lineName);
        }
    }

    public void stopAll() {
        synchronized (detMap) {
            detMap.clear();
        }
    }

    public Enumeration<String> getLines() {
        synchronized (detMap) {
            return detMap.keys();
        }
    }

    public boolean isRunning(String lineName) {
        if (detMap.containsKey(lineName)) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            Pair<String, BufferedImage> pair = store.get();
            String lineName = pair.getKey();
            BufferedImage bimg = pair.getVal();
            Pair<Detector, Params> detPair;
            synchronized (detMap) {
                if (!detMap.containsKey(lineName)) {
                    continue;
                }
                detPair = detMap.get(lineName);
            }
            Detector det = detPair.getKey();
            Params param = detPair.getVal();
            Mat imgMat = converter.convert2Mat(bimg);
            IplImage mask = det.detect(imgMat, param.alphaVal, true);
            List<Blob> blobList = analyzer.analyze(mask);
            for (Blob blob : blobList) {
                CvRect rect = blob.getRect();
                BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(),
                        rect.width(), rect.height());
                String feature = featureExt.extractIMGfeature(subimg);
                String label = classifier.classifyOneImg("4 " + feature,
                        param.finalModelPath, param.scaleParamPath,
                        param.tempimgfeaturepath,
                        param.tempscaleimgfeaturepath,
                        param.tempimageresultpath);
                if (!label.equals("0.0")) {
                    // TODO: Database manipulation.
                }
            }
        }
    }
}
