package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class StubsLifecycle {

    private static final Logger log = LoggerFactory.getLogger(StubsLifecycle.class);

    private RestTemplateClient client = new RestTemplateClient();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Before
    public void init() {
        WireMock.configureFor(wireMockRule.port()); //Настройка клиента Wire Mock. Если меняется хост или порт, то это необходимо указать тут, иначе localhost:8080
    }

    /**
     * Example 8
     * Жизненний цикл заглушек
     */
    @Test
    public void stubsLifecycleTest() {
        int port = wireMockRule.port(); // получаем порт сервера Wire Mock

        String url_1 = "/helloMetal";
        String message_1 = "Hello Metal!";
        UUID stubId_1 = UUID.randomUUID(); // генерируем уникальный идентификатор для первой заглушки

        log.info("StubId_1: " + stubId_1);

        StubMapping stub_1 = stubFor(get(urlEqualTo(url_1))
                .withId(stubId_1) // присваиваем идентификатор
                .willReturn(aResponse()
                        .withBody(message_1))); // создаём заглушку stub_1

        String url_2 = "/helloRock";
        String message_2 = "Hello Rock!";
        UUID stubId_2 = UUID.randomUUID(); // генерируем уникальный идентификатор для второй заглушки

        log.info("StubId_2: " + stubId_2);

        StubMapping stub_2 = stubFor(get(urlEqualTo(url_2))
                .withId(stubId_2) // присваиваем идентификатор
                .willReturn(aResponse()
                        .withBody(message_2))); // создаём заглушку stub_2

        assertEquals(message_1, client.getHelloMetal(port, url_1)); // отправляем запрос на заглушку stub_1 и проверяем, что тело ответа содержит ожидаемый текст message_1
        assertEquals(message_2, client.getHelloMetal(port, url_2)); // отправляем запрос на заглушку stub_2 и проверяем, что тело ответа содержит ожидаемый текст message_2

        WireMock.listAllStubMappings().getMappings().forEach(v -> log.info("\n###\n" + "StubId: " + v.getId() + " " + v.getRequest() + "\n###\n")); //читает все созданные в тесте и сохранённые в resources/mappings заглушки

        String message_3 = "Hello Pop! Everybody dance!";

        editStub(get(urlEqualTo(url_1))
                .withId(stubId_1)
                .willReturn(aResponse()
                        .withBody(message_3))); // редактируем заглушку stub_1, используя её id, изменяя текст возвращаемого сообщения

        assertEquals(message_3, client.getHelloMetal(port, url_1)); // отправляем запрос на заглушку stub_1 и проверяем, что тело ответа содержит ожидаемый текст message_3

        StubMapping editedStub = getSingleStubMapping(stubId_1); // получаем заглушку по id

        assertEquals(stubId_1, editedStub.getUuid()); //проверяем, что id, отредактированной заглушки соответствует id stub_1

        log.info("\n###\n" + "StubId: " + editedStub.getId() + " " + editedStub.getRequest() + "\n###\n");

        removeStub(stub_1); // удаление зашлушки stub_1

        assertTrue(listAllStubMappings().getMappings().contains(stub_2)); // stub_2 всё ещё существует
        assertFalse(listAllStubMappings().getMappings().contains(stub_1)); // stub_1 больше не существует
    }
}
