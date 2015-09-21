package edu.bit.eline.demo;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

public class MainPanel extends JFrame {
    private static final long serialVersionUID = -8054742885149944542L;

    private ArrayList<String> imgList;
    private Params            param;
    private Detection         detection;

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

    public MainPanel() {
        imgList = new ArrayList<String>();
        setupGUI();
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

    protected void runButtom() {
        varThrsh.setEditable(false);
        ;
        minArea.setEditable(false);
        alpha.setEditable(false);
        if (detectInitialize()) {
            new Thread(detection).start();
        }
        varThrsh.setEditable(true);
        ;
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
        float varVal = 0;
        int areaVal = 0;
        double alphaVal = 0;
        boolean success = true;
        try {
            varVal = Float.parseFloat(varThrsh.getText());
            areaVal = Integer.parseInt(minArea.getText());
            alphaVal = Double.parseDouble(alpha.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Parameters error.");
            success = false;
        }
        if (!getImgList()) {
            JOptionPane.showMessageDialog(null, "Image directory error.");
            success = false;
        }
        if (!success || alphaVal <= 0 || varVal <= 0) {
            return false;
        }

        param = new Params();
        param.alphaVal = alphaVal;
        param.minAreaVal = areaVal;
        param.varThrshVal = varVal;
        param.imgList = imgList;
        param.imagePanel = imagePanel;
        param.bottomPane = bottomPane;
        detection = new Detection(param);
        return true;
    }

    private void finalSetting() {
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
