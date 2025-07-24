package com.speedier.fracturegens.listeners;

import com.speedier.fracturegens.FractureGens;
import com.speedier.fracturegens.gui.GeneratorGUI;
import com.speedier.fracturegens.gui.ItemSelectionGUI;
import com.speedier.fracturegens.models.Generator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIListener implements Listener {
    private final FractureGens plugin;
    
    public GUIListener(FractureGens plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        
        plugin.getLogger().info("GUI Click Debug - Player: " + player.getName() + ", Title: '" + title + "'");
        
        if (title.contains("Generator Configuration")) {
            plugin.getLogger().info("GUI Click Debug - Handling Generator GUI click");
            handleGeneratorGUIClick(event, player);
        } else if (title.contains("Select Items")) {
            plugin.getLogger().info("GUI Click Debug - Handling Item Selection GUI click");
            handleItemSelectionGUIClick(event, player);
        } else {
            plugin.getLogger().info("GUI Click Debug - No matching GUI title found");
        }
    }
    
    private void handleGeneratorGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        GeneratorGUI gui = findGeneratorGUI(player);
        if (gui == null) {
            return;
        }
        
        Generator generator = gui.getGenerator();
        int slot = event.getSlot();
        
        switch (slot) {
            case 4:
                generator.setEnabled(!generator.isEnabled());
                gui.updateInventory();
                playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                break;
                
            case 37:
                int decreaseAmount = event.getClick() == ClickType.SHIFT_LEFT ? 1 : 10;
                int newRate = Math.max(1, generator.getSpawnRate() - decreaseAmount);
                generator.setSpawnRate(newRate);
                gui.updateInventory();
                playSound(player, Sound.UI_BUTTON_CLICK);
                break;
                
            case 43:
                int increaseAmount = event.getClick() == ClickType.SHIFT_LEFT ? 1 : 10;
                int maxRate = plugin.getConfig().getInt("generators.max-spawn-rate", 300);
                int newRateInc = Math.min(maxRate, generator.getSpawnRate() + increaseAmount);
                generator.setSpawnRate(newRateInc);
                gui.updateInventory();
                playSound(player, Sound.UI_BUTTON_CLICK);
                break;
                
            case 40:
                Generator.SpawnDirection[] directions = Generator.SpawnDirection.values();
                int currentIndex = generator.getDirection().ordinal();
                int nextIndex = (currentIndex + 1) % directions.length;
                generator.setDirection(directions[nextIndex]);
                gui.updateInventory();
                playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME);
                break;
                
            case 49:
                player.closeInventory();
                break;
                
            case 53:
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    gui.getGeneratorManager().removeGenerator(generator.getId());
                    player.closeInventory();
                    sendMessage(player, plugin.getConfig().getString("messages.generator-removed", 
                        "&aGenerator removed successfully!"));
                    playSound(player, Sound.ENTITY_GENERIC_EXPLODE);
                } else {
                    sendMessage(player, "&cShift-click to confirm removal!");
                }
                break;
                
            default:
                if (slot >= 19 && slot <= 25) {
                    int itemIndex = slot - 19;
                    List<ItemStack> items = generator.getItems();
                    
                    if (itemIndex < items.size()) {
                        if (event.getClick() == ClickType.RIGHT) {
                            items.remove(itemIndex);
                            generator.setItems(items);
                            gui.updateInventory();
                            playSound(player, Sound.ENTITY_ITEM_BREAK);
                        }
                    } else if (clicked.getType() == Material.LIME_STAINED_GLASS_PANE) {
                        new ItemSelectionGUI(plugin, gui.getGeneratorManager(), generator, gui).open(player);
                    }
                }
                break;
        }
        
        gui.getGeneratorManager().saveGenerators();
    }
    
    private void handleItemSelectionGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        ItemSelectionGUI gui = findItemSelectionGUI(player);
        if (gui == null) {
            return;
        }
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 45:
                gui.previousPage();
                playSound(player, Sound.UI_BUTTON_CLICK);
                break;
                
            case 53:
                gui.nextPage();
                playSound(player, Sound.UI_BUTTON_CLICK);
                break;
                
            case 46:
                gui.getParentGUI().open(player);
                break;
                
            case 49:
            case 52:
                break;
                
            default:
                if (slot >= 10 && slot <= 43 && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    gui.addItemToGenerator(clicked.getType());
                    gui.getParentGUI().updateInventory();
                    gui.getParentGUI().open(player);
                    sendMessage(player, "&aItem added to generator!");
                    playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                }
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title.contains("Generator Configuration") || title.contains("Select Items")) {
            plugin.removeOpenGUI(player.getUniqueId());
            playSound(player, Sound.BLOCK_CHEST_CLOSE);
        }
    }
    
    private GeneratorGUI findGeneratorGUI(Player player) {
        Object gui = plugin.getOpenGUI(player.getUniqueId());
        plugin.getLogger().info("GUI Debug - findGeneratorGUI for " + player.getName() + ", found: " + (gui != null ? gui.getClass().getSimpleName() : "null"));
        if (gui instanceof GeneratorGUI) {
            return (GeneratorGUI) gui;
        }
        return null;
    }
    
    private ItemSelectionGUI findItemSelectionGUI(Player player) {
        Object gui = plugin.getOpenGUI(player.getUniqueId());
        plugin.getLogger().info("GUI Debug - findItemSelectionGUI for " + player.getName() + ", found: " + (gui != null ? gui.getClass().getSimpleName() : "null"));
        if (gui instanceof ItemSelectionGUI) {
            return (ItemSelectionGUI) gui;
        }
        return null;
    }
    
    private void playSound(Player player, Sound sound) {
        if (plugin.getConfig().getBoolean("gui.sounds", true)) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
    
    private void sendMessage(Player player, String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6FractureGens&8]&r ");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
