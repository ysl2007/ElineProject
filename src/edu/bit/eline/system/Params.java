package edu.bit.eline.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Params {
    public float  varThrshVal;
    public int    minAreaVal;
    public double alphaVal;
    public String scaleParamPath;
    public String finalModelPath;
    public String tempimgfeaturepath;
    public String tempscaleimgfeaturepath;
    public String tempimageresultpath;

    public Params(String lineName) throws IOException {
        String rootPath = "./config/" + lineName + "/";
        String tempPath = rootPath + "temp/";
        scaleParamPath = rootPath + "scale.params";
        finalModelPath = rootPath + "final.model";
        tempimageresultpath = tempPath + "image.result.temp";
        tempscaleimgfeaturepath = tempPath + "image.feature.scale.temp";
        tempimgfeaturepath = tempPath + "image.feature.temp";

        BufferedReader reader = new BufferedReader(new FileReader(new File(
                rootPath + "recog.params")));
        reader.close();
        String params = reader.readLine();
        String[] paramList = params.split(" ");
        varThrshVal = Float.valueOf(paramList[0]);
        alphaVal = Double.valueOf(paramList[1]);
        minAreaVal = Integer.valueOf(paramList[2]);
    }

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
