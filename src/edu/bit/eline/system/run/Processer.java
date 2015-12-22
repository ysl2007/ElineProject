package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
import edu.bit.eline.system.Params;

public class Processer implements Runnable {
    private ConcurrentHashMap<String, Pair<Detector, Params>> detMap;
    private ImageStorage                                      store;
    private ImageConverter                                    converter;
    private BlobAnalyzer                                      analyzer;
    private ExtractFeature                                    featureExt;
    private ImageClassification                               classifier;
    private SQLConnection                                     dbconn;
    private int                                               maxRetry = 5;

    public Processer(ImageStorage store) {
        this.store = store;
        detMap = new ConcurrentHashMap<String, Pair<Detector, Params>>();
        converter = new ImageConverter();
        featureExt = new ExtractFeature();
        classifier = new ImageClassification();
        dbconn = new SQLConnection();
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
            Pair<String, Pair<String, BufferedImage>> pair = store.get();
            String lineName = pair.getKey();
            Pair<String, BufferedImage> imgPair = pair.getVal();
            String imgFilename = imgPair.getKey();
            int timeBegIndex = imgFilename.indexOf("_") + 1;
            int timeMidIndex = imgFilename.indexOf("_", timeBegIndex);
            int timeEndIndex = imgFilename.indexOf("_", timeMidIndex + 1);
            String date = imgFilename.substring(timeBegIndex, timeMidIndex);
            String time = imgFilename.substring(timeMidIndex + 1, timeEndIndex);
            String datetime = date + " " + time;
            BufferedImage bimg = imgPair.getVal();
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
                int x = rect.x();
                int y = rect.y();
                int width = rect.width();
                int height = rect.height();
                BufferedImage subimg = bimg.getSubimage(x, y, width, height);
                String feature = featureExt.extractIMGfeature(subimg);
                String label = classifier.classifyOneImg("4 " + feature,
                        param.finalModelPath, param.scaleParamPath,
                        param.tempimgfeaturepath,
                        param.tempscaleimgfeaturepath,
                        param.tempimageresultpath);
                if (!label.equals("0.0")) {
                    if (dbconn == null) {
                        JOptionPane.showMessageDialog(null,
                                "数据库连接错误！所有线路的运行即将停止。", "致命错误",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int retry = 0;
                    while (dbconn.isClosed() && retry < maxRetry) {
                        dbconn.connect();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (retry == maxRetry) {
                        JOptionPane.showMessageDialog(null,
                                "数据库连接错误！所有线路的运行即将停止。", "致命错误",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String sql = "Insert into Table_AlarmInfo () Values(";
                    boolean status = dbconn.insert(sql);
                    if (status == true) {
                        break;
                    }
                }
            }
        }
    }
}
