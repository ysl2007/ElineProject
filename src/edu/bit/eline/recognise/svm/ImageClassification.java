package edu.bit.eline.recognise.svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ImageClassification {

    public String generateTrainPredict(String sourcepath, int predictrate, String trainpath, String predictpath) {
        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(sourcepath));
            BufferedWriter trainFile = new BufferedWriter(new FileWriter(trainpath));
            BufferedWriter predictFile = new BufferedWriter(new FileWriter(predictpath));
            String line = null;

            List<String> list = new ArrayList<String>();
            while ((line = sourceFile.readLine()) != null) {
                list.add(line);
            }
            int predictnum = list.size() * predictrate / 100;
            int[] predictindex = randomCommon(0, list.size() - 1, predictnum);
            for (int i = 0; i < predictindex.length; i++) {
                predictFile.write(list.get(predictindex[i]) + "\n");
            }
            for (int i = 0; i < list.size(); i++) {
                if (!exist(i, predictindex))
                    trainFile.write(list.get(i) + "\n");
            }
            sourceFile.close();
            trainFile.close();
            predictFile.close();
            return trainpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String scaledata(String sfeaturepath, String dfeaturepath, Boolean saveflag, String paramfilepath) {
        String scaleArgs[];
        String args;
        if (saveflag) {
            args = "-s " + paramfilepath + " " + sfeaturepath;
            scaleArgs = args.split(" ");
        } else {
            args = "-r " + paramfilepath + " " + sfeaturepath;
            scaleArgs = args.split(" ");
        }
        try {
            PrintStream orgout = System.out;
            File featurefile = new File(dfeaturepath);
            PrintStream out = new PrintStream(new FileOutputStream(featurefile));
            System.setOut(out);
            svm_scale.main(scaleArgs);
            System.setOut(orgout);
            return dfeaturepath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String train(String options, String sfeaturepath, String modelpath) {
        try {
            String args = options + " " + sfeaturepath + " " + modelpath;
            String trainArgs[] = args.split(" ");
            svm_train.main(trainArgs);
            return modelpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String classifyOneImg(String imgfeatures, String modelpath, String paramfilepath, String sfeaturepath,
            String dfeaturepath, String resultpath) {
        String scaledpath = scaleOneImg(imgfeatures, paramfilepath, sfeaturepath, dfeaturepath);
        predict("-b 0", scaledpath, modelpath, resultpath);
        String label1[] = getLabels(resultpath);
        return label1[0];
    }

    // private methods
    private String[] getLabels(String resultpath) {
        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(resultpath));
            String line = null;
            String label = "";
            while ((line = sourceFile.readLine()) != null) {
                label = label + " " + line.split(" ")[0];

            }
            sourceFile.close();
            return label.trim().split(" ");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean exist(int i, int[] predictindex) {
        boolean result = false;
        for (int j = 0; j < predictindex.length; j++) {
            if (i == predictindex[j])
                result = true;
        }
        return result;
    }

    private int[] randomCommon(int min, int max, int n) {
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while (count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    private String predict(String option, String dfeaturepath, String modelpath, String resultpath) {
        try {
            String args = option + " " + dfeaturepath + " " + modelpath + " " + resultpath;
            String testArgs[] = args.split(" ");
            svm_predict.main(testArgs);
            return resultpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String scaleOneImg(String imgfeatures, String paramfilepath, String sfeaturepath, String dfeaturepath) {
        try {
            BufferedWriter outFile = new BufferedWriter(new FileWriter(sfeaturepath));
            outFile.write(imgfeatures + "\n");
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        scaledata(sfeaturepath, dfeaturepath, false, paramfilepath);
        return dfeaturepath;
    }
}
