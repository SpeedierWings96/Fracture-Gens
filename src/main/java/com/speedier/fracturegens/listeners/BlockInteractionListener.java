package com.speedier.fracturegens.listeners;

import com.speedier.fracturegens.FractureGens;
import com.speedier.fracturegens.gui.GeneratorGUI;
import com.speedier.fracturegens.managers.GeneratorManager;
import com.speedier.fracturegens.models.Generator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractionListener implements Listener {
    private final FractureGens plugin;
    private final GeneratorManager generatorManager;
    
    public BlockInteractionListener(FractureGens plugin, GeneratorManager generatorManager) {
        this.plugin = plugin;
        this.generatorManager = generatorManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null || !player.isSneaking()) {
            return;
        }
        
        if (!isValidGeneratorBlock(block)) {
            return;
        }
        
        Generator existingGenerator = generatorManager.getGeneratorAt(block.getLocation());
        
        if (existingGenerator != null) {
            if (!player.hasPermission("fracturegens.configure")) {
                sendMessage(player, plugin.getConfig().getString("messages.no-permission", 
                    "&cYou don't have permission to do that!"));
                return;
            }
            
            if (!existingGenerator.getOwner().equals(player.getUniqueId()) && 
                !player.hasPermission("fracturegens.admin")) {
                sendMessage(player, "&cYou can only configure your own generators!");
                return;
            }
            
            event.setCancelled(true);
            new GeneratorGUI(plugin, generatorManager, existingGenerator).open(player);
            
        } else {
            if (!player.hasPermission("fracturegens.create")) {
                sendMessage(player, plugin.getConfig().getString("messages.no-permission", 
                    "&cYou don't have permission to do that!"));
                return;
            }
            
            if (!plugin.isPlayerInCreationMode(player.getUniqueId())) {
                sendMessage(player, plugin.getConfig().getString("messages.not-in-creation-mode", 
                    "&cYou must enable creation mode first! Use &f/fracturegens create &cto toggle creation mode."));
                return;
            }
            
            if (generatorManager.createGenerator(block.getLocation(), player.getUniqueId())) {
                event.setCancelled(true);
                sendMessage(player, plugin.getConfig().getString("messages.generator-created", 
                    "&aGenerator created successfully!"));
                
                Generator newGenerator = generatorManager.getGeneratorAt(block.getLocation());
                if (newGenerator != null) {
                    new GeneratorGUI(plugin, generatorManager, newGenerator).open(player);
                }
            } else {
                sendMessage(player, plugin.getConfig().getString("messages.max-generators-reached", 
                    "&cYou have reached the maximum number of generators!"));
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Generator generator = generatorManager.getGeneratorAt(block.getLocation());
        
        if (generator != null) {
            Player player = event.getPlayer();
            
            if (!generator.getOwner().equals(player.getUniqueId()) && 
                !player.hasPermission("fracturegens.admin")) {
                event.setCancelled(true);
                sendMessage(player, "&cYou can only remove your own generators!");
                return;
            }
            
            if (!player.hasPermission("fracturegens.remove")) {
                event.setCancelled(true);
                sendMessage(player, plugin.getConfig().getString("messages.no-permission", 
                    "&cYou don't have permission to do that!"));
                return;
            }
            
            generatorManager.removeGenerator(generator.getId());
            sendMessage(player, plugin.getConfig().getString("messages.generator-removed", 
                "&aGenerator removed successfully!"));
        }
    }
    
    private boolean isValidGeneratorBlock(Block block) {
        Material type = block.getType();
        return type.isSolid() && 
               type != Material.BEDROCK && 
               type != Material.BARRIER && 
               type != Material.AIR &&
               !type.name().contains("SHULKER_BOX");
    }
    
    private void sendMessage(Player player, String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6FractureGens&8]&r ");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
