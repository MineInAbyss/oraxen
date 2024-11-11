package com.codecraft.nexo.nms;


import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class NMSHandlers {

    private static final VersionUtil.NMSVersion[] SUPPORTED_VERSION = VersionUtil.NMSVersion.values();
    private static NMSHandler handler;
    private static String version;

    @NotNull
    public static NMSHandler getHandler() {
        return Optional.ofNullable(handler).orElse(setupHandler());
    }

    public static String getVersion() {
        return version;
    }

    public static void resetHandler() {
        handler = null;
        setupHandler();
    }

    public static NMSHandler setupHandler() {
        if (handler == null) for (VersionUtil.NMSVersion selectedVersion : SUPPORTED_VERSION) {
            if (!VersionUtil.NMSVersion.matchesServer(selectedVersion)) continue;

            version = selectedVersion.name();
            try {
                handler = (NMSHandler) Class.forName("io.th0rgal.nexo.nms." + version + ".NMSHandler").getConstructor().newInstance();
                Logs.logSuccess("Version " + version + " has been detected.");
                Logs.logInfo("nexo will use the NMSHandler for this version.", true);
                return handler;
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                if (Settings.DEBUG.toBool()) e.printStackTrace();
                Logs.logWarning("Nexo does not support this version of Minecraft (" + version + ") yet.");
                Logs.logWarning("NMS features will be disabled...", true);
                handler = new EmptyNMSHandler();
            }
        }

        return handler;
    }
    public static boolean isTripwireUpdatesDisabled() {
        return handler != null && handler.tripwireUpdatesDisabled();
    }

    public static boolean isNoteblockUpdatesDisabled() {
        return handler != null && handler.noteblockUpdatesDisabled();
    }
}
