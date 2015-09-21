package edu.bit.eline.system;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Color;
import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

public class ElineRepr extends JFrame {
    ArrayList<String>         imgList;
    Detector                  detector;
    BlobAnalyzer              analyzer;
    ImageConverter            converter;
    ExtractFeature            ef;
    ImageClassification       ic;

    float                     varThrshVal      = 0;
    int                       minAreaVal       = 0;
    double                    alphaVal         = 0;

    private Container         container;
    private ImgPanel          imagePanel;
    private JButton           browse;
    private JTextField        dir;
    private JTextField        varThrsh;
    private JTextField        minArea;
    private JTextField        alpha;
    private JButton           run;
    private JPanel            topLeft;
    private JPanel            topRight;
    private JSplitPane        hSplitPane;
    private JSplitPane        vSplitPane;
    private JScrollPane       bottomPane;

    private static final long serialVersionUID = -8054742885149944542L;

    protected boolean getImgList() {
        imgList.clear();
        String path = dir.getText();
        File directory = new File(path);
        dir.setText(directory.getAbsolutePath());
        File[] imgInDir = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                arg1 = arg1.toLowerCase();
                if (arg1.endsWith("jpg"))
                    return true;
                return false;
            }
        });
        for (File img : imgInDir) {
            imgList.add(img.getAbsolutePath());
        }
        if (imgList.isEmpty())
            return false;
        return true;
    }

    public void setupGUI() {
        container = getContentPane();
        vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        dir = new JTextField();
        dir.setColumns(20);
        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                browseButton();
            }
        });

        topLeft = new JPanel();
        topLeft.setLayout(new FlowLayout(2));
        topLeft.add(browse);
        topLeft.add(dir);

        topRight = new JPanel();
        topRight.setLayout(new FlowLayout());

        topRight.add(new JLabel("Variance:"));
        varThrsh = new JTextField();
        varThrsh.setText("9");
        varThrsh.setColumns(5);
        topRight.add(varThrsh);

        topRight.add(new JLabel("Learning Rate:"));
        alpha = new JTextField();
        alpha.setText("0.1");
        alpha.setColumns(5);
        topRight.add(alpha);

        topRight.add(new JLabel("Min Blob Area:"));
        minArea = new JTextField();
        minArea.setText("4000");
        minArea.setColumns(5);
        topRight.add(minArea);

        run = new JButton("Run");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                runButtom();
            }
        });
        topRight.add(run);

        hSplitPane.setLeftComponent(topLeft);
        hSplitPane.setRightComponent(topRight);
        hSplitPane.setDividerLocation(0.4);
        hSplitPane.setEnabled(false);

        imagePanel = new ImgPanel();
        bottomPane = new JScrollPane();
        bottomPane.add(imagePanel);

        vSplitPane.setLeftComponent(hSplitPane);
        vSplitPane.setRightComponent(bottomPane);

        container.add(vSplitPane);

        finalSetting();
    }

    public ElineRepr() {
        imgList = new ArrayList<String>();
        setupGUI();
    }

    private void finalSetting() {
        setSize(1290, 800);
        setTitle("��ʾ");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    protected void runButtom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    detect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        varThrsh.setEditable(true);
        minArea.setEditable(true);
        alpha.setEditable(true);
    }

    protected void browseButton() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
            dir.setText(selectedDir);
        }
    }

    protected boolean detectInitialize() {
        varThrsh.setEditable(false);
        minArea.setEditable(false);
        alpha.setEditable(false);
        boolean success = true;
        try {
            varThrshVal = Float.parseFloat(varThrsh.getText());
            minAreaVal = Integer.parseInt(minArea.getText());
            alphaVal = Double.parseDouble(alpha.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "�����������������");
            success = false;
        }
        if (!getImgList()) {
            JOptionPane.showMessageDialog(null, "�ļ��б�Ϊ�գ�����Ŀ¼��");
            success = false;
        }
        if (!success || alphaVal <= 0 || varThrshVal <= 0) {
            varThrsh.setEditable(true);
            minArea.setEditable(true);
            alpha.setEditable(true);
            return false;
        }
        detector = new Detector(0, varThrshVal, false);
        analyzer = new BlobAnalyzer(minAreaVal);
        converter = new ImageConverter();
        ef = new ExtractFeature();
        ic = new ImageClassification();
        return true;
    }

    protected void detect() throws InterruptedException {
        detectInitialize();
        int i = 0;
        for (String imgFilename : imgList) {
            BufferedImage bimg;
            try {
                bimg = ImageIO.read(new File(imgFilename));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (bimg == null)
                continue;
            System.out.println(++i);
            Mat imgMat = converter.convert2Mat(bimg);
            IplImage mask = detector.detect(imgMat, alphaVal, true);
            List<Blob> blobList;
            blobList = analyzer.analyze(mask);
            Color color = new Color(255, 0, 0);
            for (Blob blob : blobList) {
                CvRect rect = blob.getRect();
                BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(), rect.width(), rect.height());
                String feature = ef.extractIMGfeature(subimg);
                // String label = ic.classifyOneImg("1 " + feature);
                String label = ic.classifyOneImg("4 " + feature, ".\\config\\models\\model0.0.4.0.model", ".\\config\\models\\scale.params");
                if (!label.equals("0.0"))
                    imgMat = blob.drawRect(imgMat, color);
            }
            // ��ʾͼ��
            BufferedImage imgProcessed = converter.convert2JavaImg(imgMat);
            imagePanel.setImage(imgProcessed);
            bottomPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            bottomPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            bottomPane.setViewportView(imagePanel);
            Thread.sleep(500);
        }
        varThrsh.setEditable(true);
        minArea.setEditable(true);
        alpha.setEditable(true);
    }

    public static void main(String[] args) {
        new ElineRepr();
    }
}
