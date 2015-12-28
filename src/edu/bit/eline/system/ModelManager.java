package edu.bit.eline.system;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ModelManager extends JFrame {
    private static final long serialVersionUID = -6948336292066740769L;
    private String            configFile       = "./config.json";
    private String            modelDir;
    private String            curSelectedLine;
    private String            curSelectedModel;

    private Container     container;
    private JButton       refresh;
    private JButton       trainModel;
    private JButton       delModel;
    private JButton       add;
    private JList<String> cameraList;
    private JList<String> modelList;
    private JPanel        leftPane;
    private JPanel        midPane;
    private JPanel        rightPane;
    private JScrollPane   cameraListController;
    private JScrollPane   modelListController;

    public ModelManager(List<String> lineList) {
        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        JSONObject jo = new JSONObject(tokener);
        String configPath;
        try {
            configPath = jo.getString("config_root_path");
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "配置文件不完整。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        modelDir = configPath + "/models/";
        setupGUI(lineList);
    }

    private void setupGUI(List<String> lineList) {
        // 摄像头列表
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (String line : lineList) {
            listModel.addElement(line);
        }
        cameraList = new JList<String>();
        cameraList.setModel(listModel);
        cameraList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cameraList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                curSelectedLine = cameraList.getSelectedValue();
            }
        });
        cameraListController = new JScrollPane(cameraList);

        // 左侧线路列表
        leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.add(Box.createVerticalStrut(10));
        leftPane.add(new JLabel("线路列表："));
        leftPane.add(Box.createVerticalStrut(10));
        leftPane.add(cameraListController);
        leftPane.add(Box.createVerticalStrut(10));
        leftPane.setPreferredSize(new Dimension(50, 20));

        // 模型列表
        modelList = new JList<String>();
        modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                curSelectedModel = modelList.getSelectedValue();
            }
        });
        modelListController = new JScrollPane(modelList);

        // 右边列表
        rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(new JLabel("模型列表："));
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(modelListController);
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.setPreferredSize(new Dimension(50, 20));

        // 中部按钮部分
        add = new JButton("添加");
        add.setAlignmentX(CENTER_ALIGNMENT);
        add.setMinimumSize(new Dimension(90, 20));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new Train(null);
            }
        });

        refresh = new JButton("刷新");
        refresh.setAlignmentX(CENTER_ALIGNMENT);
        refresh.setMinimumSize(new Dimension(90, 20));
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!refreshModelList()) {
                    JOptionPane.showMessageDialog(null, "获取模型列表失败，请检查配置文件夹。", "错误", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        trainModel = new JButton("训练");
        trainModel.setAlignmentX(CENTER_ALIGNMENT);
        trainModel.setMinimumSize(new Dimension(90, 20));
        trainModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (curSelectedLine == null)
                    JOptionPane.showMessageDialog(null, "没有选择任何线路。", "错误", JOptionPane.WARNING_MESSAGE);
                else
                    new Train(curSelectedLine);
            }
        });

        delModel = new JButton("删除");
        delModel.setAlignmentX(CENTER_ALIGNMENT);
        delModel.setMinimumSize(new Dimension(90, 20));
        delModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteModel();
            }
        });

        // 中间部分
        midPane = new JPanel();
        midPane.setLayout(new BoxLayout(midPane, BoxLayout.Y_AXIS));
        midPane.add(add);
        midPane.add(Box.createRigidArea(new Dimension(20, 40)));
        midPane.add(refresh);
        midPane.add(Box.createRigidArea(new Dimension(20, 40)));
        midPane.add(trainModel);
        midPane.add(Box.createRigidArea(new Dimension(20, 40)));
        midPane.add(delModel);

        // Container
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(Box.createHorizontalStrut(10));
        container.add(leftPane);
        container.add(Box.createHorizontalStrut(10));
        container.add(midPane);
        container.add(Box.createHorizontalStrut(10));
        container.add(rightPane);
        container.add(Box.createHorizontalStrut(10));
        finalSettings();
    }

    private void deleteModel() {
        if (curSelectedModel == null || curSelectedModel.length() == 0) {
            JOptionPane.showMessageDialog(null, "没有选择任何模型", "没有选择模型", JOptionPane.WARNING_MESSAGE);
        } else {
            int selectedOption = JOptionPane.showConfirmDialog(null, "确定删除所选模型吗？", "确认删除",
                    JOptionPane.OK_CANCEL_OPTION);
            if (selectedOption == JOptionPane.OK_OPTION) {
                if (!Utils.delete(new File("./config/" + curSelectedModel))) {
                    JOptionPane.showMessageDialog(null, "删除失败，可能有文件未删除。", "删除失败", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "删除成功！", "成功", JOptionPane.PLAIN_MESSAGE);
                }
                refreshModelList();
            }
        }
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(650, 500);
        setTitle("模型管理器");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private boolean refreshModelList() {
        try {
            File modelDir = new File(this.modelDir);
            File[] modelDirList = modelDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File arg0) {
                    if (arg0.isDirectory())
                        return true;
                    return false;
                }
            });
            DefaultListModel<String> listModel = new DefaultListModel<String>();
            for (File model : modelDirList) {
                listModel.addElement(model.getName().toUpperCase());
            }
            modelList.setModel(listModel);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String[] lines = { "line1", "line2", "line3", "line4" };
        new ModelManager(Arrays.asList(lines));
    }
}
