package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
    private boolean           increaseTrain    = false;

    private Container         container;
    private JPanel            detParams;
    private JPanel            center;
    private JPanel            topPanel;
    private JPanel            detection;
    private JPanel            samplePanel;
    private JPanel            buttonPanel;
    private JPanel            actionPanel;
    private JPanel            increasePanel;
    private JButton           getReady;
    private JButton           generData;
    private JButton           featExtract;
    private JButton           featNorm;
    private JButton           paramOpti;
    private JButton           train;
    private JButton           towerDirBrowse;
    private JButton           groundDirBrowse;
    private JButton           negativeDirBrowse;
    private JButton           exit;
    private JCheckBox         increase;
    private JTextField        name;
    private JTextField        var;
    private JTextField        alpha;
    private JTextField        minArea;
    private JTextField        towerDirField;
    private JTextField        groundDirField;
    private JTextField        negativeDirField;
    private JProgressBar      progressBar;

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

        // 样本路径选择
        JLabel towerDir = new JLabel("杆塔设备样本路径：");
        towerDirField = new JTextField();
        towerDirField.setColumns(10);
        towerDirBrowse = new JButton("浏览");
        towerDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile()
                            .getAbsolutePath();
                    towerDirField.setText(selectedDir);
                }
            }
        });
        JPanel tower = new JPanel();
        tower.add(towerDir);
        tower.add(towerDirField);
        tower.add(towerDirBrowse);

        JLabel groundDir = new JLabel("杆塔设备样本路径：");
        groundDirField = new JTextField();
        groundDirField.setColumns(10);
        groundDirBrowse = new JButton("浏览");
        groundDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile()
                            .getAbsolutePath();
                    groundDirField.setText(selectedDir);
                }
            }
        });
        JPanel ground = new JPanel();
        ground.add(groundDir);
        ground.add(groundDirField);
        ground.add(groundDirBrowse);

        JLabel negativeDir = new JLabel("无威胁类样本路径：");
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
        samplePanel.add(tower);
        samplePanel.add(ground);
        samplePanel.add(negative);

        // 增量学习部分
        increase = new JCheckBox("增量学习");
        increase.setSelected(false);
        increase.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (increase.isSelected()){
                    increaseTrain = true;
                } else{
                    increaseTrain = false;
                }
                System.out.println(increaseTrain);
            }
        });
        increase.setAlignmentX(LEFT_ALIGNMENT);
        increasePanel = new JPanel();
        increasePanel.setLayout(new BoxLayout(increasePanel, BoxLayout.X_AXIS));
        increasePanel.setBorder(BorderFactory.createTitledBorder("增量学习"));
        increasePanel.add(increase);

        // 整个中部
        center = new JPanel();
        center.setLayout(new GridLayout(3, 1));
        center.add(detection);
        center.add(samplePanel);
        center.add(increasePanel);

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
        String pos = towerDirField.getText();
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
        tHelper.featureExtract(progressBar, increaseTrain);
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
                tHelper.stopThread();
                Train.this.dispose();
            }
        } else {
            Train.this.dispose();
        }
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
