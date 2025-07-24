package com.speedier.fracturegens.managers;

import com.speedier.fracturegens.FractureGens;
import com.speedier.fracturegens.models.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GeneratorManager {
    private final FractureGens plugin;
    private final Map<UUID, Generator> generators;
    private final Map<String, Set<UUID>> locationIndex;
    private final Map<UUID, Set<UUID>> ownerIndex;
    private File dataFile;
    private BukkitRunnable generatorTask;
    
    public GeneratorManager(FractureGens plugin) {
        this.plugin = plugin;
        this.generators = new ConcurrentHashMap<>();
        this.locationIndex = new ConcurrentHashMap<>();
        this.ownerIndex = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "generators.yml");
        
        loadGenerators();
        startGeneratorTask();
    }
    
    public boolean createGenerator(Location location, UUID owner) {
        if (getGeneratorAt(location) != null) {
            return false;
        }
        
        int maxPerPlayer = plugin.getConfig().getInt("performance.max-generators-per-player", 50);
        if (getGeneratorsByOwner(owner).size() >= maxPerPlayer) {
            return false;
        }
        
        int maxPerChunk = plugin.getConfig().getInt("performance.max-generators-per-chunk", 10);
        if (getGeneratorsInChunk(location).size() >= maxPerChunk) {
            return false;
        }
        
        Generator generator = new Generator(location, owner);
        generators.put(generator.getId(), generator);
        
        String locationKey = getLocationKey(location);
        locationIndex.computeIfAbsent(locationKey, k -> ConcurrentHashMap.newKeySet()).add(generator.getId());
        ownerIndex.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(generator.getId());
        
        saveGenerators();
        return true;
    }
    
    public boolean removeGenerator(UUID generatorId) {
        Generator generator = generators.remove(generatorId);
        if (generator == null) {
            return false;
        }
        
        String locationKey = getLocationKey(generator.getLocation());
        Set<UUID> locationGens = locationIndex.get(locationKey);
        if (locationGens != null) {
            locationGens.remove(generatorId);
            if (locationGens.isEmpty()) {
                locationIndex.remove(locationKey);
            }
        }
        
        Set<UUID> ownerGens = ownerIndex.get(generator.getOwner());
        if (ownerGens != null) {
            ownerGens.remove(generatorId);
            if (ownerGens.isEmpty()) {
                ownerIndex.remove(generator.getOwner());
            }
        }
        
        saveGenerators();
        return true;
    }
    
    public Generator getGeneratorAt(Location location) {
        String locationKey = getLocationKey(location);
        Set<UUID> locationGens = locationIndex.get(locationKey);
        if (locationGens == null || locationGens.isEmpty()) {
            return null;
        }
        
        return generators.get(locationGens.iterator().next());
    }
    
    public List<Generator> getGeneratorsByOwner(UUID owner) {
        Set<UUID> ownerGens = ownerIndex.get(owner);
        if (ownerGens == null) {
            return new ArrayList<>();
        }
        
        return ownerGens.stream()
                .map(generators::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public List<Generator> getGeneratorsInChunk(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        
        return generators.values().stream()
                .filter(gen -> {
                    Location genLoc = gen.getLocation();
                    return genLoc.getWorld().equals(location.getWorld()) &&
                           (genLoc.getBlockX() >> 4) == chunkX &&
                           (genLoc.getBlockZ() >> 4) == chunkZ;
                })
                .collect(Collectors.toList());
    }
    
    public Generator getGenerator(UUID id) {
        return generators.get(id);
    }
    
    public Collection<Generator> getAllGenerators() {
        return new ArrayList<>(generators.values());
    }
    
    private void startGeneratorTask() {
        int interval = plugin.getConfig().getInt("performance.generator-tick-interval", 20);
        boolean asyncProcessing = plugin.getConfig().getBoolean("performance.async-processing", true);
        
        generatorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (asyncProcessing) {
                    processGeneratorsAsync();
                } else {
                    processGenerators();
                }
            }
        };
        
        generatorTask.runTaskTimer(plugin, interval, interval);
    }
    
    private void processGenerators() {
        int maxItemsPerTick = plugin.getConfig().getInt("performance.max-items-per-tick", 100);
        int itemsSpawned = 0;
        
        for (Generator generator : generators.values()) {
            if (itemsSpawned >= maxItemsPerTick) {
                break;
            }
            
            if (generator.shouldSpawn() && generator.isValidLocation()) {
                spawnItem(generator);
                generator.updateLastSpawn();
                itemsSpawned++;
            }
        }
    }
    
    private void processGeneratorsAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Generator> readyGenerators = generators.values().stream()
                    .filter(gen -> gen.shouldSpawn() && gen.isValidLocation())
                    .limit(plugin.getConfig().getInt("performance.max-items-per-tick", 100))
                    .collect(Collectors.toList());
            
            if (!readyGenerators.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Generator generator : readyGenerators) {
                        spawnItem(generator);
                        generator.updateLastSpawn();
                    }
                });
            }
        });
    }
    
    private void spawnItem(Generator generator) {
        Location spawnLoc = generator.getSpawnLocation();
        ItemStack item = generator.getRandomItem();
        
        if (spawnLoc.getWorld() != null) {
            Item droppedItem = spawnLoc.getWorld().dropItem(spawnLoc, item);
            droppedItem.setVelocity(new Vector(0, 0.1, 0));
            droppedItem.setPickupDelay(20);
        }
    }
    
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + ":" + 
               location.getBlockX() + ":" + 
               location.getBlockY() + ":" + 
               location.getBlockZ();
    }
    
    public void saveGenerators() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                FileConfiguration config = new YamlConfiguration();
                
                for (Generator generator : generators.values()) {
                    String path = "generators." + generator.getId().toString();
                    config.set(path + ".location.world", generator.getLocation().getWorld().getName());
                    config.set(path + ".location.x", generator.getLocation().getBlockX());
                    config.set(path + ".location.y", generator.getLocation().getBlockY());
                    config.set(path + ".location.z", generator.getLocation().getBlockZ());
                    config.set(path + ".owner", generator.getOwner().toString());
                    config.set(path + ".spawn-rate", generator.getSpawnRate());
                    config.set(path + ".direction", generator.getDirection().name());
                    config.set(path + ".enabled", generator.isEnabled());
                    
                    List<Map<String, Object>> itemMaps = new ArrayList<>();
                    for (ItemStack item : generator.getItems()) {
                        itemMaps.add(item.serialize());
                    }
                    config.set(path + ".items", itemMaps);
                }
                
                config.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save generators: " + e.getMessage());
            }
        });
    }
    
    private void loadGenerators() {
        if (!dataFile.exists()) {
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            ConfigurationSection generatorsSection = config.getConfigurationSection("generators");
            
            if (generatorsSection == null) {
                return;
            }
            
            for (String idString : generatorsSection.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(idString);
                    ConfigurationSection genSection = generatorsSection.getConfigurationSection(idString);
                    
                    String worldName = genSection.getString("location.world");
                    int x = genSection.getInt("location.x");
                    int y = genSection.getInt("location.y");
                    int z = genSection.getInt("location.z");
                    
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    if (location.getWorld() == null) {
                        continue;
                    }
                    
                    UUID owner = UUID.fromString(genSection.getString("owner"));
                    int spawnRate = genSection.getInt("spawn-rate", 60);
                    Generator.SpawnDirection direction = Generator.SpawnDirection.valueOf(
                            genSection.getString("direction", "TOP"));
                    boolean enabled = genSection.getBoolean("enabled", true);
                    
                    List<ItemStack> items = new ArrayList<>();
                    List<?> itemMaps = genSection.getList("items");
                    if (itemMaps != null) {
                        for (Object itemMap : itemMaps) {
                            if (itemMap instanceof Map) {
                                try {
                                    ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
                                    items.add(item);
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
                                }
                            }
                        }
                    }
                    
                    if (items.isEmpty()) {
                        items.add(new ItemStack(Material.COBBLESTONE, 1));
                    }
                    
                    Generator generator = new Generator(id, location, owner, items, spawnRate, direction, enabled);
                    generators.put(id, generator);
                    
                    String locationKey = getLocationKey(location);
                    locationIndex.computeIfAbsent(locationKey, k -> ConcurrentHashMap.newKeySet()).add(id);
                    ownerIndex.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(id);
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load generator " + idString + ": " + e.getMessage());
                }
            }
            
            plugin.getLogger().info("Loaded " + generators.size() + " generators");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load generators: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        if (generatorTask != null) {
            generatorTask.cancel();
        }
        saveGenerators();
    }
    
    public void reload() {
        if (generatorTask != null) {
            generatorTask.cancel();
        }
        startGeneratorTask();
    }
}
