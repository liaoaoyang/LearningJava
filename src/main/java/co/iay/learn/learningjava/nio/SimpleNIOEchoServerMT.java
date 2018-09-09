package co.iay.learn.learningjava.nio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleNIOEchoServerMT {
    public static void main(String[] args) {
        String hostname = System.getProperty("hostname", "127.0.0.1");
        String port = System.getProperty("port", "12345");
        String backlog = System.getProperty("backlog", "10000");
        String ioThreads = System.getProperty("ioThreads", "1");
        ExecutorService acceptThreadPool = Executors.newCachedThreadPool();
        ExecutorService ioThreadPool = Executors.newCachedThreadPool();
        String[] ports = port.split(",");
        SimpleNIOEchoServerAcceptHandler[] acceptHandlers = new SimpleNIOEchoServerAcceptHandler[ports.length];
        SimpleNIOEchoServerIOHandler[] ioHandlers = new SimpleNIOEchoServerIOHandler[Integer.parseInt(ioThreads)];

        try {
            for (int i = 0; i < ioHandlers.length; ++i) {
                ioHandlers[i] = new SimpleNIOEchoServerIOHandler();
                ioThreadPool.submit(ioHandlers[i]);
            }

            for (int i = 0; i < acceptHandlers.length; ++i) {
                acceptHandlers[i] = new SimpleNIOEchoServerAcceptHandler(hostname, Integer.parseInt(ports[i]), Integer.parseInt(backlog), ioHandlers, i + 1, ioHandlers.length);
                acceptThreadPool.submit(acceptHandlers[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
