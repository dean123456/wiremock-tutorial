package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

/**
 * Example 22
 * Односторонний TLS
 * В application.properties, чтоб увидеть handshake добавлен logging.level.org.apache.http=DEBUG
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TLS {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT1 = 6443;

    @Rule
    public WireMockRule tlsWireMockRule = new WireMockRule(options().httpsPort(PORT1) // включение https
            .keystorePath("src/test/resources/store/server/server-keystore.jks") // путь до хранилища ключей
            .keyManagerPassword("spassword") // пароль менеджера ключей
            .keystorePassword("spassword")); // пароль хранилища ключей

    @Test
    public void tTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException {
        String url = "/hello"; // присваеваем переменной url значение
        String message = "Hello Metal when https!"; // присваеваем переменной message значение

        tlsWireMockRule.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().withBody(message))); //создаём заглушку, которая принимает запросы по https по порту 6443 и возвращает message

        ResponseEntity<String> response = client.getHttps("https://", "localhost", PORT1, url); // отправляем запрос на сервер по https

        assertEquals(message, response.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }
}
