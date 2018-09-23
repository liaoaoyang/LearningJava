package co.iay.learn.learningjava.nio;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Builder
@Getter
@Setter
@Accessors(chain = true)
public class SimpleNIOEchoClientRequest {
    private SocketChannel socketChannel;
    private String        hostname;
    private int           port;
    private int           messageLength;
    private int           wroteBytes;
    private int           readBytes;
    private int           requested;
    private int           requestPerConnection;
    private int           intervalMS;
    private boolean       randomInterval;
    private ByteBuffer    buffer;
    private int           mode         = SimpleNIO.MODE_NONE;
    private long          nextActiveMs = 0;
    private byte[]        lastData;
}
