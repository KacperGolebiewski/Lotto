package pl.lotto.feature;


import com.fasterxml.jackson.databind.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.*;
import pl.lotto.AdjustableClockIntegration;
import pl.lotto.numbergenerator.WinningNumbersGeneratorFacade;
import pl.lotto.numberreceiver.dto.*;
import pl.lotto.resultchecker.ResultCheckerFacade;
import pl.lotto.resultchecker.dto.PlayersDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {LottoApplication.class, IntegrationConfiguration.class})
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration")
public class LottoUserWinnerIntegrationTest {

    @Autowired
    WinningNumbersGeneratorFacade winningNumbersGeneratorFacade;

    @Autowired
    ResultCheckerFacade resultCheckerFacade;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AdjustableClockIntegration clock;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @DynamicPropertySource
    private static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void happy_path_when_user_played_and_after_few_days_want_to_know_if_won() throws Exception {
        //step 1: user send POST to /inputNumbers with numbers (1,2,3,4,5,6)
        //Given
        //When
        ResultActions perform = mockMvc.perform(post("/inputNumbers")
                .content("""
                        {
                        "inputNumbers" : [1,2,3,4,5,6]
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        //Then
        MvcResult mvcResult = perform.andExpect(status().isOk()).andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        NumberReceiverResponseDto result = objectMapper.readValue(json, NumberReceiverResponseDto.class);

        LocalDateTime ticketDrawDate = LocalDateTime.of(2022, 11, 19, 12, 0, 0);
        assertAll(
                () -> assertThat(result.message()).isEqualTo("SUCCESS"),
                () -> assertThat(result.ticketDto().getDrawDate()).isEqualTo(ticketDrawDate));

        //step 2: winning numbers should have been generated
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1L))
                .until(() -> {
                    try {
                        return !winningNumbersGeneratorFacade.retrieveWinningNumberByDate(ticketDrawDate).getWinningNumbers().isEmpty();
                    } catch (RuntimeException e) {
                        return false;
                    }
                });


        //step 3: clock is adjusted to 1 minute after draw and results should be generated
        clock.plusDaysAndMinutes(3,1);
        resultCheckerFacade.generateWinners();
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1L))
                .until(() -> {
                    try {
                        return !resultCheckerFacade.generateResult(result.ticketDto().getHash()).getHash().isEmpty();
                    } catch (RuntimeException e) {
                        return false;
                    }
                });
    }
}



