package ru.chameleon.wiremocktutorial.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example 14
 * JUNIT5
 */
@SpringBootTest
public class NetworkTest {

    private static final Logger log = LoggerFactory.getLogger(NetworkTest.class);

    /*
    Сервер с хостом отличным от localhost и динамическим портом
    */
    private static WireMockServer server = new WireMockServer(wireMockConfig().bindAddress("127.0.0.2").dynamicPort()); // задаём хост по которому будет доступен сервер и динамический порт

    private RestTemplateClient client = new RestTemplateClient();

    @BeforeAll
    static void startServer() {
        server.start(); // Запуск сервера
    }

    @BeforeEach
    void init() {
        String host = server.getOptions().bindAddress(); // получаем хост сервера
        int port = server.port(); // получаем порт сервера
        log.info("WireMock server host: " + host);
        log.info("WireMock server port: " + port);
        configureFor(host, port); //Настройка клиента Wire Mock. Если меняется хост или порт, то это необходимо указать тут, иначе localhost:8080
    }

    /**
     * Заглушка, доступная по адресу, определённому в методе init()
     */
    @Test
    public void getHelloMetalTest() {
        String url = "/hello";
        String message = "Hello Metal!";
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(message)));
        assertEquals(message, client.getHelloMetal(server.getOptions().bindAddress(), server.port(), url));
    }

    /**
     * Другой способ создания заглушки
     */
    @Test
    public void getHelloMetalOtherWayTest() {
        WireMock wireMock = new WireMock(server.getOptions().bindAddress(), server.port()); // Ещё один способ настроить клиента
        String url = "/hello";
        String message = "Hello Metal!";
        wireMock.register(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(message)));
        assertEquals(message, client.getHelloMetal(server.getOptions().bindAddress(), server.port(), url));
    }

    @AfterAll
    static void stopServer() {
        server.stop(); // Остановка сервера
    }
}
