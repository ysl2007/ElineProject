package edu.bit.eline.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Train extends JFrame {
    private static final long serialVersionUID = 3541303824514014559L;
    private ArrayList<String> posList;
    private ArrayList<String> negList;

    private Container         container;
    private JPanel            labels;
    private JPanel            values;
    private JPanel            center;
    private JPanel            topPanel;
    private JPanel            detection;
    private JPanel            recognization;
    private JPanel            buttonPanel;
    private JButton           train;
    private JButton           confirm;
    private JButton           posBrowse;
    private JButton           negBrowse;
    private JTextField        name;
    private JTextField        var;
    private JTextField        alpha;
    private JTextField        minArea;
    private JTextField        posPath;
    private JTextField        negPath;

    public Train(String lineName) {
        posList = new ArrayList<String>();
        negList = new ArrayList<String>();
        setupGUI(lineName);
        finalSettings();
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
        posPath = new JTextField();
        posPath.setColumns(15);
        posBrowse = new JButton("浏览");
        posBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButton(posPath);
            }
        });
        JPanel posPane = new JPanel();
        posPane.setLayout(new BoxLayout(posPane, BoxLayout.X_AXIS));
        posPane.add(Box.createHorizontalStrut(10));
        posPane.add(posBrowse);
        posPane.add(Box.createHorizontalStrut(10));
        posPane.add(posPath);
        posPane.add(Box.createHorizontalStrut(10));

        negPath = new JTextField();
        negPath.setColumns(15);
        negBrowse = new JButton("浏览");
        negBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButton(negPath);
            }
        });
        JPanel negPane = new JPanel();
        negPane.setLayout(new BoxLayout(negPane, BoxLayout.X_AXIS));
        negPane.add(Box.createHorizontalStrut(10));
        negPane.add(negBrowse);
        negPane.add(Box.createHorizontalStrut(10));
        negPane.add(negPath);
        negPane.add(Box.createHorizontalStrut(10));

        // Recognization主体
        recognization = new JPanel();
        recognization.setBorder(BorderFactory.createTitledBorder("识别"));
        recognization.setLayout(new BoxLayout(recognization, BoxLayout.PAGE_AXIS));
        recognization.add(Box.createVerticalStrut(10));
        recognization.add(new JLabel("正例图片目录：", JLabel.LEFT));
        recognization.add(Box.createVerticalStrut(5));
        recognization.add(posPane);
        recognization.add(Box.createVerticalStrut(10));
        recognization.add(new JLabel("负例图片目录："));
        recognization.add(Box.createVerticalStrut(5));
        recognization.add(negPane);
        recognization.add(Box.createVerticalStrut(10));

        center = new JPanel();
        center.setLayout(new GridLayout(2, 1));
        center.add(detection);
        center.add(recognization);

        // 确定取消部分
        train = new JButton("训练");
        train.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                trainButton();
            }
        });

        confirm = new JButton("关闭");
        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Train.this.dispose();
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(train);
        buttonPanel.add(Box.createHorizontalStrut(50));
        buttonPanel.add(confirm);

        // 最上层部分
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(5, 10));
        topPanel.add(center, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        container = new Container();
        container.setLayout(new FlowLayout());
        container.add(topPanel);
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

    protected void trainButton() {
        if (name.getText() == ""){
            JOptionPane.showMessageDialog(null, "线路名称未指定。", "发生错误", JOptionPane.WARNING_MESSAGE);
        }
        int status = getPathList();
        if (status == 1) {
            JOptionPane.showMessageDialog(null, "没有指定训练图片来源。", "发生错误", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (status == 2) {
            JOptionPane.showMessageDialog(null, "指定目录不存在。", "发生错误", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (status == 3) {
            JOptionPane.showMessageDialog(null, "指定文件夹不包含图片。", "发生错误", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (status == 4) {
            JOptionPane.showMessageDialog(null, "图片文件夹发生未知错误。", "发生错误", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            // TODO: train interface here.
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
        setSize(300, 400);
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
