package co.iay.learn.learningjava.nio;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleThreadHttpRequester {
    final private static int  REQ              = 1;
    final private static long TIMEOUT_MS       = 5000L;
    final private static int  THREAD_POOL_SIZE = 128;

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        String hostname = "127.0.0.1";

        if (args.length >= 1) {
            hostname = args[0];
        }

        int port = 12345;

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        int totalReq = REQ;

        if (args.length >= 3) {
            totalReq = Integer.parseInt(args[2]);
        }

        long timeoutMS = TIMEOUT_MS;

        if (args.length >= 4) {
            timeoutMS = Long.parseLong(args[3]);
        }

        int threadPoolSize = THREAD_POOL_SIZE;

        if (args.length >= 5) {
            threadPoolSize = Integer.parseInt(args[4]);
        }

        System.out.println("hostname=" + hostname + " port=" + port + " totalReq=" + totalReq + " timeoutMS=" + timeoutMS + " threadPoolSize=" + threadPoolSize);

        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        long initMS = System.currentTimeMillis();

        for (int i = 0; i < totalReq; ++i) {
            ThreadHttpRequester requester = new ThreadHttpRequester(i, hostname, port, -1);
            executorService.submit(requester);
        }

        executorService.shutdown();

        System.out.println("Used " + (System.currentTimeMillis() - initMS) + "ms");
    }
}
