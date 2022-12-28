package pl.lotto.resultchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.lotto.numbergenerator.WinningNumbersGeneratorFacade;
import pl.lotto.numbergenerator.dto.WinningNumbersDto;
import pl.lotto.numberreceiver.NumberReceiverFacade;
import pl.lotto.numberreceiver.dto.TicketDto;
import pl.lotto.resultchecker.dto.ResultDto;
import pl.lotto.resultchecker.dto.PlayersDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ResultCheckerFacadeTest {

    private final PlayerRepository playerRepository = new PlayerRepositoryTestImpl();

    WinningNumbersGeneratorFacade winningNumbersGeneratorFacade;
    NumberReceiverFacade numberReceiverFacade;

    @BeforeEach
    public void setUp() {
        winningNumbersGeneratorFacade = mock(WinningNumbersGeneratorFacade.class);
        numberReceiverFacade = mock(NumberReceiverFacade.class);
    }


    @Test
    public void it_should_generate_all_players_with_correct_message() {
        //given
        LocalDateTime drawDate = LocalDateTime.of(2022, 12, 17, 12, 0, 0);
        when(winningNumbersGeneratorFacade.generateWinningNumbers()).thenReturn(WinningNumbersDto.builder()
                .winningNumbers(Set.of(1, 2, 3, 4, 5, 6))
                .build());
        when(numberReceiverFacade.getAllTicketsByNextDrawDate()).thenReturn(
                List.of(TicketDto.builder()
                                .hash("001")
                                .numbers(Set.of(1, 2, 3, 4, 5, 6))
                                .drawDate(drawDate)
                                .build(),
                        TicketDto.builder()
                                .hash("002")
                                .numbers(Set.of(1, 2, 7, 8, 9, 10))
                                .drawDate(drawDate)
                                .build(),
                        TicketDto.builder()
                                .hash("003")
                                .numbers(Set.of(7, 8, 9, 10, 11, 12))
                                .drawDate(drawDate)
                                .build())
        );
        ResultCheckerFacade resultCheckerFacade = new ResultCheckerConfiguration().createForTest(winningNumbersGeneratorFacade, numberReceiverFacade, playerRepository);
        //when
        PlayersDto playersDto = resultCheckerFacade.generateWinners();
        //then
        List<ResultDto> results = playersDto.getResults();
        ResultDto resultDto = ResultDto.builder()
                .hash("001")
                .numbers(Set.of(1, 2, 3, 4, 5, 6))
                .hitNumbers(Set.of(1, 2, 3, 4, 5, 6))
                .drawDate(drawDate)
                .isWinner(true)
                .build();
        ResultDto resultDto1 = ResultDto.builder()
                .hash("001")
                .numbers(Set.of(1, 2, 3, 4, 5, 6))
                .hitNumbers(Set.of(1, 2, 3, 4, 5, 6))
                .drawDate(drawDate)
                .isWinner(true)
                .build();
        ResultDto resultDto2 = ResultDto.builder()
                .hash("001")
                .numbers(Set.of(1, 2, 3, 4, 5, 6))
                .hitNumbers(Set.of(1, 2, 3, 4, 5, 6))
                .drawDate(drawDate)
                .isWinner(true)
                .build();
        assertThat(results).contains(resultDto,resultDto1, resultDto2);
        String message = playersDto.getMessage();
        assertThat(message).isEqualTo("Winners succeeded to retrieve");

    }

    @Test
    public void it_should_generate_fail_message_when_winningNumbers_equal_null() {
        //given
        when(winningNumbersGeneratorFacade.generateWinningNumbers()).thenReturn(WinningNumbersDto.builder()
                .winningNumbers(null)
                .build());
        ResultCheckerFacade resultCheckerFacade = new ResultCheckerConfiguration().createForTest(winningNumbersGeneratorFacade, numberReceiverFacade, playerRepository);
        //when
        PlayersDto playersDto = resultCheckerFacade.generateWinners();
        //then
        String message = playersDto.getMessage();
        assertThat(message).isEqualTo("Winners failed to retrieve");

    }

    @Test
    public void it_should_generate_fail_message_when_winningNumbers_is_empty() {
        //given
        when(winningNumbersGeneratorFacade.generateWinningNumbers()).thenReturn(WinningNumbersDto.builder()
                .winningNumbers(Set.of())
                .build());
        ResultCheckerFacade resultCheckerFacade = new ResultCheckerConfiguration().createForTest(winningNumbersGeneratorFacade, numberReceiverFacade, playerRepository);
        //when
        PlayersDto playersDto = resultCheckerFacade.generateWinners();
        //then
        String message = playersDto.getMessage();
        assertThat(message).isEqualTo("Winners failed to retrieve");

    }

    @Test
    public void it_should_generate_result_with_correct_credentials() {
        //given
        LocalDateTime drawDate = LocalDateTime.of(2022, 12, 17, 12, 0, 0);
        when(winningNumbersGeneratorFacade.generateWinningNumbers()).thenReturn(WinningNumbersDto.builder()
                .winningNumbers(Set.of(1, 2, 3, 4, 5, 6))
                .build());
        String hash = "001";
        when(numberReceiverFacade.getAllTicketsByNextDrawDate()).thenReturn(
                List.of(TicketDto.builder()
                                .hash(hash)
                                .numbers(Set.of(7, 8, 9, 10, 11, 12))
                                .drawDate(drawDate)
                                .build(),
                        TicketDto.builder()
                                .hash("002")
                                .numbers(Set.of(7, 8, 9, 10, 11, 13))
                                .drawDate(drawDate)
                                .build(),
                        TicketDto.builder()
                                .hash("003")
                                .numbers(Set.of(7, 8, 9, 10, 11, 14))
                                .drawDate(drawDate)
                                .build())
        );
        ResultCheckerFacade resultCheckerFacade = new ResultCheckerConfiguration().createForTest(winningNumbersGeneratorFacade, numberReceiverFacade, playerRepository);
        resultCheckerFacade.generateWinners();
        //when

        ResultDto resultDto = resultCheckerFacade.generateResult(hash);
        //then
        ResultDto expectedResult = ResultDto.builder()
                .hash(hash)
                .numbers(Set.of(7, 8, 9, 10, 11, 12))
                .hitNumbers(Set.of())
                .drawDate(drawDate)
                .isWinner(false)
                .build();
        assertThat(resultDto).isEqualTo(expectedResult);
    }
}