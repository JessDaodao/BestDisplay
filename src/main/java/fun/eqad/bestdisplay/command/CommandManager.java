package fun.eqad.bestdisplay.command;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.command.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final BestDisplay plugin;

    public CommandManager(BestDisplay plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            help(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reload(sender);
                break;
            case "about":
                about(sender);
                break;
            default:
                help(sender, label);
        }

        return true;
    }

    private void help(CommandSender sender, String label) {
        boolean aliases = label.equalsIgnoreCase("bd");

        if (aliases) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + "§7BestDisplay帮助:");
            sender.sendMessage(" §7/bd reload §8- §7重载配置文件");
            sender.sendMessage(" §7/bd about §8- §7关于BestDisplay");
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + "§7BestDisplay帮助:");
            sender.sendMessage(" §7/bestdisplay reload §8- §7重载配置文件");
            sender.sendMessage(" §7/bestdisplay about §8- §7关于BestDisplay");
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("bestdisplay.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + "§c你没有执行该命令的权限");
            return;
        }

        plugin.getConfigManager().reload();
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + "§a配置重载成功");
    }

    private void about(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + "§7关于BestDisplay:");
        sender.sendMessage(" §7现代化事件显示");
        sender.sendMessage(" §7版本 §8- §7" + plugin.getDescription().getVersion());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) commands.add("reload");
            if ("about".startsWith(args[0].toLowerCase())) commands.add("about");
            return commands;
        } // else if...

        Collections.sort(completions);
        return completions;
    }
}