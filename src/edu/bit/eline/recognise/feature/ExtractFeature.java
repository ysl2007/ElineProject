package edu.bit.eline.recognise.feature;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;

import edu.bit.eline.system.TrainHelper;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.Tamura;

public class ExtractFeature implements Runnable {
    private String       posPath;
    private String       negPath;
    private String       featurefilepath;
    private String       pos;
    private boolean      runFlag = true;
    private TrainHelper  th;
    private JProgressBar proBar;

    public ExtractFeature() {}

    public ExtractFeature(String posPath, String negPath, String feature, String pos, JProgressBar proBar) {
        this.posPath = posPath;
        this.negPath = negPath;
        this.featurefilepath = feature;
        this.pos = pos;
        this.proBar = proBar;
    }

    public void setCallback(TrainHelper th) {
        this.th = th;
    }

    public String[] getImgFilelist(String foldpath) {
        List<String> list = new ArrayList<String>();
        File dir = new File(foldpath);
        File[] imgs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("bmp") || name.toLowerCase().endsWith("jpg");
            }
        });
        if (imgs.length > 0) {
            for (int j = 0; j < imgs.length; j++) {
                list.add(imgs[j].getAbsolutePath());
            }

            String strings[] = new String[list.size()];
            for (int i = 0, j = list.size(); i < j; i++)
                strings[i] = list.get(i);

            return strings;
        } else
            return null;
    }

    // 提取一个图像的特征
    public String extractIMGfeature(BufferedImage subimg) {
        AutoColorCorrelogram ac = new AutoColorCorrelogram();
        CEDD cd = new CEDD();
        ColorLayout cl = new ColorLayout();
        EdgeHistogram ed = new EdgeHistogram();
        ScalableColor sc = new ScalableColor();
        FCTH ft = new FCTH();
        Tamura tm = new Tamura();
        String features = "";
        try {
            ac.extract(subimg);
            features = features + "AutoColorCorrelogram: " + ac.getStringRepresentation() + ";@";
            cd.extract(subimg);
            features = features + "CEDD: " + cd.getStringRepresentation() + ";@";
            cl.extract(subimg);
            features = features + "ColorLayout: " + cl.getStringRepresentation() + ";@";
            ed.extract(subimg);
            features = features + "EdgeHistogram: " + ed.getStringRepresentation() + ";@";
            sc.extract(subimg);
            features = features + "ScalableColor: " + sc.getStringRepresentation() + ";@";
            ft.extract(subimg);
            features = features + "FCTH: " + ft.getStringRepresentation() + ";@";
            tm.extract(subimg);
            features = features + "Tamura: " + tm.getStringRepresentation() + ";@";
            return formatfeatures(features);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 提取一个图像的特征
    public String extractIMGfeature(String filepath) {
        AutoColorCorrelogram ac = new AutoColorCorrelogram();
        CEDD cd = new CEDD();
        ColorLayout cl = new ColorLayout();
        EdgeHistogram ed = new EdgeHistogram();
        ScalableColor sc = new ScalableColor();
        FCTH ft = new FCTH();
        Tamura tm = new Tamura();
        String features = "";
        try {
            File imgfile = new File(filepath);
            BufferedImage subimg = ImageIO.read(imgfile);
            ac.extract(subimg);
            features = features + "AutoColorCorrelogram: " + ac.getStringRepresentation() + ";@";
            cd.extract(subimg);
            features = features + "CEDD: " + cd.getStringRepresentation() + ";@";
            cl.extract(subimg);
            features = features + "ColorLayout: " + cl.getStringRepresentation() + ";@";
            ed.extract(subimg);
            features = features + "EdgeHistogram: " + ed.getStringRepresentation() + ";@";
            sc.extract(subimg);
            features = features + "ScalableColor: " + sc.getStringRepresentation() + ";@";
            ft.extract(subimg);
            features = features + "FCTH: " + ft.getStringRepresentation() + ";@";
            tm.extract(subimg);
            features = features + "Tamura: " + tm.getStringRepresentation() + ";@";
            return formatfeatures(features);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String formatfeatures(String sourcefeature) {
        String[] strarrs = sourcefeature.split("@");
        String featurestr = "";
        featurestr += strarrs[1].substring(strarrs[1].indexOf(" ", 7), strarrs[1].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[2].substring(strarrs[2].indexOf(" ", 0), strarrs[2].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[3].substring(strarrs[3].indexOf(";", 20) + 1, strarrs[3].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[4].substring(strarrs[4].indexOf(";", 31) + 1, strarrs[4].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[5].substring(strarrs[5].indexOf(" ", 7) + 1, strarrs[5].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[6].substring(strarrs[6].indexOf(" ", 10) + 1, strarrs[6].length() - 1).trim();

        return addnum(featurestr);
    }

    public String addnum(String str) {

        String[] arrs = str.split(" |z");
        String result = "";
        for (int i = 0; i < arrs.length; i++) {
            String temp = arrs[i].trim();
            result = result + " " + Integer.toString(i + 1) + ":" + temp;
        }
        return result;
    }

    public void extractFoldfeature() {
        proBar.setValue(0);
        String posClass = Character.toString(pos.charAt(0));
        BufferedWriter outFile = null;
        try {
            outFile = new BufferedWriter(new FileWriter(featurefilepath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            String[] posImageList = getImgFilelist(posPath);
            String[] negImageList = getImgFilelist(negPath);
            int imgNums = posImageList.length + negImageList.length;
            int curImg = 0;
            proBar.setMaximum(imgNums);
            if (posImageList.length > 0) {
                for (int j = 0; j < posImageList.length && runFlag; j++) {
                    String features = extractIMGfeature(posImageList[j]);
                    String featurestr = posClass + " " + features;
                    outFile.write(featurestr + "\n");
                    System.out.println(featurestr);
                    curImg += 1;
                    proBar.setValue(curImg);
                }
            }
            if (negImageList.length > 0) {
                for (int j = 0; j < negImageList.length && runFlag; j++) {
                    String features = extractIMGfeature(negImageList[j]);
                    String featurestr = "0 " + features;
                    outFile.write(featurestr + "\n");
                    System.out.println(featurestr);
                    curImg += 1;
                    proBar.setValue(curImg);
                }
            }
            outFile.close();
        } catch (Exception e) {
            try {
                outFile.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (th == null) {
            return;
        }
        BufferedWriter outFile = null;
        try {
            outFile = new BufferedWriter(new FileWriter(featurefilepath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        extractFoldfeature();
        try {
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        th.featExtrCallback(TrainHelper.FEATURES_EXTRACTED, proBar);
    }

    public void setRunFlag(boolean status) {
        runFlag = status;
    }
}
