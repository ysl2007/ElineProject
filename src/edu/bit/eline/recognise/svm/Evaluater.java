package edu.bit.eline.recognise.svm;

public class Evaluater {
    private String[] classes;
    private String[] predictlabels;
    private String[] truelabels;
    private int[][]  classtrueindexs;
    private int[][]  classpredictindexs;

    public Evaluater(String[] classes, String[] predictlabels, String[] truelabels) {
        this.classes = classes;
        this.predictlabels = predictlabels;
        this.truelabels = truelabels;
        classtrueindexs = new int[classes.length][];
        classpredictindexs = new int[classes.length][];
        for (int i = 0; i < classes.length; i++) {
            classtrueindexs[i] = findlabelindexs(truelabels, classes[i]);
            classpredictindexs[i] = findlabelindexs(predictlabels, classes[i]);
        }
    }

    public int[] findlabelindexs(String[] labels, String label) {
        String indexs = "";
        if (labels == null)
            return null;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(label))
                indexs += " " + Integer.toString(i);
        }
        if (indexs.length() > 0) {
            String[] indexsarray = indexs.trim().split(" ");
            int[] index = new int[indexsarray.length];
            for (int i = 0; i < index.length; i++) {
                index[i] = Integer.parseInt(indexsarray[i]);
            }
            return index;
        } else {
            return null;
        }
    }

    public int[][] getConfusionMetrix() {

        int[][] ConfustionMetrix = new int[classes.length][classes.length];
        for (int i = 0; i < classes.length; i++) {
            String[] curpredictlabels = getSubLabels(predictlabels, classtrueindexs[i]);
            for (int j = 0; j < classes.length; j++) {
                int[] labelindex = findlabelindexs(curpredictlabels, classes[j]);
                if (labelindex != null)
                    ConfustionMetrix[i][j] = labelindex.length;
                else
                    ConfustionMetrix[i][j] = 0;
            }
        }
        return ConfustionMetrix;
    }

    public String[] getSubLabels(String[] labels, int[] indexs) {
        String[] sublabel = null;
        if (indexs != null) {
            sublabel = new String[indexs.length];
            for (int i = 0; i < indexs.length; i++) {
                sublabel[i] = labels[indexs[i]];
            }
        }
        return sublabel;
    }

    public float[] getFmeasures() {
        float[] fmeasures = new float[classes.length];
        float[] precisions = getPrecisions();
        float[] recalls = getRecalls();
        for (int i = 0; i < classes.length; i++) {
            if ((precisions[i] + recalls[i]) > 0)
                fmeasures[i] = (2 * precisions[i] * recalls[i]) / (precisions[i] + recalls[i]);
            else
                fmeasures[i] = 0;
        }
        return fmeasures;
    }

    public float[] getPrecisions() {
        float[] precisions = new float[classes.length];
        for (int i = 0; i < classes.length; i++) {
            int curpredictclasssize = 0;
            if (classpredictindexs[i] != null)
                curpredictclasssize = classpredictindexs[i].length;
            int curpredictrightsize = 0;
            int[] labelindex = findlabelindexs(getSubLabels(truelabels, classpredictindexs[i]), classes[i]);
            if (labelindex != null)
                curpredictrightsize = labelindex.length;
            if (curpredictclasssize > 0)
                precisions[i] = (float) curpredictrightsize / curpredictclasssize;
            else
                precisions[i] = 0;
        }
        return precisions;

    }

    public float getAccuracy() {
        int rightnum = 0;
        for (int i = 0; i < truelabels.length; i++) {
            if (predictlabels[i].equals(truelabels[i]))
                rightnum++;
        }
        return (float) rightnum / truelabels.length;
    }

    public float getRecall() {
        int positiveTrue = 0;
        int positiveAll = 0;
        for (int i = 0; i < truelabels.length; ++i) {
            if (truelabels[i].equals("0.0") == false) {
                ++positiveAll;
                if (predictlabels[i].equals("0.0")) {
                    ++positiveTrue;
                }
            }
        }
        if (positiveAll == 0) {
            return 0;
        }
        return (float) positiveTrue / positiveAll;
    }

    public float[] getRecalls() {
        float[] recalls = new float[classes.length];
        for (int i = 0; i < classes.length; i++) {
            int curtrueclasssize = classtrueindexs[i].length;
            int curpredictrightsize = 0;
            int[] labelindex = findlabelindexs(getSubLabels(truelabels, classpredictindexs[i]), classes[i]);
            if (labelindex != null)
                curpredictrightsize = labelindex.length;
            if (curtrueclasssize > 0)
                recalls[i] = (float) curpredictrightsize / curtrueclasssize;
            else
                recalls[i] = 0;
        }
        return recalls;
    }
}
