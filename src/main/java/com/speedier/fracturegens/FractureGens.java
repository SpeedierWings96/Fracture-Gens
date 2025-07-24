package com.speedier.fracturegens;

import com.speedier.fracturegens.commands.FractureGensCommand;
import com.speedier.fracturegens.listeners.BlockInteractionListener;
import com.speedier.fracturegens.listeners.GUIListener;
import com.speedier.fracturegens.managers.GeneratorManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FractureGens extends JavaPlugin {
    private GeneratorManager generatorManager;
    private Set<UUID> playersInCreationMode = new HashSet<>();
    private Map<UUID, Object> openGUIs = new HashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.generatorManager = new GeneratorManager(this);
        
        registerListeners();
        registerCommands();
        
        getLogger().info("FractureGens v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Loaded " + generatorManager.getAllGenerators().size() + " generators");
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Performance settings:");
            getLogger().info("- Max generators per chunk: " + getConfig().getInt("performance.max-generators-per-chunk"));
            getLogger().info("- Max generators per player: " + getConfig().getInt("performance.max-generators-per-player"));
            getLogger().info("- Generator tick interval: " + getConfig().getInt("performance.generator-tick-interval"));
            getLogger().info("- Async processing: " + getConfig().getBoolean("performance.async-processing"));
        }, 20L);
    }
    
    @Override
    public void onDisable() {
        if (generatorManager != null) {
            generatorManager.shutdown();
        }
        
        getLogger().info("FractureGens has been disabled!");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(this, generatorManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }
    
    private void registerCommands() {
        FractureGensCommand commandExecutor = new FractureGensCommand(this, generatorManager);
        getCommand("fracturegens").setExecutor(commandExecutor);
        getCommand("fracturegens").setTabCompleter(commandExecutor);
    }
    
    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }
    
    public boolean isPlayerInCreationMode(UUID playerId) {
        return playersInCreationMode.contains(playerId);
    }
    
    public void setPlayerCreationMode(UUID playerId, boolean enabled) {
        if (enabled) {
            playersInCreationMode.add(playerId);
        } else {
            playersInCreationMode.remove(playerId);
        }
    }
    
    public boolean togglePlayerCreationMode(UUID playerId) {
        boolean newState = !isPlayerInCreationMode(playerId);
        setPlayerCreationMode(playerId, newState);
        return newState;
    }
    
    public void setOpenGUI(UUID playerId, Object gui) {
        openGUIs.put(playerId, gui);
    }
    
    public Object getOpenGUI(UUID playerId) {
        return openGUIs.get(playerId);
    }
    
    public void removeOpenGUI(UUID playerId) {
        openGUIs.remove(playerId);
    }
}
