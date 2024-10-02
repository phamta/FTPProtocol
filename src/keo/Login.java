package keo;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class Login extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField tf_username;
    private JPasswordField tf_password;
    private JTextField tf_ip;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Login frame = new Login();
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
    public Login() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 270);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        JLabel label_title = new JLabel("Login");
        label_title.setForeground(new Color(0, 0, 255));
        label_title.setFont(new Font("Tahoma", Font.BOLD, 20));
        label_title.setBounds(155, 20, 110, 30);
        label_title.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(label_title);
        
        JLabel lblNewLabel = new JLabel("Username:");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblNewLabel.setBounds(55, 55, 100, 30);
        contentPane.add(lblNewLabel);
        
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblPassword.setBounds(55, 100, 100, 30);
        contentPane.add(lblPassword);
        
        tf_username = new JTextField();
        tf_username.setBounds(155, 60, 200, 25);
        contentPane.add(tf_username);
        tf_username.setColumns(10);
        
        JButton button_login = new JButton("Login");
        button_login.setFont(new Font("Tahoma", Font.PLAIN, 18));
        button_login.setBounds(255, 190, 100, 30);
        contentPane.add(button_login);
        
        tf_password = new JPasswordField();
        tf_password.setBounds(155, 105, 200, 25);
        contentPane.add(tf_password);
        
        JLabel lblIpServer = new JLabel("IP server");
        lblIpServer.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblIpServer.setBounds(55, 145, 100, 30);
        contentPane.add(lblIpServer);
        
        tf_ip = new JTextField();
        tf_ip.setColumns(10);
        tf_ip.setBounds(155, 150, 200, 25);
        contentPane.add(tf_ip);
        
        // Bắt sự kiện khi nhấn nút "Login"
        button_login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = tf_username.getText();
                char[] password_raw = tf_password.getPassword();
                String password = new String(password_raw);
                
                String ip_server = tf_ip.getText();

                if (checkLogin(username, password)) {
                    if (checkFTPServer(ip_server)) {
                        test t = new test(ip_server);
                        t.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Không thể kết nối đến server FTP.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Hàm kiểm tra kết nối đến FTP server
    private boolean checkFTPServer(String server) {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");
        int port = 21;  // Sử dụng cổng 21 mặc định cho FTP
        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                return false;
            }
            ftpClient.disconnect();
            return true;  // Kết nối thành công
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm kiểm tra đăng nhập
    private boolean checkLogin(String username, String password) {
        String url = "jdbc:mysql://localhost:3306/login";  // Cấu trúc URL kết nối đến MySQL
        String user = "root";  // Tên tài khoản MySQL
        String pass = "12345678";  // Mật khẩu MySQL

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Kết nối đến cơ sở dữ liệu
            conn = DriverManager.getConnection(url, user, pass);

            // Câu truy vấn kiểm tra username và password
            String sql = "SELECT * FROM account WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // Thực thi câu truy vấn
            rs = pstmt.executeQuery();

            // Kiểm tra kết quả
            if (rs.next()) {
                return true;  // Đăng nhập thành công
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // Đóng kết nối và giải phóng tài nguyên
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;  // Đăng nhập thất bại
    }
}
