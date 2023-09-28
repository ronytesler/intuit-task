package com.ronyt.hometask;

import com.ronyt.hometask.controller.PlayerController;
import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.service.PlayerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

@RunWith(SpringRunner.class)
@WebFluxTest(PlayerController.class)
class ApplicationTests {

    private final WebTestClient webTestClient;
    private final PlayerService playerService;

    ApplicationTests(WebTestClient webTestClient, PlayerService playerService) {
        this.webTestClient = webTestClient;
        this.playerService = playerService;
    }

    @Test
    public void getAllPlayersTest() {
        PlayerDto player1 = PlayerDto.builder().playerID("appp02").birthYear(1990).build();
        PlayerDto player2 = PlayerDto.builder().playerID("appp03").nameFirst("first name").build();
        Flux<PlayerDto> playerDtoFlux = Flux.just(player1, player2);
        when(playerService.getAllPlayers()).thenReturn(playerDtoFlux);
        Flux<PlayerDto> responseBody = webTestClient.get().uri("/api/players")
                .exchange()
                .expectStatus().isOk()
                .returnResult(PlayerDto.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .expectNext(player1)
                .expectNext(player2)
                .verifyComplete();
    }

    @Test
    public void getPlayerTest() {
        String playerId = "aapppo01";
        PlayerDto player = PlayerDto.builder().playerID(playerId).build();
        Mono<PlayerDto> playerDtoMono = Mono.just(player);
        when(playerService.getPlayer(any())).thenReturn(playerDtoMono);
        Flux<PlayerDto> responseBody = webTestClient.get().uri("/api/player/" + playerId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PlayerDto.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .expectNext(player)
                .verifyComplete();
    }

}
