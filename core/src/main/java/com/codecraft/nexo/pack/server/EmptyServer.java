package com.codecraft.nexo.pack.server;

import org.bukkit.entity.Player;

public class EmptyServer implements NexoPackServer {
    @Override
    public void sendPack(Player player) {

    }

    @Override
    public String packUrl() {
        return "";
    }
}
