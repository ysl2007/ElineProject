package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.bit.eline.detection.Detector;
import edu.bit.eline.system.run.HttpInterface;
import edu.bit.eline.system.run.ImageProvider;
import edu.bit.eline.system.run.ImageStorage;
import edu.bit.eline.system.run.Processer;

public class MainPanel extends JFrame {
    private static final long serialVersionUID = -8054742885149944542L;
    private String            configFile       = "./config.json";
    private String            curSelect        = null;
    private String            configPath;
    private Processer         processer;
    private ImageStorage      storage;
    private ImageProvider     imgProvider;

    private Container   container;
    private JTree       treePanel;
    private JLabel      statusTitle;
    private JLabel      status;
    private JPanel      commandPanel;
    private JPanel      eastPanel;
    private JPanel      centerPanel;
    private JPanel      statusPanel;
    private JButton     getTree;
    private JButton     modelManager;
    private JButton     runLine;
    private JButton     runAll;
    private JButton     stopLine;
    private JButton     stopAll;
    private JScrollPane treeController;

    class TreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -8890987966973311991L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            String lineName = (String) node.getUserObject();
            File modelFile = new File(configPath + "models/" + lineName);
            if (modelFile.exists()) {
                setForeground(Color.RED);
            }
            if (processer.isRunning(lineName)) {
                setForeground(Color.GREEN);
            }
            return this;
        }
    }

    public MainPanel() throws FileNotFoundException {
        // 配置文件
        JSONTokener tokener = new JSONTokener(new FileReader(configFile));
        JSONObject jo = new JSONObject(tokener);
        configPath = jo.getString("config_root_path");
        configPath += "/";

        storage = new ImageStorage();
        processer = new Processer(storage);
        setupGUI();
        imgProvider = new ImageProvider(storage, processer);

        new Thread(imgProvider).start();
        new Thread(processer).start();
    }

    private void setupGUI() {
        // 各个按钮
        getTree = new JButton("获取设备树");
        getTree.setAlignmentX(CENTER_ALIGNMENT);
        getTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCameraTreeFromWeb()) {
                    System.out.println("Device tree obtained.");
                } else {
                    JOptionPane.showMessageDialog(null, "获取设备树失败。", "发生错误", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        runLine = new JButton("运行所选线路");
        runLine.setAlignmentX(CENTER_ALIGNMENT);
        runLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (curSelect == null) {
                    JOptionPane.showMessageDialog(null, "未选择任何线路。");
                    return;
                }
                Params p = initRunner(curSelect, false);
                if (p != null) {
                    runLine(curSelect, p);
                }
            }
        });

        runAll = new JButton("运行所有线路");
        runAll.setAlignmentX(CENTER_ALIGNMENT);
        runAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> allLeaves = getAllLeaf((DefaultMutableTreeNode) treePanel.getModel().getRoot());
                for (String lineName : allLeaves) {
                    Params p = initRunner(lineName, true);
                    if (p != null) {
                        runLine(lineName, p);
                    }
                }
            }
        });

        stopLine = new JButton("停止所选线路");
        stopLine.setAlignmentX(CENTER_ALIGNMENT);
        stopLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (curSelect == null) {
                    JOptionPane.showMessageDialog(null, "未选择任何线路。");
                    return;
                }
                if (isRunning(curSelect)) {
                    stopLine(curSelect);
                } else {
                    JOptionPane.showMessageDialog(null, "所选择的线路没有运行。");
                }
            }
        });

        stopAll = new JButton("停止所有线路");
        stopAll.setAlignmentX(CENTER_ALIGNMENT);
        stopAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAll();
            }
        });

        modelManager = new JButton("模型管理器");
        modelManager.setAlignmentX(CENTER_ALIGNMENT);
        modelManager.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TreeModel tree = treePanel.getModel();
                DefaultMutableTreeNode tree = (DefaultMutableTreeNode) treePanel.getModel().getRoot();
                List<String> cameraList = getAllLeaf(tree);
                new ModelManager(cameraList);
            }
        });

        // 状态
        statusTitle = new JLabel("线路状态：");
        statusTitle.setAlignmentX(LEFT_ALIGNMENT);
        status = new JLabel();
        status.setAlignmentX(LEFT_ALIGNMENT);
        statusPanel = new JPanel();
        statusPanel.setAlignmentX(CENTER_ALIGNMENT);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(statusTitle);
        statusPanel.add(Box.createVerticalStrut(15));
        statusPanel.add(status);

        // 按钮
        commandPanel = new JPanel();
        commandPanel.setBorder(BorderFactory.createTitledBorder("命令"));
        commandPanel.setAlignmentX(LEFT_ALIGNMENT);
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(getTree);
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(modelManager);
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(runLine);
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(runAll);
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(stopLine);
        commandPanel.add(Box.createVerticalStrut(20));
        commandPanel.add(stopAll);
        commandPanel.add(Box.createVerticalStrut(50));
        commandPanel.add(statusPanel);

        // 东部
        eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(Box.createHorizontalStrut(20), BorderLayout.EAST);
        eastPanel.add(commandPanel, BorderLayout.CENTER);
        eastPanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);

        // 中部设备树
        treePanel = new JTree();
        getCameraTreeFromFile();
        treePanel.setCellRenderer(new TreeCellRenderer());
        treePanel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) treePanel.getLastSelectedPathComponent();
                if (tnode.isLeaf()) {
                    curSelect = (String) tnode.getUserObject();
                    if (isRunning(curSelect)) {
                        status.setText("线路正在运行");
                    } else {
                        status.setText("线路未运行");
                    }
                } else {
                    curSelect = null;
                    status.setText(null);
                }
                System.out.println(curSelect);
            }
        });
        treeController = new JScrollPane(treePanel);
        treeController.setBorder(BorderFactory.createEtchedBorder());

        centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createTitledBorder("设备树"));
        centerPanel.add(treeController);
        centerPanel.setLayout(new GridLayout(1, 1));

        // 顶层
        container = new JPanel();
        container.setLayout(new BorderLayout());
        container.add(centerPanel, BorderLayout.CENTER);
        container.add(eastPanel, BorderLayout.EAST);
        container.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        container.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        container.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        finalSettings();
    }

    protected boolean getCameraTreeFromWeb() {
        String jsonTree = HttpInterface.getDeviceTree();
        if (jsonTree == null || jsonTree.trim().length() == 0) {
            return false;
        }
        try {
            DefaultTreeModel dt = parseJsonTree(jsonTree);
            treePanel.setModel(dt);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean getCameraTreeFromFile() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("./deviceTree.json"), "utf-8"))) {
            String jsonTree = br.readLine();
            DefaultTreeModel dt = parseJsonTree(jsonTree);
            treePanel.setModel(dt);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private DefaultTreeModel parseJsonTree(String jsonStr) throws JSONException {
        JSONArray topArr = new JSONArray(jsonStr);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("根节点");
        recursiveParse(topArr, root, null);
        return new DefaultTreeModel(root);
    }

    private void recursiveParse(JSONArray arr, DefaultMutableTreeNode father, String fatName) throws JSONException {
        for (int i = 0; i < arr.length(); ++i) {
            JSONObject obj = arr.getJSONObject(i);
            String name = obj.getString("text");
            if (obj.has("children")) {
                JSONArray childArr = obj.getJSONArray("children");
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
                father.add(node);
                int index = name.indexOf("(");
                index = index == -1 ? name.length() : index;
                recursiveParse(childArr, node, name.substring(0, index));
            } else {
                father.add(new DefaultMutableTreeNode(fatName + name));
            }
        }
    }

    private boolean isRunning(String lineName) {
        return processer.isRunning(lineName);
    }

    private Params initRunner(String lineName, boolean quiet) {
        String modelPath = configPath + "/models/" + lineName;
        if (!(new File(modelPath).exists())) {
            if (!quiet) {
                JOptionPane.showMessageDialog(null, "模型文件不存在。", "遇到问题", JOptionPane.PLAIN_MESSAGE);
            }
            return null;
        }

        Params param;
        try {
            param = new Params(lineName);
        } catch (IOException e) {
            if (!quiet) {
                JOptionPane.showMessageDialog(null, "模型文件读取错误，请检查文件是否存在或是否损坏。", "遇到问题", JOptionPane.PLAIN_MESSAGE);
            }
            return null;
        }
        if (param.checkParams() == false) {
            if (!quiet) {
                JOptionPane.showMessageDialog(null, "模型文件不完整，可能需要重新训练。", "遇到问题", JOptionPane.PLAIN_MESSAGE);
            }
            return null;
        }
        return param;
    }

    private void runLine(String lineName, Params param) {
        Detector det = new Detector(0, param.varThrshVal);
        processer.runLine(lineName, det, param);
    }

    private void stopLine(String lineName) {
        processer.stopLine(lineName);
    }

    private void stopAll() {
        processer.stopAll();
    }

    private List<String> getAllLeaf(DefaultMutableTreeNode tree) {
        ArrayList<String> retList = new ArrayList<String>();
        Stack<DefaultMutableTreeNode> stack = new Stack<DefaultMutableTreeNode>();
        stack.push(tree);
        while (!stack.empty()) {
            DefaultMutableTreeNode tnode = stack.pop();
            @SuppressWarnings("unchecked")
            Enumeration<DefaultMutableTreeNode> e = tnode.children();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode child = e.nextElement();
                if (child.isLeaf()) {
                    retList.add((String) child.getUserObject());
                } else {
                    stack.add(child);
                }
            }
        }
        Collections.sort(retList);
        return retList;
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(500, 500);
        setTitle("主界面");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            new MainPanel();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
    }
}
