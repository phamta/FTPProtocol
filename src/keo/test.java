package keo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

//import demo.FTPClientDemo;

public class test extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

//	private String server = "192.168.178.131";
//	private String user = "local-user";

//	private static String server = "10.10.58.56";
//	private static String user = "library-ftp";

//	private static String server = "192.168.1.8";
//	private static String user = "nha-ftp";

	private static String server = "10.10.29.165";
	private static String user = "giangvien-ftp";
	

//	private static String server = "10.10.56.198";
//	private static String user = "home-ftp";
	private int port = 21;
	private String pass = "12345678";

	private FTPClient ftpClient;

	private JPanel panel_listfile; // panel display folder in server
	private JButton button_back; // do back to previous folder
	private Stack<FTPFile> folderHistory; // save history open folder
	private JScrollPane scrollPane_listfile;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					test frame = new test();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public test() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 352);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		contentPane.setBackground(new Color(25, 25, 25));

		scrollPane_listfile = new JScrollPane();
		scrollPane_listfile.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_listfile.setBounds(0, 30, 685, 275);
		contentPane.add(scrollPane_listfile);
		
		System.out.println("Thu");

		panel_listfile = new JPanel();
//		panel_listfile.setBounds(0, 30, 685, 270);
		scrollPane_listfile.setViewportView(panel_listfile);
		panel_listfile.setBackground(new Color(25, 25, 25));
		panel_listfile.setLayout(null);
