package edu.bit.eline.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

public class TrainHelper {
    public static final int NOT_INITIALIZED    = -1;
    public static final int INITIALIZED        = 0;
    public static final int DIRS_READY         = 1;
    public static final int FEATURES_EXTRACTED = 2;
    public static final int FEATURES_NORMED    = 3;
    public static final int DATA_GENERATED     = 4;
    public static final int PARAMS_OPTIMIZED   = 5;
    public static final int MODEL_TRAINED      = 6;

    private final String configFile = "./config.json";
    private String       modelPath;
    private String       tempPath;
    private String       featPath;
    private String       posPath;
    private String       negPath;

    private String tempoptmodelpath;
    private String tempoptresultpath;
    private String featurepath;
    private String scaleparamspath;
    private String scalefeaturepath;
    private String trainfeaturepath;
    private String predictfeaturepath;
    private String finalmodelpath;

    private ExtractFeature      ef;
    private ImageClassification ic;
    private double              c;
    private double              g;
    private String[]            classes = { "0.0", "0.0" };
    private int                 status  = -1;

    public TrainHelper(String lineName, String posClass, String posPath, String negPath) {
        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        JSONObject jo = new JSONObject(tokener);
        String rootPath;
        try {
            rootPath = jo.getString("config_root_path");
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "配置文件不完整。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        modelPath = rootPath + "\\models\\" + lineName + "\\";
        tempPath = modelPath + "temp" + "\\";
        featPath = modelPath + "features" + "\\";

        tempoptmodelpath = tempPath + "opt.model.temp";
        tempoptresultpath = tempPath + "opt.result.temp";
        featurepath = featPath + "features.feature";
        scalefeaturepath = featPath + "features.feature.scale";
        trainfeaturepath = featPath + "features.feature.scale.train";
        predictfeaturepath = featPath + "features.feature.scale.predict";
        scaleparamspath = modelPath + "scale.params";
        finalmodelpath = modelPath + "final.model";

        ic = new ImageClassification();
        classes[1] = posClass;
        this.posPath = posPath;
        this.negPath = negPath;
        status = 0;
    }

    public int getStatus() {
        return status;
    }

    public void getDirsReady() {
        File dir;
        dir = new File(modelPath);
        if (!dir.exists())
            dir.mkdir();
        dir = new File(tempPath);
        if (!dir.exists())
            dir.mkdir();
        dir = new File(featPath);
        if (!dir.exists())
            dir.mkdir();
        status = 1;
    }

    public void featureExtract(JProgressBar proBar) {
        ef = new ExtractFeature(posPath, negPath, featurepath, classes[1], proBar);
        ef.setCallback(this);
        new Thread(ef).start();
    }

    public void stopThread() {
        ef.setRunFlag(false);
    }

    public void featExtrCallback(int status, JProgressBar proBar) {
        this.status = status;
        JOptionPane.showMessageDialog(null, "特征提取过程完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
        proBar.setValue(0);
    }

    public void featureNorm() {
        ic.scaledata(featurepath, scalefeaturepath, true, scaleparamspath);
        status = 3;
    }

    public void generateTrainPredict() {
        ic.generateTrainPredict(scalefeaturepath, 50, trainfeaturepath, predictfeaturepath);
        status = 4;
    }

    public void optiParams() {
        String result = ic.paramoptimize(trainfeaturepath, tempoptmodelpath, predictfeaturepath, tempoptresultpath,
                classes);
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
        FileWriter writer = new FileWriter(new File(modelPath + "recog.params"));
        writer.write(recogParam);
        writer.close();
        status = 6;
    }
}
