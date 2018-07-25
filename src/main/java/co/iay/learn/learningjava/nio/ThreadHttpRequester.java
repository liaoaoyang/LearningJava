package co.iay.learn.learningjava.nio;

import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
public class ThreadHttpRequester implements Runnable {
    final private static int    BUFFER_SIZE                = 1024;
    final private static String CRLF                       = "\r\n";
    final private static String HTTP_HEADER_END            = "\r\n\r\n";
    final private static String HTTP_HEADER_CONTENT_LENGTH = "CONTENT-LENGTH";

    private int    no;
    private String hostname;
    private int    port;
    private int    httpBodyStartIndex;

    private int getContentLengthInHeader(StringBuilder sb) {
        String contentLength = "";
        int headBodySepIndex = httpBodyStartIndex;

        if (headBodySepIndex < 0) {
            headBodySepIndex = sb.indexOf(HTTP_HEADER_END);
        }

        if (headBodySepIndex >= 0) {
            httpBodyStartIndex = headBodySepIndex + HTTP_HEADER_END.length();
            int contentLengthStartIndex = sb.substring(0, headBodySepIndex).toUpperCase().indexOf(HTTP_HEADER_CONTENT_LENGTH);

            if (contentLengthStartIndex >= 0) {
                contentLengthStartIndex += (HTTP_HEADER_CONTENT_LENGTH.length() + 2);
                int contentLengthEndIndex = sb.indexOf(CRLF, contentLengthStartIndex);

                if (contentLengthEndIndex >= 0) {
                    contentLength = sb.substring(contentLengthStartIndex, contentLengthEndIndex);
                }
            }
        }

        return contentLength.isEmpty() ? -1 : Integer.parseInt(contentLength);
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        byte[] httpRequestBytes = ("GET / HTTP/1.1\r\nHost: " + hostname + "\r\nAccept: */*\r\n\r\n").getBytes();

        try {
            socket.connect(new InetSocketAddress(hostname, port));
            socket.getOutputStream().write(httpRequestBytes);
            byte[] buffer = new byte[BUFFER_SIZE];
            StringBuilder sb = new StringBuilder();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int readBytes = socket.getInputStream().read(buffer);

            while (readBytes > 0) {
                stream.write(buffer, 0, readBytes);
                sb.append(stream.toString("UTF8"));
                int contentLength = getContentLengthInHeader(sb);

                if (contentLength > 0 && contentLength == sb.substring(httpBodyStartIndex).length()) {
                    break;
                }

                readBytes = socket.getInputStream().read(buffer);
            }

            System.out.println("Request #" + no + " already read " + sb.toString().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}