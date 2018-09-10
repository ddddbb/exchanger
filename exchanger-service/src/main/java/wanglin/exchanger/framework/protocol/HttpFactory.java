package wanglin.exchanger.framework.protocol;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import wanglin.exchanger.framework.Connection;
import wanglin.exchanger.framework.Exchanger;
import wanglin.exchanger.framework.ProtocolEnum;
import wanglin.exchanger.framework.ProtocolFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class HttpFactory implements ProtocolFactory {

    ConcurrentMap<String, ProtocolEnum.Pool> pools           = new ConcurrentHashMap<>();
    ExecutorService                          executorService = Executors.newFixedThreadPool(10);

    /**
     * 获取httpclient，并包装成HttpConnection
     *
     * @param exchanger
     * @return
     */
    @Override
    public Connection get(Exchanger exchanger) {
        ProtocolEnum.Pool pool = pools.get(hostName(exchanger));
        if (null == pool) {
            pool = createPool(exchanger);
            pools.put(hostName(exchanger), pool);
        }
        return new HttpConnection(executorService, (CloseableHttpClient) pool.borrowObject());
    }



    public String hostName(Exchanger exchanger) {
        try {
            return new URL(exchanger.url).getHost();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void export(Exchanger exchanger) {

    }

    private ProtocolEnum.Pool createPool(Exchanger exchanger) {
        switch (exchanger.protocol) {
            case HTTP:
                return new HttpPool();
            case HTTPS:
                return new HttpsPool(exchanger);
            default:
                return new HttpPool();
        }
    }


    class HttpPool implements ProtocolEnum.Pool<CloseableHttpClient> {

        @Override
        public CloseableHttpClient borrowObject() {
            return HttpClients.createDefault();
        }
    }

    class HttpsPool implements ProtocolEnum.Pool<CloseableHttpClient> {
        public HttpsPool(Exchanger exchanger) {
            this.exchanger = exchanger;
        }

        Exchanger exchanger;

        @Override
        public CloseableHttpClient borrowObject() {
            // load client certificate
            try {
                SSLContext sslContext = createSslContext(exchanger);

                SSLConnectionSocketFactory sslSocketFactoy = new SSLConnectionSocketFactory(
                        sslContext, new String[]{"SSLv3"}, null,
                        SSLConnectionSocketFactory.getDefaultHostnameVerifier());


                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                HttpHost                           target            = HttpHost.create(exchanger.url);
                connectionManager.setMaxTotal(exchanger.protocolConfig.maxTotal);
                //设置每个Route的连接最大数
                connectionManager.setDefaultMaxPerRoute(5);
                //设置指定域的连接最大数
                connectionManager.setMaxPerRoute(new HttpRoute(target), 20);
                return HttpClients.custom().setSSLSocketFactory(sslSocketFactoy).setConnectionManager(connectionManager).build();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }

    private SSLContext createSslContext(Exchanger exchanger) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException {
        SSLContext sslContext;

        if (null == exchanger.ssl) {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
                    .build();
        } else {
            KeyStore ks = KeyStore.getInstance(exchanger.ssl.keyStoreType);
            ks.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(exchanger.ssl.keyStorePath), exchanger.ssl.keyStorePassword.toCharArray());

            sslContext = SSLContexts.custom()
                    .loadKeyMaterial(ks, exchanger.ssl.keyStorePassword.toCharArray())
                    .setSecureRandom(new SecureRandom())
                    .build();
        }
        return sslContext;
    }


}
