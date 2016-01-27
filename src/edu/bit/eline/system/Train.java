package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import edu.bit.eline.system.run.DryRun;

public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private TrainHelper       tHelper;
    private String            selectedClass;
    private String            configFile       = "./config.json";
    private String            rootPath;
    private DryRun            dr;
    private Thread            dryRunThread;
    private boolean           increaseTrain    = false;

    private Container         container;
    private JPanel            detParams;
    private JPanel            center;
    private JPanel            topPanel;
    private JPanel            detection;
    private JPanel            classPanel;
    private JPanel            samplePanel;
    private JPanel            buttonPanel;
    private JPanel            actionPanel;
    private JPanel            increasePanel;
    private JPanel            dryRunPanel;
    private JButton           getReady;
    private JButton           featExtract;
    private JButton           paramOpti;
    private JButton           train;
    private JButton           positiveDirBrowse;
    private JButton           negativeDirBrowse;
    private JButton           exit;
    private JButton           dryRun;
    private JButton           dryRunDirBrowse;
    private JCheckBox         increase;
    private JTextField        name;
    private JTextField        var;
    private JTextField        alpha;
    private JTextField        minArea;
    private JTextField        positiveDirField;
    private JTextField        negativeDirField;
    private JTextField        dryRunDate;
    private JTextField        dryRunDir;
    private ButtonGroup       btGroup;
    private JProgressBar      progressBar;
    private JRadioButton      towerCheck;
    private JRadioButton      diggerCheck;

    public Train(String lineName) {
        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        JSONObject jo = new JSONObject(tokener);
        try {
            rootPath = jo.getString("config_root_path");
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "配置文件不完整。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        setupGUI(lineName);
        finalSettings();
    }

    class CheckListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (towerCheck.isSelected())
                selectedClass = "1.0";
            else if (diggerCheck.isSelected())
                selectedClass = "2.0";
            else
                selectedClass = null;
        }
    }

    private void setupGUI(String lineName) {
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
        detection.setPreferredSize(new Dimension(320, 140));
        detection.setBorder(BorderFactory.createTitledBorder("检测"));
        detection.setLayout(new BoxLayout(detection, BoxLayout.X_AXIS));
        detection.add(Box.createHorizontalStrut(5));
        detection.add(detParams);
        detection.add(Box.createHorizontalStrut(5));

        // Dry run
        JLabel dryRunDirLabel = new JLabel("文件夹：");
        dryRunDirLabel.setAlignmentX(LEFT_ALIGNMENT);
        dryRunDir = new JTextField();
        dryRunDir.setColumns(14);
        dryRunDirBrowse = new JButton("浏览");
        dryRunDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
                    dryRunDir.setText(selectedDir);
                }
            }
        });

        JLabel dryRunDateLabel = new JLabel("日期：");
        dryRunDateLabel.setAlignmentX(LEFT_ALIGNMENT);
        dryRunDate = new JTextField();
        dryRunDate.setColumns(14);
        dryRun = new JButton("运行");
        dryRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dryRun(dryRunDir.getText(), dryRunDate.getText());
            }
        });

        dryRunPanel = new JPanel();
        GridBagLayout dryRunLayout = new GridBagLayout();
        dryRunPanel.setPreferredSize(new Dimension(320, 95));
        dryRunPanel.setBorder(BorderFactory.createTitledBorder("空跑"));
        dryRunPanel.setLayout(dryRunLayout);
        dryRunPanel.add(dryRunDirLabel);
        dryRunPanel.add(dryRunDir);
        dryRunPanel.add(dryRunDirBrowse);
        dryRunPanel.add(dryRunDateLabel);
        dryRunPanel.add(dryRunDate);
        dryRunPanel.add(dryRun);

        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.anchor = GridBagConstraints.WEST;
        cons.insets = new Insets(5, 5, 5, 5);
        dryRunLayout.setConstraints(dryRunDirLabel, cons);
        cons.gridx = 1;
        dryRunLayout.setConstraints(dryRunDir, cons);
        cons.gridx = 2;
        dryRunLayout.setConstraints(dryRunDirBrowse, cons);
        cons.gridx = 0;
        cons.gridy = 1;
        dryRunLayout.setConstraints(dryRunDateLabel, cons);
        cons.gridx = 1;
        dryRunLayout.setConstraints(dryRunDate, cons);
        cons.gridx = 2;
        dryRunLayout.setConstraints(dryRun, cons);

        // 异常类别部分
        CheckListener listener = new CheckListener();
        towerCheck = new JRadioButton("吊车");
        towerCheck.setAlignmentX(LEFT_ALIGNMENT);
        towerCheck.addActionListener(listener);

        diggerCheck = new JRadioButton("地面设备");
        diggerCheck.setAlignmentX(LEFT_ALIGNMENT);
        diggerCheck.addActionListener(listener);

        btGroup = new ButtonGroup();
        btGroup.add(towerCheck);
        btGroup.add(diggerCheck);

        classPanel = new JPanel();
        classPanel.setPreferredSize(new Dimension(320, 50));
        classPanel.setBorder(BorderFactory.createTitledBorder("异常类"));
        classPanel.setLayout(new GridLayout(1, 2));
        classPanel.add(towerCheck);
        classPanel.add(diggerCheck);

        // 样本路径选择
        JLabel positiveDir = new JLabel("正例样本路径：");
        positiveDirField = new JTextField();
        positiveDirField.setColumns(12);
        positiveDirBrowse = new JButton("浏览");
        positiveDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
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
        negativeDirField.setColumns(12);
        negativeDirBrowse = new JButton("浏览");
        negativeDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = dirChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String selectedDir = dirChooser.getSelectedFile().getAbsolutePath();
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
        samplePanel.setPreferredSize(new Dimension(320, 100));

        increase = new JCheckBox("增量学习");
        increase.setSelected(false);
        increase.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (increase.isSelected()) {
                    increaseTrain = true;
                } else {
                    increaseTrain = false;
                }
            }
        });
        increase.setAlignmentX(LEFT_ALIGNMENT);
        increasePanel = new JPanel();
        increasePanel.setPreferredSize(new Dimension(320, 50));
        increasePanel.setLayout(new BoxLayout(increasePanel, BoxLayout.X_AXIS));
        increasePanel.setBorder(BorderFactory.createTitledBorder("增量学习"));
        increasePanel.add(increase);

        // 整个中部
        center = new JPanel();
        center.add(detection, cons);
        center.add(dryRunPanel);
        center.add(classPanel, cons);
        center.add(samplePanel, cons);
        center.add(increasePanel, cons);

        GridBagLayout centerLayout = new GridBagLayout();
        center.setLayout(centerLayout);
        cons = new GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 0;
        centerLayout.setConstraints(detection, cons);
        cons.gridy = 1;
        centerLayout.setConstraints(dryRunPanel, cons);
        cons.gridy = 2;
        centerLayout.setConstraints(classPanel, cons);
        cons.gridy = 3;
        centerLayout.setConstraints(samplePanel, cons);
        cons.gridy = 4;
        centerLayout.setConstraints(increasePanel, cons);

        // 动作按钮
        Dimension botDim = new Dimension(100, 28);
        getReady = new JButton("准备训练");
        getReady.setPreferredSize(botDim);
        getReady.setAlignmentX(CENTER_ALIGNMENT);
        getReady.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReady();
            }
        });

        featExtract = new JButton("特征提取");
        featExtract.setPreferredSize(botDim);
        featExtract.setAlignmentX(CENTER_ALIGNMENT);
        featExtract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                featureExtract();
            }
        });

        paramOpti = new JButton("参数优化");
        paramOpti.setPreferredSize(botDim);
        paramOpti.setAlignmentX(CENTER_ALIGNMENT);
        paramOpti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optiParams();
            }
        });

        train = new JButton("训练");
        train.setPreferredSize(botDim);
        train.setAlignmentX(CENTER_ALIGNMENT);
        train.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                train();
            }
        });

        exit = new JButton("关闭");
        exit.setPreferredSize(botDim);
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        // 东侧动作按钮区
        actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createTitledBorder("动作"));
        actionPanel.add(getReady);
        actionPanel.add(featExtract);
        actionPanel.add(paramOpti);
        actionPanel.add(train);
        actionPanel.add(exit);

        GridBagLayout actLayout = new GridBagLayout();
        cons.insets = new Insets(15, 5, 15, 5);
        cons.gridx = 0;
        cons.gridy = 0;
        actLayout.setConstraints(getReady, cons);
        cons.gridy = 1;
        actLayout.setConstraints(featExtract, cons);
        cons.gridy = 2;
        actLayout.setConstraints(paramOpti, cons);
        cons.gridy = 3;
        actLayout.setConstraints(train, cons);
        cons.gridy = 4;
        actLayout.setConstraints(exit, cons);
        actionPanel.setLayout(actLayout);

        // 南侧进度条部分
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(430, 20));

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

    private void dryRun(String tgtDir, String date) {
        dr = new DryRun(name.getText(), tgtDir, date);
        dr.setCallBack(this);
        dryRunThread = new Thread(dr);
        dryRunThread.start();
    }

    private void getReady() {
        name.setEditable(false);
        String lineName = name.getText();
        if (lineName == null || lineName.length() == 0) {
            JOptionPane.showMessageDialog(null, "没有指定线路名称。", "错误", JOptionPane.ERROR_MESSAGE);
            name.setEditable(true);
            return;
        }
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(null, "没有选择异常类别。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String posPath = positiveDirField.getText();
        String negPath = negativeDirField.getText();
        if (posPath.trim().length() == 0 || negPath.trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "没有选择样本路径。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper = new TrainHelper(lineName, selectedClass, posPath, negPath);
        tHelper.getDirsReady();
        if (validateStatus(TrainHelper.DIRS_READY) == 0) {
            JOptionPane.showMessageDialog(null, "训练准备完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void featureExtract() {
        if (validateStatus(TrainHelper.DIRS_READY) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.featureExtract(progressBar, increaseTrain);
    }

    private void optiParams() {
        if (validateStatus(TrainHelper.FEATURES_EXTRACTED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.optiParams(progressBar);
    }

    private void train() {
        if (validateStatus(TrainHelper.PARAMS_OPTIMIZED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String varVal = var.getText();
        String alphVal = alpha.getText();
        String areaVal = minArea.getText();
        if (varVal.length() == 0 || alphVal.length() == 0 || areaVal.length() == 0) {
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
        if (validateStatus(TrainHelper.MODEL_TRAINED) == 0) {
            JOptionPane.showMessageDialog(null, "训练完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
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
        setSize(450, 510);
        setTitle("模型训练");
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void dispose() {
        if (tHelper.isOptiming()) {
            JOptionPane.showConfirmDialog(null, "参数优化正在运行，无法退出。", "训练未完成", JOptionPane.ERROR_MESSAGE);
        }
        if (tHelper != null && validateStatus(TrainHelper.MODEL_TRAINED) != 0) {
            int status = JOptionPane.showConfirmDialog(null, "训练尚未完成，如果关闭窗口，则会删除已有临时文件，是否继续？", "训练未完成",
                    JOptionPane.YES_NO_OPTION);
            if (status == JOptionPane.YES_OPTION) {
                tHelper.stopThread();
                Utils.delete(new File(rootPath + "/models/" + name.getText()));
                super.dispose();
            } else {
                return;
            }
        } else {
            if (dryRunThread != null && dryRunThread.isAlive()) {
                dr.stopRun();
            }
            super.dispose();
        }
    }

    public void dryRunCallback() {
        JOptionPane.showMessageDialog(null, "空跑完成。", "完成", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        new Train("安都17(动态风险)");
    }
}
