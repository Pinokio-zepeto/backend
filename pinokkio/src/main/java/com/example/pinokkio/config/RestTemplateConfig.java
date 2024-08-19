package com.example.pinokkio.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RestTemplateConfig {

    // RestTemplate 빈을 생성하는 메서드입니다.
    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // SSL 컨텍스트를 생성합니다. 여기서는 기본 설정을 사용합니다.
        SSLContext sslContext = SSLContexts.custom().build();

        // HTTP와 HTTPS 연결을 위한 소켓 팩토리 레지스트리를 생성합니다.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(sslContext))
                .register("http", new PlainConnectionSocketFactory())
                .build();

        // HTTP 클라이언트 연결 관리자를 생성하고 설정합니다.
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        connectionManager.setMaxTotal(100); // 전체 최대 연결 수
        connectionManager.setDefaultMaxPerRoute(20); // 라우트당 최대 연결 수

        // HTTP 클라이언트를 생성합니다.
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // RestTemplate이 사용할 요청 팩토리를 생성하고 설정합니다.
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000); // 연결 타임아웃 (5초)
        requestFactory.setConnectionRequestTimeout(5000); // 연결 요청 타임아웃 (5초)

        // 설정된 요청 팩토리로 RestTemplate을 생성하고 반환합니다.
        return new RestTemplate(requestFactory);
    }
}