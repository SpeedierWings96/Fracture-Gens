package com.speedier.fracturegens.gui;

import com.speedier.fracturegens.FractureGens;
import com.speedier.fracturegens.managers.GeneratorManager;
import com.speedier.fracturegens.models.Generator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorGUI {
    private final FractureGens plugin;
    private final GeneratorManager generatorManager;
    private final Generator generator;
    private Inventory inventory;
    
    public GeneratorGUI(FractureGens plugin, GeneratorManager generatorManager, Generator generator) {
        this.plugin = plugin;
        this.generatorManager = generatorManager;
        this.generator = generator;
        createInventory();
    }
    
    private void createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("gui.titles.main", "&6&lGenerator Configuration"));
        inventory = Bukkit.createInventory(null, 54, title);
        
        updateInventory();
    }
    
    public void updateInventory() {
        inventory.clear();
        
        addBorder();
        addGeneratorInfo();
        addItemsDisplay();
        addControlButtons();
        addSettingsButtons();
    }
    
    private void addBorder() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    private void addGeneratorInfo() {
        ItemStack info = createItem(Material.BEACON, "&6&lGenerator Information", Arrays.asList(
            "&7Location: &f" + generator.getLocation().getBlockX() + ", " + 
                generator.getLocation().getBlockY() + ", " + generator.getLocation().getBlockZ(),
            "&7World: &f" + generator.getLocation().getWorld().getName(),
            "&7Status: " + (generator.isEnabled() ? "&aEnabled" : "&cDisabled"),
            "&7Spawn Rate: &f" + generator.getSpawnRate() + " items/minute",
            "&7Direction: &f" + generator.getDirection().name(),
            "&7Items: &f" + generator.getItems().size() + " types",
            "",
            "&eClick to toggle on/off"
        ));
        inventory.setItem(4, info);
    }
    
    private void addItemsDisplay() {
        List<ItemStack> items = generator.getItems();
        int startSlot = 19;
        
        for (int i = 0; i < Math.min(items.size(), 7); i++) {
            ItemStack displayItem = items.get(i).clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.YELLOW + "Right-click to remove");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inventory.setItem(startSlot + i, displayItem);
        }
        
        if (items.size() < 7) {
            ItemStack addItem = createItem(Material.LIME_STAINED_GLASS_PANE, "&a&lAdd Item", Arrays.asList(
                "&7Click to add a new item type",
                "&7to this generator's output"
            ));
            inventory.setItem(startSlot + items.size(), addItem);
        }
    }
    
    private void addControlButtons() {
        ItemStack decreaseRate = createItem(Material.RED_CONCRETE, "&c&lDecrease Rate", Arrays.asList(
            "&7Current: &f" + generator.getSpawnRate() + " items/minute",
            "&7Click to decrease by 10",
            "&7Shift-click to decrease by 1"
        ));
        inventory.setItem(37, decreaseRate);
        
        ItemStack increaseRate = createItem(Material.GREEN_CONCRETE, "&a&lIncrease Rate", Arrays.asList(
            "&7Current: &f" + generator.getSpawnRate() + " items/minute",
            "&7Click to increase by 10",
            "&7Shift-click to increase by 1"
        ));
        inventory.setItem(43, increaseRate);
        
        ItemStack direction = createItem(Material.COMPASS, "&e&lSpawn Direction", Arrays.asList(
            "&7Current: &f" + generator.getDirection().name(),
            "&7TOP: Items spawn above the block",
            "&7SIDES: Items spawn around the block",
            "&7RANDOM: Items spawn randomly nearby",
            "",
            "&eClick to cycle through options"
        ));
        inventory.setItem(40, direction);
    }
    
    private void addSettingsButtons() {
        ItemStack close = createItem(Material.BARRIER, "&c&lClose", Arrays.asList(
            "&7Click to close this menu"
        ));
        inventory.setItem(49, close);
        
        ItemStack remove = createItem(Material.TNT, "&c&lRemove Generator", Arrays.asList(
            "&7Click to permanently remove",
            "&7this generator",
            "&c&lWARNING: This cannot be undone!"
        ));
        inventory.setItem(53, remove);
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public void open(Player player) {
        plugin.setOpenGUI(player.getUniqueId(), this);
        player.openInventory(inventory);
        if (plugin.getConfig().getBoolean("gui.sounds", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        }
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Generator getGenerator() {
        return generator;
    }
    
    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }
    
    public FractureGens getPlugin() {
        return plugin;
    }
}
