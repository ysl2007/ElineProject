package edu.bit.eline.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

public class MainPanelDemo extends JFrame {
    private static final long serialVersionUID = -8054742885149944542L;

    private Detection detection;
    private Params    param;

    private JPanel      topLeft;
    private JPanel      topRight;
    private JPanel      topPanel;
    private JPanel      statusBar;
    private JPanel      topMidArea;
    private JLabel      fileName;
    private JLabel      numOfPics;
    private JButton     browseImg;
    private JButton     browseModel;
    private JButton     run;
    private JButton     pause;
    private JButton     resume;
    private JButton     setPauseTime;
    private JButton     stop;
    private Container   container;
    private ImagePanel  imagePanel;
    private JTextField  dirField;
    private JTextField  modelField;
    private JTextField  varThrsh;
    private JTextField  minArea;
    private JTextField  alpha;
    private JTextField  pauseTime;
    private JScrollPane centerPanel;

    private class Detection implements Runnable {
        private Detector            detector;
        private BlobAnalyzer        analyzer;
        private ImageConverter      converter;
        private ExtractFeature      ef;
        private ImageClassification ic;
        private Params              param;
        private int                 pauseTime;
        private boolean             run   = true;
        private boolean             pause = false;

        public Detection(Params param) {
            this.param = param;
            detector = new Detector(0, param.varThrshVal);
            analyzer = new BlobAnalyzer(param.minAreaVal);
            converter = new ImageConverter();
            ef = new ExtractFeature();
            ic = new ImageClassification();
        }

        public void stop() {
            synchronized (this) {
                this.run = false;
            }
        }

        public void pause() {
            synchronized (this) {
                this.pause = true;
            }
        }

        public void resume() {
            synchronized (this) {
                pause = false;
                notifyAll();
            }
        }

        public void setPauseTime(float time) {
            pauseTime = (int) (time * 1000);
        }

        @Override
        public void run() {
            if (!param.checkParams()) {
                JOptionPane.showMessageDialog(null, "Parameter error.");
                return;
            }
            int i = 0;
            for (String imgFilename : param.imgList) {
                synchronized (this) {
                    if (run == false) {
                        break;
                    }
                    if (pause) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                ++i;
                BufferedImage bimg;
                try {
                    bimg = ImageIO.read(new File(imgFilename));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to open image file: " + imgFilename);
                    continue;
                }
                if (bimg == null)
                    continue;
                Mat imgMat = converter.convert2Mat(bimg);
                IplImage mask = detector.detect(imgMat, param.alphaVal, true);
                List<Blob> blobList = analyzer.analyze(mask);
                Color color = new Color(255, 0, 0);
                System.out.println(blobList.size());
                for (Blob blob : blobList) {
                    if (run == false) {
                        break;
                    }
                    CvRect rect = blob.getRect();
                    BufferedImage subimg = bimg.getSubimage(rect.x(), rect.y(), rect.width(), rect.height());
                    String feature = ef.extractIMGfeature(subimg);
                    String label = ic.classifyOneImg("4 " + feature, param.finalModelPath, param.scaleParamPath,
                            param.tempimgfeaturepath, param.tempscaleimgfeaturepath, param.tempimageresultpath);
                    if (!label.equals("0.0")) {
                        imgMat = blob.drawRect(imgMat, color);
                    }
                }
                BufferedImage imgProcessed = converter.convert2JavaImg(imgMat);
                fileName.setText(imgFilename);
                numOfPics.setText(i + "/" + param.imgList.size());
                imagePanel.setImage(imgProcessed);
                centerPanel.setViewportView(imagePanel);
                imagePanel.setPreferredSize(new Dimension(imgProcessed.getWidth(), imgProcessed.getHeight()));
                try {
                    Thread.sleep(pauseTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(null, "处理完毕，已停止。");
        }
    }

    public MainPanelDemo() {
        setupGUI();
    }

    private void setupGUI() {
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 左上角浏览文件夹部分
        JLabel dirLabel = new JLabel("图片目录");
        dirField = new JTextField();
        dirField.setColumns(12);
        browseImg = new JButton("浏览");
        browseImg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                browseButton(dirField);
            }
        });

        JLabel modLabel = new JLabel("模型目录");
        modelField = new JTextField();
        modelField.setColumns(12);
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
        topLeft.setBorder(BorderFactory.createTitledBorder("文件"));
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.X_AXIS));
        topLeft.add(Box.createHorizontalStrut(5));
        topLeft.add(dirPanel);
        topLeft.add(Box.createHorizontalStrut(5));
        topLeft.add(modPanel);

