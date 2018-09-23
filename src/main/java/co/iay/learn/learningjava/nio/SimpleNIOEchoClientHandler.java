package co.iay.learn.learningjava.nio;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleNIOEchoClientHandler implements Runnable {
    final private static String                            BYTES       = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private              Long                              clientId;
    private              Long                              clientIdStartValue;
    private              Long                              clientIdIncrease;
    private              Selector                          selector;
    private              boolean                           initialized = false;
    private              Semaphore                         semaphore;
    @Getter
    private              Deque<SimpleNIOEchoClientRequest> requests;
    private              AtomicInteger                     finished;
    private              int                               requestNumber;

    public SimpleNIOEchoClientHandler(Semaphore semaphore, AtomicInteger finished, int requestNumber, Long clientId, Long clientIdIncrease) {
        this.semaphore = semaphore;
        this.finished = finished;
        this.clientId = this.clientIdStartValue = clientId;
        this.clientIdIncrease = clientIdIncrease;
        this.requests = new ConcurrentLinkedDeque<>();
        this.requestNumber = requestNumber;

        try {
            selector = Selector.open();
            this.initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            this.initialized = false;
        }
    }

    @Override
    public void run() {
        if (!this.initialized) {
            System.out.println(this.getClass().getName() + " not initialized");
            return;
        }

        SelectionKey key = null;
        Iterator<SelectionKey> keysIterator = null;
        SimpleNIOEchoClientRequest request = null;

        do {
            if (this.finished.get() >= this.requestNumber) {
                break;
            }

            try {
                handleNewConnections();
                key = null;
                keysIterator = null;
                request = null;

                if (this.selector.select(1000) <= 0) {
                    continue;
                }

                keysIterator = this.selector.selectedKeys().iterator();

                while (keysIterator.hasNext()) {
                    key = keysIterator.next();
                    request = (SimpleNIOEchoClientRequest) key.attachment();

                    if (key.isConnectable()) {
                        request.getSocketChannel().finishConnect();
                        request.setMode(SimpleNIO.MODE_WRITE_PREPARE);
                        request.getSocketChannel().register(this.selector, SelectionKey.OP_WRITE, request);
                        keysIterator.remove();
                        continue;
                    }

                    if (key.isWritable()) {
                        if (request.getMode() == SimpleNIO.MODE_WRITE_PREPARE) {
                            int tryLength = request.getMessageLength() - request.getWroteBytes();
                            byte[] testData = getRandomBytes(tryLength > SimpleNIO.BUFFER_SIZE ? SimpleNIO.BUFFER_SIZE : tryLength);
                            request.getBuffer().put(testData);
                            request.setLastData(testData);
                            request.setMode(SimpleNIO.MODE_WRITE);
                            request.getBuffer().flip();
                        }

                        int wroteBytes = request.getSocketChannel().write(request.getBuffer());

                        if (wroteBytes < 0) {
                            this.semaphore.release();
                            request.getSocketChannel().close();
                            keysIterator.remove();
                            key.cancel();
                            continue;
                        } else if (wroteBytes > 0) {
                            request.setWroteBytes(request.getWroteBytes() + wroteBytes);

                            if (request.getWroteBytes() >= request.getMessageLength()) {
                                request.setRequested(request.getRequested() + 1);
                            }

                            request.setMode(SimpleNIO.MODE_READ);
                            request.getBuffer().clear();
                            request.getSocketChannel().register(this.selector, SelectionKey.OP_READ, request);
                        }
                    }

                    if (key.isReadable()) {
                        int readBytes = request.getSocketChannel().read(request.getBuffer());

                        if (readBytes > 0) {
                            if (request.getBuffer().limit() == request.getLastData().length) {
                                request.setReadBytes(request.getReadBytes() + request.getBuffer().limit());

                                if (request.getRequested() >= request.getRequestPerConnection()) {
                                    this.semaphore.release();
                                    request.getSocketChannel().close();
                                    this.finished.incrementAndGet();
                                    keysIterator.remove();
                                    key.cancel();
                                    continue;
                                } else {
                                    request.setMode(SimpleNIO.MODE_WRITE_PREPARE);
                                    request.getBuffer().clear();
                                    request.getSocketChannel().register(this.selector, SelectionKey.OP_WRITE, request);
                                }
                            }

                            request.getBuffer().clear();
                        }
                    }

                    keysIterator.remove();
                }
            } catch (Exception e) {
                if (request != null) {
                    this.semaphore.release();

                    try {
                        request.getSocketChannel().close();
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }

                    this.finished.incrementAndGet();

                    if (key != null) {
                        key.cancel();
                    }
                }
                e.printStackTrace();
            }
        } while (true);
    }

    private static byte[] getRandomBytes(int length) {
        byte[] result = new byte[length];

        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) BYTES.charAt(i);
        }

        return result;
    }

    private void handleNewConnections() throws Exception {
        if (null == this.requests || this.requests.isEmpty()) {
            return;
        }

        while (!this.requests.isEmpty()) {
            SimpleNIOEchoClientRequest request = this.requests.pop();
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            request.setBuffer(ByteBuffer.allocate(SimpleNIO.BUFFER_SIZE)).setSocketChannel(sc);
            this.clientId += this.clientIdIncrease;
            sc.register(this.selector, SelectionKey.OP_CONNECT, request);
            sc.connect(new InetSocketAddress(request.getHostname(), request.getPort()));
        }
    }
}
