package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class Proxy {

    private static final Logger log = LoggerFactory.getLogger(Proxy.class);

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT1 = 6443;
    private static final int PORT2 = 8089;

    @Rule
    public WireMockRule wireMockRuleHttps = new WireMockRule(options().httpsPort(PORT1)); // WireMock-сервер, доступный по https по порту 6443

    @Rule
    public WireMockRule wireMockRuleHttp = new WireMockRule(PORT2); // WireMock-сервер, доступный по http по порту 8089

    /**
     * Example 16
     * Проксирование запроса в другой сервис с изменением протокола с https на http
     */
    @Test
    public void proxyTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String url = "/hello"; // присваеваем переменной url значение
        String message = "Hello Metal from proxy!"; // присваеваем переменной message значение

        wireMockRuleHttps.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().proxiedFrom("http://localhost:" + PORT2))); //создаём заглушку, работающую как прокси-сервер, который принимает запросы по https по порту 6443 и перенаправляет их на http://localhost:8089 с тем же url (это может быть любой реальный сервис)

        wireMockRuleHttp.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().withBody(message))); //создаём заглушку, которая принимает запросы по http по порту 8089 и возвращает message

        ResponseEntity<String> response = client.getHelloMetalHttps("https://", "localhost", PORT1, url); // отправляем запрос на прокси-сервер

        assertEquals(message, response.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 18
     * Снэпшот заглушки с помощью клиента WireMock.
     */
    @Test
    public void snapshotTest() {
        WireMock wireMockClient = new WireMock(8089); // конфигурирование клиента
        wireMockRuleHttp.stubFor(proxyAllTo("http://example.mocklab.io").atPriority(1)); //создаём заглушку, работающую как прокси-сервер, который принимает запросы по http по порту 8089 и перенаправляет их на http://example.mocklab.io с тем же url

        RestTemplate restTemplate = new RestTemplate(); // создание экземрляра RestTemplate
        restTemplate.getForEntity("http://localhost:8089/recordables/123", String.class); // отправка GET запроса в прокси-сервер по указанному url

        List<ServeEvent> allServeEvents_1 = getAllServeEvents(); // получаем все успешные события

        log.info("### All serve events ###");
        printEvents(allServeEvents_1); // выводим в консоль все успешные события

        wireMockClient.takeSnapshotRecording(); // делаем снэпшот, в результате которого заглушка появится в resources/mappings
    }

    /**
     * Метод выводит в консоль все успешные события
     */
    private void printEvents(List<ServeEvent> events) {
        events.forEach(event ->
                log.info("Event with id=" +
                        event.getId() +
                        " and response message {" +
                        event.getResponse().getBodyAsString() +
                        "}"));
    }
}
