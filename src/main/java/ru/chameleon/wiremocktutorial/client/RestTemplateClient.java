package ru.chameleon.wiremocktutorial.client;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.chameleon.wiremocktutorial.models.Group;
import ru.chameleon.wiremocktutorial.models.Musician;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.UUID;

@Service
public class RestTemplateClient {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateClient.class);

    private static final String host = "http://localhost:";

    private RestTemplate restTemplate = new RestTemplate();

    public Group getGroupById(int port, String url, UUID id) {
        return restTemplate.getForObject(host + port + url + "/" + id, Group.class);
    }

    public String getHelloMetal(int port, String url) {
        log.info("Sending getHelloMetal request");
        return restTemplate.getForObject(host + port + url, String.class);
    }

    public ResponseEntity<String> getHelloMetalHttps(String protocol, String host, int port, String url) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        log.info("Sending getHelloMetalHttps request");

        /* Конфигурирование TLS для RestTemplate (принять все сертификаты) */
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslsf)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        /**/

        return new RestTemplate(requestFactory)
                .exchange(protocol + host + ":" + port + url, HttpMethod.GET, null, String.class);
    }

    public String getHelloMetal(String host, int port, String url) {
        log.info("Sending getHelloMetal request");
        return restTemplate.getForObject("http://" + host + ":" + port + url, String.class);
    }

    public ResponseEntity<String> getRequest(int port, String url) {
        log.info("Sending getRequest to " + url);
        return restTemplate.getForEntity(host + port + url, String.class);
    }

    public ResponseEntity<String> getHelloMetalWithHeadersInRequest(int port, String url) {
        log.info("Sending getHelloMetalWithHeadersInRequest request");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setCacheControl("no-cache");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(host + port + url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> postGroup(int port, String url, Group group) {
        log.info("Sending postGroup request with " + group.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "web-site=developer@chameleon.ru");
        HttpEntity<Group> entity = new HttpEntity<>(group, headers);
        return restTemplate.exchange(host + port + url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<Group> postMusicianAndReturnGroup(int port, String url, Musician musician) {
        log.info("Sending postMusicianAndReturnGroup request with " + musician.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Musician> entity = new HttpEntity<>(musician, headers);
        return restTemplate.exchange(host + port + url, HttpMethod.POST, entity, Group.class);
    }

    public ResponseEntity<Group> postMusicianAndReturnGroupXML(int port, String url, Musician musician) {
        log.info("Sending postMusicianAndReturnGroupXML request with " + musician.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<Musician> entity = new HttpEntity<>(musician, headers);
        restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter());
        return restTemplate.exchange(host + port + url, HttpMethod.POST, entity, Group.class);
    }

    public ResponseEntity<String> postMusicianAndReturnStringXML(int port, String url, Musician musician) {
        log.info("Sending postMusicianAndReturnStringXML request with " + musician.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<Musician> entity = new HttpEntity<>(musician, headers);
        restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter());
        return restTemplate.exchange(host + port + url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> postStubToWireMockStandalone(int port, String url, String body) {
        log.info("POST stub to WireMock standalone: \n" + body);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(host + port + url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> resetAllWireMockStandaloneStubs(int port) {
        log.info("Reset all stubs");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(host + port + "/__admin/reset", HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> getHttps(String protocol, String host, int port, String url) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException {
        String password_str = "cpassword";
        char[] ctspassword = password_str.toCharArray();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClientBuilder
                .create()
                .setSSLContext(SSLContextBuilder
                        .create()
                        .loadKeyMaterial(
                                new File("src/test/resources/store/client/client-keystore.jks"),
                                ctspassword,
                                ctspassword
                        )
                        .loadTrustMaterial(
                                new File("src/test/resources/store/client/client-truststore.jks"),
                                ctspassword
                        )
                        .setProtocol("TLSv1.2")
                        .build())
                .build()));
        return restTemplate.exchange(protocol + host + ":" + port + url, HttpMethod.GET, null, String.class);
    }

}
