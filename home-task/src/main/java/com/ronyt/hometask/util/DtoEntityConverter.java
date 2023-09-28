package com.ronyt.hometask.util;

import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.model.entity.Player;
import org.springframework.beans.BeanUtils;

public class DtoEntityConverter {

    public static PlayerDto playerEntityToDto(Player player) {
        PlayerDto playerDto = new PlayerDto();
        BeanUtils.copyProperties(player, playerDto);
        return playerDto;
    }

    public static Player playerDtoToEntity(PlayerDto playerDto) {
        Player player = new Player();
        BeanUtils.copyProperties(playerDto, player);
        return player;
    }
}
