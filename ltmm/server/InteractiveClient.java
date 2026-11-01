import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class InteractiveClient {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(
                    "Cach chay: java -cp out InteractiveClient <host> <port>"
            );
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            try (Socket socket = new Socket();
                 Scanner keyboard = new Scanner(System.in)) {

                socket.connect(new InetSocketAddress(host, port), 5000);
                socket.setSoTimeout(5000);

                DataInputStream input =
                        new DataInputStream(socket.getInputStream());

                DataOutputStream output =
                        new DataOutputStream(socket.getOutputStream());

                System.out.println("Da ket noi. Gui HELLO truoc.");
                System.out.println("Nhap EXIT de dong client.");

                while (true) {
                    System.out.print("> ");

                    if (!keyboard.hasNextLine()) {
                        break;
                    }

                    String command = keyboard.nextLine().trim();

                    if (command.equals("EXIT")) {
                        break;
                    }

                    if (command.isEmpty()) {
                        continue;
                    }

                    ControlProtocol.sendMessage(output, command);

                    String response =
                            ControlProtocol.readMessage(input);

                    System.out.println(response);
                }
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Loi dia chi hoac cong.");

        } catch (IOException e) {
            System.out.println("Loi ket noi: " + e.getMessage());
        }
    }
}