        // 右上角参数选择
        topMidArea = new JPanel();
        topMidArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        topMidArea.add(new JLabel("方差"));
        varThrsh = new JTextField();
        varThrsh.setText("9");
        varThrsh.setColumns(3);
        topMidArea.add(varThrsh);
        topMidArea.add(new JLabel("学习率"));
        alpha = new JTextField();
        alpha.setText("0.1");
        alpha.setColumns(3);
        topMidArea.add(alpha);
        topMidArea.add(new JLabel("面积"));
        minArea = new JTextField();
        minArea.setText("4000");
        minArea.setColumns(4);
        topMidArea.add(minArea);
        topMidArea.setBorder(BorderFactory.createTitledBorder("参数"));

        JLabel pauseLabel = new JLabel("暂停时间");
        pauseTime = new JTextField();
        pauseTime.setText("1");
        pauseTime.setColumns(3);
        setPauseTime = new JButton("设置");
        setPauseTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (detection == null) {
                    System.out.println("detection null");
                    return;
                }
                detection.setPauseTime(Float.parseFloat(pauseTime.getText()));
            }
        });

        run = new JButton("运行");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                runButtom();
            }
        });
        pause = new JButton("暂停");
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseButton();
            }
        });
        resume = new JButton("继续");
        resume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeButton();
            }
        });
        stop = new JButton("停止");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detection.stop();
            }
        });

        topRight = new JPanel();
        topRight.setBorder(BorderFactory.createTitledBorder("运行"));
        topRight.setLayout(new FlowLayout());
        topRight.add(pauseLabel);
        topRight.add(pauseTime);
        topRight.add(setPauseTime);
        topRight.add(run);
        topRight.add(pause);
        topRight.add(resume);
        topRight.add(stop);

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        topPanel.add(topLeft);
        topPanel.add(topMidArea);
        topPanel.add(topRight);
        topPanel.setEnabled(false);

        // 中部
        imagePanel = new ImagePanel();
        imagePanel.setAlignmentX(CENTER_ALIGNMENT);
        imagePanel.setAlignmentY(CENTER_ALIGNMENT);
        centerPanel = new JScrollPane();
        centerPanel.add(imagePanel);
        centerPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 状态栏
        fileName = new JLabel();
        fileName.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        numOfPics = new JLabel();
        numOfPics.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        statusBar = new JPanel();
        statusBar.setPreferredSize(new Dimension(100, 23));
        statusBar.add(fileName);
        statusBar.add(numOfPics);
        GridBagLayout gd = new GridBagLayout();
        GridBagConstraints gdCon = new GridBagConstraints();
        gdCon.fill = GridBagConstraints.BOTH;
        gdCon.anchor = GridBagConstraints.WEST;
        gdCon.gridx = 0;
        gdCon.gridy = 0;
        gdCon.gridheight = 1;
        gdCon.gridwidth = 2;
        gdCon.insets = new Insets(0, 0, 0, 40);
        gd.setConstraints(fileName, gdCon);
        gdCon.gridx = 2;
        gdCon.gridy = 0;
        gdCon.gridheight = 1;
        gdCon.gridwidth = 1;
        gdCon.insets = new Insets(0, 40, 0, 0);
        gd.setConstraints(numOfPics, gdCon);
        statusBar.setLayout(gd);

        // 整个下部
        container.setLayout(new BorderLayout());
        container.add(topPanel, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);

        finalSettings();
    }

    private void browseButton(JTextField textField) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
            textField.setText(selectedDir);
        }
    }

    private List<String> getImgList() {
        List<String> imgList = new ArrayList<String>();
        String path = dirField.getText();
        File directory = new File(path);
        dirField.setText(directory.getAbsolutePath());
        File[] imgInDir = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                return arg1.toLowerCase().endsWith("jpg");
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

    private void runButtom() {
        varThrsh.setEditable(false);
        minArea.setEditable(false);
        alpha.setEditable(false);
        if (paramInitialize(modelField.getText())) {
            detection = new Detection(param);
            try {
                detection.setPauseTime(Float.parseFloat(pauseTime.getText()));
                new Thread(detection).start();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "参数有误。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        varThrsh.setEditable(true);
        minArea.setEditable(true);
        alpha.setEditable(true);
    }

    private void pauseButton() {
        detection.pause();
    }

    private void resumeButton() {
        detection.resume();
    }

    private boolean paramInitialize(String rootDir) {
        param = new Params(rootDir);
        try {
            param.varThrshVal = Float.parseFloat(varThrsh.getText());
            param.minAreaVal = Integer.parseInt(minArea.getText());
            param.alphaVal = Double.parseDouble(alpha.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "参数输入错误。");
            return false;
        }
        param.imgList = getImgList();
        if (!param.checkParams()) {
            return false;
        }
        return true;
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(1300, 855);
        setTitle("演示程序");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new MainPanelDemo();
    }
}
