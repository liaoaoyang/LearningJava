package co.iay.learn.learningjava.basic;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpClientTest {
    @Test
    public void testSimplePOST() {
        String UTF8 = "UTF-8";
        // spring-study project must start
        HttpPost post = new HttpPost("http://127.0.0.1:8080/post/test/case/1");
        List<NameValuePair> params = new ArrayList<>();
        String v = "中文";
        params.add(new BasicNameValuePair("v", v));

        StringBuilder sb = new StringBuilder();
        byte[] bytes = new byte[4];

        try {
            post.setEntity(new UrlEncodedFormEntity(params, UTF8));
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post);

            while (response.getEntity().getContent().read(bytes, 0, bytes.length) > 0) {
                sb.append(Arrays.toString(bytes));
            }

            response.close();

            Assert.assertEquals(v, new String(bytes, UTF8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
