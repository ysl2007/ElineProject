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

import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.Tamura;

public class ExtractFeature implements Runnable {
    private String       towerPath;
    private String       groundPath;
    private String       negPath;
    private String       featurefilepath;
//    private String       pos;
    private boolean      goOn         = true;
    private boolean      increase     = false;
    private boolean      folderStatus = false;
    private JProgressBar proBar;

    public ExtractFeature() {
    }

    public ExtractFeature(JProgressBar proBar, boolean increase) {
        this.proBar = proBar;
        this.increase = increase;
    }

    public void setFolders(String towerPath, String groundPath, String negPath,
            String feature) {
        this.towerPath = towerPath;
        this.groundPath = groundPath;
        this.negPath = negPath;
        this.featurefilepath = feature;
        folderStatus = true;
    }

    public void setRunFlag(boolean status) {
        goOn = status;
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
            features = features + "AutoColorCorrelogram: "
                    + ac.getStringRepresentation() + ";@";
            cd.extract(subimg);
            features = features + "CEDD: " + cd.getStringRepresentation()
                    + ";@";
            cl.extract(subimg);
            features = features + "ColorLayout: "
                    + cl.getStringRepresentation() + ";@";
            ed.extract(subimg);
            features = features + "EdgeHistogram: "
                    + ed.getStringRepresentation() + ";@";
            sc.extract(subimg);
            features = features + "ScalableColor: "
                    + sc.getStringRepresentation() + ";@";
            ft.extract(subimg);
            features = features + "FCTH: " + ft.getStringRepresentation()
                    + ";@";
            tm.extract(subimg);
            features = features + "Tamura: " + tm.getStringRepresentation()
                    + ";@";
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
            features = features + "AutoColorCorrelogram: "
                    + ac.getStringRepresentation() + ";@";
            cd.extract(subimg);
            features = features + "CEDD: " + cd.getStringRepresentation()
                    + ";@";
            cl.extract(subimg);
            features = features + "ColorLayout: "
                    + cl.getStringRepresentation() + ";@";
            ed.extract(subimg);
            features = features + "EdgeHistogram: "
                    + ed.getStringRepresentation() + ";@";
            sc.extract(subimg);
            features = features + "ScalableColor: "
                    + sc.getStringRepresentation() + ";@";
            ft.extract(subimg);
            features = features + "FCTH: " + ft.getStringRepresentation()
                    + ";@";
            tm.extract(subimg);
            features = features + "Tamura: " + tm.getStringRepresentation()
                    + ";@";
            return formatfeatures(features);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        extractFoldfeature();
        if (increase) {
            extractDBFeature();
        } else {
            proBar.setValue(proBar.getMaximum());
        }
    }

    private String[] getImgFilelist(String foldpath) {
        List<String> list = new ArrayList<String>();
        File dir = new File(foldpath);
        File[] imgs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("bmp")
                        || name.toLowerCase().endsWith("jpg");
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

    private void extractFoldfeature() {
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
            proBar.setMaximum(imgNums + 10);
            if (posImageList.length > 0) {
                for (int j = 0; j < posImageList.length && goOn; j++) {
                    String features = extractIMGfeature(posImageList[j]);
                    String featurestr = posClass + " " + features;
                    outFile.write(featurestr + "\n");
                    System.out.println(featurestr);
                    curImg += 1;
                    proBar.setValue(curImg);
                }
            }
            if (negImageList.length > 0) {
                for (int j = 0; j < negImageList.length && goOn; j++) {
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

    private String formatfeatures(String sourcefeature) {
        String[] strarrs = sourcefeature.split("@");
        String featurestr = "";
        featurestr += strarrs[1].substring(strarrs[1].indexOf(" ", 7),
                strarrs[1].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[2].substring(strarrs[2].indexOf(" ", 0),
                strarrs[2].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[3].substring(strarrs[3].indexOf(";", 20) + 1,
                strarrs[3].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[4].substring(strarrs[4].indexOf(";", 31) + 1,
                strarrs[4].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[5].substring(strarrs[5].indexOf(" ", 7) + 1,
                strarrs[5].length() - 1).trim();
        featurestr += " ";
        featurestr += strarrs[6].substring(strarrs[6].indexOf(" ", 10) + 1,
                strarrs[6].length() - 1).trim();

        return addnum(featurestr);
    }

    private void extractDBFeature() {
        sdfasd
        proBar.setValue(proBar.getMaximum());
    }

    private String addnum(String str) {
        String[] arrs = str.split(" |z");
        String result = "";
        for (int i = 0; i < arrs.length; i++) {
            String temp = arrs[i].trim();
            result = result + " " + Integer.toString(i + 1) + ":" + temp;
        }
        return result;
    }
}
