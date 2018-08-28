package co.iay.learn.learningjava.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SimpleNIOEchoServer {
    public static void main(String[] args) {
        ServerSocketChannel serverSocketChannel;
        Selector selector;
        ByteBuffer buffer = ByteBuffer.allocate(SimpleNIO.BUFFER_SIZE);

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 12345), 10000);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                int selectorResult = selector.select(1000);
                if (selectorResult <= 0) {
                    if (selectorResult < 0) {
                        System.out.println("selectorResult = " + selectorResult);
                    }
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    SocketChannel sc;
                    try {
                        sc = serverSocketChannel.accept();
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (sc == null) {
                        continue;
                    }

                    int readNum = 0;

                    try {
                        readNum = sc.read(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    while (readNum > 0) {
                        buffer.flip();
                        int writeNum = 0;
                        try {
                            while (writeNum < readNum) {
                                writeNum += sc.write(buffer);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        buffer.clear();

                        try {
                            readNum = sc.read(buffer);

                            if (readNum == -1) {
                                buffer.clear();
                                sc.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                it.remove();
            }
        }
    }
}
