package com.codecraft.nexo.mechanics.misc.custom.fields;

import com.codecraft.nexo.mechanics.misc.custom.listeners.*;
import com.codecraft.nexo.utils.actions.ClickAction;

public enum CustomEventType {

    BREAK(BreakListener::new),
    CLICK(ClickListener::new),
    INV_CLICK(InvClickListener::new),
    DROP(DropListener::new),
    PICKUP(PickupListener::new),
    EQUIP(EquipListener::new),
    UNEQUIP(UnequipListener::new),
    DEATH(DeathListener::new)
    ;

    public final CustomListenerConstructor constructor;

    CustomEventType(CustomListenerConstructor constructor) {
        this.constructor = constructor;
    }

    @FunctionalInterface
    interface CustomListenerConstructor {
        CustomListener create(String itemID, long cooldown, CustomEvent event, ClickAction clickAction);
    }

}
