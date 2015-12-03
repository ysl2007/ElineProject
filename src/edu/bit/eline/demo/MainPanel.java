package edu.bit.eline.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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

public class MainPanel extends JFrame {
    private static final long serialVersionUID = -8054742885149944542L;

    private Detection         detection;
    private Params            param;

    private JPanel            topLeft;
    private JPanel            topRight;
    private JPanel            topPanel;
    private JButton           browseImg;
    private JButton           browseModel;
    private JButton           run;
    private Container         container;
    private ImagePanel        imagePanel;
    private JTextField        dirField;
    private JTextField        modelField;
    private JTextField        varThrsh;
    private JTextField        minArea;
    private JTextField        alpha;
    private JScrollPane       centerPanel;

    private class Detection implements Runnable {
        private Detector            detector;
        private BlobAnalyzer        analyzer;
        private ImageConverter      converter;
        private ExtractFeature      ef;
        private ImageClassification ic;
        private Params              param;

        public Detection(Params param) {
            this.param = param;
            detector = new Detector(0, param.varThrshVal);
            analyzer = new BlobAnalyzer(param.minAreaVal);
            converter = new ImageConverter();
            ef = new ExtractFeature();
            ic = new ImageClassification();
        }

        @Override
        public void run() {
            if (!param.checkParams()) {
                JOptionPane.showMessageDialog(null, "Parameter error.");
                return;
            }
            int i = 0;
            for (String imgFilename : param.imgList) {
                BufferedImage bimg;
                try {
                    bimg = ImageIO.read(new File(imgFilename));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to open image file: "
                            + imgFilename);
                    continue;
                }
                if (bimg == null)
                    continue;
                System.out.println(++i);
                Mat imgMat = converter.convert2Mat(bimg);
                IplImage mask = detector.detect(imgMat, param.alphaVal, true);
                List<Blob> blobList;
                blobList = analyzer.analyze(mask);
                Color color = new Color(255, 0, 0);
                for (Blob blob : blobList) {
                    CvRect rect = blob.getRect();
                    BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(),
                            rect.width(), rect.height());
                    String feature = ef.extractIMGfeature(subimg);
                    String label = ic.classifyOneImg("4 " + feature,
                            ".\\config\\default\\model0.0.4.0.model",
                            ".\\config\\default\\scale.params");
                    if (!label.equals("0.0"))
                        imgMat = blob.drawRect(imgMat, color);
                }
                BufferedImage imgProcessed = converter.convert2JavaImg(imgMat);
                imagePanel.setImage(imgProcessed);
                centerPanel.setViewportView(imagePanel);
                imagePanel.setPreferredSize(new Dimension(imgProcessed
                        .getWidth(), imgProcessed.getHeight()));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MainPanel() {
        param = new Params();
        setupGUI();
    }

    public void setupGUI() {
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 左上角浏览文件夹部分
        JLabel dirLabel = new JLabel("图片文件夹：");
        dirField = new JTextField("e:/example/5");
        dirField.setColumns(15);
        browseImg = new JButton("浏览");
        browseImg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                browseButton(dirField);
            }
        });

        JLabel modLabel = new JLabel("模型文件夹：");
        modelField = new JTextField();
        modelField.setColumns(15);
        browseModel = new JButton("浏览");
        browseModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButton(modelField);
            }
        });

        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new FlowLayout());
        dirPanel.add(dirLabel);
        dirPanel.add(dirField);
        dirPanel.add(browseImg);

        JPanel modPanel = new JPanel();
        modPanel.setLayout(new FlowLayout());
        modPanel.add(modLabel);
        modPanel.add(modelField);
        modPanel.add(browseModel);

        topLeft = new JPanel();
        topLeft.setBorder(BorderFactory.createTitledBorder("文件："));
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.X_AXIS));
        topLeft.add(Box.createHorizontalStrut(5));
        topLeft.add(dirPanel);
        topLeft.add(Box.createHorizontalStrut(5));
        topLeft.add(modPanel);

        // 右上角参数选择、运行
        JPanel topRightArea = new JPanel();
        topRightArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        topRightArea.add(new JLabel("Variance:"));
        varThrsh = new JTextField();
        varThrsh.setText("9");
        varThrsh.setColumns(5);
        topRightArea.add(varThrsh);
        topRightArea.add(new JLabel("Learning Rate:"));
        alpha = new JTextField();
        alpha.setText("0.1");
        alpha.setColumns(5);
        topRightArea.add(alpha);
        topRightArea.add(new JLabel("Min Blob Area:"));
        minArea = new JTextField();
        minArea.setText("4000");
        minArea.setColumns(5);
        topRightArea.add(minArea);
        run = new JButton("运行");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                runButtomDemo();
            }
        });

        topRight = new JPanel();
        topRight.setBorder(BorderFactory.createTitledBorder("参数："));
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.X_AXIS));
        topRightArea.add(run);
        topRight.add(topRightArea);
        topRight.add(Box.createVerticalStrut(5));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        topPanel.add(topLeft);
        topPanel.add(topRight);
        topPanel.setEnabled(false);

        // 中部
        imagePanel = new ImagePanel();
        centerPanel = new JScrollPane();
        centerPanel.add(imagePanel);
        centerPanel
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 整个下部
        container.setLayout(new BorderLayout());
        container.add(topPanel, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);

        finalSettings();
    }

    protected List<String> getImgList() {
        List<String> imgList = new ArrayList<String>();
        String path = dirField.getText();
        File directory = new File(path);
        dirField.setText(directory.getAbsolutePath());
        File[] imgInDir = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                arg1 = arg1.toLowerCase();
                if (arg1.endsWith("jpg"))
                    return true;
                return false;
            }
        });
        if (null == imgInDir) {
            return null;
        }
        for (File img : imgInDir) {
            imgList.add(img.getAbsolutePath());
        }
        return imgList;
    }

    protected void runButtomDemo() {
        varThrsh.setEditable(false);
        minArea.setEditable(false);
        alpha.setEditable(false);
        if (detectInitialize()) {
            new Thread(detection).start();
        }
        varThrsh.setEditable(true);
        minArea.setEditable(true);
        alpha.setEditable(true);
    }

    protected void browseButton(JTextField textField) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
            textField.setText(selectedDir);
        }
    }

    protected boolean detectInitialize() {
        try {
            param.varThrshVal = Float.parseFloat(varThrsh.getText());
            param.minAreaVal = Integer.parseInt(minArea.getText());
            param.alphaVal = Double.parseDouble(alpha.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Parameters error.");
            return false;
        }

        List<String> imgList;
        if ((imgList = getImgList()) == null) {
            JOptionPane.showMessageDialog(null, "Image directory error.");
            return false;
        }

        param.imgList = imgList;
        detection = new Detection(param);
        return true;
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(1290, 800);
        setTitle("Demo");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new MainPanel();
    }
}
