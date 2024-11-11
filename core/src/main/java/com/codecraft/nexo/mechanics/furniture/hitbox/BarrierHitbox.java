package com.codecraft.nexo.mechanics.furniture.hitbox;

import com.codecraft.nexo.mechanics.furniture.BlockLocation;
import org.bukkit.Location;

public class BarrierHitbox extends BlockLocation {

    public BarrierHitbox from(Object hitboxObject) {
        if (hitboxObject instanceof String string) {
            return new BarrierHitbox(string);
        } else return new BarrierHitbox("0,0,0");
    }

    public BarrierHitbox(String hitboxString) {
        super(hitboxString);
    }

    public BarrierHitbox(Location location) {
        super(location);
    }

}
