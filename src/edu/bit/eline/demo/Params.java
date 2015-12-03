package edu.bit.eline.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Params {
    public float  varThrshVal;
    public int    minAreaVal;
    public double alphaVal;
    public List<String> imgList;

    public Params() {
    }

    public boolean checkParams() {
        if (varThrshVal < 0 || alphaVal <= 0 || minAreaVal < 0)
            return false;
        return true;
    }
}
