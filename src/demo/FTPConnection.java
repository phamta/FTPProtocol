package demo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.IOException;

public class FTPConnection {

    private static String server = "192.168.178.131";
    private static String user = "local-user";
    private static int port = 21;
    private static String pass = "12345678";

    public static void main(String[] args) {
        FTPClient ftpClient = new FTPClient();
        try {
            // Kết nối đến FTP server
            ftpClient.connect(server, port);
            System.out.println("Kết nối đến server thành công!");

            // Đăng nhập
            boolean login = ftpClient.login(user, pass);
            if (login) {
                System.out.println("Đăng nhập thành công!");

                // Lấy danh sách các file và folder
                FTPFile[] files = ftpClient.listFiles();
                if (files != null && files.length > 0) {
                    System.out.println("Danh sách các file/thư mục:");
                    for (FTPFile file : files) {
                        if (file.isFile()) {
                            System.out.println("File: " + file.getName());
                        } else if (file.isDirectory()) {
                            System.out.println("Thư mục: " + file.getName());
                        } else {
                            System.out.println("Khác: " + file.getName());
                        }
                    }
                } else {
                    System.out.println("Không có file/thư mục nào.");
                }

                // Đăng xuất
                ftpClient.logout();
            } else {
                System.out.println("Đăng nhập thất bại!");
            }

        } catch (IOException ex) {
            System.out.println("Lỗi kết nối: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                    System.out.println("Đã ngắt kết nối FTP server.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

