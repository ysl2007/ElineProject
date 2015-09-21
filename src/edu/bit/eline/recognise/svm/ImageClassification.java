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

import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.feature.Record;
import libsvm.*;

public class ImageClassification {

    /*
     * private String configpath; private String modelspath; private String
     * featurepath; private String temppath;
     */
    public ImageClassification() {
        // TODO Auto-generated constructor stub
    }

    /*
     * public String setConfigpath(String path) { this.configpath=path;
     * this.modelspath=path+"\\models"; this.featurepath=path+"\\features";
     * this.temppath=path+"\\temp"; return this.configpath; }
     */
    /*
     * public void paramoptforall1classes() { String
     * result1=paramoptimize4oneclass(featurepath+"\\train-1.0.feature",
     * temppath+"\\model1.0.model",featurepath+"\\predict-1.0.feature",temppath+
     * "result1.result"); String
     * result2=paramoptimize4oneclass(featurepath+"\\train-2.0.feature",
     * temppath+"\\model2.0.model",featurepath+"\\predict-2.0.feature",temppath+
     * "result2.result"); String
     * result3=paramoptimize4oneclass(featurepath+"\\train-3.0.feature",
     * temppath+"\\model3.0.model",featurepath+"\\predict-3.0.feature",temppath+
     * "result3.result"); String
     * result4=paramoptimize4oneclass(featurepath+"\\train-4.0.feature",
     * temppath+"\\model4.0.model",featurepath+"\\predict-4.0.feature",temppath+
     * "result4.result"); String
     * result5=paramoptimize4oneclass(featurepath+"\\train-5.0.feature",
     * temppath+"\\model5.0.model",featurepath+"\\predict-5.0.feature",temppath+
     * "result5.result"); System.out.println(result1);
     * System.out.println(result2); System.out.println(result3);
     * System.out.println(result4); System.out.println(result5); }
     */
    /*
     * public void trainallwithoptparams() {
     * train("-s 2 -t 2 -c 0.0009765625 -g 0.03125 -n 0.05",
     * featurepath+"\\train-1.0.feature", modelspath+"\\model1.0.model");
     * train("-s 2 -t 2 -c 0.0009765625 -g 0.0625 -n 0.05",
     * featurepath+"\\train-2.0.feature", modelspath+"\\model2.0.model");
     * train("-s 2 -t 2 -c 0.0009765625 -g 0.0078125 -n 0.05",
     * featurepath+"\\train-3.0.feature", modelspath+"\\model3.0.model");
     * train("-s 2 -t 2 -c 0.0009765625 -g 0.03125 -n 0.05",
     * featurepath+"\\train-4.0.feature", modelspath+"\\model4.0.model");
     * train("-s 2 -t 2 -c 0.0009765625 -g 0.0625 -n 0.05",
     * featurepath+"\\train-5.0.feature", modelspath+"\\model5.0.model"); }
     */
    public String[] getLabels(String resultpath) {
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

    public String dataStat(String featurepath) {

        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(featurepath));
            String line = null;
            int allcount = 0, count1 = 0, count2 = 0, count3 = 0, count4 = 0, count5 = 0, count6 = 0, countother = 0;
            while ((line = sourceFile.readLine()) != null) {
                allcount++;
                switch (line.charAt(0)) {
                    case '1':
                        count1++;
                        break;
                    case '2':
                        count2++;
                        break;
                    case '3':
                        count3++;
                        break;
                    case '4':
                        count4++;
                        break;
                    case '5':
                        count5++;
                        break;
                    case '6':
                        count6++;
                        break;
                    default:
                        countother++;

                }
            }
            sourceFile.close();

            return "all:" + Integer.toString(allcount) + 
                    "\n1:" + Integer.toString(count1) + 
                    "\n2:" + Integer.toString(count2) + 
                    "\n3:" + Integer.toString(count3) + 
                    "\n4:" + Integer.toString(count4) + 
                    "\n5:" + Integer.toString(count5) + 
                    "\n6:" + Integer.toString(count6) + 
                    "\nother:" + Integer.toString(countother);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] randomCommon(int min, int max, int n) {
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

    public String revisedLabel(String sourcepath, String classlabel, String revisedpath) {
        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(sourcepath));
            BufferedWriter revisedFile = new BufferedWriter(new FileWriter(revisedpath));
            String line = null;

            while ((line = sourceFile.readLine()) != null) {
                String[] strarr = line.split(" ");
                if (classlabel.equals(strarr[0]))
                    strarr[0] = "1.0";
                else
                    strarr[0] = "-1.0";
                String temp = "";
                for (int i = 0; i < strarr.length; i++)
                    temp = temp + " " + strarr[i];
                revisedFile.write(temp.trim() + "\n");
            }

            sourceFile.close();
            revisedFile.close();
            return revisedpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String filterOneClass(String sourcepath, String classlabel, String filteredpath) {

        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(sourcepath));
            BufferedWriter fliteredFile = new BufferedWriter(new FileWriter(filteredpath));
            String line = null;

            while ((line = sourceFile.readLine()) != null) {
                if (classlabel.equals(line.split(" ")[0]))
                    fliteredFile.write(line + "\n");
            }

            sourceFile.close();
            fliteredFile.close();
            return filteredpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    private boolean exist(int i, int[] predictindex) {
        // TODO Auto-generated method stub
        boolean result = false;
        for (int j = 0; j < predictindex.length; j++) {
            if (i == predictindex[j])
                result = true;
        }
        return result;
    }

    /*
     * "Usage: svm-scale [options] data_filename\n" +"options:\n"
     * +"-l lower : x scaling lower limit (default -1)\n"
     * +"-u upper : x scaling upper limit (default +1)\n"
     * +"-y y_lower y_upper : y scaling limits (default: no y scaling)\n"
     * +"-s save_filename : save scaling parameters to save_filename\n"
     * +"-r restore_filename : restore scaling parameters from restore_filename\n"
     * );
     */
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    // ѵ����optionsΪ������sfeaturepathΪѵ��������·����modelpathΪѵ���õ�ģ�͵�·��
    public String train(String options, String sfeaturepath, String modelpath) {

        try {
            String args = options + " " + sfeaturepath + " " + modelpath;
            String trainArgs[] = args.split(" ");
            svm_train.main(trainArgs);
            return modelpath;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    // Ԥ�⣬optionΪ������dfeaturepathΪԤ��������·����modelpathΪԤ��ģ��·����resultpathΪ�����·����
    public String predict(String option, String dfeaturepath, String modelpath, String resultpath) {

        try {

            String args = option + " " + dfeaturepath + " " + modelpath + " " + resultpath;
            String testArgs[] = args.split(" ");
            svm_predict.main(testArgs);
            return resultpath;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    // ���������Ż�����
    public String paramoptimize(String trainpath, String modelpath, String predictpath, String resultpath, String[] classes) {

        // float [][]accs=new float[21][21];
        double bestacc = 0, bestc = 0, bestg = 0;
        for (int i = -2; i <= 2; i++)
            for (int j = -10; j <= 10; j++) {
                double c = Math.pow(2, i);
                double g = Math.pow(2, j);
                String option = "-t 2 " + "-c " + Double.toString(c) + " -g " + Double.toString(g);
                train(option, trainpath, modelpath);
                predict("-b 0", predictpath, modelpath, resultpath);
                // String []classes={"1.0","2.0","3.0","4.0","5.0"};
                Evaluater eva = new Evaluater(classes, getLabels(resultpath), getLabels(predictpath));
                // accs[i+10][j+10]=eva.getAccuracy();
                // if(accs[i+10][j+10]>bestacc){
                if (eva.getAccuracy() > bestacc) {
                    bestacc = eva.getAccuracy();
                    bestc = c;
                    bestg = g;
                }
            }

        // Best C: 8.0 Best G�� 0.015625 Best Acc�� 0.987
        return "Best C: " + Double.toString(bestc) + " Best G: " + Double.toString(bestg) + " Best Acc: " + Double.toString(bestacc);
    }

    // һ��������Ż�����
    public String paramoptimize4oneclass(String trainpath, String modelpath, String predictpath, String resultpath) {

        float[][] accs = new float[21][21];
        double bestacc = 0, bestc = 0, bestg = 0;
        for (int i = -10; i <= 10; i++)
            for (int j = -10; j <= 10; j++) {
                double c = Math.pow(2, i);
                double g = Math.pow(2, j);
                String option = "-s 2 -t 2 -n 0.05 " + "-c " + Double.toString(c) + " -g " + Double.toString(g);
                train(option, trainpath, modelpath);
                predict("-b 0", predictpath, modelpath, resultpath);
                String[] classes = { "1.0", "-1.0" };
                Evaluater eva = new Evaluater(classes, getLabels(resultpath), getLabels(predictpath));
                accs[i + 10][j + 10] = eva.getAccuracy();
                if (accs[i + 10][j + 10] > bestacc) {
                    bestacc = accs[i + 10][j + 10];
                    bestc = c;
                    bestg = g;
                }
            }

        return "Best C: " + Double.toString(bestc) + " Best G: " + Double.toString(bestg) + " Best Acc: " + Double.toString(bestacc);
    }

    public String classifyOneImg(String imgfeatures, String modelpath, String scaleparamfilepath) {
        return classifyOneImg(imgfeatures, modelpath, scaleparamfilepath, ".\\tempimage.feature", ".\\tempimage.feature.scale", "\\tempimage.result");
    }

    public String classifyOneImg(String imgfeatures, String modelpath, String paramfilepath, String sfeaturepath, String dfeaturepath, String resultpath) {
        String scaledpath = scaleOneImg(imgfeatures, paramfilepath, sfeaturepath, dfeaturepath);
        // String label="0.0";
        predict("-b 0", scaledpath, modelpath, resultpath);

        String label1[] = getLabels(resultpath);
        /*
         * String label2[]=getLabels(temppath+"\\oneimgresult2.0"); String
         * label3[]=getLabels(temppath+"\\oneimgresult3.0"); String
         * label4[]=getLabels(temppath+"\\oneimgresult4.0"); String
         * label5[]=getLabels(temppath+"\\oneimgresult5.0");
         */
        // if (label1[0].equals("4.0"))
        // label="4.0";
        /*
         * if (label2[0].equals("1.0")) label="2.0"; if
         * (label3[0].equals("1.0")) label="3.0"; if (label4[0].equals("1.0"))
         * label="4.0"; if (label5[0].equals("1.0")) label="5.0";
         */
        return label1[0];
    }

    public String scaleOneImg(String imgfeatures, String paramfilepath, String sfeaturepath, String dfeaturepath) {
        // String sfeaturepath=temppath+"\\oneimgfeature.feature";
        // String dfeaturepath=temppath+"\\oneimgscalefeature.feature";

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

    public String combinefeaturefiles(String sourcepath1, String sourcepath2, String destpath) {
        try {
            BufferedWriter outFile = new BufferedWriter(new FileWriter(destpath));
            BufferedReader inFile1 = new BufferedReader(new FileReader(sourcepath1));
            BufferedReader inFile2 = new BufferedReader(new FileReader(sourcepath2));
            String line = null;
            while ((line = inFile1.readLine()) != null)
                outFile.write(line + "\n");
            while ((line = inFile2.readLine()) != null)
                outFile.write(line + "\n");

            outFile.close();
            inFile1.close();
            inFile2.close();

            return destpath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        /*
         * String featurepath="E:\\������Ŀ\\train\\feature.feature"; String
         * scalefeaturepath="E:\\������Ŀ\\train\\feature.feature.scale"; String
         * modelpath="E:\\������Ŀ\\train\\model.model"; String
         * resultpath="E:\\������Ŀ\\train\\result.result"; String
         * trainpath="E:\\������Ŀ\\train\\train.feature.scale"; String
         * predictpath="E:\\������Ŀ\\train\\predict.feature.scale"; String
         * traindir="E:\\������Ŀ\\train"; String
         * predictdir="E:\\������Ŀ\\train"; String
         * []classes={"1.0","2.0","3.0","4.0","5.0"};
         */
        /*
         * case "crane": str="1"; break; case "pump": str="2"; break; case
         * "tower": str="3"; break; case "diggerLoader": str="4"; break; case
         * "fog": str="5";
         */
        // ExtractFeature ef=new ExtractFeature();
        // String
        // feature=ef.extractIMGfeature("E:\\������Ŀ\\config\\classes\\diggerLoader\\����һ��37(����)_20140921_103951_T_CH1_P1.jpg.sub.bmp");

        ImageClassification ic = new ImageClassification();
        // ic.setConfigpath("E:\\������Ŀ\\config");
        // String label=ic.classifyOneImg("1 "+feature);
        // System.out.println(label);
        // Integer.parseFloat("37098214285714284");

        // ic.trainallwithoptparams();
        // ic.scaledata("E:\\������Ŀ\\config\\features\\feature.feature",
        // "E:\\������Ŀ\\config\\features\\feature.feature.scalev2", true,
        // "E:\\������Ŀ\\config\\models\\scale.params");
        // ic.train("-s 2 -t 2 -c 9.765625E-4 -g 9.765625E-4 -n 0.05",
        // sfeaturepath, modelpath);
        /*
         * ic.revisedLabel(predictpath,
         * "1.0",predictdir+"\\predict-1.0.feature");
         * ic.revisedLabel(predictpath,
         * "2.0",predictdir+"\\predict-2.0.feature");
         * ic.revisedLabel(predictpath,
         * "3.0",predictdir+"\\predict-3.0.feature");
         * ic.revisedLabel(predictpath,
         * "4.0",predictdir+"\\predict-4.0.feature");
         * ic.revisedLabel(predictpath,
         * "5.0",predictdir+"\\predict-5.0.feature");
         * /*ic.filterOneClass(trainpath, "1.0",
         * traindir+"\\train-1.0.feature"); ic.filterOneClass(trainpath, "2.0",
         * traindir+"\\train-2.0.feature"); ic.filterOneClass(trainpath, "3.0",
         * traindir+"\\train-3.0.feature"); ic.filterOneClass(trainpath, "4.0",
         * traindir+"\\train-4.0.feature"); ic.filterOneClass(trainpath, "5.0",
         * traindir+"\\train-5.0.feature");
         */
        // Best C: 8.0 Best G�� 0.015625 Best Acc�� 0.987
        // C-SVC ic.train("-t 2 -c 9 -g 0.015625", trainpath, modelpath);
        /*
         * String
         * result1=ic.paramoptimize4oneclass(traindir+"\\train-1.0.feature",
         * modelpath,predictdir+"\\predict-1.0.feature",resultpath); String
         * result2=ic.paramoptimize4oneclass(traindir+"\\train-2.0.feature",
         * modelpath,predictdir+"\\predict-2.0.feature",resultpath); String
         * result3=ic.paramoptimize4oneclass(traindir+"\\train-3.0.feature",
         * modelpath,predictdir+"\\predict-3.0.feature",resultpath); String
         * result4=ic.paramoptimize4oneclass(traindir+"\\train-4.0.feature",
         * modelpath,predictdir+"\\predict-4.0.feature",resultpath); String
         * result5=ic.paramoptimize4oneclass(traindir+"\\train-5.0.feature",
         * modelpath,predictdir+"\\predict-5.0.feature",resultpath); // String
         * result1=ic.paramoptimize4oneclass(traindir+"\\train-1.0.feature",
         * modelpath,predictdir+"\\predict-1.0.feature",resultpath);
         * System.out.println(result1); System.out.println(result2);
         * System.out.println(result3); System.out.println(result4);
         * System.out.println(result5);
         * 
         * /* ic.train("-s 2 -t 2 -c 9.765625E-4 -g 9.765625E-4 -n 0.1",
         * traindir+"\\train-1.0.feature", modelpath); ic.predict("-b 0",
         * predictdir+"\\predict-1.0.feature", modelpath, resultpath); String
         * []classes1={"1.0","-1.0"}; Evaluater eva=new
         * Evaluater(classes1,ic.getLabels
         * (resultpath),ic.getLabels(predictdir+"\\predict-1.0.feature")); int
         * [][]temp=eva.getConfusionMetrix();
         * System.out.println(eva.getAccuracy());
         */
        // ic.paramoptimize(trainpath,modelpath,predictpath,resultpath);
        /*
         * String allstat=ic.dataStat(scalefeaturepath);
         * System.out.println("All Stat:\n"+allstat); String
         * trainstat=ic.dataStat(trainpath);
         * System.out.println("Train Stat:\n"+trainstat); String
         * predictstat=ic.dataStat(predictpath);
         * System.out.println("Predict Stat:\n"+predictstat);
         */

        // Evaluater eva=new
        // Evaluater(classes,ic.getLabels(resultpath),ic.getLabels(predictpath));
        // int [][]cfm=eva.getConfusionMetrix();
        // float acc=eva.getAccuracy();
        // float []p=eva.getPrecisions();
        // float []r=eva.getRecalls();
        // float [] fm=eva.getFmeasures();
        // System.out.println(" ");
        // ic.scaledata(featurepath,scalefeaturepath);
        // ic.generateTrainPredict(scalefeaturepath, 50, trainpath,
        // predictpath);

        // ic.train("-t 0", trainpath, modelpath);
        // ic.predict("-b 0", predictpath, modelpath, resultpath);

        // try {

        /*
         * String trainArgs[] = { "E:/������Ŀ/train/222.features.format",
         * "model.model" }; String testArgs[] = {
         * "E:/������Ŀ/train/111.features.format", "model.model",
         * "result.result" }; String scaleArgs1
         * []={">>E:/������Ŀ/train/222.features.scale.format"
         * ,"E:/������Ŀ/train/222.features.format"}; String scaleArgs2 []={
         * "E:/������Ŀ/train/111.features.format",
         * ">>E:/������Ŀ/train/111.features.scale.format"};
         * svm_scale.main(scaleArgs1); svm_scale.main(scaleArgs2);
         * svm_train.main(trainArgs); svm_predict.main(testArgs);
         * 
         * } catch (IOException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */

    }

}
