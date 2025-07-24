package com.speedier.fracturegens.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class PerformanceUtils {
    
    public static boolean isChunkLoaded(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        
        return world.isChunkLoaded(chunkX, chunkZ);
    }
    
    public static double getCurrentTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            Object[] recentTps = (Object[]) server.getClass().getField("recentTps").get(server);
            return (Double) recentTps[0];
        } catch (Exception e) {
            return 20.0;
        }
    }
    
    public static boolean isServerOverloaded() {
        return getCurrentTPS() < 18.0;
    }
    
    public static String formatLocation(Location location) {
        return location.getWorld().getName() + " " + 
               location.getBlockX() + ", " + 
               location.getBlockY() + ", " + 
               location.getBlockZ();
    }
    
    public static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
    }
    
    public static long getMaxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024;
    }
    
    public static double getMemoryUsagePercent() {
        return (double) getUsedMemoryMB() / getMaxMemoryMB() * 100;
    }
}
