package com.ronyt.hometask.service;

import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.model.entity.Player;
import reactor.core.publisher.Mono;

// implemented by a class that can handle a player that needs to be saved to the db

public interface PlayerMessageHandler {
    Mono<PlayerDto> handlePlayerMessage(PlayerDto playerDto);
}
