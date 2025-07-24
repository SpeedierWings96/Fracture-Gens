package com.speedier.fracturegens.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Generator {
    private final UUID id;
    private final Location location;
    private final UUID owner;
    private List<ItemStack> items;
    private int spawnRate;
    private SpawnDirection direction;
    private boolean enabled;
    private long lastSpawn;
    
    public enum SpawnDirection {
        TOP, SIDES, RANDOM
    }
    
    public Generator(Location location, UUID owner) {
        this.id = UUID.randomUUID();
        this.location = location.clone();
        this.owner = owner;
        this.items = new ArrayList<>();
        this.items.add(new ItemStack(Material.COBBLESTONE, 1));
        this.spawnRate = 60;
        this.direction = SpawnDirection.TOP;
        this.enabled = true;
        this.lastSpawn = System.currentTimeMillis();
    }
    
    public Generator(UUID id, Location location, UUID owner, List<ItemStack> items, 
                    int spawnRate, SpawnDirection direction, boolean enabled) {
        this.id = id;
        this.location = location.clone();
        this.owner = owner;
        this.items = new ArrayList<>(items);
        this.spawnRate = spawnRate;
        this.direction = direction;
        this.enabled = enabled;
        this.lastSpawn = System.currentTimeMillis();
    }
    
    public boolean shouldSpawn() {
        if (!enabled) return false;
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastSpawn = currentTime - lastSpawn;
        long spawnInterval = (60000 / spawnRate);
        
        return timeSinceLastSpawn >= spawnInterval;
    }
    
    public void updateLastSpawn() {
        this.lastSpawn = System.currentTimeMillis();
    }
    
    public ItemStack getRandomItem() {
        if (items.isEmpty()) {
            return new ItemStack(Material.COBBLESTONE, 1);
        }
        return items.get((int) (Math.random() * items.size())).clone();
    }
    
    public Location getSpawnLocation() {
        Location spawnLoc = location.clone();
        
        switch (direction) {
            case TOP:
                spawnLoc.add(0.5, 1.1, 0.5);
                break;
            case SIDES:
                double angle = Math.random() * 2 * Math.PI;
                double radius = 1.2;
                spawnLoc.add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius);
                break;
            case RANDOM:
                spawnLoc.add(
                    (Math.random() - 0.5) * 2,
                    Math.random() + 0.5,
                    (Math.random() - 0.5) * 2
                );
                break;
        }
        
        return spawnLoc;
    }
    
    public boolean isValidLocation() {
        World world = location.getWorld();
        return world != null && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }
    
    public UUID getId() { return id; }
    public Location getLocation() { return location.clone(); }
    public UUID getOwner() { return owner; }
    public List<ItemStack> getItems() { return new ArrayList<>(items); }
    public void setItems(List<ItemStack> items) { this.items = new ArrayList<>(items); }
    public int getSpawnRate() { return spawnRate; }
    public void setSpawnRate(int spawnRate) { this.spawnRate = Math.max(1, Math.min(300, spawnRate)); }
    public SpawnDirection getDirection() { return direction; }
    public void setDirection(SpawnDirection direction) { this.direction = direction; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
