package com.ronyt.hometask.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ronyt.hometask.model.enums.Hand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerDto implements Serializable {
    String playerID;
    Integer birthYear;
    Integer birthMonth;
    Integer birthDay;
    String birthCountry;
    String birthState;
    String birthCity;
    Integer deathYear;
    Integer deathMonth;
    Integer deathDay;
    String deathCountry;
    String deathState;
    String deathCity;
    String nameFirst;
    String nameLast;
    String nameGiven;
    Integer weight;
    Integer height;
    Hand bats;
    @JsonProperty("throws")
    Hand throwsHand;
    String debut;
    String finalGame;
    String retroID;
    String bbrefID;
}