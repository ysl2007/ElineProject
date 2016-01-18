package edu.bit.eline.demo;

import java.io.File;
import java.util.List;

public class Params {
    public float          varThrshVal = -1;
    public int            minAreaVal  = -1;
    public double         alphaVal    = -1;
    public String         scaleParamPath;
    public String         finalModelPath;
    public String         tempimgfeaturepath;
    public String         tempscaleimgfeaturepath;
    public String         tempimageresultpath;
    public List<String>   imgList;

    public Params(String rootPath) {
        rootPath += "/";
        String tempPath = rootPath + "temp/";
        File tmp = new File(tempPath);
        if (!tmp.isDirectory()) {
            tmp.mkdir();
        }

        scaleParamPath = rootPath + "scale.params";
        finalModelPath = rootPath + "final.model";
        tempimageresultpath = tempPath + "image.result.temp";
        tempscaleimgfeaturepath = tempPath + "image.feature.scale.temp";
        tempimgfeaturepath = tempPath + "image.feature.temp";
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
