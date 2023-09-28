package com.ronyt.hometask.service;

import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.repository.PlayerRepository;
import com.ronyt.hometask.util.DtoEntityConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PlayerService implements PlayerMessageHandler {
    private final PlayerRepository playerRepository;
    @Value("${cache.ttl.minutes:5}")
    private int cacheTTLMinutes;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Flux<PlayerDto> getAllPlayers() {
        return playerRepository.findAll().map(DtoEntityConverter::playerEntityToDto);
    }

    public Mono<PlayerDto> getPlayer(String id) {
        return playerRepository.findById(id)
                .map(DtoEntityConverter::playerEntityToDto);
    }

    // insert/update a player to/in the db and cache.
    @CachePut(value = "${cache.name:playersCache}", key = "#playerDto.playerID")
    public Mono<PlayerDto> handlePlayerMessage(PlayerDto playerDto) {
        Mono<PlayerDto> playerDtoMono = Mono.just(playerDto);
        playerDtoMono
                .map(DtoEntityConverter::playerDtoToEntity)
                .flatMap(playerRepository::save)
                .cache(Duration.ofMinutes(cacheTTLMinutes))
                .doOnError(throwable -> {
                    System.out.println("Error saving a player" + throwable);
                    throw new RuntimeException("could not save player " + playerDto.getPlayerID());
                })
                .subscribe();
        return playerDtoMono;
    }
}
