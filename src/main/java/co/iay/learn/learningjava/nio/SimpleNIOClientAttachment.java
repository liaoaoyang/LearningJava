package co.iay.learn.learningjava.nio;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Getter
@Setter
public class SimpleNIOClientAttachment extends SimpleNIOAttachment {
    private SocketChannel socketChannel;
    private int           mode;
    private ByteBuffer    buffer;

    public SimpleNIOClientAttachment(long id, SocketChannel socketChannel, ByteBuffer buffer, long lastActive, int mode) {
        super(id, lastActive);

        this.buffer = buffer;
        this.socketChannel = socketChannel;
        this.mode = mode;
    }
}
