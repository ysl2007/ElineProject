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

import javax.swing.JProgressBar;

import edu.bit.eline.system.TrainHelper;

public class ImageClassification implements Runnable {
    private boolean      runFlag      = true;
    private boolean      folderStatus = false;
    private String       trainpath;
    private String       modelpath;
    private String       predictpath;
    private String       resultpath;
    private String[]     classes;
    private TrainHelper  callback;
    private JProgressBar proBar;

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

    public String paramoptimize() {
        double bestCcandi = 0, bestGcandi = 0;
        double bestrec = 0, bestacc = 0, bestc = 0, bestg = 0;
        int stage = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -10; j <= 10; j++) {
                if (!runFlag) {
                    return null;
                }
                System.out.println("opti: i=" + i + " j=" + j);
                double c = Math.pow(2, i);
                double g = Math.pow(2, j);
                String option = "-t 2 " + "-c " + Double.toString(c) + " -g " + Double.toString(g);
                train(option, trainpath, modelpath);
                predict("-b 0", predictpath, modelpath, resultpath);
                Evaluater eva = new Evaluater(classes, getLabels(resultpath), getLabels(predictpath));
                double acc = eva.getAccuracy();
                double rec = eva.getRecall();
                if (acc >= bestacc && rec >= bestrec){
                    bestCcandi = c;
                    bestGcandi = g;
                }
                if (acc > 0.5 && rec > bestrec) {
                    bestacc = acc;
                    bestrec = rec;
                    bestc = c;
                    bestg = g;
                }
                ++stage;
                proBar.setValue((int) ((float) stage / 105 * 100));
            }
        }
        bestc = bestc == 0 ? bestCcandi : bestc;
        bestg = bestg == 0 ? bestGcandi : bestg;
        return "Best C: " + Double.toString(bestc) + " Best G: " + Double.toString(bestg) + " Best Acc: "
                + Double.toString(bestacc);
    }

    public String classifyOneImg(String imgfeatures, String modelpath, String paramfilepath, String sfeaturepath,
            String dfeaturepath, String resultpath) {
        String scaledpath = scaleOneImg(imgfeatures, paramfilepath, sfeaturepath, dfeaturepath);
        predict("-b 0", scaledpath, modelpath, resultpath);
        String label1[] = getLabels(resultpath);
        return label1[0];
    }

    @Override
    public void run() {
        if (!(runFlag && folderStatus)) {
            return;
        }
        String result = paramoptimize();
        if (result != null) {
            callback.optiCallBack(result, TrainHelper.PARAMS_OPTIMIZED, proBar);
        }
    }

    public void setFolders(String trainpath, String modelpath, String predictpath, String resultpath,
            String[] classes) {
        this.trainpath = trainpath;
        this.modelpath = modelpath;
        this.predictpath = predictpath;
        this.resultpath = resultpath;
        this.classes = classes;
        this.folderStatus = true;
    }

    public void setCallback(TrainHelper callback, JProgressBar proBar) {
        this.callback = callback;
        this.proBar = proBar;
    }

    public void setRunFlag(boolean flag) {
        this.runFlag = flag;
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

    // private String classifyOneImg(String imgfeatures, String modelpath,
    // String scaleparamfilepath) {
    // return classifyOneImg(imgfeatures, modelpath, scaleparamfilepath,
    // ".\\tempimage.feature",
    // ".\\tempimage.feature.scale", "\\tempimage.result");
    // }
    //
    // private String dataStat(String featurepath) {
    // try {
    // BufferedReader sourceFile = new BufferedReader(new
    // FileReader(featurepath));
    // String line = null;
    // int allcount = 0, count1 = 0, count2 = 0, count3 = 0, count4 = 0, count5
    // = 0, count6 = 0, countother = 0;
    // while ((line = sourceFile.readLine()) != null) {
    // allcount++;
    // switch (line.charAt(0)) {
    // case '1':
    // count1++;
    // break;
    // case '2':
    // count2++;
    // break;
    // case '3':
    // count3++;
    // break;
    // case '4':
    // count4++;
    // break;
    // case '5':
    // count5++;
    // break;
    // case '6':
    // count6++;
    // break;
    // default:
    // countother++;
    //
    // }
    // }
    // sourceFile.close();
    // return "all:" + Integer.toString(allcount) + "\n1:" +
    // Integer.toString(count1) + "\n2:"
    // + Integer.toString(count2) + "\n3:" + Integer.toString(count3) + "\n4:" +
    // Integer.toString(count4)
    // + "\n5:" + Integer.toString(count5) + "\n6:" + Integer.toString(count6) +
    // "\nother:"
    // + Integer.toString(countother);
    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    // private String revisedLabel(String sourcepath, String classlabel, String
    // revisedpath) {
    // try {
    // BufferedReader sourceFile = new BufferedReader(new
    // FileReader(sourcepath));
    // BufferedWriter revisedFile = new BufferedWriter(new
    // FileWriter(revisedpath));
    // String line = null;
    // while ((line = sourceFile.readLine()) != null) {
    // String[] strarr = line.split(" ");
    // if (classlabel.equals(strarr[0]))
    // strarr[0] = "1.0";
    // else
    // strarr[0] = "-1.0";
    // String temp = "";
    // for (int i = 0; i < strarr.length; i++)
    // temp = temp + " " + strarr[i];
    // revisedFile.write(temp.trim() + "\n");
    // }
    // sourceFile.close();
    // revisedFile.close();
    // return revisedpath;
    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    // private String filterOneClass(String sourcepath, String classlabel,
    // String filteredpath) {
    // try {
    // BufferedReader sourceFile = new BufferedReader(new
    // FileReader(sourcepath));
    // BufferedWriter fliteredFile = new BufferedWriter(new
    // FileWriter(filteredpath));
    // String line = null;
    // while ((line = sourceFile.readLine()) != null) {
    // if (classlabel.equals(line.split(" ")[0]))
    // fliteredFile.write(line + "\n");
    // }
    // sourceFile.close();
    // fliteredFile.close();
    // return filteredpath;
    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    // private String paramoptimize4oneclass(String trainpath, String modelpath,
    // String predictpath, String resultpath) {
    // float[][] accs = new float[21][21];
    // double bestacc = 0, bestc = 0, bestg = 0;
    // for (int i = -10; i <= 10; i++)
    // for (int j = -10; j <= 10; j++) {
    // double c = Math.pow(2, i);
    // double g = Math.pow(2, j);
    // String option = "-s 2 -t 2 -n 0.05 " + "-c " + Double.toString(c) + " -g
    // " + Double.toString(g);
    // train(option, trainpath, modelpath);
    // predict("-b 0", predictpath, modelpath, resultpath);
    // String[] classes = { "1.0", "-1.0" };
    // Evaluater eva = new Evaluater(classes, getLabels(resultpath),
    // getLabels(predictpath));
    // accs[i + 10][j + 10] = eva.getAccuracy();
    // if (accs[i + 10][j + 10] > bestacc) {
    // bestacc = accs[i + 10][j + 10];
    // bestc = c;
    // bestg = g;
    // }
    // }
    // return "Best C: " + Double.toString(bestc) + " Best G: " +
    // Double.toString(bestg) + " Best Acc: "
    // + Double.toString(bestacc);
    // }
    //
    // private String combinefeaturefiles(String sourcepath1, String
    // sourcepath2, String destpath) {
    // try {
    // BufferedWriter outFile = new BufferedWriter(new FileWriter(destpath));
    // BufferedReader inFile1 = new BufferedReader(new FileReader(sourcepath1));
    // BufferedReader inFile2 = new BufferedReader(new FileReader(sourcepath2));
    // String line = null;
    // while ((line = inFile1.readLine()) != null)
    // outFile.write(line + "\n");
    // while ((line = inFile2.readLine()) != null)
    // outFile.write(line + "\n");
    // outFile.close();
    // inFile1.close();
    // inFile2.close();
    // return destpath;
    // } catch (Exception e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
}
