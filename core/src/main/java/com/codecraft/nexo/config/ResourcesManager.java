package com.codecraft.nexo.config;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.utils.NexoYaml;
import com.codecraft.nexo.utils.ReflectionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourcesManager {

    final JavaPlugin plugin;

    public ResourcesManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Entry<File, YamlConfiguration> settings;
    private Entry<File, YamlConfiguration> mechanics;

    public YamlConfiguration getSettings() {
        return getSettingsEntry().getValue();
    }

    public Entry<File, YamlConfiguration> getSettingsEntry() {
        return settings != null ? settings : (settings = getEntry("settings.yml"));
    }

    public YamlConfiguration getMechanics() {
        return getMechanicsEntry().getValue();
    }

    public Entry<File, YamlConfiguration> getMechanicsEntry() {
        return mechanics != null ? mechanics : (mechanics = getEntry("mechanics.yml"));
    }

    public Entry<File, YamlConfiguration> getEntry(String fileName) {
        File file = extractConfiguration(fileName);
        return new AbstractMap.SimpleEntry<>(file, NexoYaml.loadConfiguration(file));
    }

    public File extractConfiguration(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists())
            this.plugin.saveResource(fileName, false);
        return file;
    }

    public void extractConfigsInFolder(String folder, String fileExtension) {
        ZipInputStream zip = browse();
        try {
            extractConfigsInFolder(zip, folder, fileExtension);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void extractConfigsInFolder(ZipInputStream zip, String folder, String fileExtension) throws IOException {
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            extractFileAccordingToExtension(entry, folder, fileExtension);
            entry = zip.getNextEntry();
        }
        zip.closeEntry();
        zip.close();
    }

    public void extractFileIfTrue(ZipEntry entry, boolean isSuitable) {
        if (entry.isDirectory()) return;
        if (isSuitable) plugin.saveResource(entry.getName(), true);
        else if (entry.getName().startsWith("pack/textures/models/armor/")) {
            if (NexoPlugin.get().getDataFolder().toPath().resolve(entry.getName()).toFile().exists()) return;
            plugin.saveResource(entry.getName(), false);
        }
    }

    private void extractFileAccordingToExtension(ZipEntry entry, String folder, String fileExtension) {
        boolean isSuitable = entry.getName().startsWith(folder + "/") && entry.getName().endsWith("." + fileExtension);
        extractFileIfTrue(entry, isSuitable);
    }

    public static ZipInputStream browse() {
        return ReflectionUtils.getJarStream(NexoPlugin.class).orElseThrow(() -> {
            Message.ZIP_BROWSE_ERROR.log();
            return new RuntimeException("NexoResources not found!");
        });
    }

}
