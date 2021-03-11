package ru.chameleon.wiremocktutorial.junit4;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chameleon.wiremocktutorial.client.RestTemplateClient;
import ru.chameleon.wiremocktutorial.models.Musician;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Example 11
 * Для работы тестов необходимо запустить wireMockRun.bat (Windows) или wireMockRun.sh (Linux)
 * и закоментировать аннотацию @Ignore над объявлением класса StandaloneTests.
 * Тесты следует запускать поочереди для наглядности.
 */
@Ignore
@RunWith(SpringRunner.class)
public class NearMisses {

    private static final Logger log = LoggerFactory.getLogger(NearMisses.class);

    private RestTemplateClient client = new RestTemplateClient();

    private static final int PORT = 8888; // порт WireMock Standalone

    @BeforeClass
    public static void init() {
        WireMock.configureFor(PORT);
    }

    /**
     * Тест, который не обрабатывается ни одной заглушкой (не верно указан url)
     */
    @Test
    //@Test(expected = org.springframework.web.client.HttpClientErrorException.class)
    public void NotMatchedTest() {
        Musician musician = new Musician("Bruce Dickinson", "vocal"); // создаём музыканта
        String url = "/musicia"; // url указан не верно. Должно быть /musician
        client.postMusicianAndReturnGroup(PORT, url, musician); // отправляем музыканта
    }

    /**
     * Тест, позволяющий выявить несоответствие запроса и заглушек
     */
    @Test
    public void NearMissesTest() {
        String url = "/musicia"; // url, по которому был произведен не обработанный ни одной заглушкой запрос
        LoggedRequest request = findAll(postRequestedFor(urlEqualTo(url)))
                .stream()
                .findFirst()
                .orElse(null); // запрос, соответствующий этому url
        if (request != null) {
            List<NearMiss> nearMisses = WireMock.findNearMissesFor(request); // получаем список близжайших промохов
            nearMisses.forEach(v -> log.info("Diffs: " + v.getDiff())); // выводим в консоль различия
        }
    }

    @Test
    public void reset() {
        WireMock.resetAllRequests(); // очищаем журнал запросов
    }
}
