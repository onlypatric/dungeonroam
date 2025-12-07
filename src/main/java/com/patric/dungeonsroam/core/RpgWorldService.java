package com.patric.dungeonsroam.core;

import com.patric.dungeonsroam.config.ConfigService;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class RpgWorldService {
    private final ConfigService configService;
    private final Set<String> configuredWorlds = new HashSet<>();

    public RpgWorldService(ConfigService configService) {
        this.configService = configService;
        refresh();
    }

    public void refresh() {
        configuredWorlds.clear();
        configService.getConfig().getRpgWorlds()
                .forEach(world -> configuredWorlds.add(world.toLowerCase(Locale.ROOT)));
    }

    public boolean isRpgWorld(World world) {
        return world != null && configuredWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

    public boolean isRpgWorld(String worldName) {
        return worldName != null && configuredWorlds.contains(worldName.toLowerCase(Locale.ROOT));
    }
}
