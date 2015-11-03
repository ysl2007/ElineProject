package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private ArrayList<String> posList;
    private ArrayList<String> negList;
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
    private JTextField        posPath;
    private JTextField        negPath;
    private ButtonGroup       btGroup;
    private JRadioButton      craneCheck;
    private JRadioButton      pumpCheck;
    private JRadioButton      towerCheck;
    private JRadioButton      diggerCheck;
    private JRadioButton      fogCheck;

    public Train(String lineName) {
        posList = new ArrayList<String>();
        negList = new ArrayList<String>();
        setupGUI(lineName);
        finalSettings();
    }

    class CheckListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
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
        craneCheck = new JRadioButton("吊车");
        craneCheck.setAlignmentX(LEFT_ALIGNMENT);
        craneCheck.addActionListener(new CheckListener());
        pumpCheck = new JRadioButton("泵车");
        pumpCheck.setAlignmentX(LEFT_ALIGNMENT);
        diggerCheck = new JRadioButton("地面设备");
        diggerCheck.setAlignmentX(LEFT_ALIGNMENT);
        towerCheck = new JRadioButton("塔吊");
        towerCheck.setAlignmentX(LEFT_ALIGNMENT);
        fogCheck = new JRadioButton("烟雾");
        fogCheck.setAlignmentX(LEFT_ALIGNMENT);

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
                String lineName = name.getText();
                if (lineName == null || lineName.length() == 0) {
                    JOptionPane.showMessageDialog(null, "没有指定线路名称", "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (selectedClass == null) {
                    JOptionPane.showMessageDialog(null, "没有选择异常类别", "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tHelper = new TrainHelper(lineName, selectedClass);
                tHelper.getDirsReady();
                if (validateStatus(TrainHelper.DIRS_READY)) {
                    JOptionPane.showMessageDialog(null, "特征提取完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        featExtract = new JButton("特征提取");
        featExtract.setAlignmentX(CENTER_ALIGNMENT);
        featExtract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateStatus(TrainHelper.DIRS_READY) == false)
                    return;
                tHelper.featureExtract();
                if (validateStatus(TrainHelper.FEATURES_EXTRACTED)) {
                    JOptionPane.showMessageDialog(null, "特征提取完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        featNorm = new JButton("特征归一化");
        featNorm.setAlignmentX(CENTER_ALIGNMENT);
        featNorm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateStatus(TrainHelper.FEATURES_EXTRACTED) == false)
                    return;
                tHelper.featureNorm();
                if (validateStatus(TrainHelper.FEATURES_NORMED)) {
                    JOptionPane.showMessageDialog(null, "特征归一化完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        generData = new JButton("获取训练数据");
        generData.setAlignmentX(CENTER_ALIGNMENT);
        generData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateStatus(TrainHelper.FEATURES_NORMED) == false)
                    return;
                tHelper.generateTrainPredict();
                if (validateStatus(TrainHelper.DATA_GENERATED)) {
                    JOptionPane.showMessageDialog(null, "训练数据准备完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        paramOpti = new JButton("参数优化");
        paramOpti.setAlignmentX(CENTER_ALIGNMENT);
        paramOpti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateStatus(TrainHelper.DATA_GENERATED) == false)
                    return;
                tHelper.optiParams();
                if (validateStatus(TrainHelper.PARAMS_OPTIMIZED)) {
                    JOptionPane.showMessageDialog(null, "参数优化完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        train = new JButton("训练");
        train.setAlignmentX(CENTER_ALIGNMENT);
        train.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (validateStatus(TrainHelper.PARAMS_OPTIMIZED) == false)
                    return;
                String varVal = var.getText();
                String alphVal = alpha.getText();
                String areaVal = minArea.getText();
                if (varVal.length() == 0 || alphVal.length() == 0 || areaVal.length() == 0){
                    JOptionPane.showMessageDialog(null, "未指定识别参数。", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String recogParam = varVal + " " + alphVal + " " + areaVal;
                try {
                    tHelper.train(recogParam);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "文件写入错误。", "错误", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
                if (validateStatus(TrainHelper.MODEL_TRAINED)) {
                    JOptionPane.showMessageDialog(null, "训练完成！", "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
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
                Train.this.dispose();
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

    private boolean validateStatus(int status) {
        if (tHelper == null || tHelper.status == TrainHelper.NOT_INITIALIZED) {
            JOptionPane.showMessageDialog(null, "还未准备好进行训练，请先进行上一步！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (tHelper.status == status) {
            return true;
        } else if (tHelper.status > status) {
            int select = JOptionPane.showConfirmDialog(null,
                    "已完成该步骤，是否还要继续进行？", "提示信息", JOptionPane.OK_CANCEL_OPTION);
            if (select == JOptionPane.OK_OPTION) {
                return true;
            } else {
                return false;
            }
        } else {
            switch (tHelper.status) {
                case TrainHelper.INITIALIZED:
                    JOptionPane.showMessageDialog(null, "还未完成文件夹准备，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                case TrainHelper.DIRS_READY:
                    JOptionPane.showMessageDialog(null, "还未完成特征提取，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                case TrainHelper.FEATURES_EXTRACTED:
                    JOptionPane.showMessageDialog(null, "还未完成特征归一化，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                case TrainHelper.FEATURES_NORMED:
                    JOptionPane.showMessageDialog(null, "还未完成数据获取，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                case TrainHelper.DATA_GENERATED:
                    JOptionPane.showMessageDialog(null, "还未完成参数优化，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                case TrainHelper.PARAMS_OPTIMIZED:
                    JOptionPane.showMessageDialog(null, "还未完成模型训练，请先进行上一步！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
            }
        }
        return true;
    }

    protected void browseButton(JTextField text) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
            text.setText(selectedDir);
        }
    }

    protected int getPathList() {
        try {
            String posPathText = posPath.getText();
            String negPathText = negPath.getText();
            if (posPathText.length() == 0 || negPathText.length() == 0) {
                return 1;
            }
            File pos = new File(posPathText);
            File neg = new File(negPathText);
            if (!pos.exists() || neg.exists()) {
                return 2;
            }

            String[] files = pos.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.toLowerCase().endsWith(".jpg"))
                        return true;
                    else
                        return false;
                }
            });
            for (String filename : files) {
                posList.add(posPathText + "/" + filename);
            }

            files = neg.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.toLowerCase().endsWith(".jpg"))
                        return true;
                    else
                        return false;
                }
            });
            for (String filename : files) {
                negList.add(negPathText + "/" + filename);
            }

            if (posList.size() == 0 || negList.size() == 0) {
                return 3;
            }
            return 0;
        } catch (Exception e) {
            return 4;
        }
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
