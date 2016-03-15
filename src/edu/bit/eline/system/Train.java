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

/**
 * @author ysl 模型界面训练。每次训练将产生一个对应的TrainHelper完成训练的各个步骤。TrainHelper负责
 *         调用ExtractFeature和ImageClassfication两个训练接口，创建对应的线程进行训练，并管理各个步骤之间的状态。
 */
public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private TrainHelper       tHelper;
    private String            selectedClass;
    private String            configFile       = "./config.json";
    private String            rootPath;
    private DryRun            dr;
    private Thread            dryRunThread;
    private boolean           increaseTrain    = false;

    private Container    container;
    private JPanel       detParams;
    private JPanel       center;
    private JPanel       topPanel;
    private JPanel       detection;
    private JPanel       classPanel;
    private JPanel       samplePanel;
    private JPanel       buttonPanel;
    private JPanel       actionPanel;
    private JPanel       increasePanel;
    private JPanel       dryRunPanel;
    private JButton      getReady;
    private JButton      featExtract;
    private JButton      paramOpti;
    private JButton      train;
    private JButton      positiveDirBrowse;
    private JButton      negativeDirBrowse;
    private JButton      exit;
    private JButton      dryRun;
    private JButton      dryRunDirBrowse;
    private JCheckBox    increase;
    private JTextField   name;
    private JTextField   var;
    private JTextField   alpha;
    private JTextField   minArea;
    private JTextField   positiveDirField;
    private JTextField   negativeDirField;
    private JTextField   dryRunDate;
    private JTextField   dryRunDir;
    private ButtonGroup  btGroup;
    private JProgressBar progressBar;
    private JRadioButton towerCheck;
    private JRadioButton diggerCheck;

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

    // 关闭界面
    @Override
    public void dispose() {
        // 特征提取和参数优化过程中不允许退出
        if (tHelper != null && tHelper.isExtracting()) {
            JOptionPane.showConfirmDialog(null, "特征提起正在运行，无法退出。", "训练未完成", JOptionPane.ERROR_MESSAGE);
        }
        if (tHelper != null && tHelper.isOptiming()) {
            JOptionPane.showConfirmDialog(null, "参数优化正在运行，无法退出。", "训练未完成", JOptionPane.ERROR_MESSAGE);
        }
        // 如果训练过程整体未完成，则发出提示
        if (tHelper != null && validateStatus(TrainHelper.MODEL_TRAINED) != 0) {
            int status = JOptionPane.showConfirmDialog(null, "训练尚未完成，如果关闭窗口，则会删除已有临时文件，是否继续？", "训练未完成",
                    JOptionPane.YES_NO_OPTION);
            // 确认后，删除所有临时文件
            if (status == JOptionPane.YES_OPTION) {
                tHelper.stopThread();
                Utils.delete(new File(rootPath + "/models/" + name.getText()));
                super.dispose();
            } else {
                return;
            }
            // 打断干跑过程
        } else {
            if (dryRunThread != null && dryRunThread.isAlive()) {
                dr.stopRun();
            }
            super.dispose();
        }
    }

    // 干跑回调，用于界面提示。
    public void dryRunCallback() {
        JOptionPane.showMessageDialog(null, "空跑完成。", "完成", JOptionPane.PLAIN_MESSAGE);
    }

    // private methods
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
        JLabel dryRunDirLabel = new JLabel("目标目录：");
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
        JPanel drDir = new JPanel();
        drDir.add(dryRunDirLabel);
        drDir.add(dryRunDir);
        drDir.add(dryRunDirBrowse);

        JLabel dryRunDateLabel = new JLabel("空跑日期：");
        dryRunDate = new JTextField();
        dryRunDate.setColumns(14);
        dryRun = new JButton("运行");
        dryRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dryRun(dryRunDir.getText(), dryRunDate.getText());
            }
        });
        JPanel drRun = new JPanel();
        drRun.setLayout(new FlowLayout());
        drRun.add(dryRunDateLabel);
        drRun.add(dryRunDate);
        drRun.add(dryRun);

        dryRunPanel = new JPanel();
        dryRunPanel.setPreferredSize(new Dimension(320, 95));
        dryRunPanel.setLayout(new BoxLayout(dryRunPanel, BoxLayout.Y_AXIS));
        dryRunPanel.setBorder(BorderFactory.createTitledBorder("空跑"));
        dryRunPanel.add(drDir);
        dryRunPanel.add(drRun);

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
        GridBagConstraints cons = new GridBagConstraints();
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

    // dryRun按钮
    // 干跑以生成训练样本，只进行检测过程，将入侵物直接保存到磁盘
    private void dryRun(String tgtDir, String date) {
        dr = new DryRun(name.getText(), tgtDir, date);
        dr.setCallBack(this);
        dryRunThread = new Thread(dr);
        dryRunThread.start();
    }

    // 准备训练，新建所有需要的文件夹，检查界面中所有项目是否完整。
    private void getReady() {
        // 检查界面
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
        // 新建TrainHelper对象
        tHelper = new TrainHelper(lineName, selectedClass, posPath, negPath);
        tHelper.getDirsReady();
        // 检查状态是否完成
        if (validateStatus(TrainHelper.DIRS_READY) == 0) {
            JOptionPane.showMessageDialog(null, "训练准备完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 特征提取
    private void featureExtract() {
        // 检查状态
        if (validateStatus(TrainHelper.DIRS_READY) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.featureExtract(progressBar, increaseTrain);
    }

    // 参数优化
    private void optiParams() {
        // 检查状态
        if (validateStatus(TrainHelper.FEATURES_EXTRACTED) == -1) {
            JOptionPane.showMessageDialog(null, "前一步骤尚未完成！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tHelper.optiParams(progressBar);
    }

    // 训练
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

    // 检查trainHelper的状态，参数为所需要达到的状态。如果当前状态在需要达到的状态之前，
    // 则返回-1，如果当前状态在需要达到的状态之后则返回1，如果满足当前需要的状态则返回0
    // 状态检查
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

    public static void main(String[] args) {
        new Train("");
    }
}
