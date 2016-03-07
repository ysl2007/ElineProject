package edu.bit.eline.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
// 参数对象
public class Params {
    private String configFile = "./config.json";
    public float   varThrshVal;
    public int     minAreaVal;
    public double  alphaVal;
    public String  scaleParamPath;
    public String  finalModelPath;
    public String  tempimgfeaturepath;
    public String  tempscaleimgfeaturepath;
    public String  tempimageresultpath;

    // 读配置文件
    public Params(String lineName) throws IOException {
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

        String modelPath = rootPath + "/models/" + lineName + "/";
        String tempPath = modelPath + "temp/";
        scaleParamPath = modelPath + "scale.params";
        finalModelPath = modelPath + "final.model";
        tempimageresultpath = tempPath + "image.result.temp";
        tempscaleimgfeaturepath = tempPath + "image.feature.scale.temp";
        tempimgfeaturepath = tempPath + "image.feature.temp";

        BufferedReader reader = new BufferedReader(new FileReader(new File(modelPath + "recog.params")));
        String params = reader.readLine();
        String[] paramList = params.split(" ");
        varThrshVal = Float.valueOf(paramList[0]);
        alphaVal = Double.valueOf(paramList[1]);
        minAreaVal = Integer.valueOf(paramList[2]);
        reader.close();
    }

    // 参数完整检查
    public boolean checkParams() {
        if (!new File(scaleParamPath).exists())
            return false;
        if (!new File(finalModelPath).exists())
            return false;
        if (varThrshVal < 0 || alphaVal <= 0)
            return false;
        return true;
    }
}
