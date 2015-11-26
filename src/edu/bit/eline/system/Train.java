package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private TrainHelper       tHelper;
    private String            selectedClass;

    private Container         container;
    private JPanel            labels;
    private JPanel            values;
    private JPanel            center;
    private JPanel            topPanel;
    private JPanel            detection;
    private JPanel            recognization;
    private JPanel            buttonPanel;
    private JPanel            actionPanel;
    private JButton           getReady;
    private JButton           generData;
    private JButton           featExtract;
    private JButton           featNorm;
    private JButton           paramOpti;
    private JButton           train;
    private JButton           confirm;
    private JTextField        name;
    private JTextField        var;
    private JTextField        alpha;
    private JTextField        minArea;
    private ButtonGroup       btGroup;
    private JRadioButton      craneCheck;
    private JRadioButton      pumpCheck;
    private JRadioButton      towerCheck;
    private JRadioButton      diggerCheck;
    private JRadioButton      fogCheck;

    public Train(String lineName) {
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
//            System.out.println(selectedClass);
        }
    }

    protected void setupGUI(String lineName) {
        // detection部分
        labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(Box.createVerticalStrut(5));
        labels.add(new JLabel("线路名称(全称)："));
        labels.add(Box.createVerticalStrut(10));
        labels.add(new JLabel("方差阈值："));
        labels.add(Box.createVerticalStrut(10));
        labels.add(new JLabel("学习率："));
        labels.add(Box.createVerticalStrut(10));
        labels.add(new JLabel("最小识别面积："));
        labels.add(Box.createVerticalStrut(8));

        name = new JTextField();
        if (!(lineName == null || lineName.equals(""))) {
            name.setText(lineName);
            name.setEditable(false);
        }
        name.setColumns(10);
        var = new JTextField();
        var.setText("9");
        var.setColumns(5);
        alpha = new JTextField();
        alpha.setText("0.1");
        alpha.setColumns(5);
        minArea = new JTextField();
        minArea.setText("4000");
        minArea.setColumns(5);

        values = new JPanel();
        values.setLayout(new BoxLayout(values, BoxLayout.Y_AXIS));
        values.add(Box.createVerticalStrut(8));
        values.add(name);
        values.add(Box.createVerticalStrut(8));
        values.add(var);
        values.add(Box.createVerticalStrut(8));
        values.add(alpha);
        values.add(Box.createVerticalStrut(8));
        values.add(minArea);
        values.add(Box.createVerticalStrut(8));

        detection = new JPanel();
        detection.setLayout(new BoxLayout(detection, BoxLayout.X_AXIS));
        detection.setBorder(BorderFactory.createTitledBorder("检测"));
        detection.add(Box.createHorizontalStrut(10));
        detection.add(labels);
        detection.add(Box.createHorizontalStrut(10));
        detection.add(values);
        detection.add(Box.createHorizontalStrut(10));

        // Recognization部分
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

        // Recognization主体
        recognization = new JPanel();
        recognization.setBorder(BorderFactory.createTitledBorder("异常类"));
        recognization.setLayout(new GridLayout(3, 2));
        recognization.setPreferredSize(new Dimension(100, 30));
        recognization.add(craneCheck);
        recognization.add(pumpCheck);
        recognization.add(diggerCheck);
        recognization.add(towerCheck);
        recognization.add(fogCheck);

        // 整个中部
        center = new JPanel();
        center.setLayout(new GridLayout(2, 1));
        center.add(detection);
        center.add(recognization);

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

        generData = new JButton("获取训练数据");
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

        // 东侧动作按钮区
        actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createTitledBorder("动作"));
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(5));
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

        // 南侧按钮部分
        confirm = new JButton("关闭");
        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                disposeMe();
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(confirm);

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
            JOptionPane.showMessageDialog(null, "没有指定线路名称", "错误",
                    JOptionPane.ERROR_MESSAGE);
            name.setEditable(true);
            return;
        }
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(null, "没有选择异常类别", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper = new TrainHelper(lineName, selectedClass);
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
        tHelper.featureExtract();
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
                Utils.delete(new File("./config/" + name.getText()));
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
        setSize(420, 380);
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
