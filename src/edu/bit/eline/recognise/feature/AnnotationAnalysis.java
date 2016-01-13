package edu.bit.eline.recognise.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// 标注分析类
public class AnnotationAnalysis {
    private String fileName;
    private String shootTime;
    private String detectTime;
    private String lineName;
    private String illum;
    private String bgType;
    private String objType;
    private String leftTop;
    private String rightLow;
    private String filepath;

    public AnnotationAnalysis(String annotationpath) {
        // TODO Auto-generated constructor stub
        try {
            // read file content from file

            this.filepath = annotationpath.substring(0, annotationpath.lastIndexOf("\\"));
            FileReader reader = new FileReader(annotationpath);
            BufferedReader br = new BufferedReader(reader);

            String str = null;
            String[] arrs = null;

            while ((str = br.readLine()) != null) {
                arrs = str.split(":");

                switch (arrs[0]) {
                    case "fileName":
                        this.fileName = arrs[1];
                        break;
                    case "shootTime":
                        this.shootTime = arrs[1];
                        break;
                    case "detectTime":
                        this.detectTime = arrs[1];
                        break;
                    case "lineName":
                        this.lineName = arrs[1];
                        break;
                    case "illum":
                        this.illum = arrs[1];
                        break;
                    case "bgType":
                        this.bgType = arrs[1];
                        break;
                    case "objType":
                        this.objType = arrs[1];
                        break;
                    case "LeftTop":
                        this.leftTop = arrs[1];
                        break;
                    case "RightLow":
                        this.rightLow = arrs[1];
                        break;
                    default:
                        ;
                        // System.out.println("Unknown items!");
                }

            }

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getfileName() {
        return this.fileName;
    }

    public String getillum() {
        return this.illum;
    }

    public String getbgType() {
        return this.bgType;
    }

    public String getobjType() {
        return this.objType;
    }

    public String getRect() {
        return (this.leftTop + ";" + this.rightLow).replace("(", "").replace(")", "");
    }

    public String getfilepath() {
        return this.filepath;
    }

    public String getlineName() {
        return this.lineName;
    }

    public static void main(String[] args) {
        AnnotationAnalysis aa = new AnnotationAnalysis(
                "E:/电网项目/train/train/1/2013-04-04门宝一二8/门宝一二8_20130404_120828_T_CH1_P1.jpg.txt");
        System.out.println(aa.getRect());
    }
}
