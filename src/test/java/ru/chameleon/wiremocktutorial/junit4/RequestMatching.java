package ru.chameleon.wiremocktutorial.junit4;

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

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.like;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class RequestMatching {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    /**
     * Example 12
     * Сопоставление запроса с требованиями заглушки
     */
    @Test
    public void addGroupRequestMatchingTest() {
        String message = "Group added successful!";
        String url_1 = "/group/ironmaiden"; // url
        Group group_1 = new Group("Iron Maiden", 1975, List.of("The Trooper", "Hallowed Be Thy Name")); // создаём объект

        stubFor(any(urlPathMatching("/group/([a-z]*)")) //любой метод, путь соответствует регулярному выражению
                .withHeader("Content-Type", equalToIgnoreCase("application/json")) // соответствие хэдера без учёта регистра
                .withCookie("web-site", containing("@chameleon.ru")) // куки содержит, указанное значение
                .withRequestBody(matchingJsonPath("$.name")) // присутствует узел с таким названием
                .withRequestBody(matchingJsonPath("[?(@.year == 1975)]")) // узел с таким названием равен, указанному значению
                .withRequestBody(matchingJsonPath("[?(@.songs[1] == 'Hallowed Be Thy Name')]"))
                .willReturn(aResponse().withBody(message)));

        ResponseEntity<String> responseEntity = client.postGroup(PORT, url_1, group_1); // получаем ответ

        assertEquals(message, responseEntity.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 13
     * Сопоставление запросов с требованиями заглушек
     */
    @Test
    public void getGroupByMusicianRequestMatchingTest() {
        Musician musician_1 = new Musician("Bruce Dickinson", "vocal"); // создаём музыканта 1
        Musician musician_2 = new Musician("Nikki Sixx", "bass guitar player"); // создаём музыканта 2
        String url_1 = "/musician"; // url
        Group group_1 = new Group("Iron Maiden", 1975, List.of("The Trooper", "Hallowed Be Thy Name")); // создаём группу 1
        Group group_2 = new Group("Motley Crue", 1981, List.of("Looks That Kill", "Live Wire")); // создаём группу 2

        stubFor(post(urlPathEqualTo("/musician")) // метод POST, путь соответствует указанному
                .withHeader("Content-Type", equalToIgnoreCase("application/json")) // соответствие хэдера без учёта регистра
                .withRequestBody(matchingJsonPath("[?(@.name == 'Nikki Sixx')]")) //
                .willReturn(like(jsonResponse(group_2)).withHeader("Content-Type", "application/json")));

        stubFor(post(urlPathEqualTo("/musician")) // метод POST, путь соответствует указанному
                .withHeader("Content-Type", equalToIgnoreCase("application/json")) // соответствие хэдера без учёта регистра
                .withRequestBody(matchingJsonPath("[?(@.name == 'Bruce Dickinson')]")) //
                .willReturn(like(jsonResponse(group_1)).withHeader("Content-Type", "application/json")));

        ResponseEntity<Group> ironMaiden = client.postMusicianAndReturnGroup(PORT, url_1, musician_1); // отправляем музыканта 1, получаем ответ
        ResponseEntity<Group> motleyCrue = client.postMusicianAndReturnGroup(PORT, url_1, musician_2); // отправляем музыканта 2, получаем ответ

        assertEquals(group_1, ironMaiden.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
        assertEquals(group_2, motleyCrue.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }
}
