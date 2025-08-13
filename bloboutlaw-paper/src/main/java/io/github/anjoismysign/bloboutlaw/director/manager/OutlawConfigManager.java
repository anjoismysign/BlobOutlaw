package io.github.anjoismysign.bloboutlaw.director.manager;

import io.github.anjoismysign.bloboutlaw.configuration.OutlawConfiguration;
import io.github.anjoismysign.bloboutlaw.director.OutlawManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class OutlawConfigManager extends OutlawManager {
    private OutlawConfiguration configuration;

    public OutlawConfigManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    @Override
    public void reload() {
        File pluginDataFolder = getPlugin().getDataFolder();

        getManagerDirector().detachAsset("config.yml", false, pluginDataFolder);

        File configurationFile = new File(pluginDataFolder, "config.yml");
        Constructor constructor = new Constructor(OutlawConfiguration.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(configurationFile)) {
            configuration = yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public OutlawConfiguration getConfiguration(){
        return configuration;
    }

    public Set<String> getSafeZones() {
        return Set.copyOf(configuration.getSafeZones());
    }
}