package demo;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Scanner;

public class FTPClientLogic {

	private static String server = "10.10.58.90";
	private static String user = "library-ftp";
	private static int port = 21;
	private static String pass = "12345678";

	public static void main(String[] args) {
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			Scanner scanner = new Scanner(System.in);
			boolean running = true;

			while (running) {
				System.out.println("Chọn tác vụ:");
				System.out.println("1. Hiện danh sách các tệp tin trên máy chủ.");
				System.out.println("2. Tải một tệp tin được chọn.");
				System.out.println("3. Upload một tệp tin lên thư mục được chọn.");
				System.out.println("4. Đổi tên một tệp tin/thư mục.");
				System.out.println("5. Xóa một tệp tin/thư mục.");
				System.out.println("6. Tạo một thư mục mới.");
				System.out.println("7. Thoát.");
				int choice = scanner.nextInt();
				scanner.nextLine(); // Bỏ qua dòng trống sau khi nhập số

				switch (choice) {
				case 1:
					listFiles(ftpClient, "");
					break;
				case 2:
					System.out.print("Nhập tên tệp tin để tải: ");
					String fileName = scanner.nextLine();
					downloadFile(ftpClient, fileName);
					break;
				case 3:
					System.out.print("Nhập đường dẫn tệp tin để upload: ");
					String uploadFilePath = scanner.nextLine();
					System.out.print("Nhập thư mục đích trên máy chủ: ");
					String remoteDirPath = scanner.nextLine();
					uploadFile(ftpClient, uploadFilePath, remoteDirPath);
					break;
				case 4:
					System.out.print("Nhập tên tệp tin/thư mục cũ: ");
					String oldName = scanner.nextLine();
					System.out.print("Nhập tên tệp tin/thư mục mới: ");
					String newName = scanner.nextLine();
					renameFile(ftpClient, oldName, newName);
					break;
				case 5:
					System.out.print("Nhập tên tệp tin/thư mục để xóa: ");
					String deleteName = scanner.nextLine();
					deleteFile(ftpClient, deleteName);
					break;
				case 6:
					System.out.print("Nhập tên thư mục mới: ");
					String newDir = scanner.nextLine();
					createDirectory(ftpClient, newDir);
					break;
				case 7:
					running = false;
					break;
				default:
					System.out.println("Lựa chọn không hợp lệ!");
				}
			}

			ftpClient.logout();
			ftpClient.disconnect();
			scanner.close();
		} catch (IOException ex) {
			System.out.println("Lỗi: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void listFiles(FTPClient ftpClient, String path) throws IOException {
		FTPFile[] files = ftpClient.listFiles(path);
		for (FTPFile file : files) {
			if (file.isDirectory()) {
				System.out.println("[Thư mục] " + file.getName());
				listFiles(ftpClient, path + "/" + file.getName());
			} else {
				System.out.println("  Tệp tin: " + file.getName());
			}
		}
	}

	public static void downloadFile(FTPClient ftpClient, String remoteFilePath) throws IOException {
		FileOutputStream fos = new FileOutputStream(remoteFilePath);
		boolean success = ftpClient.retrieveFile(remoteFilePath, fos);
		fos.close();
		if (success) {
			System.out.println("Tải tệp tin thành công: " + remoteFilePath);
		} else {
			System.out.println("Tải tệp tin thất bại: " + remoteFilePath);
		}
	}

	public static void uploadFile(FTPClient ftpClient, String localFilePath, String remoteDirPath) throws IOException {
		FileInputStream fis = new FileInputStream(localFilePath);
		String remoteFilePath = remoteDirPath + "/" + localFilePath.substring(localFilePath.lastIndexOf("/") + 1);
		boolean success = ftpClient.storeFile(remoteFilePath, fis);
		fis.close();
		if (success) {
			System.out.println("Upload tệp tin thành công: " + localFilePath);
		} else {
			System.out.println("Upload tệp tin thất bại: " + localFilePath);
		}
	}

	public static void renameFile(FTPClient ftpClient, String oldName, String newName) throws IOException {
		boolean success = ftpClient.rename(oldName, newName);
		if (success) {
			System.out.println("Đổi tên thành công: " + oldName + " thành " + newName);
		} else {
			System.out.println("Đổi tên thất bại: " + oldName);
		}
	}

	public static void deleteFile(FTPClient ftpClient, String filePath) throws IOException {
		boolean success = ftpClient.deleteFile(filePath);
		if (success) {
			System.out.println("Xóa thành công: " + filePath);
		} else {
			System.out.println("Xóa thất bại: " + filePath);
		}
	}

	public static void createDirectory(FTPClient ftpClient, String dirPath) throws IOException {
		boolean success = ftpClient.makeDirectory(dirPath);
		if (success) {
			System.out.println("Tạo thư mục thành công: " + dirPath);
		} else {
			System.out.println("Tạo thư mục thất bại: " + dirPath);
		}
	}
}
