package plainplaying.plugin.gens;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import oshi.util.tuples.Pair;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabExecutor {
    private Economy eco;
    public CommandHandler(Economy eco){
        this.eco = eco;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        if (command.getName().equalsIgnoreCase("sell")){
            Inventory playerInv = player.getInventory();
            Pair<Integer,Integer> answerData = SimpleGenerators.sellInventory(playerInv,eco,player,1);
            HashMap<String,String> env = new HashMap<>();
            env.put("a",answerData.getA().toString());
            env.put("n",answerData.getB().toString());
            SimpleGenerators.getInstance().executeMessage(SimpleGenerators.config.getConfigurationSection("messages.sell_with_command"),player,env);
        }else if (command.getName().equalsIgnoreCase("SimpleGenerators")){
            switch (strings[0]){
                case "reload" -> {
                    if (player.hasPermission("simplegenerators.admin.reload")) {
                        SimpleGenerators.getInstance().onEnableRun();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPlugin reloaded successfully"));
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo permissions"));
                    }
                }
                case "credits" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&bPlugin made by &2PlainPlaying"));
                case "info" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',"Currently running version &81.0.0"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("SimpleGenerators")){
            List<String> possibleCompletions = Arrays.asList("reload","credits","info");
            String input = strings[0].toLowerCase();
            List<String> completions = null;
            for (String completion : possibleCompletions){
                if (completion.startsWith(input)){
                    if (completions == null){
                        completions = new ArrayList<>();
                    }
                    completions.add(completion);
                }
            }
            if (completions != null){
                Collections.sort(completions);
            }
            return completions;
        }
        return null;
    }
}
