package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;

public class keo1 extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField tf_search;
    private JPanel panel_show;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    keo1 frame = new keo1();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public keo1() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 750, 400);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(254, 215, 124));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        panel_show = new JPanel();
        panel_show.setBounds(10, 57, 720, 290);
        panel_show.setLayout(new GridLayout(0, 5, 10, 10)); // 5 columns, with gaps
        JScrollPane scrollPane = new JScrollPane(panel_show);
        scrollPane.setBounds(10, 57, 720, 290);
        contentPane.add(scrollPane);

        JLabel lblNewLabel = new JLabel("Tìm kiếm");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        lblNewLabel.setBounds(470, 10, 90, 30);
        contentPane.add(lblNewLabel);

        tf_search = new JTextField();
        tf_search.setBounds(570, 10, 160, 30);
        contentPane.add(tf_search);
        tf_search.setColumns(10);

        JButton button_add = new JButton("Open Folder");
        button_add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    displayFiles(selectedDirectory);
                }
            }
        });
        button_add.setFont(new Font("Tahoma", Font.PLAIN, 20));
        button_add.setBounds(210, 10, 150, 30);
        contentPane.add(button_add);
    }

    // Method to display files and folders as icons in the panel_show
    private void displayFiles(File directory) {
        panel_show.removeAll(); // Clear the panel
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                JLabel fileLabel = new JLabel(fileName);
                fileLabel.setHorizontalTextPosition(JLabel.CENTER);
                fileLabel.setVerticalTextPosition(JLabel.BOTTOM);
                
                // Set icon based on whether it's a file or directory
                ImageIcon icon;
                if (file.isDirectory()) {
                    icon = new ImageIcon(new ImageIcon("folder-icon.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                } else {
                    icon = new ImageIcon(new ImageIcon("file-icon.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                }
                fileLabel.setIcon(icon);

                panel_show.add(fileLabel);
            }
        }
        panel_show.revalidate();
        panel_show.repaint();
    }
}
