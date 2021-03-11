package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;
import ru.chameleon.wiremocktutorial.models.Group;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class Scenarios {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    /**
     * Example 20
     * Описание сценария применения заглушек
     */
    @Test
    public void scenarioTest() {
        final String SCENARIO_NAME = "Add group"; // имя сценария
        String url = "/groups"; // url
        String emptyDBMessage = "No groups added"; // сообщение получаемое из БД, когда ни одна группа не добавлена
        String groupAddedMessage = "Group added"; // сообщение получаемое, когда группа добавлена в БД
        String groupCountMessage = "Count: 1 group"; // сообщение, получаемое из БД, когда группа добавлена

        Group group = new Group("Skid Row", 1986, List.of("18 And Life", "Youth Gone Wild")); // создаём объект

        stubFor(post(urlEqualTo(url))
                .withName("1")
                .inScenario(SCENARIO_NAME) // имя сценария
                .whenScenarioStateIs(STARTED) // сценарий в стадии STARTED
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(groupAddedMessage))
                .willSetStateTo("Group added")); // заглушка вернёт ответ и переведёт сценарий в стадию "Group added"

        stubFor(get(urlEqualTo(url))
                .withName("2")
                .inScenario(SCENARIO_NAME) // имя сценария
                .whenScenarioStateIs(STARTED) // сценарий в стадии STARTED
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(emptyDBMessage))); // заглушка вернёт ответ, только если сценарий находится в стадии STARTED

        stubFor(get(urlEqualTo(url))
                .withName("3")
                .inScenario(SCENARIO_NAME) // имя сценария
                .whenScenarioStateIs("Group added") // сценарий в стадии "Group added"
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(groupCountMessage))); // заглушка вернёт ответ, только если сценарий находится в стадии "Group added"

        ResponseEntity<String> emptyDB = client.getRequest(PORT, url); // отправляем GET запрос и получаем ответ от заглушки 2
        assertEquals(200, emptyDB.getStatusCode().value()); // сравниваем ожидаемый статус со статусом, отправленным заглушкой
        assertEquals(emptyDBMessage, emptyDB.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой

        ResponseEntity<String> responseEntity = client.postGroup(PORT, url, group); // отправляем POST запрос и получаем ответ от заглушки 1
        assertEquals(200, responseEntity.getStatusCode().value()); // сравниваем ожидаемый статус со статусом, отправленным заглушкой
        assertEquals(groupAddedMessage, responseEntity.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой

        ResponseEntity<String> notEmptyDB = client.getRequest(PORT, url); // отправляем GET запрос и получаем ответ от заглушки 3
        assertEquals(200, notEmptyDB.getStatusCode().value()); // сравниваем ожидаемый статус со статусом, отправленным заглушкой
        assertEquals(groupCountMessage, notEmptyDB.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }
}
