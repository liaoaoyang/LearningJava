package co.iay.learn.learningjava.nio;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class HttpCompRequester {
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

        int totalReq = NIOUtils.REQ;

        if (args.length >= 3) {
            totalReq = Integer.parseInt(args[2]);
        }

        long timeoutMS = NIOUtils.DEFAULT_TIMEOUT_MS;

        if (args.length >= 4) {
            timeoutMS = Long.parseLong(args[3]);
        }

        int threadPoolSize = NIOUtils.DEFAULT_THREAD_POOL_SIZE;

        if (args.length >= 5) {
            threadPoolSize = Integer.parseInt(args[4]);
        }

        long startMs = System.currentTimeMillis();

        System.out.println("hostname=" + hostname + " port=" + port + " totalReq=" + totalReq + " timeoutMS=" + timeoutMS + " threadPoolSize=" + threadPoolSize);

        String url = "http://" + hostname + ":" + port;

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout((int) timeoutMS)
                .setConnectTimeout((int) timeoutMS).build();
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        try {
            httpclient.start();
            HttpGet[] requests = new HttpGet[totalReq];

            for (int i = 0; i < totalReq; ++i) {
                requests[i] = new HttpGet(url);
            }

            final CountDownLatch latch = new CountDownLatch(requests.length);
            for (final HttpGet request : requests) {
                httpclient.execute(request, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + " cancelled");
                    }

                });
            }
            latch.await();
            System.out.println("Shutting down");
            httpclient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done");
        System.out.println("Used: " + (System.currentTimeMillis() - startMs) + "ms");
    }
}
