package co.iay.learn.learningjava.nio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleNIOEchoClientMT {
    public static void main(String[] args) {
        String hostname = System.getProperty("hostname", "127.0.0.1");
        String[] portsString = System.getProperty("port", "12345").split(",");
        Integer[] ports = new Integer[portsString.length];

        for (int i = 0; i < portsString.length; ++i) {
            ports[i] = Integer.parseInt(portsString[i]);
        }

        int messageLength = Integer.parseInt(System.getProperty("messageLength", "32"));
        int concurrent = Integer.parseInt(System.getProperty("concurrent", "1"));
        int threads = Integer.parseInt(System.getProperty("threads", "1"));
        int requestNumber = Integer.parseInt(System.getProperty("requestNumber", "1"));
        int requestPerConnection = Integer.parseInt(System.getProperty("requestPerConnection", "1"));
        int intervalMS = Integer.parseInt(System.getProperty("intervalMS", "1000"));
        Boolean randomInterval = Boolean.parseBoolean(System.getProperty("randomInterval", "false"));
        Semaphore concurrentSemaphore = new Semaphore(concurrent);
        AtomicInteger finished = new AtomicInteger(0);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        SimpleNIOEchoClientHandler[] handlers = new SimpleNIOEchoClientHandler[threads];

        for (int i = 0; i < threads; ++i) {
            handlers[i] = new SimpleNIOEchoClientHandler(concurrentSemaphore, finished, requestNumber, (long) (i + 1), (long) threads);
            threadPool.submit(handlers[i]);
        }

        for (int i = 0; i < requestNumber; ++i) {
            try {
                concurrentSemaphore.acquire();
                int executorIndex = i % threads;
                handlers[executorIndex].getRequests().push(SimpleNIOEchoClientRequest.builder().
                        hostname(hostname).
                        port(ports[executorIndex]).
                        requestPerConnection(requestPerConnection).
                        messageLength(messageLength).
                        intervalMS(intervalMS).
                        randomInterval(randomInterval).
                        build());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        while (finished.get() < requestNumber) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        threadPool.shutdown();
    }
}

