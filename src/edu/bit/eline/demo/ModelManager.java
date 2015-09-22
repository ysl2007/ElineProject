package edu.bit.eline.demo;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class ModelManager extends JFrame {
    private static final long serialVersionUID = -6948336292066740769L;

    private Container         container;
    private JButton           refresh;
    private JButton           trainModel;
    private JButton           delModel;
    private JList<String>     cameraList;
    private JList<String>     modelList;
    private JPanel            leftPane;
    private JPanel            midPane;
    private JPanel            rightPane;
    private JScrollPane       cameraListController;
    private JScrollPane       modelListController;

    public ModelManager() {
        setupGUI();
    }

    private void setupGUI() {

        cameraList = new JList<String>();
        cameraListController = new JScrollPane(cameraList);
        leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.add(Box.createVerticalStrut(10));
        leftPane.add(new JLabel("Camera list:"));
        leftPane.add(Box.createVerticalStrut(10));
        leftPane.add(cameraListController);
        leftPane.add(Box.createVerticalStrut(10));

        modelList = new JList<String>();
        modelListController = new JScrollPane(modelList);
        rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(new JLabel("Model list:"));
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(modelListController);
        rightPane.add(Box.createVerticalStrut(10));

        refresh = new JButton("Refresh");
        refresh.setAlignmentX(CENTER_ALIGNMENT);
        refresh.setMinimumSize(new Dimension(90, 20));
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!refreshCameraList()) {
                    JOptionPane.showMessageDialog(null, "Failed to obtain camera list.");
                }
                if (!refreshModelList()) {
                    JOptionPane.showMessageDialog(null, "Failed to obtain model list.");
                }
            }
        });

        trainModel = new JButton("Train");
        trainModel.setAlignmentX(CENTER_ALIGNMENT);
        trainModel.setMinimumSize(new Dimension(90, 20));
        trainModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
            }
        });

        delModel = new JButton("Delete");
        delModel.setAlignmentX(CENTER_ALIGNMENT);
        delModel.setMinimumSize(new Dimension(90, 20));
        delModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
            }
        });

        midPane = new JPanel();
        midPane.setLayout(new BoxLayout(midPane, BoxLayout.Y_AXIS));
        midPane.add(refresh);
        midPane.add(Box.createRigidArea(new Dimension(20, 80)));
        midPane.add(trainModel);
        midPane.add(Box.createRigidArea(new Dimension(20, 80)));
        midPane.add(delModel);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(Box.createHorizontalStrut(10));
        container.add(leftPane);
        container.add(Box.createVerticalStrut(5));
        container.add(midPane);
        container.add(Box.createVerticalStrut(5));
        container.add(rightPane);
        container.add(Box.createHorizontalStrut(10));

        finalSettings();
    }

    private void finalSettings() {
        this.setContentPane(container);
        setSize(650, 500);
        setTitle("Model Manager");
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private boolean refreshCameraList() {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        listModel.addElement("Line1");
        listModel.addElement("Line2");
        cameraList.setModel(listModel);
        return true;
    }

    private boolean refreshModelList() {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        listModel.addElement("Line3");
        listModel.addElement("Line4");
        modelList.setModel(listModel);
        return true;
    }

    public static void main(String[] args) {
        new ModelManager();
    }
}
