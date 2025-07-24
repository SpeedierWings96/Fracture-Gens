package com.speedier.fracturegens;

import org.bukkit.plugin.java.JavaPlugin;

public class FractureGens extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Fracture Gens plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Fracture Gens plugin has been disabled!");
    }
}
