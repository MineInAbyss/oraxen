package com.codecraft.nexo.mechanics.furniture.hitbox;

import com.codecraft.nexo.utils.ParseUtils;
import com.codecraft.nexo.utils.VectorUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.util.Vector;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

public class InteractionHitbox {
    private final Vector offset;
    private final float width;
    private final float height;

    public static InteractionHitbox DEFAULT = new InteractionHitbox(new Vector(), 1.0, 1.0);

    public InteractionHitbox(Object hitboxObject) {
        InteractionHitbox hitbox;
        if (hitboxObject instanceof String string) hitbox = new InteractionHitbox(string);
        else hitbox = DEFAULT;

        this.offset = hitbox.offset;
        this.width = hitbox.width;
        this.height = hitbox.height;
    }

    public InteractionHitbox(String hitboxString) {
        List<String> split = new ArrayList<>(List.of(hitboxString.split(" ", 2)));
        // Add vector offset
        if (split.size() == 1) split.add("1,1");

        this.offset = VectorUtils.getVectorFromString(split.getFirst(), 0);
        this.width = ParseUtils.parseFloat(StringUtils.substringBefore(split.getLast(), ","), 1.0f);
        this.height = ParseUtils.parseFloat(StringUtils.substringAfter(split.getLast(), ","), 1.0f);
    }

    public InteractionHitbox(Vector offset, double width, double height) {
        this.offset = offset;
        this.width = (float) width;
        this.height = (float) height;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public Vector offset() {
        return offset;
    }

    /**
     * Offset rotated around the baseEntity's yaw
     * @param angle Yaw of baseEntity
     * @return Rotated offset vector
     */
    public Vector offset(float angle) {
        if (angle < 0) angle += 360;  // Ensure yaw is positive
        double radians = Math.toRadians(angle);

        // Get the coordinates relative to the local y-axis
        double x = offset.getX() * Math.cos(radians) - (-offset.getZ()) * Math.sin(radians);
        double z = offset.getX() * Math.sin(radians) + (-offset.getZ()) * Math.cos(radians);
        double y = offset.getY();

        return new Vector(x, y, z);
    }
}
