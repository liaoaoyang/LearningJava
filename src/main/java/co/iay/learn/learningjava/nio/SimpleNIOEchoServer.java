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
        String hostname = System.getProperty("hostname", "127.0.0.1");
        String port = System.getProperty("port", "12345");
        String backlog = System.getProperty("backlog", "10000");
        ServerSocketChannel serverSocketChannel;
        Selector selector;
        long clientId = 1L;

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(hostname, Integer.parseInt(port)), Integer.parseInt(backlog));
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

                if (key.isValid() && key.isAcceptable()) {
                    SocketChannel sc;
                    try {
                        sc = serverSocketChannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                                new SimpleNIOClientAttachment(clientId, sc, ByteBuffer.allocate(SimpleNIO.BUFFER_SIZE), System.currentTimeMillis(), SimpleNIO.MODE_READ));
                        ++clientId;

                        if (clientId > SimpleNIO.DEFAULT_MAX_CLIENT_ID) {
                            clientId = 1L;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                if (key.isValid() && key.isReadable()) {
                    SimpleNIOClientAttachment attachment = (SimpleNIOClientAttachment) key.attachment();
                    System.out.println("Client #" + attachment.getId() + " isReadable");
                    if (attachment.getMode() == SimpleNIO.MODE_READ && attachment.getBuffer().hasRemaining()) {
                        try {
                            int readNum = attachment.getSocketChannel().read(attachment.getBuffer());

                            if (readNum < 0) {
                                key.cancel();
                                System.out.println("Client #" + attachment.getId() + " disconnected");
                                attachment.getSocketChannel().close();
                                continue;
                            } else {
                                System.out.println("Client #" + attachment.getId() + " read " + readNum + " bytes");
                            }
                        } catch (Exception e) {
                            key.cancel();
                            System.out.println("Client #" + attachment.getId() + " read exception:" + e.getMessage());
                            e.printStackTrace();
                            it.remove();
                            continue;
                        }
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    SimpleNIOClientAttachment attachment = (SimpleNIOClientAttachment) key.attachment();
                    System.out.println("Client #" + attachment.getId() + " isWritable");

                    try {
                        if (attachment.getMode() == SimpleNIO.MODE_READ) {
                            attachment.getBuffer().flip();
                            attachment.setMode(SimpleNIO.MODE_WRITE);
                        }

                        if (attachment.getBuffer().hasRemaining()) {
                            int writeNum = attachment.getSocketChannel().write(attachment.getBuffer());

                            if (writeNum < 0) {
                                key.cancel();
                                System.out.println("Client #" + attachment.getId() + " failed to write");
                                attachment.getSocketChannel().close();
                                continue;
                            } else {
                                System.out.println("Client #" + attachment.getId() + " wrote " + writeNum + " bytes");
                            }
                        }

                        if (!attachment.getBuffer().hasRemaining()) {
                            attachment.getBuffer().clear();
                            attachment.setMode(SimpleNIO.MODE_READ);
                        }
                    } catch (Exception e) {
                        key.cancel();
                        System.out.println("Client #" + attachment.getId() + " write exception:" + e.getMessage());
                        it.remove();
                        continue;
                    }
                }

                it.remove();
            }
        }
    }
}
