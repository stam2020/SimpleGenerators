package plainplaying.plugin.gens;
import com.mojang.serialization.Decoder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.regex.Pattern;

public class CommandHandler implements CommandExecutor, TabExecutor {
    private final Economy eco;
    private final SimpleGenerators plugin;
    public CommandHandler(SimpleGenerators plugin, Economy eco){
        this.eco = eco;
        this.plugin = plugin;
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
                case "generator" -> {
                    switch (strings[1]) {
                        case "add" -> {
                            if (player.hasPermission("simgplegenerators.admin.gen.add")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                SimpleGenerators.logger.info(Arrays.toString(SimpleGenerators.config.getConfigurationSection("generators").getKeys(false).toArray()));
                                if (quotedArguments.size() != 9) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".name", quotedArguments.get(3));
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".lore", quotedArguments.get(4));
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".block", quotedArguments.get(5));
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".drop", quotedArguments.get(6));
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".upgrade_price", Integer.parseInt(quotedArguments.get(7)));
                                    for (String genName : SimpleGenerators.generators.getKeys(false)) {
                                        int genTier = SimpleGenerators.config.getInt("generators." + genName + ".tier");
                                        if (genTier >= Integer.parseInt(quotedArguments.get(8))) {
                                            SimpleGenerators.config.set("generators." + genName + ".tier", genTier + 1);
                                        }
                                    }
                                    SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".tier", Integer.parseInt(quotedArguments.get(8)));
                                    plugin.saveConfig();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bAdded new generator &c" + quotedArguments.get(2) + "&b, name &c" + quotedArguments.get(3) + "&b, blore &c" + quotedArguments.get(4) + "&b, block &c" + quotedArguments.get(5) + "&b, drop &c" + quotedArguments.get(6) + "&b, upgrade price &c" + quotedArguments.get(7) + "&b, tier &c" + quotedArguments.get(8)));
                                    SimpleGenerators.getInstance().onEnableRun();
                                }
                            }
                        }
                        case "remove" -> {
                            if (player.hasPermission("simgplegenerators.admin.gen.remove")) {
                                if (strings.length != 3) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    if (!SimpleGenerators.config.contains("generators." + strings[2])) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRUnknown generator &c " + strings[2] + " &b. Please enter a valid id"));
                                    } else {
                                        int currenttier = SimpleGenerators.config.getInt("generators." + strings[2] + ".tier");
                                        for (String genName : SimpleGenerators.generators.getKeys(false)) {
                                            int genTier = SimpleGenerators.config.getInt("generators." + genName + ".tier");
                                            if (genTier > currenttier) {
                                                SimpleGenerators.config.set("generators." + genName + ".tier", genTier - 1);
                                            }
                                        }
                                        SimpleGenerators.config.set("generators." + strings[2], null);
                                        plugin.saveConfig();
                                        SimpleGenerators.getInstance().onEnableRun();
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRemoved generator with id &c" + strings[2]));
                                    }
                                }
                            }
                        }
                        case "edit" -> {
                            if (player.hasPermission("simgplegenerators.admin.gen.edit")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                if (quotedArguments.size() != 5) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    {
                                        Pattern isNumber = Pattern.compile("^\\d+$");
                                        if (isNumber.matcher(quotedArguments.get(4)).matches()) {
                                            if (quotedArguments.get(3).equals("tier")) {
                                                int currenttier = SimpleGenerators.config.getInt("generators." + quotedArguments.get(2) + ".tier");
                                                for (String genName : SimpleGenerators.generators.getKeys(false)) {
                                                    int genTier = SimpleGenerators.config.getInt("generators." + genName + ".tier");
                                                    if (genTier > currenttier) {
                                                        SimpleGenerators.config.set("generators." + genName + ".tier", genTier - 1);
                                                    }
                                                }
                                                SimpleGenerators.config.set("generators." + quotedArguments.get(2) + ".tier", Integer.parseInt(quotedArguments.get(4)));
                                                currenttier = Integer.parseInt(quotedArguments.get(4));
                                                for (String genName : SimpleGenerators.generators.getKeys(false)) {
                                                    int genTier = SimpleGenerators.config.getInt("generators." + genName + ".tier");
                                                    if (genTier >= currenttier && !genName.equals(quotedArguments.get(2))) {
                                                        SimpleGenerators.config.set("generators." + genName + ".tier", genTier + 1);
                                                    }
                                                }
                                            } else {
                                                SimpleGenerators.config.set("generators." + quotedArguments.get(2) + "." + quotedArguments.get(3), Integer.parseInt(quotedArguments.get(4)));
                                            }
                                        } else {
                                            SimpleGenerators.config.set("generators." + quotedArguments.get(2) + "." + quotedArguments.get(3), quotedArguments.get(4));
                                        }
                                        plugin.saveConfig();
                                        SimpleGenerators.getInstance().onEnableRun();
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bChanged property &c" + quotedArguments.get(2) + " &bof &bid &c" + quotedArguments.get(3) + "&b to &c" + quotedArguments.get(4)));
                                    }
                                }
                            }
                        }
                        default -> {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bUnknown argument for /sg generator, &3valid types are: add,remove,edit"));
                        }
                    }
                }
                case "item" -> {
                    switch (strings[1]) {
                        case "add" -> {
                            if (player.hasPermission("simgplegenerators.admin.item.add")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                SimpleGenerators.logger.info(Arrays.toString(quotedArguments.toArray()));
                                if (quotedArguments.size() != 6) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    SimpleGenerators.config.set("items." + quotedArguments.get(2) + ".sell_price", Integer.parseInt(quotedArguments.get(5)));
                                    SimpleGenerators.config.set("items." + quotedArguments.get(2) + ".name", quotedArguments.get(3));
                                    SimpleGenerators.config.set("items." + quotedArguments.get(2) + ".lore", quotedArguments.get(4));
                                    plugin.saveConfig();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bAdded new item &c" + quotedArguments.get(2) + "&b, name &c" + quotedArguments.get(3) + "&b, lore &c" + quotedArguments.get(4) + "&b, sell price &c" + quotedArguments.get(5)));
                                    SimpleGenerators.getInstance().onEnableRun();
                                }
                            }
                        }
                        case "remove" -> {
                            if (player.hasPermission("simgplegenerators.admin.item.remove")) {
                                if (strings.length != 3) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    if (!SimpleGenerators.config.contains("items." + strings[2])) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRUnknown item &c" + strings[2] + "&b. Please enter a valid name"));
                                    } else {
                                        SimpleGenerators.config.set("items." + strings[2], null);
                                        plugin.saveConfig();
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRemoved item &c" + strings[2]));
                                    }
                                }
                            }
                        }
                        case "edit" -> {
                            if (player.hasPermission("simgplegenerators.admin.item.edit")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                if (quotedArguments.size() != 5) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    Pattern isNumber = Pattern.compile("^\\d+$");
                                    if (isNumber.matcher(quotedArguments.get(4)).matches()) {
                                        SimpleGenerators.config.set("items." + quotedArguments.get(2) + "." + quotedArguments.get(3), Integer.parseInt(quotedArguments.get(4)));
                                    } else {
                                        SimpleGenerators.config.set("items." + quotedArguments.get(2) + "." + quotedArguments.get(3), quotedArguments.get(4));
                                    }
                                    plugin.saveConfig();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bChanged property &c" + quotedArguments.get(2) + "&b of item &7&c" + quotedArguments.get(3) + "&b to &c" + quotedArguments.get(4)));
                                }
                            }
                        }
                        default -> {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bUnknown argument for /sg item, &3valid types are: add,remove,edit"));
                        }
                    }
                }
                case "sellwand" -> {
                    switch (strings[1]) {
                        case "add" -> {
                            if (player.hasPermission("simgplegenerators.admin.sellwand.add")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                SimpleGenerators.logger.info(Arrays.toString(quotedArguments.toArray()));
                                if (quotedArguments.size() != 7) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + ".name", quotedArguments.get(3));
                                    SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + ".lore", quotedArguments.get(4));
                                    SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + ".item", quotedArguments.get(5));
                                    SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + ".multiplier", Integer.parseInt(quotedArguments.get(6)));
                                    plugin.saveConfig();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bAdded new sellwand &c" + quotedArguments.get(2) + "&b, name &c" + quotedArguments.get(3) + "&b, lore &c" + quotedArguments.get(4) + "&b, item &c" + quotedArguments.get(5) + "&b, multiplier &c" + quotedArguments.get(6)));
                                    SimpleGenerators.getInstance().onEnableRun();
                                }
                            }
                        }
                        case "remove" -> {
                            if (player.hasPermission("simgplegenerators.admin.sellwand.remove")) {
                                if (strings.length != 3) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    if (!SimpleGenerators.config.contains("sellwands." + strings[2])) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRUnknown sellwand &c" + strings[2] + "&b. Please enter a valid name"));
                                    } else {
                                        SimpleGenerators.config.set("sellwands." + strings[2], null);
                                        plugin.saveConfig();
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bRemoved sellwand &c" + strings[2]));
                                    }
                                }
                            }
                        }
                        case "edit" -> {
                            if (player.hasPermission("simgplegenerators.admin.sellwand.edit")) {
                                List<String> quotedArguments = SimpleGenerators.parseByQuotes(strings);
                                if (quotedArguments.size() != 5) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bIncorrect syntax, &3check documentation for more info"));
                                } else {
                                    Pattern isNumber = Pattern.compile("^\\d+$");
                                    if (isNumber.matcher(quotedArguments.get(4)).matches()) {
                                        SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + "." + quotedArguments.get(3), Integer.parseInt(quotedArguments.get(4)));
                                    } else {
                                        SimpleGenerators.config.set("sellwands." + quotedArguments.get(2) + "." + quotedArguments.get(3), quotedArguments.get(4));
                                    }
                                    plugin.saveConfig();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bChanged property &c" + quotedArguments.get(2) + "&b of sellwand &7&c" + quotedArguments.get(3) + "&b to &c" + quotedArguments.get(4)));
                                }
                            }
                        }
                        default -> {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bUnknown argument for /sg sellwand, &3valid types are: add,remove,edit"));
                        }
                    }
                }
                case "listgens" -> {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&bThe current generators are:"));
                    for (String currentGen : SimpleGenerators.generators.getKeys(false)){
                        ConfigurationSection currentSection = SimpleGenerators.config.getConfigurationSection("generators."+currentGen);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c"+currentGen+"&b: name: &c"+currentSection.getString("name")+"&b. lore: &c"+currentSection.getString("lore")+"&b. block: &c"+currentSection.getString("block")+"&b. drop: &c"+currentSection.getString("drop")+"&b. tier: &c"+currentSection.getString("tier")));

                    }
                }
                case "credits" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&bPlugin made by &2PlainPlaying"));
                case "info" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',"Currently running version &81.2"));
                default -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cUnknown command"));
            }

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("SimpleGenerators")){
            List<String> completions = null;
            if (strings.length <= 1) {
                List<String> possibleCompletions = Arrays.asList("generator", "item", "sellwand", "reload", "credits", "info", "listgens");
                String input = strings[0].toLowerCase();
                for (String completion : possibleCompletions) {
                    if (completion.startsWith(input)) {
                        if (completions == null) {
                            completions = new ArrayList<>();
                        }
                        completions.add(completion);
                    }
                }
            }else if (strings.length == 2){
                List<String> possibleCompletions = Arrays.asList("add","remove","edit");
                String input = strings[1].toLowerCase();
                if (strings[0].equals("generator") || strings[0].equals("item") || strings[0].equals("sellwand")){
                    for (String completion : possibleCompletions) {
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);
                        }
                    }
                }
            }else if (strings.length == 3){
                if ((strings[0].equals("generator") && (strings[1].equals("remove") || strings[1].equals("edit")))){
                    String input = strings[2].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("generators").getKeys(false)){
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);
                        }
                    }
                }else if (strings[0].equals("item") && (strings[1].equals("remove") || strings[1].equals("edit"))){
                    String input = strings[2].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("items").getKeys(false)){
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);
                        }
                    }
                }else if (strings[0].equals("sellwand") && (strings[1].equals("remove") || strings[1].equals("edit"))){
                    String input = strings[2].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("sellwands").getKeys(false)){
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);
                        }
                    }
                }
            }else if (strings.length == 4) {
                if (strings[0].equals("generator") && strings[1].equals("edit")) {
                    String input = strings[3].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("generators." + strings[2]).getKeys(false)) {
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);

                        }
                    }
                }else if (strings[0].equals("item") && strings[1].equals("edit")) {
                    String input = strings[3].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("items." + strings[2]).getKeys(false)) {
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);

                        }
                    }
                }else if (strings[0].equals("sellwand") && strings[1].equals("edit")) {
                    String input = strings[3].toLowerCase();
                    for (String completion : SimpleGenerators.config.getConfigurationSection("sellwands." + strings[2]).getKeys(false)) {
                        if (completion.startsWith(input)) {
                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(completion);

                        }
                    }
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
