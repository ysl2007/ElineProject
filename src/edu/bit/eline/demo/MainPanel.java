package edu.bit.eline.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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

    private Params            param;
    private Detection         detection;

    private Container         container;
    private ImgPanel          imagePanel;
    private JButton           browse;
    private JButton           run;
    private JButton           getTree;
    private JTextField        dir;
    private JTextField        varThrsh;
    private JTextField        minArea;
    private JTextField        alpha;
    private JPanel            topLeft;
    private JPanel            topRight;
    private JPanel            topPanel;
    private JTree             treePanel;
    private JPanel            westPanel;
    private JPanel            westTopPanel;
    private JSplitPane        bottomPanel;

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
                System.err.println("Parameter error.");
                return;
            }
            int i = 0;
            for (String imgFilename : param.imgList) {
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
                System.out.println(++i);
                Mat imgMat = converter.convert2Mat(bimg);
                IplImage mask = detector.detect(imgMat, param.alphaVal, true);
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
                BufferedImage imgProcessed = converter.convert2JavaImg(imgMat);
                imagePanel.setImage(imgProcessed);
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
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        topLeft.add(Box.createVerticalStrut(5));
        JPanel topLeftButton = new JPanel();
        topLeftButton.setLayout(new FlowLayout());
        topLeftButton.add(browse);
        topLeftButton.add(dir);
        topLeft.add(topLeftButton);
        topLeft.add(Box.createVerticalStrut(5));
        topLeft.setPreferredSize(new Dimension(150, 10));

        topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
        topRight.add(Box.createVerticalStrut(5));

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
        run = new JButton("Run");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                runButtom();
            }
        });
        topRightArea.add(run);
        topRight.add(topRightArea);
        topRight.add(Box.createVerticalStrut(5));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        topPanel.add(topLeft);
        topPanel.add(topRight);
        topPanel.setEnabled(false);

        getTree = new JButton("Obtain camera tree");
        getTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!getCameraTreeDemo()) {
                    JOptionPane.showMessageDialog(null, "Failed to obtain camera tree.");
                }
            }
        });

        westTopPanel = new JPanel();
        westTopPanel.setMinimumSize(new Dimension(160, 40));
        westTopPanel.add(getTree);

        treePanel = new JTree();
        treePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                int selRow = treePanel.getRowForLocation(e.getX(), e.getY());
                if(selRow != -1 && e.isPopupTrigger()) {
                    System.out.println("popup!");
                }
            }
        });
        
        westPanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();
        westPanel.setLayout(gb);
        westPanel.add(westTopPanel);
        westPanel.add(treePanel);
        GridBagConstraints gbCon = new GridBagConstraints();

        gbCon.fill = GridBagConstraints.NONE;
        gbCon.gridwidth = 0;
        gbCon.weightx = 0;
        gbCon.weighty = 0;
        gb.setConstraints(westTopPanel, gbCon);

        gbCon.fill = GridBagConstraints.BOTH;
        gbCon.gridwidth = 0;
        gbCon.weightx = 1;
        gbCon.weighty = 1;
        gb.setConstraints(treePanel, gbCon);

        imagePanel = new ImgPanel();
        bottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomPanel.setLeftComponent(westPanel);
        bottomPanel.setRightComponent(imagePanel);
        bottomPanel.setEnabled(true);

        container.setLayout(new BorderLayout());
        container.add(topPanel, BorderLayout.NORTH);
        container.add(bottomPanel, BorderLayout.CENTER);

        finalSetting();
    }

    protected boolean getCameraTree() {
        // TODO: getCameraTree
        return false;
    }
    
    protected boolean getCameraTreeDemo(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        root.add(new DefaultMutableTreeNode("child1"));
        root.add(new DefaultMutableTreeNode("child2"));
        DefaultTreeModel dt = new DefaultTreeModel(root);
        treePanel.setModel(dt);
        return true;
    }

    protected List<String> getImgList() {
        List<String> imgList = new ArrayList<String>();
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
            return null;
        return imgList;
    }

    protected void runButtom() {
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

    private void finalSetting() {
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
