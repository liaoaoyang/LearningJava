package co.iay.learn.learningjava.nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleNIOEchoServerMT {
    public static void main(String[] args) {
        String hostname = System.getProperty("hostname", "127.0.0.1");
        String port = System.getProperty("port", "12345");
        String backlog = System.getProperty("backlog", "10000");
        String ioThreads = System.getProperty("ioThreads", "1");
        ExecutorService executorService = Executors.newScheduledThreadPool(Integer.parseInt(ioThreads));
        SimpleNIOEchoServerIOHandler[] ioHandlers = new SimpleNIOEchoServerIOHandler[Integer.parseInt(ioThreads)];
        ServerSocketChannel serverSocketChannel;
        Selector selector;
        long clientId = 1L;

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(hostname, Integer.parseInt(port)), Integer.parseInt(backlog));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            for (int i = 0; i < ioHandlers.length; ++i) {
                ioHandlers[i] = new SimpleNIOEchoServerIOHandler();
                executorService.execute(ioHandlers[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            int threadIndex = 0;

            try {
                int selectorResult = selector.select(1000);
                if (selectorResult <= 0) {
                    if (selectorResult < 0) {
                        System.out.println("MT selectorResult = " + selectorResult);
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
                        ++clientId;
                        ioHandlers[threadIndex].addConnection(new SimpleNIOEchoServerIOHandler.SocketChannelInfo(sc, clientId));
                        threadIndex = (threadIndex + 1) % ioHandlers.length;

                        if (clientId > SimpleNIO.DEFAULT_MAX_CLIENT_ID) {
                            clientId = 1L;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                it.remove();
            }
        }
    }
}
