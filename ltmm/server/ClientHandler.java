import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SessionManager sessionManager;
    private final StreamManager streamManager;

    public ClientHandler(
            Socket socket,
            SessionManager sessionManager,
            StreamManager streamManager) {

        this.socket = socket;
        this.sessionManager = sessionManager;
        this.streamManager = streamManager;
    }

    @Override
    public void run() {
        Session session = new Session(socket.getInetAddress());

        try (Socket clientSocket = socket) {
            sessionManager.addSession(session);
            clientSocket.setSoTimeout(30000);

            DataInputStream input =
                    new DataInputStream(clientSocket.getInputStream());

            DataOutputStream output =
                    new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Mo phien: " + session.getId());

            while (true) {
                String command = ControlProtocol.readMessage(input);

                String response = handleCommand(command, session);

                ControlProtocol.sendMessage(output, response);
            }

        } catch (EOFException e) {
            System.out.println("Client dong luong du lieu.");

        } catch (SocketTimeoutException e) {
            System.out.println("Client khong gui du lieu trong 30 giay.");

        } catch (IOException e) {
            System.out.println("Loi ket noi: " + e.getMessage());

        } finally {
            // Giải phóng quyền phát trước khi xóa phiên.
            streamManager.removeSession(session.getId());
            sessionManager.removeSession(session.getId());

            System.out.println("Ket thuc phien: " + session.getId());
            System.out.println(
                    "So phien con lai: "
                    + sessionManager.getSessionCount()
            );
        }
    }

    private String handleCommand(String command, Session session) {
    if (command.equals("HELLO")) {
    if (session.isReady()) {
        return "ERROR ALREADY_HELLO";
    }

    session.markReady();

    return "HELLO_OK "
            + session.getId() + " "
            + session.getToken();
}

    if (!session.isReady()) {
        return "ERROR HELLO_REQUIRED";
    }

    switch (command) {
        case "PING":
            return "PONG";

        case "COUNT":
            return "COUNT_OK " + sessionManager.getSessionCount();

        case "START_STREAM":
            return streamManager.startStream(session.getId());

        case "STOP_STREAM":
            return streamManager.stopStream(session.getId());

        case "SUBSCRIBE":
            return streamManager.subscribe(session.getId());

        case "UNSUBSCRIBE":
            return streamManager.unsubscribe(session.getId());

        case "VIEWER_COUNT":
            return "VIEWER_COUNT_OK " + streamManager.getViewerCount();

        default:
            return "ERROR UNKNOWN_COMMAND";
    }
}
}