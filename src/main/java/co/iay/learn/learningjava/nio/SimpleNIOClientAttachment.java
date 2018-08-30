package co.iay.learn.learningjava.nio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@AllArgsConstructor
@Getter
@Setter
public class SimpleNIOClientAttachment {
    private long          id;
    private SocketChannel socketChannel;
    private ByteBuffer    buffer;
    private long          lastActive;
    private int           mode;
}
