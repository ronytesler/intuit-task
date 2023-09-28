package com.ronyt.hometask.controller;

import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.service.PlayerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    @Value("${cache.ttl.minutes:5}")
    private int cacheTTLMinutes;


    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public Flux<PlayerDto> getPlayers() {
        return playerService.getAllPlayers();
    }


    @Cacheable(value = "${cache.name:playersCache}", key = "#playerID")
    @GetMapping("/{playerID}")
    public Mono<PlayerDto> getPlayer(@PathVariable String playerID) {
        return playerService.getPlayer(playerID)
                .cache(Duration.ofMinutes(cacheTTLMinutes));
    }

//    //    @GetMapping(params = {"page"})
////    public Page<Player> getPlayersWithPagination(@RequestParam int page, @RequestParam(required = false, defaultValue = "${page.defaultSize:1}") int size) {
//    @GetMapping(path = "/t", params = "page")
//    public Page<Player> getPlayersWithPagination(@RequestParam int page, @RequestParam(required = false, defaultValue = "${page.defaultSize:1}") int size) {
////        return playerService.getAllPlayers(p);
//        return null;
//    }
}
