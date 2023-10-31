package cs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static void main(String[] args) {
        int port = 1234; // 服务器端口

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务端已启用，等待客户端");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // 创建一个新线程来处理客户端请求
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        ) {
            String url = "jdbc:mysql://localhost/user?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";
            String username = "root";
            String password = "123456";

            Connection conn = DriverManager.getConnection(url, username, password);

            while (true) {
                // 从客户端读取请求
                String request = (String) in.readObject();

                if (request.equals("add")) {
                    addContact(conn, in, out);
                } else if (request.equals("view")) {
                    viewContacts(conn, out);
                } else if (request.equals("update")) {
                    updateContact(conn, in, out);
                } else if (request.equals("delete")) {
                    deleteContact(conn, in, out);
                } else if (request.equals("exit")) {
                    // 处理退出请求
                    break;
                }
            }

            // 关闭数据库连接
            conn.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void addContact(Connection conn, ObjectInputStream in, ObjectOutputStream out) throws SQLException, IOException {
        try {
            String id = (String) in.readObject();
            String name = (String) in.readObject();
            String address = (String) in.readObject();
            String phone = (String) in.readObject();

            String insertQuery = "INSERT INTO person (id, name, address, phone) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(insertQuery)) {
                statement.setString(1, id);
                statement.setString(2, name);
                statement.setString(3, address);
                statement.setString(4, phone);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    out.writeObject("Contact added successfully.");
                } else {
                    out.writeObject("Failed to add the contact.");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void viewContacts(Connection conn, ObjectOutputStream out) throws SQLException, IOException {
        String selectQuery = "SELECT * FROM person";
        try (PreparedStatement statement = conn.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> contacts = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phone = resultSet.getString("phone");
                contacts.add("ID: " + id + ", Name: " + name + ", Address: " + address + ", Phone: " + phone);
            }
            out.writeObject(contacts);
        }
    }

    private void updateContact(Connection conn, ObjectInputStream in, ObjectOutputStream out) throws SQLException, IOException {
        try {
            int id = Integer.parseInt((String) in.readObject());
            String newName = (String) in.readObject();
            String newAddress = (String) in.readObject();
            String newPhone = (String) in.readObject();

            String updateQuery = "UPDATE person SET name = ?, address = ?, phone = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(updateQuery)) {
                statement.setString(1, newName);
                statement.setString(2, newAddress);
                statement.setString(3, newPhone);
                statement.setInt(4, id);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    out.writeObject("Contact updated successfully.");
                } else {
                    out.writeObject("No contact found with the specified ID.");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteContact(Connection conn, ObjectInputStream in, ObjectOutputStream out) throws SQLException, IOException {
        try {
            int id = Integer.parseInt((String) in.readObject());

            String deleteQuery = "DELETE FROM person WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(deleteQuery)) {
                statement.setInt(1, id);

                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    out.writeObject("Contact deleted successfully.");
                } else {
                    out.writeObject("No contact found with the specified ID.");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
