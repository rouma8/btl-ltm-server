import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

// Mot client giu ca TCP dieu khien va UDP du lieu trong suot phien.
public class DemoConnection implements AutoCloseable {
    private final Socket tcp = new Socket();
    public final DatagramSocket udp;
    public final String sessionId;
    private DataInputStream input;
    private DataOutputStream output;
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor();

    public DemoConnection(String host, int tcpPort, int udpPort) throws IOException {
        udp = new DatagramSocket();
        try {
            InetAddress address = InetAddress.getByName(host);
            tcp.connect(new InetSocketAddress(address, tcpPort), 5000);
            tcp.setSoTimeout(5000);
            input = new DataInputStream(tcp.getInputStream());
            output = new DataOutputStream(tcp.getOutputStream());
            udp.connect(address, udpPort);
            udp.setSoTimeout(1500);

            String[] hello = request("HELLO").split(" ");
            if (hello.length != 3 || !hello[0].equals("HELLO_OK")) {
                throw new IOException("HELLO response invalid");
            }
            sessionId = hello[1];
            boolean registered = false;
            for (int attempt = 0; attempt < 3 && !registered; attempt++) {
                sendUdp("REGISTER " + sessionId + " " + hello[2]);
                try {
                    registered = receiveUdp().equals("REGISTER_OK");
                } catch (SocketTimeoutException e) {
                    // Gui lai cung yeu cau tren cung socket neu mat goi.
                }
            }
            if (!registered) throw new IOException("UDP registration failed");
            udp.setSoTimeout(200);
            heartbeat.scheduleAtFixedRate(() -> {
                try {
                    if (!request("PING").equals("PONG")) throw new IOException("Bad PONG");
                } catch (IOException e) {
                    System.err.println("TCP disconnected: " + e.getMessage());
                    close();
                }
            }, 10, 10, TimeUnit.SECONDS);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    // Dong bo ca gui va nhan de heartbeat khong lay nham phan hoi lenh khac.
    public synchronized String request(String command) throws IOException {
        ControlProtocol.sendMessage(output, command);
        return ControlProtocol.readMessage(input);
    }

    public void sendUdp(String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        if (data.length > 512) throw new IOException("UDP packet exceeds 512 bytes");
        udp.send(new DatagramPacket(data, data.length, udp.getRemoteSocketAddress()));
    }

    public String receiveUdp() throws IOException {
        byte[] data = new byte[513];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        udp.receive(packet);
        if (packet.getLength() > 512) return "";
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    public void close() {
        heartbeat.shutdownNow();
        udp.close();
        try { tcp.close(); } catch (IOException ignored) { }
    }
}
