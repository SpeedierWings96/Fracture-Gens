package com.speedier.fracturegens.commands;

import com.speedier.fracturegens.FractureGens;
import com.speedier.fracturegens.managers.GeneratorManager;
import com.speedier.fracturegens.models.Generator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FractureGensCommand implements CommandExecutor, TabCompleter {
    private final FractureGens plugin;
    private final GeneratorManager generatorManager;
    
    public FractureGensCommand(FractureGens plugin, GeneratorManager generatorManager) {
        this.plugin = plugin;
        this.generatorManager = generatorManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("fracturegens.admin")) {
                    sendMessage(sender, plugin.getConfig().getString("messages.no-permission", 
                        "&cYou don't have permission to do that!"));
                    return true;
                }
                
                plugin.reloadConfig();
                generatorManager.reload();
                sendMessage(sender, plugin.getConfig().getString("messages.config-reloaded", 
                    "&aConfiguration reloaded successfully!"));
                break;
                
            case "list":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by players!");
                    return true;
                }
                
                Player player = (Player) sender;
                List<Generator> playerGenerators = generatorManager.getGeneratorsByOwner(player.getUniqueId());
                
                if (playerGenerators.isEmpty()) {
                    sendMessage(sender, "&7You don't have any generators.");
                } else {
                    sendMessage(sender, "&6Your Generators (" + playerGenerators.size() + "):");
                    for (int i = 0; i < playerGenerators.size(); i++) {
                        Generator gen = playerGenerators.get(i);
                        String status = gen.isEnabled() ? "&aEnabled" : "&cDisabled";
                        sendMessage(sender, "&7" + (i + 1) + ". &f" + 
                            gen.getLocation().getBlockX() + ", " + 
                            gen.getLocation().getBlockY() + ", " + 
                            gen.getLocation().getBlockZ() + " " + status);
                    }
                }
                break;
                
            case "stats":
                if (!sender.hasPermission("fracturegens.admin")) {
                    sendMessage(sender, plugin.getConfig().getString("messages.no-permission", 
                        "&cYou don't have permission to do that!"));
                    return true;
                }
                
                int totalGenerators = generatorManager.getAllGenerators().size();
                long enabledGenerators = generatorManager.getAllGenerators().stream()
                    .mapToLong(gen -> gen.isEnabled() ? 1 : 0)
                    .sum();
                
                sendMessage(sender, "&6FractureGens Statistics:");
                sendMessage(sender, "&7Total Generators: &f" + totalGenerators);
                sendMessage(sender, "&7Enabled Generators: &f" + enabledGenerators);
                sendMessage(sender, "&7Disabled Generators: &f" + (totalGenerators - enabledGenerators));
                break;
                
            default:
                sendMessage(sender, "&cUnknown command. Use &f/" + label + " help &cfor help.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> commands = Arrays.asList("help", "reload", "list", "stats");
            
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (cmd.equals("reload") || cmd.equals("stats")) {
                        if (sender.hasPermission("fracturegens.admin")) {
                            completions.add(cmd);
                        }
                    } else {
                        completions.add(cmd);
                    }
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
    
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "&6&lFractureGens Commands:");
        sendMessage(sender, "&e/fracturegens help &7- Show this help message");
        sendMessage(sender, "&e/fracturegens list &7- List your generators");
        
        if (sender.hasPermission("fracturegens.admin")) {
            sendMessage(sender, "&e/fracturegens reload &7- Reload the configuration");
            sendMessage(sender, "&e/fracturegens stats &7- Show plugin statistics");
        }
        
        sendMessage(sender, "");
        sendMessage(sender, "&6&lHow to use:");
        sendMessage(sender, "&7• Shift + Right-click any solid block to create a generator");
        sendMessage(sender, "&7• Shift + Right-click an existing generator to configure it");
        sendMessage(sender, "&7• Break a generator block to remove it");
    }
    
    private void sendMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6FractureGens&8]&r ");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
