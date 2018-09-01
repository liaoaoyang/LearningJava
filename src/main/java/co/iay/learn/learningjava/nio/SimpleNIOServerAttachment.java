package co.iay.learn.learningjava.nio;

import lombok.Getter;
import lombok.Setter;

import java.nio.channels.ServerSocketChannel;

@Getter
@Setter
public class SimpleNIOServerAttachment extends SimpleNIOAttachment {
    private ServerSocketChannel serverSocketChannel;

    public SimpleNIOServerAttachment(long id, ServerSocketChannel serverSocketChannel, long lastActive) {
        super(id, lastActive);

        this.serverSocketChannel = serverSocketChannel;
    }
}
