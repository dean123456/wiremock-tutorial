package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class JUnit4RuleTest {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT); // Настройка сервера WireMock

    /**
     * @see "Example 1"
     * Заглушка, возвращающая "Hello Metal!"
     */
    @Test
    public void getHelloMetalTest() {
        String url = "/hello";
        String message = "Hello Metal!";
        stubFor(get(urlEqualTo(url)) // HTTP метод GET, url=/hello
                .willReturn(aResponse()
                        .withStatus(200) // возвращает статус 200
                        .withBody(message))); // возвращает message
        assertEquals(message, client.getHelloMetal(PORT, url)); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 2
     * Заглушка, возвращающая "Hello Metal!" и дополнительные параметры
     */
    @Test
    public void getHelloMetalWithExtendedConfigurationTest() {
        String url = "/hello";
        String message = "Hello Metal!";
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain") // добавление хэдера ключ - массив_значений
                        .withHeader("Set-Cookie", "session_id=91837492837")
                        .withHeader("Set-Cookie", "split_test_group=B")
                        .withHeader("Cache-Control", "no-cache")
                        .withBody(message))); // аналогичная заглушка лежит в resources/mappings и доступна по url=/hello/jsonCreatedStub

        ResponseEntity<String> response = client.getRequest(PORT, url); // отправляем запрос в заглушку и получаем ответ

        // проверки:
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType()); // можно так
        assertEquals(List.of("text/plain"), response.getHeaders().get("Content-Type")); // а можно вот так
        assertEquals(List.of("session_id=91837492837", "split_test_group=B"), response.getHeaders().get("Set-Cookie"));
        assertEquals(List.of("no-cache"), response.getHeaders().get("Cache-Control")); // можно так
        assertEquals("no-cache", response.getHeaders().getCacheControl()); // а можно вот так
        assertEquals(message, response.getBody());
    }


    /**
     * Example 3
     * Заглушка лежит в resources/mappings/jsonCreatedStub и доступна по url=/hello/jsonCreatedStub
     */
    @Test
    public void getHelloMetalFromJsonCreatedStubTest() {
        String url = "/hello/jsonCreatedStub";
        String message = "Hello Metal!";

        ResponseEntity<String> response = client.getRequest(PORT, url); // отправляем запрос в заглушку и получаем ответ

        // проверки:
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType()); // можно так
        assertEquals(List.of("text/plain"), response.getHeaders().get("Content-Type")); // а можно вот так
        assertEquals(List.of("session_id=91837492837", "split_test_group=B"), response.getHeaders().get("Set-Cookie"));
        assertEquals(List.of("no-cache"), response.getHeaders().get("Cache-Control")); // можно так
        assertEquals("no-cache", response.getHeaders().getCacheControl()); // а можно вот так
        assertEquals(message, response.getBody());
    }

    /**
     * Example 5
     * Заглушка возвращает сообщение, которое лежит в файле __file/bodyFromFile.txt
     */
    @Test
    public void getHelloMetalWithBodyFromFileTest() {
        String literalMessage = "Hello Metal Literal!";
        stubFor(get(urlEqualTo("/hello/bodyFromFile"))
                .willReturn(aResponse()
                        .withBodyFile("bodyFromFile.txt"))); // тело сообщения лежит в файле как literal

        assertEquals(literalMessage, client.getHelloMetal(PORT, "/hello/bodyFromFile"));
    }

    /*
    Example 25
    Заглушка лежит в resources/mappings/jsonBodyStub и доступна по url=/ironMaiden/jsonBody и возвращает тело сообщения ввиде json
    */
    @Test
    public void getIronMaidenFromStubWithJsonBodyTest() {
        String jsonMessage = "{\"group\":\"Iron Maiden\",\"frontMan\":\"Bruce Dickinson\"}";
        assertEquals(jsonMessage, client.getHelloMetal(PORT, "/ironMaiden/jsonBody"));
    }

    /**
     * Example 24
     * Заглушки обрабатывают запросы в соответствии с приоритетом
     */
    @Test
    public void getHelloMetalWithPriority() {
        String message_1 = "Hello Metal!";
        String message_2 = "Hello Metal again!";
        stubFor(get(urlMatching("/hello/.*")) // заглушка доступна по любому url, который начинается с /hello/
                .atPriority(5) // устанавливаем более низкий приоритет
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(message_1))); // возвращает message_1
        stubFor(get(urlEqualTo("/hello/priority")) // заглушка доступна только по url=/hello/priority
                .atPriority(1) // устанавливаем самый высокий приоритет
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(message_2))); // возвращает message_2
        ResponseEntity<String> response1 = client.getRequest(PORT, "/hello/first"); // этот запрос перехватит заглушка с низким приоритетом
        ResponseEntity<String> response2 = client.getRequest(PORT, "/hello/priority"); // этот запрос перехватит заглушка с высоким приоритетом
        assertEquals(message_1, response1.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
        assertEquals(message_2, response2.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 9
     * Заглушка, возвращающая повреждённый ответ. При этом ожидается исключение ввода/вывода ResourceAccessException
     */
    @Test(expected = ResourceAccessException.class)
    public void FaultTest() {
        String url = "/hello";
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withFault(Fault.EMPTY_RESPONSE))); // плохие ответы задаются константами
        client.getHelloMetal(PORT, url);
    }

}
