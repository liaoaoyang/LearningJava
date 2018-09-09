package co.iay.learn.learningjava.nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SimpleNIOEchoServerAcceptHandler implements Runnable {
    final private String TAG = this.getClass().getName();

    private String                         hostname;
    private int                            port;
    private int                            backlog;
    private Selector                       selector;
    private ServerSocketChannel            serverSocketChannel;
    private SimpleNIOEchoServerIOHandler[] ioTreads;
    private long                           clientId;
    private long                           clientIdStart;
    private long                           clientIdIncrease;
    private boolean                        initialized = false;

    public SimpleNIOEchoServerAcceptHandler(String hostname, int port, int backlog, SimpleNIOEchoServerIOHandler[] ioTreads, long clientId, long clientIdIncrease) {
        this.hostname = hostname;
        this.port = port;
        this.backlog = backlog;
        this.ioTreads = ioTreads;
        this.clientId = clientId;
        this.clientIdStart = clientId;
        this.clientIdIncrease = clientIdIncrease;

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(this.hostname, this.port), this.backlog);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        if (!initialized) {
            System.out.println(this.TAG + " failed to init accept handler thread");
            return;
        }

        int threadIndex = 0;

        while (true) {
            try {
                selector.select();
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

                        if (sc == null) {
                            it.remove();
                            continue;
                        }

                        clientId += clientIdIncrease;
                        ioTreads[threadIndex].addConnection(new SimpleNIOEchoServerIOHandler.SocketChannelInfo(sc, clientId));
                        threadIndex = (threadIndex + 1) % ioTreads.length;

                        if (clientId > SimpleNIO.DEFAULT_MAX_CLIENT_ID) {
                            clientId = clientIdStart;
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
