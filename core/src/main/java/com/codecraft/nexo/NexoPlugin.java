package com.codecraft.nexo;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.commands.CommandsManager;
import com.codecraft.nexo.compatibilities.CompatibilitiesManager;
import com.codecraft.nexo.config.*;
import com.codecraft.nexo.font.FontManager;
import com.codecraft.nexo.items.ItemUpdater;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.pack.PackGenerator;
import com.codecraft.nexo.pack.server.EmptyServer;
import com.codecraft.nexo.pack.server.NexoPackServer;
import com.codecraft.nexo.recipes.RecipesManager;
import com.codecraft.nexo.utils.LU;
import com.codecraft.nexo.utils.NoticeUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.actions.ClickActionManager;
import com.codecraft.nexo.utils.armorequipevent.ArmorEquipEvent;
import com.codecraft.nexo.utils.breaker.BreakerManager;
import com.codecraft.nexo.utils.breaker.LegacyBreakerManager;
import com.codecraft.nexo.utils.breaker.ModernBreakerManager;
import com.codecraft.nexo.utils.customarmor.CustomArmorListener;
import com.codecraft.nexo.utils.inventories.InvManager;
import com.jeff_media.customblockdata.CustomBlockData;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.th0rgal.protectionlib.ProtectionLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

public class NexoPlugin extends JavaPlugin {

    private static NexoPlugin nexo;
    private ConfigsManager configsManager;
    private ResourcesManager resourceManager;
    private BukkitAudiences audience;
    private FontManager fontManager;
    private SoundManager soundManager;
    private InvManager invManager;
    private PackGenerator packGenerator;
    @Nullable private NexoPackServer packServer;
    private ClickActionManager clickActionManager;
    private BreakerManager breakerManager;

    public NexoPlugin() {
        nexo = this;
    }

    public static NexoPlugin get() {
        return nexo;
    }

    @Nullable
    public static JarFile getJarFile() {
        try {
            return new JarFile(nexo.getFile());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true).skipReloadDatapacks(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        ProtectionLib.init(this);
        audience = BukkitAudiences.create(this);
        reloadConfigs();
        clickActionManager = new ClickActionManager(this);
        fontManager = new FontManager(configsManager);
        soundManager = new SoundManager(configsManager.getSounds());
        breakerManager = VersionUtil.atleast("1.20.5") ? new ModernBreakerManager(new ConcurrentHashMap<>())
                : new LegacyBreakerManager(new ConcurrentHashMap<>());
        ProtectionLib.setDebug(Settings.DEBUG.toBool());

        if (Settings.KEEP_UP_TO_DATE.toBool())
            new SettingsUpdater().handleSettingsUpdate();
        Bukkit.getPluginManager().registerEvents(new CustomArmorListener(), this);
        NMSHandlers.setupHandler();
        packGenerator = new PackGenerator();

        fontManager.registerEvents();
        Bukkit.getPluginManager().registerEvents(new ItemUpdater(), this);

        invManager = new InvManager();
        ArmorEquipEvent.registerListener(this);
        CustomBlockData.registerListener(this);

        new CommandsManager().loadCommands();

        packServer = NexoPackServer.initializeServer();
        packServer.start();

        postLoading();
        CompatibilitiesManager.enableNativeCompatibilities();
        if (VersionUtil.isCompiled()) NoticeUtils.compileNotice();
        if (VersionUtil.isLeaked()) NoticeUtils.leakNotice();
    }

    private void postLoading() {
        new Metrics(this, 5371);
        new LU().l();
        Bukkit.getScheduler().runTask(this, () -> {
            MechanicsManager.registerNativeMechanics();
            NexoItems.loadItems();
            RecipesManager.load(NexoPlugin.get());
            packGenerator.generatePack();
        });
    }

    @Override
    public void onDisable() {
        if (packServer != null) packServer.stop();
        HandlerList.unregisterAll(this);
        FurnitureFactory.unregisterEvolution();
        FurnitureFactory.removeAllFurniturePackets();

        CompatibilitiesManager.disableCompatibilities();
        CommandAPI.onDisable();
        Message.PLUGIN_UNLOADED.log();
    }

    public Path packPath() {
        return getDataFolder().toPath().resolve("pack");
    }

    public BukkitAudiences audience() {
        return audience;
    }

    public void reloadConfigs() {
        resourceManager = new ResourcesManager(this);
        configsManager = new ConfigsManager(this);
        configsManager.validatesConfig();
    }

    public ConfigsManager configsManager() {
        return configsManager;
    }
    public ResourcesManager resourceManager() {
        return resourceManager;
    }
    public void resourceManager(ResourcesManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public FontManager fontManager() {
        return fontManager;
    }

    public void fontManager(final FontManager fontManager) {
        this.fontManager.unregisterEvents();
        this.fontManager = fontManager;
        fontManager.registerEvents();
    }

    public SoundManager soundManager() {
        return soundManager;
    }

    public void soundManager(final SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public BreakerManager breakerManager() {
        return breakerManager;
    }

    public InvManager invManager() {
        return invManager;
    }

    public PackGenerator packGenerator() {
        return packGenerator;
    }

    public void packGenerator(PackGenerator packGenerator) {
        PackGenerator.stopPackGeneration();
        this.packGenerator = packGenerator;
    }

    public NexoPackServer packServer() {
        return packServer != null ? packServer : new EmptyServer();
    }
    public void packServer(@Nullable NexoPackServer server) {
        if (packServer != null) packServer.stop();
        packServer = server;
        if (packServer != null) packServer.start();
    }

    public ClickActionManager clickActionManager() {
        return clickActionManager;
    }
}
