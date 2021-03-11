package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class VerifyTests {

    private static final Logger log = LoggerFactory.getLogger(VerifyTests.class);

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    /**
     * Example 10
     * Верификация запросов
     */
    @Test
    public void getHelloMetalVerifyTest() {
        String url = "/hello";
        String message = "Hello Metal!";
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200))); // создаём заглушку

        client.getHelloMetalWithHeadersInRequest(PORT, url); // отправляем запрос первый раз

        verify(getRequestedFor(urlEqualTo(url))
                .withHeader("Content-Type", equalTo("text/plain"))
                .withHeader("Cache-Control", equalTo("no-cache"))); // хотя бы один раз был такой запрос

        client.getHelloMetalWithHeadersInRequest(PORT, url); // отправляем запрос второй раз
        client.getHelloMetalWithHeadersInRequest(PORT, url); // отправляем запрос третий раз

        verify(3, getRequestedFor(urlEqualTo(url))
                .withHeader("Content-Type", equalTo("text/plain"))
                .withHeader("Cache-Control", equalTo("no-cache"))); // проверяем, что такой запрос был 3 раза

        //варианты верификации
        verify(lessThan(4), getRequestedFor(urlEqualTo(url))); // < 4
        verify(lessThanOrExactly(4), getRequestedFor(urlEqualTo(url))); // <= 4
        verify(exactly(3), getRequestedFor(urlEqualTo(url))); // = 4
        verify(moreThan(2), getRequestedFor(urlEqualTo(url))); // >= 2
        verify(moreThanOrExactly(2), getRequestedFor(urlEqualTo(url))); // > 2

        List<ServeEvent> allServeEvents_1 = getAllServeEvents(); // получаем все успешные события

        log.info("### All serve events ###");
        printEvents(allServeEvents_1); // выводим в консоль все успешные события

        log.info("### Requests which url equals to " + url + " ###");
        List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo(url))); // получаем все успешные запросы
        requests.forEach(req ->
                log.info("Request with url=" +
                        req.getUrl() +
                        " and method=" +
                        req.getMethod()));

        UUID eventId = allServeEvents_1.get(1).getId(); // получим id второго события из журнала
        removeServeEvent(eventId); // удалим событие с id равным eventId
        List<ServeEvent> allServeEventsAfterOneDeleted = getAllServeEvents(); // получаем все успешные события

        log.info("### All serve events after one deleted ###");
        printEvents(allServeEventsAfterOneDeleted);// выводим в консоль все успешные события после удаления одного события

        WireMock.resetAllRequests(); // сброс журнала запросов

        verify(0, getRequestedFor(urlEqualTo(url))); // проверяем, не было не одного запроса по данному url после очистки журнала запросов

        List<LoggedRequest> requestsAfterReset = findAll(getRequestedFor(anyUrl())); // получаем все запросы после сброса
        assertTrue(requestsAfterReset.isEmpty()); // проверяем очистились ли запросы из журнала
        List<ServeEvent> allServeEventsAfterReset = getAllServeEvents(); // получаем все события после сброса
        assertTrue(allServeEventsAfterReset.isEmpty()); // проверяем очистились ли события журнала

        client.getHelloMetalWithHeadersInRequest(PORT, url); // отправляем запрос четвёртый раз
        List<ServeEvent> allServeEvents_2 = getAllServeEvents(); // получаем все успешные события

        log.info("### All serve events after one requested again ###");
        printEvents(allServeEvents_2); // выводим в консоль все успешные события

        removeServeEvents(getRequestedFor(urlMatching(url))
                .withHeader("Content-Type", equalTo("text/plain"))); // удалим событие, отвечающее переданным критериям

        List<ServeEvent> allServeEvents_3 = getAllServeEvents(); // получаем все успешные события
        assertTrue(allServeEvents_3.isEmpty()); // проверяем очистились ли события журнала
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
