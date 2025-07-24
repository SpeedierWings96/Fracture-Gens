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

public class ItemSelectionGUI {
    private final FractureGens plugin;
    private final GeneratorManager generatorManager;
    private final Generator generator;
    private final GeneratorGUI parentGUI;
    private Inventory inventory;
    private int currentPage = 0;
    private final List<Material> availableItems;
    
    public ItemSelectionGUI(FractureGens plugin, GeneratorManager generatorManager, 
                           Generator generator, GeneratorGUI parentGUI) {
        this.plugin = plugin;
        this.generatorManager = generatorManager;
        this.generator = generator;
        this.parentGUI = parentGUI;
        this.availableItems = getAvailableItems();
        createInventory();
    }
    
    private List<Material> getAvailableItems() {
        List<Material> items = new ArrayList<>();
        
        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir() && material != Material.BARRIER) {
                items.add(material);
            }
        }
        
        return items;
    }
    
    private void createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("gui.titles.item-selection", "&6&lSelect Items"));
        inventory = Bukkit.createInventory(null, 54, title);
        
        updateInventory();
    }
    
    public void updateInventory() {
        inventory.clear();
        
        addBorder();
        addNavigationButtons();
        addItems();
        addControlButtons();
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
    
    private void addNavigationButtons() {
        if (currentPage > 0) {
            ItemStack prevPage = createItem(Material.ARROW, "&e&lPrevious Page", Arrays.asList(
                "&7Page " + currentPage + " of " + getMaxPages()
            ));
            inventory.setItem(45, prevPage);
        }
        
        if (currentPage < getMaxPages() - 1) {
            ItemStack nextPage = createItem(Material.ARROW, "&e&lNext Page", Arrays.asList(
                "&7Page " + (currentPage + 2) + " of " + getMaxPages()
            ));
            inventory.setItem(53, nextPage);
        }
        
        ItemStack pageInfo = createItem(Material.BOOK, "&6&lPage Information", Arrays.asList(
            "&7Current Page: &f" + (currentPage + 1),
            "&7Total Pages: &f" + getMaxPages(),
            "&7Total Items: &f" + availableItems.size()
        ));
        inventory.setItem(49, pageInfo);
    }
    
    private void addItems() {
        int itemsPerPage = 28;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableItems.size());
        
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot == 17 || slot == 26 || slot == 35) {
                slot += 2;
            }
            
            Material material = availableItems.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + formatMaterialName(material.name()));
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Click to add this item",
                    ChatColor.GRAY + "to your generator"
                ));
                item.setItemMeta(meta);
            }
            
            inventory.setItem(slot, item);
            slot++;
        }
    }
    
    private void addControlButtons() {
        ItemStack back = createItem(Material.BARRIER, "&c&lBack", Arrays.asList(
            "&7Return to generator configuration"
        ));
        inventory.setItem(46, back);
        
        ItemStack search = createItem(Material.COMPASS, "&e&lSearch", Arrays.asList(
            "&7Search for specific items",
            "&c(Feature coming soon)"
        ));
        inventory.setItem(52, search);
    }
    
    private String formatMaterialName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        
        return formatted.toString();
    }
    
    private int getMaxPages() {
        return (int) Math.ceil((double) availableItems.size() / 28);
    }
    
    public void nextPage() {
        if (currentPage < getMaxPages() - 1) {
            currentPage++;
            updateInventory();
        }
    }
    
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateInventory();
        }
    }
    
    public void addItemToGenerator(Material material) {
        List<ItemStack> items = generator.getItems();
        if (items.size() >= plugin.getConfig().getInt("generators.max-item-types", 10)) {
            return;
        }
        
        ItemStack newItem = new ItemStack(material, 1);
        items.add(newItem);
        generator.setItems(items);
        generatorManager.saveGenerators();
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
    
    public GeneratorGUI getParentGUI() {
        return parentGUI;
    }
}
