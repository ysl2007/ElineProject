package edu.bit.eline.system;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

public class TrainHelper {
    public static final int     NOT_INITIALIZED    = -1;
    public static final int     INITIALIZED        = 0;
    public static final int     DIRS_READY         = 1;
    public static final int     FEATURES_EXTRACTED = 2;
    public static final int     FEATURES_NORMED    = 3;
    public static final int     DATA_GENERATED     = 4;
    public static final int     PARAMS_OPTIMIZED   = 5;
    public static final int     MODEL_TRAINED      = 6;

    private final String        configPath         = ".\\config\\";
    private String              rootPath;
    private String              imagespath;
    private String              tempPath;
    private String              featPath;

    private String              tempoptmodelpath;
    private String              tempoptresultpath;
    private String              featurepath;
    private String              scaleparamspath;
    private String              scalefeaturepath;
    private String              trainfeaturepath;
    private String              predictfeaturepath;
    private String              finalmodelpath;

    private ExtractFeature      ef;
    private ImageClassification ic;
    private double              c;
    private double              g;
    private String[]            classes            = { "0.0", "0.0" };
    private int                  status             = -1;

    public TrainHelper(String lineName, String posClass) {
        rootPath = configPath + lineName + "\\";
        imagespath = rootPath + "classes" + "\\";
        tempPath = rootPath + "temp" + "\\";
        featPath = rootPath + "features" + "\\";

        tempoptmodelpath = tempPath + "opt.model.temp";
        tempoptresultpath = tempPath + "opt.result.temp";
        featurepath = featPath + "features.feature";
        scalefeaturepath = featPath + "features.feature.scale";
        trainfeaturepath = featPath + "features.feature.scale.train";
        predictfeaturepath = featPath + "features.feature.scale.predict";
        scaleparamspath = rootPath + "scale.params";
        finalmodelpath = rootPath + "finalmodelpath";

        ef = new ExtractFeature();
        ic = new ImageClassification();
        classes[1] = posClass;
        status = 0;
    }

    public int getStatus(){
        return status;
    }
    
    public void getDirsReady() {
        File dir;
        dir = new File(rootPath);
        if (!dir.exists())
            dir.mkdir();
        dir = new File(tempPath);
        if (!dir.exists())
            dir.mkdir();
        dir = new File(imagespath);
        if (!dir.exists())
            dir.mkdir();
        dir = new File(featPath);
        if (!dir.exists())
            dir.mkdir();
        status = 1;
    }

    public void featureExtract() {
        ef.extractFoldfeature(imagespath, featurepath);
        status = 2;
    }

    public void featureNorm() {
        ic.scaledata(featurepath, scalefeaturepath, true, scaleparamspath);
        status = 3;
    }

    public void generateTrainPredict() {
        ic.generateTrainPredict(scalefeaturepath, 50, trainfeaturepath,
                predictfeaturepath);
        status = 4;
    }

    public void optiParams() {

        String result = ic.paramoptimize(trainfeaturepath, tempoptmodelpath,
                predictfeaturepath, tempoptresultpath, classes);
        int idx1 = result.indexOf(':') + 2;
        int idx2 = result.indexOf(' ', idx1);
        c = Double.valueOf(result.substring(idx1, idx2));
        idx1 = result.indexOf(':', idx2) + 2;
        idx2 = result.indexOf(' ', idx1);
        g = Double.valueOf(result.substring(idx1, idx2));
        status = 5;
    }

    public void train(String recogParam) throws IOException {
        String params = "-t 2 -c " + c + " -g " + g;
        ic.train(params, scalefeaturepath, finalmodelpath);
        FileWriter writer = new FileWriter(new File(rootPath + "recog.params"));
        writer.write(recogParam);
        writer.close();
        status = 6;
    }

//    public static void main(String[] args) {
//        String result = "Best C: 2.0 Best G: 0.00390625 Best Acc: 0.9974358677864075";
//        int idx1 = result.indexOf(':') + 2;
//        int idx2 = result.indexOf(' ', idx1);
//        double c = Double.valueOf(result.substring(idx1, idx2));
//        idx1 = result.indexOf(':', idx2) + 2;
//        idx2 = result.indexOf(' ', idx1);
//        double g = Double.valueOf(result.substring(idx1, idx2));
//        String params = "-t 2 -c " + c + " -g " + g;
//        System.out.println(params);
//    }
}
