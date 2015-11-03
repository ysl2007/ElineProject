package edu.bit.eline.system;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class MainPanel extends JFrame {
    private static final long serialVersionUID = -8054742885149944542L;
    private DetectorRunner    detection;
    
    private Container         container;
    private JTree             treePanel;
    private JPanel            westPanel;
    private JPanel            westTopPanel;
    private JButton           getTree;
    private JButton           modelManager;
    private JButton           runLine;
    private JButton           runAll;
    private ImagePanel        imagePanel;
    private JSplitPane        bottomPanel;
    private JTextField        varThrsh;
    private JTextField        minArea;
    private JTextField        alpha;
    private JScrollPane       centerPanel;

    public MainPanel() {
        setupGUI();
    }

    public void setupGUI() {
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 左边设备树及各个按钮
        getTree = new JButton("获取摄像头树");
        getTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!getCameraTreeDemo()) {
                    JOptionPane.showMessageDialog(null, "获取设备树失败。", "发生错误",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        getTree.setAlignmentX(CENTER_ALIGNMENT);
        getTree.setMinimumSize(new Dimension(160, 30));

        runLine = new JButton("运行所选线路");
        runLine.setAlignmentX(CENTER_ALIGNMENT);
        runLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        runAll = new JButton("运行所有线路");
        runAll.setAlignmentX(CENTER_ALIGNMENT);
        runAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        modelManager = new JButton("模型管理器");
        modelManager.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TreeModel tree = treePanel.getModel();
                DefaultMutableTreeNode tree = (DefaultMutableTreeNode) treePanel
                        .getModel().getRoot();
                List<String> cameraList = getAllLeaf(tree);
                new ModelManager(cameraList);
            }
        });
        modelManager.setAlignmentX(CENTER_ALIGNMENT);

        // 左边按钮部分
        westTopPanel = new JPanel();
        westTopPanel.setBorder(BorderFactory.createEtchedBorder());
        westTopPanel.setLayout(new BoxLayout(westTopPanel, BoxLayout.Y_AXIS));
        westTopPanel.add(Box.createVerticalStrut(5));
        westTopPanel.add(getTree);
        westTopPanel.add(Box.createVerticalStrut(5));
        westTopPanel.add(modelManager);
        westTopPanel.add(Box.createVerticalStrut(5));
        westTopPanel.add(runLine);
        westTopPanel.add(Box.createVerticalStrut(5));
        westTopPanel.add(runAll);
        westTopPanel.add(Box.createVerticalStrut(5));

        treePanel = new JTree();
        treePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                int selRow = treePanel.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1 && e.isPopupTrigger()) {
                    System.out.println("popup!");
                }
            }
        });

        // 整个左边
        westPanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();
        westPanel.setLayout(gb);
        westPanel.add(westTopPanel);
        westPanel.add(treePanel);
        GridBagConstraints gbCon = new GridBagConstraints();

        gbCon.fill = GridBagConstraints.BOTH;
        gbCon.gridwidth = 0;
        gbCon.weightx = 0;
        gbCon.weighty = 0;
        gb.setConstraints(westTopPanel, gbCon);

        gbCon.fill = GridBagConstraints.BOTH;
        gbCon.gridwidth = 0;
        gbCon.weightx = 1;
        gbCon.weighty = 1;
        gb.setConstraints(treePanel, gbCon);

        // 中部
        imagePanel = new ImagePanel();
        centerPanel = new JScrollPane();
        centerPanel.add(imagePanel);
        centerPanel
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 整个下部
        bottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder());
        bottomPanel.setLeftComponent(westPanel);
        bottomPanel.setRightComponent(centerPanel);
        bottomPanel.setEnabled(true);

        container.setLayout(new BorderLayout());
        container.add(bottomPanel, BorderLayout.CENTER);

        finalSettings();
    }

    protected boolean getCameraTree() {
        // TODO: getCameraTree
        return false;
    }

    protected boolean getCameraTreeDemo() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        root.add(new DefaultMutableTreeNode("child1"));
        root.add(new DefaultMutableTreeNode("child2"));
        DefaultTreeModel dt = new DefaultTreeModel(root);
        treePanel.setModel(dt);
        return true;
    }

    protected void runButtomDemo() {
        varThrsh.setEditable(false);
        minArea.setEditable(false);
        alpha.setEditable(false);
        if (detectInitialize()) {
            new Thread(detection).start();
        }
        varThrsh.setEditable(true);
        minArea.setEditable(true);
        alpha.setEditable(true);
    }

    protected void runLine(String lineName) {
        new Thread(detection).start();
    }

    protected boolean detectInitialize() {
        // TODO: initialize parameters.
        return false;
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
        return retList;
    }

    private void finalSettings() {
        this.setContentPane(container);
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
