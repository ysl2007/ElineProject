package edu.bit.eline.demo;

import java.awt.image.BufferedImage;
import java.util.List;

public class ParamsDemo {
    public float         varThrshVal = 0;
    public int           minAreaVal  = 0;
    public double        alphaVal    = 0;
    public List<String>  imgList;
    public BufferedImage bimg;
    
    public boolean checkParams(){
        if (varThrshVal < 0 || alphaVal <= 0 || imgList == null || imgList.isEmpty())
            return false;
        return true;
    }
}
