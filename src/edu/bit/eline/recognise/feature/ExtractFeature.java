package edu.bit.eline.recognise.feature;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;

import edu.bit.eline.system.TrainHelper;
import edu.bit.eline.system.run.SQLConnection;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.Tamura;

public class ExtractFeature implements Runnable {
    private String[]     classPath;
    private String       featurefilepath;
    private String       lineName;
    private boolean      runFlag      = true;
    private boolean      increase     = false;
    private boolean      folderStatus = false;
    private TrainHelper  th;
    private JProgressBar proBar;

    public ExtractFeature() {}

    public ExtractFeature(String lineName, JProgressBar proBar, boolean increase) {
        this.proBar = proBar;
        this.increase = increase;
        this.lineName = lineName;
    }

    public void setFolders(String towerPath, String groundPath, String negPath, String feature) {
        classPath = new String[3];
        classPath[0] = negPath;
        classPath[1] = towerPath;
        classPath[2] = groundPath;
        this.featurefilepath = feature;
        folderStatus = true;
    }

    public void setRunFlag(boolean status) {
        runFlag = status;
    }

    public void setCallback(TrainHelper th) {
        this.th = th;
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

    @Override
    public void run() {
        if (!folderStatus || th == null) {
            return;
        }
        BufferedWriter outFile = null;
        try {
            outFile = new BufferedWriter(new FileWriter(featurefilepath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        extractFoldfeature(outFile);
        if (increase) {
            extractDBFeature(outFile);
        } else {
            proBar.setValue(proBar.getMaximum());
        }
        try {
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        th.featExtrCallback(TrainHelper.FEATURES_EXTRACTED, proBar);
    }

    private String[] getImgFilelist(String foldpath) {
        List<String> list = new ArrayList<String>();
        File dir = new File(foldpath);
        File[] imgs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("bmp") || name.toLowerCase().endsWith("jpg")
                        || name.toLowerCase().endsWith("png");
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

    private void extractFoldfeature(BufferedWriter outFile) {
        proBar.setValue(0);
        proBar.setMaximum(100);

        int total = 0;
        for (String dir : classPath) {
            String[] imgList = getImgFilelist(dir);
            total += imgList.length;
        }
        int curNum = 0;
        for (int i = 0; i < classPath.length; ++i) {
            String classTag = Integer.toString(i);
            try {
                String[] imageList = getImgFilelist(classPath[i]);
                if (imageList.length > 0) {
                    for (int j = 0; j < imageList.length; j++) {
                        if (!runFlag) {
                            return;
                        }
                        String features = extractIMGfeature(imageList[j]);
                        String featurestr = classTag + " " + features;
                        outFile.write(featurestr + "\n");
                        System.out.println(featurestr);
                        curNum += 1;
                        if (increase) {
                            proBar.setValue((int) ((float) curNum / total * 80));
                        } else {
                            proBar.setValue((int) ((float) curNum / total * 100));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String formatfeatures(String sourcefeature) {
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

    private void extractDBFeature(BufferedWriter outFile) {
        System.out.println("DB feature...");
        SQLConnection dbConn = new SQLConnection();
        if (dbConn.isClosed()) {
            return;
        }
        String sql = "SELECT FileContent, Params, RiskType FROM Table_AbnormalImg WHERE CameraName = '" + lineName
                + "'";
        ResultSet results;
        try {
            results = dbConn.select(sql);
            while (results.next()) {
                Blob blob = results.getBlob("FileContent");
                BufferedInputStream bis = new BufferedInputStream(blob.getBinaryStream());
                String riskType = results.getString("RiskType");
                String paramStr = results.getString("Params");
                if (bis == null || riskType == null || paramStr == null) {
                    continue;
                }
                BufferedImage bimg = ImageIO.read(bis);
                if (bimg == null) {
                    continue;
                }
                String[] params = paramStr.split(";");
                for (String oneParam : params) {
                    if (!runFlag) {
                        return;
                    }
                    String[] axisVals = oneParam.split(" ");
                    BufferedImage cropped;
                    try {
                        float x = Float.parseFloat(axisVals[0]);
                        float y = Float.parseFloat(axisVals[1]);
                        float w = Float.parseFloat(axisVals[2]);
                        float h = Float.parseFloat(axisVals[3]);
                        cropped = bimg.getSubimage((int) x, (int) y, (int) w, (int) h);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        continue;
                    }
                    String feature = extractIMGfeature(cropped);
                    switch (riskType) {
                        case "杆塔设备":
                            feature = "1 " + feature;
                            break;
                        case "地面设备":
                            feature = "2 " + feature;
                            break;
                        default:
                            continue;
                    }
                    System.out.println(feature);
                    outFile.write(feature + "\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        proBar.setValue(proBar.getValue() + 20);
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
