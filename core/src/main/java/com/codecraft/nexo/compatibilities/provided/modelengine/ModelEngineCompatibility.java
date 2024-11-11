package com.codecraft.nexo.compatibilities.provided.modelengine;

import com.codecraft.nexo.compatibilities.CompatibilityProvider;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.utils.PluginUtils;
import com.codecraft.nexo.utils.logs.Logs;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.events.ModelRegistrationEvent;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import org.bukkit.event.EventHandler;

import java.util.concurrent.CompletableFuture;

public class ModelEngineCompatibility extends CompatibilityProvider<ModelEngineAPI> {

    private static CompletableFuture<Void> modelEngineFuture;

    static {
        modelEngineFuture = new CompletableFuture<>();
        if (!PluginUtils.isEnabled("ModelEngine") || !Settings.PACK_IMPORT_MODEL_ENGINE.toBool())
            modelEngineFuture.complete(null);
    }

    public static CompletableFuture<Void> modelEngineFuture() {
        if (modelEngineFuture == null) modelEngineFuture = new CompletableFuture<>();
        if (!PluginUtils.isEnabled("ModelEngine") || !Settings.PACK_IMPORT_MODEL_ENGINE.toBool())
            modelEngineFuture.complete(null);

        return modelEngineFuture;
    }

    @EventHandler
    public void onMegReload(ModelRegistrationEvent event) {
        if (event.getPhase() == ModelGenerator.Phase.PRE_IMPORT) {
            Logs.logInfo("Awaiting ModelEngine ResourcePack...");
            modelEngineFuture = new CompletableFuture<>();
        } else if (event.getPhase() == ModelGenerator.Phase.FINISHED) {
            modelEngineFuture.complete(null);
            Logs.logInfo("ModelEngine ResourcePack is ready.");
        }
    }
}
