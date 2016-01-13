package edu.bit.eline.recognise.feature;

//格式转换类，转换成SVM需要的格式
public class Record {

    public String fileName;
    public String LineName;
    public String illum;
    public String bgType;
    public String objType;
    public String AutoColorCorrelogram;
    public String CEDD;
    public String ColorLayout;
    public String EdgeHistogram;
    public String ScalableColor;
    public String FCTH;
    public String Tamura;

    public Record(String record) {
        // TODO Auto-generated constructor stub
        String[] strarrs = record.split("@");
        this.objType = strarrs[4].substring(strarrs[4].indexOf(" ", 0), strarrs[4].length()).trim();
        this.AutoColorCorrelogram = strarrs[5].substring(strarrs[5].indexOf(" ", 0), strarrs[5].length() - 1).trim();
        this.CEDD = strarrs[6].substring(strarrs[6].indexOf(" ", 7), strarrs[6].length() - 1).trim();
        this.ColorLayout = strarrs[7].substring(strarrs[7].indexOf(" ", 0), strarrs[7].length() - 1).trim();
        this.EdgeHistogram = strarrs[8].substring(strarrs[8].indexOf(";", 20) + 1, strarrs[8].length() - 1).trim();
        this.ScalableColor = strarrs[9].substring(strarrs[9].indexOf(";", 31) + 1, strarrs[9].length() - 1).trim();
        this.FCTH = strarrs[10].substring(strarrs[10].indexOf(" ", 7) + 1, strarrs[10].length() - 1).trim();
        this.Tamura = strarrs[11].substring(strarrs[11].indexOf(" ", 10) + 1, strarrs[11].length() - 1).trim();
    }

    public String getRepresentation() {
        String str = "";
        String featurestr = "";
        switch (this.objType) {
            case "crane":
                str = "1";
                break;
            case "pump":
                str = "2";
                break;
            case "tower":
                str = "3";
                break;
            case "diggerLoader":
                str = "4";
                break;
            case "fog":
                str = "5";
                break;
            case "other":
                str = "6";
                break;
            default:
                str = "0";

        }
        str += " ";
        // featurestr+=this.AutoColorCorrelogram;
        // featurestr+=" ";
        featurestr += this.CEDD;
        featurestr += " ";
        featurestr += this.ColorLayout;
        featurestr += " ";
        featurestr += this.EdgeHistogram;
        featurestr += " ";
        featurestr += this.ScalableColor;
        featurestr += " ";
        featurestr += this.FCTH;
        featurestr += " ";
        featurestr += this.Tamura;

        return str + addnum(featurestr);
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

}
