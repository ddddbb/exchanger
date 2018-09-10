package wanglin.exchanger.framework.protocol;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import wanglin.exchanger.framework.AssemblyException;
import wanglin.exchanger.framework.Assemparer;
import wanglin.exchanger.framework.Connection;
import wanglin.exchanger.framework.Exchanger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class HttpConnection<AADT> implements Connection<AADT, Map<String, Object>> {
    Logger              logger = LoggerFactory.getLogger(getClass());
    CloseableHttpClient client;
    ExecutorService     es;

    public HttpConnection(ExecutorService es, CloseableHttpClient client) {
        this.es = es;
        this.client = client;
    }


    @Override
    public Future<Map<String, Object>> send(Exchanger exchanger, AADT args, Assemparer assemparer) {
        HttpUriRequest request;
        try {
            request = (HttpUriRequest) assemparer.assemble(exchanger, args);
        } catch (AssemblyException e) {
            throw new RuntimeException("报文组装错误", e);
        }
        Assert.notNull(request, "组装报文不能为空");
        return es.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                CloseableHttpResponse response = client.execute(request);
                logger.info("渠道{}发送报文{}", exchanger.code, sendText(request));
                if (response.getStatusLine().getStatusCode() >= 300) {
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(),
                            response.getStatusLine().getReasonPhrase());
                }
                Map<String, Object> result = (Map<String, Object>) assemparer.parse(exchanger, response.getEntity());
                logger.info("渠道{}接收报文{}", exchanger.code, result);
                return result;
            }

        });
    }

    private Object sendText(HttpUriRequest request) {
        String text = null;
        if (request instanceof HttpGet) {
            text = ((HttpGet) request).getURI().toString();
        } else if (request instanceof HttpPost) {
            try {
                text = EntityUtils.toString(((HttpPost) request).getEntity(), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }
}
