package demo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Explorer extends JFrame {
    static InetAddress localHost;
    static {
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static final long serialVersionUID = 1L;
    private static final String ROOT_NAME = "FTP Server";
    private static final String FTP_SERVER = localHost.getHostAddress(); // Thay đổi với địa chỉ FTP server của bạn
    private static final String FTP_USER = "Lyhoquy"; // Tên người dùng FTP
    private static final String FTP_PASS = "20072004"; // Mật khẩu FTP

    private JPanel contentPane;
    private JTextField txtPath;
    private JTable table;
    private DefaultTableModel model;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private FTPClient ftpClient;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Explorer frame = new Explorer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Explorer() throws UnknownHostException {
        setupFrame();
        connectToFTP();
        setupTreePanel();
        setupTablePanel();
        setupRefreshButton(); // Thêm nút tải lại
        populateTable("/"); //Tai toan bo file va folder khi chay chuong trinh
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                        showInfoDialog("Da ngat ket noi FTP");
                    }
                } catch (IOException ex) {
                    showErrorDialog("Error while closing the connection: " + ex.getMessage());
                }
            }
        });

    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 700);
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
    }

    private void connectToFTP() {
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER);
            ftpClient.login(FTP_USER, FTP_PASS);
            ftpClient.enterLocalPassiveMode();
        } catch (IOException ex) {
            showErrorDialog("Connection failed: " + ex.getMessage());
        }
    }

    private void setupTreePanel() {
        JPanel panelLeft = new JPanel();
        panelLeft.setPreferredSize(new Dimension(100, 0)); // Đặt kích thước cố định cho sidebar
        contentPane.add(panelLeft, BorderLayout.WEST);

        JScrollPane treeScrollPane = new JScrollPane();
        panelLeft.add(treeScrollPane);

        JTree fileTree = createFileTree();
        treeScrollPane.setViewportView(fileTree);
    }

    private JTree createFileTree() {
        root = new DefaultMutableTreeNode(ROOT_NAME);
        loadFTPFiles(root, "/");
        treeModel = new DefaultTreeModel(root);
        JTree fileTree = new JTree(treeModel);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.addTreeSelectionListener(this::treeSelectionChanged);
        fileTree.addMouseListener(createTreeMouseListener(fileTree));
        return fileTree;
    }

    private void loadFTPFiles(DefaultMutableTreeNode node, String path) {
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(path);
            for (FTPFile file : ftpFiles) {
                if (file.isDirectory()) {
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file.getName());
                    loadFTPFiles(fileNode, path + "/" + file.getName());
                    node.add(fileNode);
                }
            }
        } catch (IOException e) {
            showErrorDialog("Failed to list files: " + e.getMessage());
        }
    }

    class NonEditableTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;  // Không cho phép chỉnh sửa bất kỳ ô nào
        }
    }

    private void setupTablePanel() {
        JPanel panelTop = new JPanel();
        contentPane.add(panelTop, BorderLayout.NORTH);
        txtPath = new JTextField(40);
        panelTop.add(txtPath);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        table.setRowSelectionAllowed(true);  // Allow row selection
        table.setColumnSelectionAllowed(false);  // Disable column selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Allow single row selection
//        table.setCellSelectionEnabled(false);
        table.setShowGrid(false);
        table.setAutoCreateRowSorter(false);
        scrollPane.setViewportView(table);

        model = new DefaultTableModel(new String[]{"Name", "Date modified", "Type", "Size"}, 0);
        table.setModel(model);
        table.addMouseListener(createTableMouseListener());
    }

    private void setupRefreshButton() {
        JButton refreshButton = new JButton("Reload");
        refreshButton.addActionListener(e -> refresh());
        ((JPanel) contentPane.getComponent(0)).add(refreshButton); // Thêm nút vào panel
    }

    private void refresh() {
        String currentPath = txtPath.getText();
        populateTable(currentPath);
        updateFileTree();
    }

    private void updateFileTree() {
        root.removeAllChildren();
        loadFTPFiles(root, "/");
        treeModel.reload();
    }

    private MouseListener createTreeMouseListener(JTree fileTree) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e, fileTree, true);
                }
            }
        };
    }

    private MouseListener createTableMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e, table, false);
                }
            }
        };
    }

    private void showContextMenu(MouseEvent e, JComponent component, boolean isTree) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(event -> renameFile(getSelectedName(component, isTree)));
        menu.add(renameItem);
        JMenuItem newFolderItem = new JMenuItem("New Folder");
        newFolderItem.addActionListener(event -> createNewFolder());
        menu.add(newFolderItem);
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(event -> Delete(getSelectedName(component, isTree)));
        menu.add(deleteItem);
        JMenuItem DownloadItem = new JMenuItem("Download");
        DownloadItem.addActionListener(event -> download(getSelectedName(component, isTree)));
        menu.add(DownloadItem);
        JMenuItem UploadItem = new JMenuItem("Upload");
        UploadItem.addActionListener(event -> upload(getSelectedName(component, isTree)));
        menu.add(UploadItem);
        menu.show(component, e.getX(), e.getY());
    }

    private String getSelectedName(JComponent component, boolean isTree) {
        if (isTree) {
            TreePath path = ((JTree) component).getSelectionPath();
            return path != null ? path.getLastPathComponent().toString() : null;
        } else {
            int selectedRow = table.getSelectedRow();
            return selectedRow != -1 ? table.getValueAt(selectedRow, 0).toString() : null;
        }
    }

    private void treeSelectionChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        txtPath.setText(getTreePath(e.getPath()));
        populateTable(txtPath.getText());

        if(txtPath.getText().equals("[FTP Server")) {
            populateTable("/");
        }
    }

    private void renameFile(String currentName) {
        String newName = JOptionPane.showInputDialog(this, "Enter new name:", currentName);
        if (isValidFileName(newName)) {
            executeFTPOperation(() -> {
                try {
                    if (ftpClient.rename(txtPath.getText() + "/" + currentName, txtPath.getText() + "/" + newName)) {
                        showInfoDialog("Rename successful!");
                        refresh(); // Cập nhật bảng và cây sau khi đổi tên
                    } else {
                        showErrorDialog("Rename failed!");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    //Chuc nang xoa file hoac folder
    private void Delete(String fileName){
        executeFTPOperation(() -> {
            try {
                String path = txtPath.getText() + "/" + fileName;
                FTPFile[] files = ftpClient.listFiles(path);

                if (files.length == 0) { // Thư mục rỗng
                    if (ftpClient.removeDirectory(path)) {
                        showInfoDialog("Folder deleted successfully.");
                    } else {
                        showErrorDialog("Delete folder failed.");
                    }
                } else if (files.length == 1 && files[0].isFile()) { // Là file
                    if (ftpClient.deleteFile(path)) {
                        showInfoDialog("File deleted successfully.");
                    } else {
                        showErrorDialog("Delete file failed.");
                    }
                } else { // Thư mục không rỗng
                    showErrorDialog("Folder is not empty. Cannot delete non-empty folder.");
                }

                refresh();
            } catch (IOException e) {
                showErrorDialog("Delete failed: " + e.getMessage());
            }
        });
    }

    //Chuc nang upload
    private void upload(String name) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to upload");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File localFile = fileChooser.getSelectedFile();
            executeFTPOperation(() -> {
                try {
                    uploadFile(String.valueOf(localFile), name);
                } catch (IOException e) {
                    showErrorDialog("Upload failed: " + e.getMessage());
                }
            });
        }
    }

    private void uploadFolderRecursively(java.io.File localFolder, String remoteFolderPath) throws IOException {
        java.io.File[] localFiles = localFolder.listFiles();
        for (java.io.File localFile : localFiles) {
            String localFilePath = localFile.getAbsolutePath(); // Đường dẫn file hoặc folder cục bộ
            String remoteFilePath = remoteFolderPath + "/" + localFile.getName(); // Đường dẫn từ xa trên server FTP

            if (localFile.isDirectory()) {
                // Tạo thư mục từ xa trên server
                ftpClient.makeDirectory(remoteFilePath);
                // Gọi đệ quy để upload các file và folder con
                uploadFolderRecursively(localFile, remoteFilePath);
            } else {
                // Upload file vào thư mục từ xa đã chọn
                uploadFile(localFilePath, remoteFilePath);
            }
        }
    }

    private void uploadFile(String localFilePath, String remoteFilePath) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(localFilePath))) {
            boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
            if (!success) {
                throw new IOException("Failed to upload file: " + localFilePath);
            }
        }
    }


    //Chuc nang download
    private void download(String name) {
        executeFTPOperation(() -> {
            String remotePath = txtPath.getText() + "/" + name;
            FTPFile[] files;
            try {
                files = ftpClient.listFiles(remotePath);
                if (files.length == 1 && files[0].isFile()) { // Là file
                    downloadFile(remotePath, name);
                } else { // Là thư mục
                    downloadFolder(remotePath, name);
                }
            } catch (IOException e) {
                showErrorDialog("Download failed: " + e.getMessage());
            }
        });
    }

    //Ham download File
    private void downloadFile(String remoteFilePath, String localFilePath) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath))) {
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
            if (!success) {
                throw new IOException("Failed to download file: " + remoteFilePath);
            }
        }
    }

    //Ham download Folder
    private void downloadFolder(String remotePath, String folderName) throws IOException {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Chỉ chọn thư mục
        folderChooser.setDialogTitle("Select Folder to Save");

        // Chỉ hỏi vị trí lưu một lần
        if (folderChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String localFolderPath = folderChooser.getSelectedFile().getAbsolutePath() + "/" + folderName; // Tạo thư mục cục bộ để lưu
            downloadFolderRecursively(remotePath, localFolderPath); // Tải thư mục và các thư mục con
            showInfoDialog("Folder downloaded successfully.");
        }
    }

    private void downloadFolderRecursively(String remotePath, String localFolderPath) throws IOException {
        java.io.File localFolder = new java.io.File(localFolderPath);
        if (!localFolder.exists()) {
            localFolder.mkdirs(); // Tạo thư mục cục bộ nếu chưa tồn tại
        }

        FTPFile[] ftpFiles = ftpClient.listFiles(remotePath);
        for (FTPFile file : ftpFiles) {
            String remoteFilePath = remotePath + "/" + file.getName(); // Tạo đường dẫn file/folder từ xa
            String localFilePath = localFolderPath + "/" + file.getName(); // Tạo đường dẫn cục bộ tương ứng

            if (file.isDirectory()) {
                // Gọi đệ quy để tải thư mục con
                downloadFolderRecursively(remoteFilePath, localFilePath); // Truyền đường dẫn cục bộ cho folder con
            } else {
                // Tải file với đường dẫn cục bộ tương ứng
                downloadFile(remoteFilePath, localFilePath);
            }
        }
    }

    private void createNewFolder() {
        String folderName = JOptionPane.showInputDialog(this, "Enter new folder name:");
        if (isValidFileName(folderName)) {
            executeFTPOperation(() -> {
                try {
                    ftpClient.makeDirectory(txtPath.getText() + "/" + folderName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                showInfoDialog("Folder created successfully.");
                refresh(); // Cập nhật bảng và cây sau khi tạo thư mục
            });
        }
    }

    private void populateTable(String folder) {
        model.setRowCount(0);
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(folder);
            for (FTPFile file : ftpFiles) {
                String type = file.isDirectory() ? "Folder" : getFileExtension(file);
                String size = file.isFile() ? file.getSize() / 1024 + " KB" : "";
                String lastModified = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(file.getTimestamp().getTime());
                model.addRow(new Object[]{file.getName(), lastModified, type, size});
            }
        } catch (IOException ex) {
            showErrorDialog("Failed to populate table: " + ex.getMessage());
        }
    }

    private String getFileExtension(FTPFile file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        return (index > 0) ? fileName.substring(index + 1).toUpperCase(Locale.ROOT) : "";
    }

    private String getTreePath(TreePath path) {
        return path.toString().replace("[FTP Server, ", "").replace(", ", "/").replace("]", "");
    }

    private boolean isValidFileName(String fileName) {
        return fileName != null && !fileName.trim().isEmpty();
    }

    private void executeFTPOperation(Runnable operation) {
        try {
            operation.run();
        } catch (Exception ex) {
            showErrorDialog("Operation failed: " + ex.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
