package ru.chameleon.wiremocktutorial.standalone;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;
import ru.chameleon.wiremocktutorial.models.Group;
import ru.chameleon.wiremocktutorial.models.Musician;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Для работы тестов необходимо запустить wireMockRun.bat (Windows) или wireMockRun.sh (Linux)
 * и закоментировать аннотацию @Ignore над объявлением класса StandaloneTests
 */
@Ignore
@RunWith(SpringRunner.class)
public class StandaloneTests {

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8888; // порт WireMock Standalone

    /**
     * Example 4
     * Создание заглушки (тело ответа лежит непосредственно в response)
     */
    @Test
    public void getHelloMetalStandaloneTest() throws IOException {
        String stubMapping = Files.readString(Path.of("src/test/resources/standalone/stub_1.json")); // сохраняем описание заглушки из файла в String
        client.postStubToWireMockStandalone(8888, "__admin/mappings", stubMapping); // отправляем в WireMock Standalone команду на создание заглушки

        String message = "Hello Metal!"; // ожидаемое сообщение
        String url = "/hello"; // url
        assertEquals(message, client.getHelloMetal(PORT, url)); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 6
     * Создание заглушки (тело ответа лежит в __files)
     */
    @Test
    public void addGroupRequestMatchingStandaloneTest() throws IOException {
        String stubMapping = Files.readString(Path.of("src/test/resources/standalone/stub_2.json")); // сохраняем описание заглушки из файла в String
        client.postStubToWireMockStandalone(8888, "__admin/mappings", stubMapping); // отправляем в WireMock Standalone команду на создание заглушки

        String message = "Group added successful!"; // ожидаемое сообщение
        String url_1 = "/group/ironmaiden"; // url
        Group group_1 = new Group("Iron Maiden", 1975, List.of("The Throoper", "Hallowed Be Thy Name")); // создаём объект

        ResponseEntity<String> responseEntity = client.postGroup(PORT, url_1, group_1); // получаем ответ

        assertEquals(message, responseEntity.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 7
     * Создание заглушки (тело ответа лежит в __files/musicians, маппинг заглушки - в mappings)
     */
    @Test
    public void getGroupByMusicianRequestMatchingStandaloneTest() throws IOException {
        Musician musician_1 = new Musician("Bruce Dickinson", "vocal"); // создаём музыканта 1
        Musician musician_2 = new Musician("Nikki Sixx", "bass guitar player"); // создаём музыканта 2
        String url_1 = "/musician"; // url
        Group group_1 = new Group(UUID.fromString("cb6e10b7-0371-4c7f-8cfa-364e1724a8b6"), "Iron Maiden", 1975, List.of("The Trooper", "Hallowed Be Thy Name")); // создаём группу 1
        Group group_2 = new Group(UUID.fromString("909fd360-8016-4b99-a0b1-2df9e862bdaa"), "Motley Crue", 1981, List.of("Looks That Kill", "Live Wire")); // создаём группу 2

        ResponseEntity<Group> ironMaiden = client.postMusicianAndReturnGroup(PORT, url_1, musician_1); // отправляем музыканта 1, получаем ответ
        ResponseEntity<Group> motleyCrue = client.postMusicianAndReturnGroup(PORT, url_1, musician_2); // отправляем музыканта 2, получаем ответ

        assertEquals(group_1, ironMaiden.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
        assertEquals(group_2, motleyCrue.getBody()); // сравниваем ожидаемое сообщение с сообщением, отправленным заглушкой
    }

    /**
     * Example 17
     * Запись заглушки с помощью клиента WireMock. По окончанию записи заглушка появится в resources/mappings
     */
    @Test
    public void recordingTest() {
        WireMock wireMockClient = new WireMock(8888); // конфигурирование клиента
        wireMockClient.startStubRecording("http://example.mocklab.io"); // запуск записи заглушки для сайта
        RestTemplate restTemplate = new RestTemplate(); // создание экземрляра RestTemplate
        restTemplate.getForEntity("http://localhost:8888/recordables/123", String.class); // отправка GET запроса по указанному url
        wireMockClient.stopStubRecording(); // остановка записи
    }

    /**
     * Example 21
     * Описание сценария применения заглушек с помощью JSON API
     */
    @Test
    public void scenarioStandaloneTest() throws IOException {
        String scenario_1 = Files.readString(Path.of("src/test/resources/standalone/scenario_1.json")); // сохраняем описание заглушки из файла в String
        String scenario_2 = Files.readString(Path.of("src/test/resources/standalone/scenario_2.json")); // сохраняем описание заглушки из файла в String
        String scenario_3 = Files.readString(Path.of("src/test/resources/standalone/scenario_3.json")); // сохраняем описание заглушки из файла в String
        client.postStubToWireMockStandalone(8888, "__admin/mappings", scenario_1); // отправляем в WireMock Standalone команду на создание заглушки
        client.postStubToWireMockStandalone(8888, "__admin/mappings", scenario_2); // отправляем в WireMock Standalone команду на создание заглушки
        client.postStubToWireMockStandalone(8888, "__admin/mappings", scenario_3); // отправляем в WireMock Standalone команду на создание заглушки

        String url = "/groups"; // url
        String emptyDBMessage = "No groups added"; // сообщение получаемое из БД, когда ни одна группа не добавлена
        String groupAddedMessage = "Group added"; // сообщение получаемое, когда группа добавлена в БД
        String groupCountMessage = "Count: 1 group"; // сообщение, получаемое из БД, когда группа добавлена

        Group group = new Group("Skid Row", 1986, List.of("18 And Life", "Youth Gone Wild")); // создаём объект

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

    /**
     * Сброс всех заглушек
     */
    @After
    public void reset() {
        client.resetAllWireMockStandaloneStubs(PORT);
    }
}
