package com.ronyt.hometask.repository;

import com.ronyt.hometask.model.entity.Player;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends ReactiveMongoRepository<Player, String> {
}
