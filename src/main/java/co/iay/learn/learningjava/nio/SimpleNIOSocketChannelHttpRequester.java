package co.iay.learn.learningjava.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleNIOSocketChannelHttpRequester {
    final private static int    REQ                        = 1;
    final private static long   TIMEOUT_MS                 = 5000L;
    final private static int    BUFFER_SIZE                = 128;
    final private static String WROTE_BYTES                = "WROTE_BYTES";
    final private static String WRITE_BUFFER               = "WRITE_BUFFER";
    final private static String READ_BUFFER                = "READ_BUFFER";
    final private static String CHANNEL                    = "CHANNEL";
    final private static String CHANNEL_ID                 = "CHANNEL_ID";
    final private static String STRING_BUILDER             = "STRING_BUILDER";
    final private static String REQUEST_START_TIME_MS      = "REQUEST_START_TIME_MS";
    final private static String CONTENT_LENGTH             = "CONTENT_LENGTH";
    final private static String HTTP_HEADER_CONTENT_LENGTH = "CONTENT-LENGTH";
    final private static String HTTP_BODY_START_INDEX      = "HTTP_BODY_START_INDEX";
    final private static String CRLF                       = "\r\n";
    final private static String HTTP_HEADER_END            = "\r\n\r\n";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        String hostname = "127.0.0.1";

        if (args.length >= 1) {
            hostname = args[0];
        }

        int port = 12345;

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        int concurrent = REQ;

        if (args.length >= 3) {
            concurrent = Integer.parseInt(args[2]);
        }

        long timeoutMS = TIMEOUT_MS;

        if (args.length >= 4) {
            timeoutMS = Long.parseLong(args[3]);
        }

        int bufferSize = BUFFER_SIZE;

        if (args.length >= 5) {
            bufferSize = Integer.parseInt(args[4]);
        }

        System.out.println("hostname=" + hostname + " port=" + port + " concurrent=" + concurrent + " timeoutMS=" + timeoutMS + " bufferSize=" + bufferSize);
        byte[] httpRequestBytes = ("GET / HTTP/1.1\r\nHost: " + hostname + "\r\nAccept: */*\r\n\r\n").getBytes();

        int[] alreadyRead = new int[concurrent];
        long[] channelStartMS = new long[concurrent];
        long[] channelFinishConnectMS = new long[concurrent];
        long[] channelFinishWriteMS = new long[concurrent];
        long[] channelFinishMS = new long[concurrent];
        StringBuilder[] sbs = new StringBuilder[concurrent];
        SocketChannel[] socketChannels = new SocketChannel[concurrent];
        Selector selector;

        int liveChannels = concurrent;
        long initTimeMs = System.currentTimeMillis();
        SocketAddress socketAddress = new InetSocketAddress(hostname, port);
        SocketChannel socketChannel;
        Charset charsetUTF8 = Charset.forName("UTF8");
        CharsetDecoder decoderUTF8 = charsetUTF8.newDecoder();

        try {
            selector = Selector.open();

            for (int i = 0; i < concurrent; ++i) {
                sbs[i] = new StringBuilder();
                socketChannel = SocketChannel.open();
                socketChannels[i] = socketChannel;
                socketChannel.configureBlocking(false);
                Map<String, Object> channelObj = new HashMap<>();
                channelObj.putIfAbsent(WRITE_BUFFER, ByteBuffer.allocate(bufferSize));
                channelObj.putIfAbsent(READ_BUFFER, ByteBuffer.allocate(bufferSize));
                channelObj.putIfAbsent(CHANNEL, socketChannel);
                channelObj.putIfAbsent(CHANNEL_ID, i);
                channelObj.putIfAbsent(STRING_BUILDER, sbs[i]);
                channelObj.putIfAbsent(REQUEST_START_TIME_MS, System.currentTimeMillis());
                channelObj.putIfAbsent(CONTENT_LENGTH, "");
                channelObj.putIfAbsent(HTTP_BODY_START_INDEX, -1);
                channelObj.putIfAbsent(WROTE_BYTES, 0);
                socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE, channelObj);
                channelStartMS[i] = System.currentTimeMillis();
                socketChannel.connect(socketAddress);
            }

            while (liveChannels > 0) {
                long currentMs = System.currentTimeMillis();

                if (selector.select(1000) == 0) {
                    if (currentMs - initTimeMs > timeoutMS) {
                        for (int i = 0; i < concurrent; ++i) {
                            SocketChannel channel = socketChannels[i];

                            if (channel.isOpen()) {
                                channel.close();
                                --liveChannels;
                            }
                        }

                        break;
                    }

                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    Map<String, Object> attachment = (Map<String, Object>) key.attachment();
                    SocketChannel channel = (SocketChannel) attachment.get(CHANNEL);
                    int channelId = (int) attachment.get(CHANNEL_ID);
                    StringBuilder sb = (StringBuilder) attachment.get(STRING_BUILDER);
                    long requestStartMs = (long) attachment.get(REQUEST_START_TIME_MS);
                    String contentLength = (String) attachment.get(CONTENT_LENGTH);
                    int httpBodyStartIndex = (int) attachment.get(HTTP_BODY_START_INDEX);
                    int requestWroteBytes = (int) attachment.get(WROTE_BYTES);

                    if (!key.isValid()) {
                        channel.close();
                        --liveChannels;
                    }

                    if (key.isConnectable()) {
                        if (channel.finishConnect()) {
                            channelFinishConnectMS[channelId] = System.currentTimeMillis();
                        } else {
                            channel.close();
                            --liveChannels;
                        }
                    }

                    if (key.isWritable() && requestWroteBytes < httpRequestBytes.length) {
                        ByteBuffer byteBuffer = (ByteBuffer) attachment.get(WRITE_BUFFER);
                        byteBuffer.clear();
                        int putLength = Math.min(bufferSize, httpRequestBytes.length - requestWroteBytes);
                        byteBuffer.put(httpRequestBytes, requestWroteBytes, putLength);
                        byteBuffer.flip();

                        while (byteBuffer.hasRemaining()) {
                            channel.write(byteBuffer);
                        }
                        requestWroteBytes += putLength;
                        attachment.put(WROTE_BYTES, requestWroteBytes);

                        channelFinishWriteMS[channelId] = System.currentTimeMillis();

                        if (requestWroteBytes >= httpRequestBytes.length) {
                            channel.register(selector, SelectionKey.OP_READ, key.attachment());
                        }
                    }

                    if (key.isReadable()) {
                        ByteBuffer byteBuffer = (ByteBuffer) attachment.get(READ_BUFFER);
                        CharBuffer charBuffer = CharBuffer.allocate(bufferSize);
                        int readNum = channel.read(byteBuffer);

                        while (readNum > 0) {
                            byteBuffer.flip();
                            decoderUTF8.decode(byteBuffer, charBuffer, false);
                            charBuffer.flip();
                            alreadyRead[channelId] += byteBuffer.limit();

                            while (charBuffer.hasRemaining()) {
                                sb.append(charBuffer.get());
                            }

                            byteBuffer.clear();
                            charBuffer.clear();
                            readNum = channel.read(byteBuffer);
                        }

                        if (contentLength.equals("")) {
                            int headBodySepIndex = httpBodyStartIndex;

                            if (headBodySepIndex < 0) {
                                headBodySepIndex = sb.indexOf(HTTP_HEADER_END);
                            }

                            if (headBodySepIndex >= 0) {
                                httpBodyStartIndex = headBodySepIndex + HTTP_HEADER_END.length();
                                attachment.put(HTTP_BODY_START_INDEX, httpBodyStartIndex);
                                int contentLengthStartIndex = sb.substring(0, headBodySepIndex).toUpperCase().indexOf(HTTP_HEADER_CONTENT_LENGTH);

                                if (contentLengthStartIndex >= 0) {
                                    contentLengthStartIndex += (HTTP_HEADER_CONTENT_LENGTH.length() + 2);
                                    int contentLengthEndIndex = sb.indexOf(CRLF, contentLengthStartIndex);

                                    if (contentLengthEndIndex >= 0) {
                                        contentLength = sb.substring(contentLengthStartIndex, contentLengthEndIndex);
                                        attachment.put(CONTENT_LENGTH, contentLength);
                                    }
                                }
                            }
                        }

                        if (contentLength.equals(sb.substring(httpBodyStartIndex).length() + "")) {
                            channel.close();
                            --liveChannels;
                            channelFinishMS[channelId] = System.currentTimeMillis();
                        }

                        if (readNum == -1 || currentMs - requestStartMs > timeoutMS) {
                            channel.close();
                            --liveChannels;
                            channelFinishMS[channelId] = System.currentTimeMillis();
                        }
                    }

                    it.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Used: %.3fs\n", ((System.currentTimeMillis() - initTimeMs)) / 1000.0);
            return;
        }

        for (int channelId = 0; channelId < alreadyRead.length; ++channelId) {
            System.out.println("Channel #" + channelId + " already read " + alreadyRead[channelId] +
                    ", connect used " + (channelFinishConnectMS[channelId] - channelStartMS[channelId]) + "ms" +
                    ", write used " + (channelFinishWriteMS[channelId] - channelFinishConnectMS[channelId]) + "ms" +
                    ", read used " + (channelFinishMS[channelId] - channelFinishWriteMS[channelId]) + "ms" +
                    ", total used " + (channelFinishMS[channelId] - channelStartMS[channelId]) + "ms"
            );
        }

        System.out.printf("Used: %.3fs\n", ((System.currentTimeMillis() - initTimeMs)) / 1000.0);
    }
}
