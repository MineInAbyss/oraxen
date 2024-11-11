package com.codecraft.nexo.api.events.resourcepack;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creative.BuiltResourcePack;

public class NexoPackUploadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final BuiltResourcePack builtResourcePack;
    private final String url;

    public NexoPackUploadEvent(BuiltResourcePack builtResourcePack, String url) {
        this.builtResourcePack = builtResourcePack;
        this.url = url;
    }

    public String url() {
        return url;
    }

    public BuiltResourcePack builtResourcePack() {
        return builtResourcePack;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() { return getHandlerList(); }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
