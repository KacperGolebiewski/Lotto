package pl.lotto.resultchecker;

import pl.lotto.numberreceiver.dto.TicketDto;
import pl.lotto.resultchecker.dto.ResultTicketDto;
import pl.lotto.resultchecker.dto.WinnersDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ResultCheckerFacade {
    public ResultTicketDto generateResult(TicketDto ticketDto, Set<Integer> winningNumbers) {
        Set<Integer> matchingNumbers = new HashSet<>();
        setMatchingNumbers(ticketDto.getNumbers(), winningNumbers, matchingNumbers);

        ResultTicketDto resultTicket = ResultTicketDto.builder()
                .hash(ticketDto.getHash())
                .numbers(matchingNumbers)
                .drawDate(ticketDto.getDrawDate())
                .build();

        ArrayList<ResultTicketDto> winningTickets = new ArrayList<>();
        winningTickets.add(resultTicket);
        WinnersDto winners = WinnersDto.builder()
                .winners(winningTickets)
                .build();

        return resultTicket;
    }

    private static void setMatchingNumbers(Set<Integer> inputNumbers, Set<Integer> winningNumbers, Set<Integer> matchingNumbers) {
        for (Integer number : winningNumbers) {
            if(inputNumbers.contains(number)) {
                matchingNumbers.add(number);
            }
        }
    }
}
