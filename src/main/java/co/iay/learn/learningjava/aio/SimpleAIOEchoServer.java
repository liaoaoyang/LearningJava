package co.iay.learn.learningjava.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SimpleAIOEchoServer {
    final private static int BUFFER_SIZE = 128;

    public static void main(String[] args) {
        String hostname = System.getProperty("hostname", "127.0.0.1");
        String port = System.getProperty("port", "12345");
        String backlog = System.getProperty("backlog", "10000");
        AsynchronousServerSocketChannel serverSocketChannel;

        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(hostname, Integer.parseInt(port)), Integer.parseInt(backlog));
            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                CompletionHandler<AsynchronousSocketChannel, Void> that = this;

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    result.read(buffer, result, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            if (!(attachment instanceof AsynchronousSocketChannel)) {
                                return;
                            }

                            AsynchronousSocketChannel sc = (AsynchronousSocketChannel) attachment;
                            buffer.flip();
                            sc.write(buffer, sc, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                                @Override
                                public void completed(Integer result, AsynchronousSocketChannel attachment) {
                                    if (attachment == null) {
                                        return;
                                    }

                                    buffer.clear();
                                }

                                @Override
                                public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                                    if (attachment == null) {
                                        return;
                                    }

                                    try {
                                        attachment.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            serverSocketChannel.accept(null, that);
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            if (!(attachment instanceof AsynchronousSocketChannel)) {
                                return;
                            }

                            AsynchronousSocketChannel sc = (AsynchronousSocketChannel) attachment;

                            try {
                                sc.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            serverSocketChannel.accept(null, that);
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Void attachment) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
