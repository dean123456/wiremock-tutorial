package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.models.Group;
import ru.chameleon.wiremocktutorial.models.Musician;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.like;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class XMLTests {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    /**
     * Example 15
     * Работа с xml
     */
    @Test
    public void xmlRequestMatchingTest() {
        String url = "/xml/musician"; // присваеваем переменной url значение
        String matchingString = "<Musician><id>${xmlunit.ignore}</id><name>Lars Ulrich</name><role>drummer</role></Musician>"; // xml для проверки

        Musician musician_1 = new Musician("Lars Ulrich", "drummer"); // создаём первого музыканта
        Group group_1 = new Group("Metallica", 1981, List.of("Master of Puppets, Nothing Else Matters")); // создаём первую группу
        Musician musician_2 = new Musician("Slash", "guitar player"); // создаём второго музыканта
        Group group_2 = new Group("Guns'n'Rosses", 1985, List.of("You Could Be Mine, November Rain")); // создаём вторую группу

        stubFor(post(url)
                .withHeader("Content-Type", equalToIgnoreCase("text/xml"))
                .withRequestBody(equalToXml(matchingString, true)) // true разрешает XMLUnit placeholders
                .willReturn(like(jsonResponse(group_1)))); // создаём заглушку, которая возвращает первую группу, если запрос приходит в формате xml и тело запроса эквивалентно переданной строке

        stubFor(post(url)
                .withHeader("Content-Type", equalToIgnoreCase("text/xml"))
                .withRequestBody(matchingXPath("//name/text()", containing("Slash")))
                .willReturn(like(jsonResponse(group_2)))); // создаём заглушку, которая возвращает вторую группу, если запрос приходит в формате xml и тело содержит тег name со значением "Slash"

        ResponseEntity<Group> GNR = client.postMusicianAndReturnGroupXML(PORT, url, musician_2); // отправляем музыканта 2, получаем ответ
        ResponseEntity<Group> Metallica = client.postMusicianAndReturnGroupXML(PORT, url, musician_1); // отправляем музыканта 1, получаем ответ

        assertEquals(group_2, GNR.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
        assertEquals(group_1, Metallica.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

}
