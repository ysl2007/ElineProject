package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private TrainHelper       tHelper;
    private String            selectedClass;
    private String            configFile       = "./config.json";
    private String            rootPath;

    private Container         container;
    private JPanel            detParams;
    private JPanel            center;
    private JPanel            topPanel;
    private JPanel            detection;
    private JPanel            classPanel;
    private JPanel            samplePanel;
    private JPanel            buttonPanel;
    private JPanel            actionPanel;
    private JButton           getReady;
    private JButton           generData;
    private JButton           featExtract;
    private JButton           featNorm;
    private JButton           paramOpti;
    private JButton           train;
    private JButton           positiveDirBrowse;
    private JButton           negativeDirBrowse;
    private JButton           exit;
    private JTextField        name;
    private JTextField        var;
    private JTextField        alpha;
    private JTextField        minArea;
    private JTextField        positiveDirField;
    private JTextField        negativeDirField;
    private ButtonGroup       btGroup;
    private JProgressBar      progressBar;
    private JRadioButton      craneCheck;
    private JRadioButton      pumpCheck;
    private JRadioButton      towerCheck;
    private JRadioButton      diggerCheck;
    private JRadioButton      fogCheck;

    public Train(String lineName) {
        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        JSONObject jo = new JSONObject(tokener);
        try {
            rootPath = jo.getString("config_root_path");
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "配置文件不完整。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        setupGUI(lineName);
        finalSettings();
    }

    class CheckListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (craneCheck.isSelected())
                selectedClass = "1.0";
            else if (pumpCheck.isSelected())
                selectedClass = "2.0";
            else if (towerCheck.isSelected())
                selectedClass = "3.0";
            else if (diggerCheck.isSelected())
                selectedClass = "4.0";
            else if (fogCheck.isSelected())
                selectedClass = "5.0";
            else
                selectedClass = null;
        }
    }

    protected void setupGUI(String lineName) {
        // detection参数部分
        detParams = new JPanel();
        GridLayout gl = new GridLayout(4, 2);
        gl.setVgap(5);
        detParams.setLayout(gl);
        detParams.add(new JLabel("线路名称（全称）："));
        name = new JTextField();
        if (!(lineName == null || lineName.equals(""))) {
            name.setText(lineName);
            name.setEditable(false);
        }
        detParams.add(name);
        detParams.add(new JLabel("方差阈值："));
        var = new JTextField("9");
        detParams.add(var);
        detParams.add(new JLabel("学习率："));
        alpha = new JTextField("0.1");
        detParams.add(alpha);
        detParams.add(new JLabel("最小识别面积："));
        minArea = new JTextField("4000");
        detParams.add(minArea);

        detection = new JPanel();
        detection.setBorder(BorderFactory.createTitledBorder("检测"));
        detection.setLayout(new BoxLayout(detection, BoxLayout.X_AXIS));
        detection.add(Box.createHorizontalStrut(5));
        detection.add(detParams);
        detection.add(Box.createHorizontalStrut(5));

        // 异常类别部分
        CheckListener listener = new CheckListener();
        craneCheck = new JRadioButton("吊车");
        craneCheck.setAlignmentX(LEFT_ALIGNMENT);
        craneCheck.addActionListener(listener);

        pumpCheck = new JRadioButton("泵车");
        pumpCheck.setAlignmentX(LEFT_ALIGNMENT);
        pumpCheck.addActionListener(listener);

        diggerCheck = new JRadioButton("地面设备");
        diggerCheck.setAlignmentX(LEFT_ALIGNMENT);
        diggerCheck.addActionListener(listener);

        towerCheck = new JRadioButton("塔吊");
        towerCheck.setAlignmentX(LEFT_ALIGNMENT);
        towerCheck.addActionListener(listener);

        fogCheck = new JRadioButton("烟雾");
        fogCheck.setAlignmentX(LEFT_ALIGNMENT);
        fogCheck.addActionListener(listener);

        btGroup = new ButtonGroup();
        btGroup.add(craneCheck);
        btGroup.add(pumpCheck);
        btGroup.add(diggerCheck);
        btGroup.add(towerCheck);
        btGroup.add(fogCheck);

        classPanel = new JPanel();
        classPanel.setBorder(BorderFactory.createTitledBorder("异常类"));
        classPanel.setLayout(new GridLayout(3, 2));
        classPanel.setPreferredSize(new Dimension(100, 30));
        classPanel.add(craneCheck);
        classPanel.add(pumpCheck);
        classPanel.add(diggerCheck);
        classPanel.add(towerCheck);
        classPanel.add(fogCheck);

        // 样本路径选择
        JLabel positiveDir = new JLabel("正例样本路径：");
        positiveDirField = new JTextField();
        positiveDirField.setColumns(10);
        positiveDirBrowse = new JButton("浏览");
        positiveDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile()
                            .getAbsolutePath();
                    positiveDirField.setText(selectedDir);
                }
            }
        });
        JPanel positive = new JPanel();
        positive.add(positiveDir);
        positive.add(positiveDirField);
        positive.add(positiveDirBrowse);

        JLabel negativeDir = new JLabel("负例样本路径：");
        negativeDirField = new JTextField();
        negativeDirField.setColumns(10);
        negativeDirBrowse = new JButton("浏览");
        negativeDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile()
                            .getAbsolutePath();
                    negativeDirField.setText(selectedDir);
                }
            }
        });
        JPanel negative = new JPanel();
        negative.add(negativeDir);
        negative.add(negativeDirField);
        negative.add(negativeDirBrowse);

        samplePanel = new JPanel();
        samplePanel.setBorder(BorderFactory.createTitledBorder("样本路径"));
        samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.Y_AXIS));
        samplePanel.add(positive);
        samplePanel.add(negative);

        // 整个中部
        center = new JPanel();
        center.setLayout(new GridLayout(3, 1));
        center.add(detection);
        center.add(classPanel);
        center.add(samplePanel);

        // 动作按钮
        getReady = new JButton("准备训练");
        getReady.setAlignmentX(CENTER_ALIGNMENT);
        getReady.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReady();
            }
        });

        featExtract = new JButton("特征提取");
        featExtract.setAlignmentX(CENTER_ALIGNMENT);
        featExtract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                featureExtract();
            }
        });

        featNorm = new JButton("特征归一化");
        featNorm.setAlignmentX(CENTER_ALIGNMENT);
        featNorm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                featureNorm();
            }
        });

        generData = new JButton("准备数据");
        generData.setAlignmentX(CENTER_ALIGNMENT);
        generData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateData();
            }
        });

        paramOpti = new JButton("参数优化");
        paramOpti.setAlignmentX(CENTER_ALIGNMENT);
        paramOpti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optiParams();
            }
        });

        train = new JButton("训练");
        train.setAlignmentX(CENTER_ALIGNMENT);
        train.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                train();
            }
        });

        exit = new JButton("关闭");
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                disposeMe();
            }
        });

        // 东侧动作按钮区
        actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createTitledBorder("动作"));
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(getReady);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(featExtract);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(featNorm);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(generData);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(paramOpti);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(train);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(exit);

        // 南侧进度条部分
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(400, 20));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(progressBar);

        // 最上层部分
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(5, 10));
        topPanel.add(center, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        topPanel.add(actionPanel, BorderLayout.EAST);

        container = new Container();
        container.setLayout(new FlowLayout());
        container.add(topPanel);
    }

    private void getReady() {
        name.setEditable(false);
        String lineName = name.getText();
        if (lineName == null || lineName.length() == 0) {
            JOptionPane.showMessageDialog(null, "没有指定线路名称。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            name.setEditable(true);
            return;
        }
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(null, "没有选择异常类别。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String pos = positiveDirField.getText();
        String neg = negativeDirField.getText();
        if (pos == null || neg == null || pos.trim().length() == 0
                || neg.trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "没有选择样本路径。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper = new TrainHelper(lineName, selectedClass, pos, neg);
        tHelper.getDirsReady();
        if (validateStatus(TrainHelper.DIRS_READY) == 0) {
            JOptionPane.showMessageDialog(null, "训练准备完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void featureExtract() {
        if (validateStatus(TrainHelper.DIRS_READY) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // JDialog waitDialog = new JDialog(this, true);
        // waitDialog.setSize(200, 100);
        // waitDialog.setVisible(true);
        // waitDialog.add(new JLabel("Hello!"));

        int status = tHelper.featureExtract();
        if (status != 1) {
            JOptionPane.showMessageDialog(null, "特征提取出现错误。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (validateStatus(TrainHelper.FEATURES_EXTRACTED) == 0) {
            JOptionPane.showMessageDialog(null, "特征提取完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void featureNorm() {
        if (validateStatus(TrainHelper.FEATURES_EXTRACTED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.featureNorm();
        if (validateStatus(TrainHelper.FEATURES_NORMED) == 0) {
            JOptionPane.showMessageDialog(null, "特征归一化完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateData() {
        if (validateStatus(TrainHelper.FEATURES_NORMED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.generateTrainPredict();
        if (validateStatus(TrainHelper.DATA_GENERATED) == 0) {
            JOptionPane.showMessageDialog(null, "训练数据准备完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void optiParams() {
        if (validateStatus(TrainHelper.DATA_GENERATED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.optiParams();
        if (validateStatus(TrainHelper.PARAMS_OPTIMIZED) == 0) {
            JOptionPane.showMessageDialog(null, "参数优化完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void train() {
        if (validateStatus(TrainHelper.PARAMS_OPTIMIZED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        System.out.println(var);
        String varVal = var.getText();
        String alphVal = alpha.getText();
        String areaVal = minArea.getText();
        if (varVal.length() == 0 || alphVal.length() == 0
                || areaVal.length() == 0) {
            JOptionPane.showMessageDialog(null, "未指定识别参数。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String recogParam = varVal + " " + alphVal + " " + areaVal;
        try {
            tHelper.train(recogParam);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "文件写入错误。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        if (validateStatus(TrainHelper.MODEL_TRAINED) == 0) {
            JOptionPane.showMessageDialog(null, "训练完成！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void disposeMe() {
        if (tHelper != null && validateStatus(TrainHelper.MODEL_TRAINED) != 0) {
            int status = JOptionPane.showConfirmDialog(null,
                    "训练尚未完成，如果关闭窗口，则会删除已有临时文件，是否继续？", "训练未完成",
                    JOptionPane.YES_NO_OPTION);
            if (status == JOptionPane.YES_OPTION) {
                Utils.delete(new File(rootPath + "/models/" + name.getText()));
            }
        }
        Train.this.dispose();
    }

    private int validateStatus(int status) {
        if (tHelper == null)
            return -1;
        int sts = tHelper.getStatus();
        if (sts < status)
            return -1;
        else if (sts == status)
            return 0;
        else
            return 1;
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(450, 480);
        setTitle("模型训练");
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        new Train("line");
    }
}
