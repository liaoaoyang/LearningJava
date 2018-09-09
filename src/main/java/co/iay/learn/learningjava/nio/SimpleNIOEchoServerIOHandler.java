package co.iay.learn.learningjava.nio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;

public class SimpleNIOEchoServerIOHandler implements Runnable {
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SocketChannelInfo {
        private SocketChannel socketChannel;
        private long          clientId;
    }

    final private       String TAG                   = this.getClass().getName();
    final public static int    MAX_ONE_TIME_REGISTER = 100;

    private Selector                      selector;
    private boolean                       initialized = false;
    private ArrayDeque<SocketChannelInfo> socketChannelInfos;

    public SimpleNIOEchoServerIOHandler() {
        try {
            selector = Selector.open();
            socketChannelInfos = new ArrayDeque<>(1000);
            initialized = true;
        } catch (Exception e) {
            initialized = false;
            e.printStackTrace();
        }
    }

    public synchronized boolean addConnection(SocketChannelInfo info) throws Exception {
        if (!initialized) {
            throw new Exception("Not initialized");
        }

        info.getSocketChannel().configureBlocking(false);

        return socketChannelInfos.add(info);
    }

    @Override
    public void run() {
        if (!initialized) {
            System.out.println(this.TAG + " failed to init io handler thread");
            return;
        }

        System.out.println(this.getClass().getName() + " I/O thread start");

        while (true) {
            registerNewChannel();

            try {
                if (selector.select(10) <= 0) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isValid() && key.isReadable()) {
                    SimpleNIOClientAttachment attachment = (SimpleNIOClientAttachment) key.attachment();
                    System.out.println(this.TAG + " Client #" + attachment.getId() + " isReadable");
                    if (attachment.getMode() == SimpleNIO.MODE_READ && attachment.getBuffer().hasRemaining()) {
                        try {
                            int readNum = attachment.getSocketChannel().read(attachment.getBuffer());

                            if (readNum < 0) {
                                key.cancel();
                                System.out.println(this.TAG + " Client #" + attachment.getId() + " disconnected");
                                attachment.getSocketChannel().close();
                                continue;
                            } else if (readNum > 0) {
                                attachment.getSocketChannel().register(selector, SelectionKey.OP_WRITE, attachment);
                            } else {
                                System.out.println(this.TAG + " Client #" + attachment.getId() + " read " + readNum + " bytes");
                            }
                        } catch (Exception e) {
                            key.cancel();
                            System.out.println(this.TAG + " Client #" + attachment.getId() + " read exception:" + e.getMessage());
                            e.printStackTrace();
                            it.remove();
                            continue;
                        }
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    SimpleNIOClientAttachment attachment = (SimpleNIOClientAttachment) key.attachment();
                    System.out.println(this.TAG + " Client #" + attachment.getId() + " isWritable");

                    try {
                        if (attachment.getMode() == SimpleNIO.MODE_READ) {
                            attachment.getBuffer().flip();
                            attachment.setMode(SimpleNIO.MODE_WRITE);
                        }

                        if (attachment.getBuffer().hasRemaining()) {
                            int writeNum = attachment.getSocketChannel().write(attachment.getBuffer());

                            if (writeNum < 0) {
                                key.cancel();
                                System.out.println(this.TAG + " Client #" + attachment.getId() + " failed to write");
                                attachment.getSocketChannel().close();
                                continue;
                            } else {
                                System.out.println(this.TAG + " Client #" + attachment.getId() + " wrote " + writeNum + " bytes");
                            }
                        }

                        if (!attachment.getBuffer().hasRemaining()) {
                            attachment.getBuffer().clear();
                            attachment.setMode(SimpleNIO.MODE_READ);
                            attachment.getSocketChannel().register(selector, SelectionKey.OP_READ, attachment);
                        }
                    } catch (Exception e) {
                        key.cancel();
                        System.out.println(this.TAG + " Client #" + attachment.getId() + " write exception:" + e.getMessage());
                        it.remove();
                        continue;
                    }
                }

                it.remove();
            }

            registerNewChannel();
        }
    }

    private void registerNewChannel() {
        if (socketChannelInfos == null || selector == null) {
            return;
        }

        while (!socketChannelInfos.isEmpty()) {
            SocketChannelInfo info = socketChannelInfos.pop();

            if (!info.getSocketChannel().isOpen()) {
                continue;
            }

            try {
                info.getSocketChannel().register(selector, SelectionKey.OP_READ,
                        new SimpleNIOClientAttachment(info.getClientId(), info.getSocketChannel(), ByteBuffer.allocate(SimpleNIO.BUFFER_SIZE), System.currentTimeMillis(), SimpleNIO.MODE_READ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
