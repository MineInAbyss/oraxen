package com.codecraft.nexo.utils.customarmor;

import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;

public enum CustomArmorType {
    NONE, TRIMS, COMPONENT;

    public static CustomArmorType getSetting() {
        return fromString(Settings.CUSTOM_ARMOR_TYPE.toString());
    }

    public static CustomArmorType fromString(String type) {
        try {
            CustomArmorType customArmorType = CustomArmorType.valueOf(type.toUpperCase());
            if (!VersionUtil.atleast("1.21.2") && customArmorType == COMPONENT) {
                Logs.logError("Component based custom armor is only supported in 1.21.2 and above.");
                throw new IllegalArgumentException();
            } else if (!VersionUtil.atleast("1.20") && customArmorType == CustomArmorType.TRIMS) {
                Logs.logError("Trim based custom armor is only supported in 1.20 and above.");
                throw new IllegalArgumentException();
            }
            return customArmorType;
        } catch (IllegalArgumentException e) {
            Logs.logError("Invalid custom armor type: " + type);
            Logs.logError("Defaulting to NONE.");
            return NONE;
        }
    }
}
