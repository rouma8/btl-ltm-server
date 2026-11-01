import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ControlProtocol {

    // Giới hạn tạm thời cho một thông điệp điều khiển.
    public static final int MAX_MESSAGE_SIZE = 8192;

    public static String readMessage(DataInputStream input)
            throws IOException {

        // Đọc đúng 4 byte chứa độ dài.
        int length = input.readInt();

        // Kiểm tra trước khi cấp phát bộ nhớ.
        if (length <= 0 || length > MAX_MESSAGE_SIZE) {
            throw new IOException(
                    "Do dai thong diep khong hop le: " + length
            );
        }

        byte[] data = new byte[length];

        // Chờ đọc đủ nội dung, hoặc báo lỗi nếu kết nối bị đóng.
        input.readFully(data);

        return new String(data, StandardCharsets.UTF_8);
    }

    public static void sendMessage(
            DataOutputStream output,
            String message) throws IOException {

        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        if (data.length == 0 || data.length > MAX_MESSAGE_SIZE) {
            throw new IOException("Thong diep gui vuot gioi han.");
        }

        output.writeInt(data.length);
        output.write(data);
        output.flush();
    }
}