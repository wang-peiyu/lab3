package cs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 1234;

        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("请选择操作:");
                System.out.println("1. 添加联系人");
                System.out.println("2. 查看联系人");
                System.out.println("3. 修改联系人");
                System.out.println("4. 删除联系人");
                System.out.println("5. 退出");

                int choice = scanner.nextInt();
                out.flush();

                if (choice == 1) {
                    out.writeObject("add");
                    out.flush();
                    addContact(out, in);
                } else if (choice == 2) {
                    out.writeObject("view");
                    out.flush();
                    viewContacts(in);
                } else if (choice == 3) {
                    out.writeObject("update");
                    out.flush();
                    updateContact(out, in);
                } else if (choice == 4) {
                    out.writeObject("delete");
                    out.flush();
                    deleteContact(out, in);
                } else if (choice == 5) {
                    out.writeObject("exit");
                    out.flush();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addContact(ObjectOutputStream out, ObjectInputStream in) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("输入id: ");
            String id = scanner.nextLine();
            System.out.println("输入name: ");
            String name = scanner.nextLine();
            System.out.println("输入address: ");
            String address = scanner.nextLine();
            System.out.println("输入phone: ");
            String phone = scanner.nextLine();

            out.writeObject(id);
            out.writeObject(name);
            out.writeObject(address);
            out.writeObject(phone);

            String response = (String) in.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void viewContacts(ObjectInputStream in) {
        try {
            List<String> contacts = (List<String>) in.readObject();
            for (String contact : contacts) {
                System.out.println(contact);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void updateContact(ObjectOutputStream out, ObjectInputStream in) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("输入要更新的联系人id: ");
            String id = scanner.nextLine();
            System.out.println("输入新name: ");
            String newName = scanner.nextLine();
            System.out.println("输入新address: ");
            String newAddress = scanner.nextLine();
            System.out.println("输入新phone: ");
            String newPhone = scanner.nextLine();

            out.writeObject(id);
            out.writeObject(newName);
            out.writeObject(newAddress);
            out.writeObject(newPhone);

            String response = (String) in.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void deleteContact(ObjectOutputStream out, ObjectInputStream in) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("输入要删除的联系人id: ");
            String id = scanner.nextLine();

            out.writeObject(id);

            String response = (String) in.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
