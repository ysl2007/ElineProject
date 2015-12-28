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
    public static final int PARAMS_OPTIMIZED   = 3;
    public static final int MODEL_TRAINED      = 4;

    private final String configFile = "./config.json";
    private String       lineName;
    private String       modelPath;
    private String       tempPath;
    private String       featPath;
    private String       towerPath;
    private String       groundPath;
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
    private int                 status          = NOT_INITIALIZED;
    private String              optiResult;
    private Thread              featExtThread   = null;
    private Thread              paramOptiThread = null;

    public TrainHelper(String lineName, String towerPath, String groundPath, String negPath) {
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
        this.lineName = lineName;
        this.towerPath = towerPath;
        this.groundPath = groundPath;
        this.negPath = negPath;
        status = INITIALIZED;
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
        status = DIRS_READY;
    }

    public void featureExtract(JProgressBar proBar, boolean incraseTrain) {
        if (featExtThread == null || featExtThread.isAlive() == false) {
            ef = new ExtractFeature(lineName, proBar, incraseTrain);
            ef.setCallback(this);
            ef.setFolders(towerPath, groundPath, negPath, featurepath);
            featExtThread = new Thread(ef);
            featExtThread.start();
        } else {
            JOptionPane.showMessageDialog(null, "该过程正在进行，请不要重复点击。", "正在运行", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void optiParams(JProgressBar proBar) {
        if (paramOptiThread == null || paramOptiThread.isAlive() == false) {
            ic.scaledata(featurepath, scalefeaturepath, true, scaleparamspath);
            ic.generateTrainPredict(scalefeaturepath, 50, trainfeaturepath, predictfeaturepath);
            String[] classes = { "0", "1", "2" };
            ic.setFolders(trainfeaturepath, tempoptmodelpath, predictfeaturepath, tempoptresultpath, classes);
            ic.setCallback(this, proBar);
            paramOptiThread = new Thread(ic);
            paramOptiThread.start();
        } else {
            JOptionPane.showMessageDialog(null, "该过程正在进行，请不要重复点击。", "正在运行", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void train(String recogParam) throws IOException {
        String params = "-t 2 -c " + c + " -g " + g;
        ic.train(params, scalefeaturepath, finalmodelpath);
        FileWriter writer = new FileWriter(new File(modelPath + "recog.params"));
        writer.write(recogParam);
        writer.close();
        status = MODEL_TRAINED;
    }

    public void stopThread() {
        ef.setRunFlag(false);
        ic.setRunFlag(false);
    }

    public void featExtrCallback(int status, JProgressBar proBar) {
        this.status = status;
        JOptionPane.showMessageDialog(null, "特征提取过程完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
        proBar.setValue(0);
    }

    public void optiCallBack(String result, int status, JProgressBar proBar) {
        this.optiResult = result;
        this.status = status;
        int idx1 = optiResult.indexOf(':') + 2;
        int idx2 = optiResult.indexOf(' ', idx1);
        c = Double.valueOf(optiResult.substring(idx1, idx2));
        idx1 = optiResult.indexOf(':', idx2) + 2;
        idx2 = optiResult.indexOf(' ', idx1);
        g = Double.valueOf(optiResult.substring(idx1, idx2));
        JOptionPane.showMessageDialog(null, "参数优化过程完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
        proBar.setValue(0);
    }
}