//		contentPane.add(panel_listfile);

		button_back = new JButton("Back");
		button_back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					goBackToPreviousFolder();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		button_back.setFont(new Font("Tahoma", Font.PLAIN, 15));
		button_back.setBounds(5, 5, 85, 20);
		contentPane.add(button_back);

		folderHistory = new Stack<FTPFile>();

		ftpClient = new FTPClient();
		ftpClient.setControlEncoding("UTF-8");
		try {
			ftpClient.connect(server, port);
			boolean login = ftpClient.login(user, pass);

			if (login) {
				FTPFile[] files = ftpClient.listFiles();
				display(files);
			} else {
				System.out.println("Không thể đăng nhập vào FTP server.");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem addFolderItem = new JMenuItem("Add Folder");
		popupMenu.add(addFolderItem);

		addFolderItem.addActionListener(e -> {
			String baseFolderName = "New Folder"; // Tên gốc cho thư mục mới
			String folderName = baseFolderName; // Bắt đầu với tên mặc định

			// Duyệt qua tất cả các folder hiện có để kiểm tra trùng lặp tên
			int folderIndex = 1;
			boolean folderExists = true;

			while (folderExists) {
				folderExists = false;
				try {
					for (FTPFile file : ftpClient.listFiles()) {
						if (file.isDirectory() && file.getName().equals(folderName)) {
							folderExists = true;
							break; // Nếu tên đã tồn tại, dừng vòng lặp và kiểm tra tên mới
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (folderExists) {
					folderIndex++;
					folderName = baseFolderName + " (" + folderIndex + ")";
				}
			}

			// Sau khi duyệt qua tất cả các tên, tạo thư mục với tên không trùng
			try {
				boolean success = ftpClient.makeDirectory(folderName);
				if (success) {
					FTPFile[] listFile = ftpClient.listFiles();
					display(listFile);
					JOptionPane.showMessageDialog(panel_listfile, "Tạo thư mục thành công: " + folderName);
				} else {
					JOptionPane.showMessageDialog(panel_listfile, "Tạo thư mục thất bại: " + folderName, "Lỗi",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(panel_listfile, "Đã xảy ra lỗi khi tạo thư mục: " + folderName, "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		scrollPane_listfile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(scrollPane_listfile, e.getX(), e.getY());
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if (ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
						JOptionPane.showMessageDialog(null, "Đã ngắt kết nối khỏi FTP server.");
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

	}

	private JPanel createPanelFile(FTPFile file, int x, int y) {
		ftpClient.setControlEncoding("UTF-8");

		JPanel panel = new JPanel();
		panel.setBounds(x, y, 650, 20);
		panel.setLayout(null);
		panel.setBackground(contentPane.getBackground());
		panel.setForeground(Color.white);

		int padding_top = 2;
		int padding_left = 2;

		ImageIcon icon = null;
		File tempFile = null;
		String extension = null;

		String fileName = null;
		try {
			fileName = new String(file.getName().getBytes("ISO-8859-1"), "UTF-8");
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				extension = fileName.substring(i + 1).toLowerCase();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Các phần mở rộng cho video và audio
		List<String> videoExtensions = Arrays.asList("mp4", "avi", "mkv", "mov", "flv");
		List<String> audioExtensions = Arrays.asList("mp3", "wav", "aac", "flac", "ogg");

		if (file.isDirectory()) {
			java.net.URL imgUrl = test.class.getResource("/image/filefolder.png");
			if (imgUrl != null) {
				icon = new ImageIcon(imgUrl);
			} else {
				System.out.println("Icon filefolder.png không tìm thấy!");
				imgUrl = test.class.getResource("/image/text_document.png"); // Fallback icon
				if (imgUrl != null) {
					icon = new ImageIcon(imgUrl);
				}
			}
			extension = "File folder";
		} else if (videoExtensions.contains(extension)) {
			java.net.URL videoIconUrl = test.class.getResource("/image/video.png");
			if (videoIconUrl != null) {
				icon = new ImageIcon(videoIconUrl);
			} else {
				System.out.println("Icon video.png không tìm thấy!");
			}
		} else if (audioExtensions.contains(extension)) {
			java.net.URL audioIconUrl = test.class.getResource("/image/audio.jpg");
			if (audioIconUrl != null) {
				icon = new ImageIcon(audioIconUrl);
			} else {
				System.out.println("Icon audio.jpg không tìm thấy!");
			}
		} else {
			try {
				tempFile = File.createTempFile("temp", file.getName());

				InputStream inputStream = ftpClient.retrieveFileStream(file.getName());
				if (inputStream != null) {
					FileOutputStream outputStream = new FileOutputStream(tempFile);
					byte[] bytesArray = new byte[4096];
					int bytesRead;
					while ((bytesRead = inputStream.read(bytesArray)) != -1) {
						outputStream.write(bytesArray, 0, bytesRead);
					}
					outputStream.close();
					inputStream.close();
					ftpClient.completePendingCommand();

					icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(tempFile);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (tempFile != null && tempFile.exists()) {
					tempFile.deleteOnExit();
				}
			}

		}

		// Kiểm tra nếu icon là null
		if (icon == null) {
			URL url = test.class.getResource("/image/txt.png");
			icon = new ImageIcon(url);
		}

		JLabel label_icon = new JLabel(icon);
		label_icon.setBounds(padding_left, padding_top, icon.getIconWidth(), icon.getIconHeight()); // Width và height
																									// là 20, đồng bộ
																									// với icon
		panel.add(label_icon);

//		System.out.println(icon.getIconHeight() + " " + icon.getIconWidth());

		// Tiếp tục phần còn lại của hàm để hiển thị tên file và thông tin khác
		Font font = new Font("Tahoma", Font.PLAIN, 12);

		JLabel label_filename = new JLabel(fileName);
		label_filename.setBounds(padding_left + 25, padding_top, 250, 20); // Adjust bounds
		label_filename.setForeground(Color.white);
		label_filename.setFont(font);
		panel.add(label_filename);

		JLabel label_type = new JLabel(extension);
		label_type.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label_type.setBounds(label_filename.getX() + label_filename.getWidth() + 5, padding_top, 80, 20);
		label_type.setForeground(Color.white);
		panel.add(label_type);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		java.util.Date fileDate = file.getTimestamp().getTime();
		String formattedDate = dateFormat.format(fileDate);

		JLabel label_date = new JLabel(formattedDate);
		label_date.setBounds(label_type.getX() + label_type.getWidth() + 5, padding_top, 200, 20);
		label_date.setForeground(Color.white);
		label_date.setFont(font);
		panel.add(label_date);

		panel.putClientProperty("choice", false);
		panel.putClientProperty("isDirectory", file.isDirectory());

		JPopupMenu popupMenu = new JPopupMenu(); // pop up menu when right click on file
		if (file.isDirectory()) {
			JMenuItem uploadItem = new JMenuItem("Upload");
			popupMenu.add(uploadItem);
			uploadItem.addActionListener(e -> {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				int result = fileChooser.showOpenDialog(panel);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					try {
						if (selectedFile.isDirectory()) {
							addFolderToFTP(file.getName(), selectedFile);
						} else {
							addFileToFTP(file.getName(), selectedFile);
						}
					} catch (IOException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(panel, "Failed to upload!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}

		JMenuItem downloadItem = new JMenuItem("Download");
		JMenuItem deleteItem = new JMenuItem("Delete");
		JMenuItem renameItem = new JMenuItem("Rename");

		popupMenu.add(downloadItem);
		popupMenu.add(deleteItem);
		popupMenu.add(renameItem);

		downloadItem.addActionListener(e -> {
			// Thư mục mặc định nơi lưu file tải về
			String defaultDownloadFolderPath = System.getProperty("user.home") + "/Downloads";

			// Tạo thư mục nếu chưa tồn tại
			File localFolder = new File(defaultDownloadFolderPath);
			if (!localFolder.exists()) {
				localFolder.mkdirs();
			}

			try {
				// Lấy tên thư mục từ file trên server
				String remoteFolderPath = file.getName();
				String localFolderPath = localFolder.getAbsolutePath();

				if (file.isDirectory()) {
					// Gọi phương thức downloadFolder để tải thư mục
					downloadFolder(ftpClient, remoteFolderPath, localFolderPath);
				} else {
					String localFilePath = defaultDownloadFolderPath + "/" + file.getName();
					downloadFile(localFilePath, file.getName());
				}
				JOptionPane.showMessageDialog(panel, "Tải xuống thư mục thành công: " + localFolderPath);
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(panel, "Đã xảy ra lỗi khi tải xuống thư mục: " + file.getName(), "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		deleteItem.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(panel,
					"Are you sure you want to delete " + file.getName() + "?", "Delete Confirmation",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				try {
					if (file.isDirectory()) {
						deleteFolder(file, "");
						JOptionPane.showMessageDialog(panel, "Thư mục " + file.getName() + " đã được xóa thành công!");
					} else {
						boolean success = ftpClient.deleteFile(file.getName());
						if (success) {
							FTPFile[] listFile = ftpClient.listFiles();
							display(listFile);
							JOptionPane.showMessageDialog(panel, "Tệp " + file.getName() + " đã được xóa thành công!");
						} else {
							JOptionPane.showMessageDialog(panel, "Xóa tệp thất bại: " + file.getName());
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(panel, "Đã xảy ra lỗi khi xóa: " + file.getName(), "Lỗi",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		renameItem.addActionListener(e -> {
			String currentFileName = label_filename.getText();
			String newName = JOptionPane.showInputDialog(panel, "Enter new name:", currentFileName);

			if (newName != null && !newName.trim().isEmpty()) {
				// Lấy phần mở rộng của file gốc nếu chưa có phần mở rộng trong tên mới
				if (!newName.contains(".")) {
					int dotIndex = currentFileName.lastIndexOf('.'); // Renamed 'i' to 'dotIndex'
					if (dotIndex > 0) {
						String type = currentFileName.substring(dotIndex);
						newName += type; // Thêm phần mở rộng
					}
				}

				try {
					if (ftpClient.rename(currentFileName, newName)) {
						JOptionPane.showMessageDialog(panel, "Rename successful!");
						label_filename.setText(newName);
					} else {
						JOptionPane.showMessageDialog(panel, "Rename failed!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(panel, "Error during renaming!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean currentChoice = (Boolean) panel.getClientProperty("choice");

				if (e.getClickCount() == 2 && (Boolean) panel.getClientProperty("isDirectory")) {
					openFolder(file);
				} else {
					if (!currentChoice) {
						panel.setBackground(new Color(80, 80, 80));
					} else {
						panel.setBackground(contentPane.getBackground());
					}

					panel.putClientProperty("choice", !currentChoice);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(panel, e.getX(), e.getY());
				}
			}
		});

		return panel;
	}

	private void display(FTPFile[] files) {
		int x = 10;
		int y = 10;
		int padding = 10;

		panel_listfile.removeAll();
		for (FTPFile ftpFile : files) {
			JPanel panel = null;
			panel = createPanelFile(ftpFile, x, y);

			if (panel != null) {
				panel_listfile.add(panel);
				y += panel.getHeight() + padding;
			}
		}

		panel_listfile.revalidate();
		panel_listfile.repaint();

		panel_listfile.setPreferredSize(new Dimension(scrollPane_listfile.getWidth(), y + 20));
		if (y > scrollPane_listfile.getHeight()) {
			scrollPane_listfile.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		} else {
			scrollPane_listfile.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		}
	}

	private void openFolder(FTPFile folder) {
		try {
			String currentPath = ftpClient.printWorkingDirectory();
			folderHistory.push(new FTPFile());
			folderHistory.peek().setName(currentPath);

			ftpClient.changeWorkingDirectory(folder.getName());
			FTPFile[] files = ftpClient.listFiles();

			display(files);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Không thể mở thư mục!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void goBackToPreviousFolder() throws IOException {
		if (!folderHistory.isEmpty()) {
			FTPFile previousFolder = folderHistory.pop();
			String previousPath = previousFolder.getName();

			ftpClient.changeWorkingDirectory(previousPath);
			FTPFile[] files = ftpClient.listFiles();

			display(files);
		} else {
			ftpClient.changeToParentDirectory();
			FTPFile[] files = ftpClient.listFiles();
			display(files);
		}
	}

	private void addFileToFTP(String remotePath, File localFile) throws IOException {
		ftpClient.changeWorkingDirectory(remotePath);

		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		try (FileInputStream fis = new FileInputStream(localFile)) {
			boolean success = ftpClient.storeFile(localFile.getName(), fis);
			if (success) {
				JOptionPane.showMessageDialog(null, "File uploaded successfully!");
			} else {
				JOptionPane.showMessageDialog(null, "Failed to upload file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void addFolderToFTP(String remotePath, File localFolder) throws IOException {
		ftpClient.changeWorkingDirectory(remotePath);

		boolean created = ftpClient.makeDirectory(localFolder.getName());
		if (created) {
			JOptionPane.showMessageDialog(null, "Folder created successfully!");
		} else {
			JOptionPane.showMessageDialog(null, "Failed to create folder.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File[] files = localFolder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					addFolderToFTP(localFolder.getName(), file);
				} else {
					addFileToFTP(localFolder.getName(), file);
				}
			}
		}
	}

	private void deleteFolder(FTPFile folder, String parentPath) {
		try {
			String currentPath = parentPath + "/" + folder.getName();

			FTPFile[] subFiles = ftpClient.listFiles(currentPath);

			if (subFiles != null && subFiles.length > 0) {
				for (FTPFile subFile : subFiles) {
					String filePath = currentPath + "/" + subFile.getName();
					if (subFile.isDirectory()) {
						deleteFolder(subFile, currentPath);
					} else {
						boolean deletedFile = ftpClient.deleteFile(filePath);
						if (!deletedFile) {
							JOptionPane.showMessageDialog(null, "Không thể xóa file: " + subFile.getName(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}

			boolean deletedFolder = ftpClient.removeDirectory(currentPath);
			if (!deletedFolder) {
				JOptionPane.showMessageDialog(null, "Không thể xóa thư mục: " + folder.getName(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Lỗi khi xóa thư mục!", "Error", JOptionPane.ERROR_MESSAGE);
		}

		FTPFile[] listFile;
		try {
			listFile = ftpClient.listFiles();
			display(listFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void downloadFolder(FTPClient ftpClient, String remoteFolderPath, String localFolderPath)
			throws IOException {
		FTPFile[] subFiles = ftpClient.listFiles(remoteFolderPath);

		File localDir = new File(localFolderPath, remoteFolderPath);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}

		for (FTPFile file : subFiles) {
			String remoteFilePath = remoteFolderPath + "/" + file.getName();
			if (file.isDirectory()) {
				downloadFolder(ftpClient, remoteFilePath, localFolderPath);
			} else {
				File localFile = new File(localDir, file.getName());
				try (FileOutputStream fos = new FileOutputStream(localFile)) {
					ftpClient.retrieveFile(remoteFilePath, fos);
				}
			}
		}
	}

	public void downloadFile(String localFilePath, String fileName) {
		try (FileOutputStream fileOutputStream = new FileOutputStream(localFilePath)) {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			boolean success = ftpClient.retrieveFile(fileName, fileOutputStream);

			if (success) {
				System.out.println("File " + fileName + " đã được tải về thành công tại: " + localFilePath);
			} else {
				System.out.println("Không thể tải file: " + fileName);
			}
		} catch (IOException e) {

		}
	}
}
