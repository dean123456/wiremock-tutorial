package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;
import ru.chameleon.wiremocktutorial.models.Group;
import ru.chameleon.wiremocktutorial.models.Musician;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

/**
 * Example 19
 * Handlebars
 */
@RunWith(SpringRunner.class)
public class HandlebarsTest {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT)
            .extensions(new ResponseTemplateTransformer(true))); // true - глобальное включение шаблонизации, false - шаблонизация, должна быть указана явно в заглушке

    /**
     * Создание заглушки, которая обрабатывает любой запрос, по любому url и возвращает ответ с заполненными шаблонами
     */
    @Test
    public void addGroupRequestMatchingWithHandlebarsTest() {
        String url_1 = "/group/ironmaiden"; // url
        Group group_1 = new Group(UUID.fromString("c89f0e47-006b-491d-9466-b11bed5fdf3c"), "Iron Maiden", 1975, List.of("The Trooper", "Hallowed Be Thy Name")); // создаём объект

        // Проверяемое сообщение
        String message = "This request has method = POST, baseUrl = http://localhost:8089, url = /group/ironmaiden, headers = [Content-Type = application/json]," +
                " cookie = [web-site = developer@chameleon.ru] and body={\"id\":\"c89f0e47-006b-491d-9466-b11bed5fdf3c\",\"name\":\"Iron Maiden\",\"year\":1975,\"songs\":[\"The Trooper\",\"Hallowed Be Thy Name\"]}";

        // Сообщение с шаблонами
        String handlebar_msg = "This request has method = {{request.method}}, baseUrl = {{request.baseUrl}}, url = {{request.url}}, headers = [Content-Type = {{request.headers.Content-Type}}]," +
                " cookie = [web-site = {{request.cookies.web-site}}] and body={{{request.body}}}";

        stubFor(any(anyUrl())
                .willReturn(aResponse()
                        .withBody(handlebar_msg))); // создаём заглушку, которая обрабатывает любой запрос, по любому url и возвращает ответ с заполненнми шаблонами

        ResponseEntity<String> responseEntity = client.postGroup(PORT, url_1, group_1); // получаем ответ

        assertEquals(message, responseEntity.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Создание заглушки, которая обрабатывает любой запрос, по любому url и возвращает ответ с заполненными шаблонами (в т.ч. xPath)
     */
    @Test
    public void xmlRequestMatchingWithHandlebarsTest() {
        String url = "/xml/musician"; // присваеваем переменной url значение

        Musician musician = new Musician("Lars Ulrich", "drummer"); // создаём музыканта

        // Проверяемое сообщение
        String message = "This request has method = POST, baseUrl = http://localhost:8089, url = /xml/musician, headers = [Content-Type = text/xml]," +
                " and body=[name=Lars Ulrich, role=drummer]";

        // Сообщение с шаблонами
        String handlebar_msg = "This request has method = {{request.method}}, baseUrl = {{request.baseUrl}}, url = {{request.url}}, headers = [Content-Type = {{request.headers.Content-Type}}]," +
                " and body=[name={{xPath request.body 'Musician/name/text()'}}, role={{xPath request.body 'Musician/role/text()'}}]";

        stubFor(any(anyUrl())
                .willReturn(aResponse()
                        .withBody(handlebar_msg))); // создаём заглушку, которая обрабатывает любой запрос, по любому url и возвращает ответ с заполненнми шаблонами

        ResponseEntity<String> responseEntity = client.postMusicianAndReturnStringXML(PORT, url, musician); // отправляем музыканта, получаем ответ

        assertEquals(message, responseEntity.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }
}
