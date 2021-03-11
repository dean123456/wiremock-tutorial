package ru.chameleon.wiremocktutorial.standalone;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Example 26
 * Запуск WireMock standalone сервера с помощью java в методе main
 * curl -v http://localhost:9999/standalone
 */
public class Standalone {

    public static void main(String[] args) {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9999)); // конфигурирование сервера на порту 9999
        wireMockServer.start(); // запуск сервера
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/standalone"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html;charset=UTF-8")
                        .withBody("Hello Metal!"))); // создание заглушки
    }
}
