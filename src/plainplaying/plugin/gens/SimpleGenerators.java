package plainplaying.plugin.gens;


import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class SimpleGenerators extends JavaPlugin implements Listener {
    private final File configFile = new File(getDataFolder(),"config.yml");
    private Connection conn;
    private Statement stmtExecutor;
    public static ConfigurationSection generators;
    public static ConfigurationSection items;
    public static ConfigurationSection sellWands;
    private static Economy eco;
    public static Logger logger;
    public static FileConfiguration config;
    private static boolean offlineGeneration;
    private static SimpleGenerators instance;
    private static PlaceholderHandler placeholderHandler;
    String max_gen;
    HashMap<Player,Long> lastClickDelay = new HashMap<>();
    int id;
    @Override
    public void onLoad() {
        getLogger().info("Plugin loaded successfully");
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("Disabled due to no PlaceholderAPI dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }else {
            placeholderHandler = new PlaceholderHandler(this);
            placeholderHandler.register();
        }
        getCommand("sell").setExecutor(new CommandHandler(this,eco));
        getCommand("SimpleGenerators").setExecutor(new CommandHandler(this,eco));
        getCommand("SimpleGenerators").setTabCompleter(new CommandHandler(this,eco));
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:info.db");
            stmtExecutor = conn.createStatement();
        } catch ( Exception e ) {
            getLogger().severe(e.getStackTrace().toString());
            conn = null;
        }
        String generateGens = "CREATE TABLE IF NOT EXISTS gens(" +
                "location TEXT," +
                "type TEXT," +
                "placer TEXT" +
                ");";
        String generateChests = "CREATE TABLE IF NOT EXISTS chests(" +
                "location TEXT," +
                "player TEXT" +
                ");";
        try {
            stmtExecutor.executeUpdate(generateGens);
            stmtExecutor.executeUpdate(generateChests);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //mysqlSetup();
        //db = new SQLite(this);
       // db.load();
        getServer().getPluginManager().registerEvents(this,this);
        onEnableRun();
    }

    @Override
    public void onDisable() {
        getLogger().info("Simple generators is down.");
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block placedBlock = e.getBlock();
        Player player = e.getPlayer();
        ItemMeta heldItem = player.getInventory().getItemInMainHand().getItemMeta();
        for (String gen : generators.getKeys(false)){
            ConfigurationSection currentGen = getConfig().getConfigurationSection("generators."+gen);
            if (placedBlock.getType().equals(Material.matchMaterial(currentGen.getString("block")))){
                if (heldItem.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(currentGen.getString("name")))) && (currentGen.getString("lore").isEmpty() || (heldItem.hasLore() && (heldItem.getLore().get(0).equals(ChatColor.translateAlternateColorCodes('&', currentGen.getString("lore"))))))){
                    try {
                        int genPlaced = getPlacedGens(player);
                        int maxGens = getMaxGens(player);
                        if (genPlaced <= maxGens-1) {
                            String query = "INSERT INTO gens(location,type,placer) values(?,?,?)";
                            PreparedStatement stmt = conn.prepareStatement(query);
                            stmt.setString(1, parseLocation(placedBlock.getLocation()));
                            stmt.setString(2, gen);
                            stmt.setString(3, player.getUniqueId().toString());
                            stmt.execute();
                            HashMap<String,String> env = new HashMap<>();
                            env.put("l",Integer.toString(genPlaced+1));
                            env.put("m",Integer.toString(maxGens));
                            executeMessage(getConfig().getConfigurationSection("messages.gen_place"),player,env);
                        }else{
                            executeMessage(getConfig().getConfigurationSection("messages.max_gen"),player,new HashMap<>());
                            e.setCancelled(true);
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
        if (placedBlock.getType().equals(Material.CHEST)){
            try {
                String query = "INSERT INTO chests(location,player) values(?,?)";
                PreparedStatement addChest = conn.prepareStatement(query);
                addChest.setString(1,parseLocation(placedBlock.getLocation()));
                addChest.setString(2,player.getUniqueId().toString());
                addChest.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block brokenBlock = e.getBlock();
        if (brokenBlock.getType().equals(Material.CHEST)){
            try {
                String query = "DELETE FROM chests WHERE location=?";
                PreparedStatement addChest = conn.prepareStatement(query);
                addChest.setString(1,parseLocation(brokenBlock.getLocation()));
                addChest.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (e.getClickedBlock() != null) {
            String blockLocation = parseLocation(e.getClickedBlock().getLocation());
            try {
                String query = "SELECT location,type,placer FROM gens";
                ResultSet ans = stmtExecutor.executeQuery(query);
                while (ans.next()) {
                    String location = ans.getString("location");
                    String placer = ans.getString("placer");
                    String type = ans.getString("type");
                    if (location.equals(blockLocation)) {
                        if (placer.equals(player.getUniqueId().toString())) {
                            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                                e.getClickedBlock().setType(Material.AIR);
                                e.setCancelled(true);
                                ConfigurationSection itemInfo = getConfig().getConfigurationSection("generators." + type);
                                ItemStack genItem = new ItemStack(Material.matchMaterial(itemInfo.getString("block")));
                                ItemMeta genItemMeta = genItem.getItemMeta();
                                genItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemInfo.getString("name")));
                                if (itemInfo.contains("lore")) {
                                    genItemMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', itemInfo.getString("lore"))));
                                }
                                genItem.setItemMeta(genItemMeta);
                                giveItemsNaturally(player, genItem, e.getClickedBlock().getLocation());
                                String deletionQuery = "DELETE FROM gens WHERE location='" + blockLocation + "'";
                                PreparedStatement delete = conn.prepareStatement(deletionQuery);
                                delete.execute();
                                HashMap<String, String> env = new HashMap<>();
                                int genPlaced = getPlacedGens(player);
                                int maxGens = getMaxGens(player);
                                env.put("l", Integer.toString(genPlaced));
                                env.put("m", Integer.toString(maxGens));
                                executeMessage(getConfig().getConfigurationSection("messages.gen_break"), player, new HashMap<>());
                                break;
                            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                if (player.isSneaking()) {
                                    if (System.currentTimeMillis() - (lastClickDelay.containsKey(player) ? lastClickDelay.get(player) : 0) > 100) {
                                        lastClickDelay.put(player, System.currentTimeMillis());
                                        if (eco.getBalance(player) >= getConfig().getInt("generators." + type + ".upgrade_price") || max_gen.equals(type)) {
                                            if (max_gen.equals(type)) {
                                                executeMessage(getConfig().getConfigurationSection("messages.highest_gen"), player, new HashMap<>());
                                                return;
                                            }
                                            String nextGen = getNextGen(type);
                                            e.setCancelled(true);
                                            if (!nextGen.equals("")) {
                                                eco.withdrawPlayer(player, getConfig().getInt("generators." + type + ".upgrade_price"));
                                                e.getClickedBlock().setType(Material.matchMaterial(getConfig().getString("generators." + nextGen + ".block")));
                                                String genUpdate = "UPDATE gens SET type=? WHERE location=?";
                                                PreparedStatement update = conn.prepareStatement(genUpdate);
                                                update.setString(1, nextGen);
                                                update.setString(2, location);
                                                update.execute();
                                                executeMessage(getConfig().getConfigurationSection("messages.gen_upgrade"), player, new HashMap<>());
                                            }else{
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cerror in config.yaml, no next generator for non highest generator type, please notify a staff member. This is likely caused by the highest generator being set improperly, or a repeating tier"));
                                            }
                                        } else {
                                            executeMessage(getConfig().getConfigurationSection("messages.not_enough_money"), player, new HashMap<>());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                                e.setCancelled(true);
                                executeMessage(getConfig().getConfigurationSection("messages.cant_break"), player, new HashMap<>());
                            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                                e.setCancelled(true);
                                executeMessage(getConfig().getConfigurationSection("messages.cant_upgrade"), player, new HashMap<>());
                            }
                        }
                    }
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
            for (String sellwand : getConfig().getConfigurationSection("sellwands").getKeys(false)) {
                ConfigurationSection currentSellwand = getConfig().getConfigurationSection("sellwands."+sellwand);
                if (mainHandItem.getType().equals(Material.matchMaterial(currentSellwand.getString("item"))) && Objects.requireNonNull(mainHandItem.getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',currentSellwand.getString("name"))) && (currentSellwand.getString("lore").isEmpty() || Objects.requireNonNull(mainHandItem.getItemMeta().getLore()).get(0).equals(ChatColor.translateAlternateColorCodes('&',currentSellwand.getString("lore"))))){
                    if (e.getClickedBlock().getType() == Material.CHEST){
                        try {
                            String getPlacer = "SELECT player FROM chests WHERE location=\""+parseLocation(e.getClickedBlock().getLocation())+"\"";
                            ResultSet ans = stmtExecutor.executeQuery(getPlacer);
                            ans.next();
                            String placer = "";
                            try {
                                placer = ans.getString("player");
                            }catch (SQLException exception) {
                                try {
                                    String query = "INSERT INTO chests(location,player) values(?,?)";
                                    PreparedStatement addChest = conn.prepareStatement(query);
                                    addChest.setString(1, parseLocation(e.getClickedBlock().getLocation()));
                                    addChest.setString(2, player.getUniqueId().toString());
                                    addChest.execute();
                                    placer = player.getUniqueId().toString();
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            }
                            e.setCancelled(true);
                            if (placer.equals(player.getUniqueId().toString())) {
                                Pair<Double, Integer> answerData = sellInventory(((Container) e.getClickedBlock().getState()).getInventory(), eco, player, currentSellwand.getDouble("multiplier"));
                                HashMap<String,String> env = new HashMap<>();
                                env.put("a",answerData.getA().toString());
                                env.put("n",answerData.getB().toString());
                                executeMessage(getConfig().getConfigurationSection("messages.sell_with_wand"),player,env);
                            }else{
                                executeMessage(getConfig().getConfigurationSection("messages.cant_sell_contents"),player,new HashMap<>());
                            }
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public static SimpleGenerators getInstance(){
        return instance;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }
    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
    public String parseLocation(Location loc){
        return loc.getWorld().getName()+" "+loc.getX()+" "+loc.getY()+" "+loc.getZ();
    }
    public Location unparseLocation(String loc){
        String[] locationData = loc.split(" ");
        return new Location(getServer().getWorld(locationData[0]),Double.parseDouble(locationData[1]),Double.parseDouble(locationData[2]),Double.parseDouble(locationData[3]));
    }
    public void giveItemsNaturally(Player player, ItemStack items, Location dropLocation){
        Inventory playerInv = player.getInventory();
        int firstAvailable = playerInv.firstEmpty();
        for (ItemStack item : playerInv) {
            if (item != null) {
                if (item.isSimilar(items) && item.getItemMeta().getDisplayName().equals(items.getItemMeta().getDisplayName()) && item.getItemMeta().getLore().equals(items.getItemMeta().getLore())) {
                    int newAmount = item.getAmount() + items.getAmount();
                    if (newAmount <= 64) {
                        item.setAmount(newAmount);
                        return;
                    } else {
                        item.setAmount(64);
                        items.setAmount(newAmount - 64);

                    }
                }
            } else {
                if (firstAvailable != -1) {
                    playerInv.setItem(firstAvailable, items);
                }else{
                    player.getWorld().dropItemNaturally(dropLocation, items);
                }
                return;
            }
        }
        player.getWorld().dropItemNaturally(dropLocation, items);
    }
    public static Pair<Double,Integer> sellInventory(Inventory inv, Economy eco, Player player, double multiplier){
        int itemsSold = 0;
        double sellPrice = 0;
        Set<String> sellableItems = SimpleGenerators.config.getConfigurationSection("items").getKeys(false);
        for (ItemStack item : inv){
            if (item != null) {
                String itemId = item.getType().name();
                for (String sellableItem : sellableItems) {
                    ConfigurationSection currentItem = SimpleGenerators.config.getConfigurationSection("items."+sellableItem);
                    if (itemId.equalsIgnoreCase(sellableItem)
                            && (currentItem.getString("name").isEmpty() ||
                            Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',currentItem.getString("name"))))
                            && (currentItem.getString("lore").isEmpty() ||
                            Objects.requireNonNull(item.getItemMeta().getLore()).get(0).equals(ChatColor.translateAlternateColorCodes('&',currentItem.getString("lore"))))){
                        itemsSold += item.getAmount();
                        sellPrice += SimpleGenerators.config.getInt("items."+sellableItem+".sell_price")*item.getAmount();
                        item.setAmount(0);
                    }
                }
            }
        }
        eco.depositPlayer(player,sellPrice*multiplier);
        return new Pair<>(sellPrice*multiplier,itemsSold);
    }
    public void executeMessage(ConfigurationSection message, Player player, HashMap<String,String> env){
        switch (message.getString("type")){
            case "message" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("message"),env)));
            case "title" ->  player.sendTitle(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("title"),env)),ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("subtitle"),env)),message.getInt("fade_in"),message.getInt("stay"),message.getInt("fade_out"));
            case "actionbar" -> {player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("contents"),env))));}
            case "none" -> {}
        }
    }
    private String translateEnv(String message, HashMap<String,String> env){
        for (String key : env.keySet()){
            message = message.replace("%"+key+"%",env.get(key));
        }
        return message;
    }
    protected int getMaxGens(Player player){
        int maxGens = 0;
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith("simplegenerators.max_gens.")) {
                maxGens = Math.max(maxGens, Integer.parseInt(perm.getPermission().substring(26)));
            }
        }
        return maxGens;
    }
    public String getNextGen(String gen){
        Pair<String,Integer> next_gen = new Pair<>("",Integer.MAX_VALUE);
        int genTier = getConfig().getInt("generators."+gen+".tier");
        for (String configurationSection : getConfig().getConfigurationSection("generators").getKeys(false)){
            int currentTier = generators.getInt(configurationSection+".tier");
            if (next_gen.getB() > currentTier-genTier && currentTier > genTier){
                next_gen.set(configurationSection,currentTier-genTier);
                if (currentTier-genTier == 1){
                    return configurationSection;
                }
            }
        }
        return next_gen.getA();
    }
    public static String removeCharacter(String s, char c){
        for (int i = 0;i < s.length();i++){
            if (s.charAt(i) == c){
                s = s.substring(0,i) + s.substring(i+1);
            }
        }
        return s;
    }
    private String getHighestGen(){
        Pair<String,Integer> highest_gen = new Pair<>("",0);
        for (String configurationSection : getConfig().getConfigurationSection("generators").getKeys(false)){
            if (highest_gen.getB() < generators.getInt(configurationSection+".tier")){
                highest_gen.set(configurationSection,generators.getInt(configurationSection+".tier"));
            }
        }
        return highest_gen.getA();
    }
    protected int getPlacedGens(OfflinePlayer player) throws SQLException {
        String countGensQuery = "SELECT count(*) FROM gens WHERE placer=\""+player.getUniqueId().toString()+"\"";
        ResultSet countGens = stmtExecutor.executeQuery(countGensQuery);
        countGens.next();
        return countGens.getInt(1);
    }
    public static List<String> parseByQuotes(String[] strings){
        StringBuilder currentArgument = new StringBuilder();
        List<String> quotedArguments = new ArrayList<>();
        boolean inQuotes = false;
        for (String s : strings){
            if (s.charAt(0) == '\"' && s.charAt(s.length()-1) == '\"'){
                quotedArguments.add(s.substring(1,s.length()-1));
            } else if (s.charAt(0) == '\"'){
                currentArgument.append(s.substring(1)).append(" ");
                inQuotes = true;
            }else if (s.charAt(s.length()-1) == '\"'){
                quotedArguments.add(currentArgument.append(s, 0, s.length()-1).toString());
                currentArgument.setLength(0);
                inQuotes = false;
            }else{
                if (inQuotes) {
                    currentArgument.append(s).append(" ");
                }else{
                    quotedArguments.add(s);
                }
            }
        }
        return quotedArguments;
    }
    public void onEnableRun(){
        generators = getConfig().getConfigurationSection("generators");
        items = getConfig().getConfigurationSection("items");
        sellWands = getConfig().getConfigurationSection("sellwands");
        getLogger().info("Simple generators is setup!");
        if (!configFile.exists()){
            saveDefaultConfig();
        }
        this.reloadConfig();
        max_gen = getHighestGen();
        offlineGeneration = getConfig().getBoolean("generate_offline");
        logger = getLogger();
        config = getConfig();
        try {
            getServer().getScheduler().cancelTask(id);
        } catch (Exception ignored){}
        id = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            try {
                String query = "SELECT location,type,placer FROM gens";
                ResultSet gens = stmtExecutor.executeQuery(query);
                Set<String> genTypes = generators.getKeys(false);
                while (gens.next()){
                    OfflinePlayer playerGen = getServer().getOfflinePlayer(UUID.fromString(gens.getString("placer")));
                    if (genTypes.contains(gens.getString("type")) && !unparseLocation(gens.getString("location")).getBlock().getType().isAir()) {
                        if ((offlineGeneration || playerGen.isOnline())) {
                            ConfigurationSection itemInfo = getConfig().getConfigurationSection("generators." + gens.getString("type"));
                            ConfigurationSection dropInfo = getConfig().getConfigurationSection("items." + (itemInfo.getString("drop").startsWith("minecraft:") ? itemInfo.getString("drop").substring(10) : itemInfo.getString("drop")));
                            if (dropInfo == null){
                                logger.warning("It seems like item "+itemInfo.getString("drop")+" doesn't exist");
                                continue;
                            }
                            Location dropLoc = unparseLocation(gens.getString("location"));
                            dropLoc.add(0, 1, 0);
                            ItemStack drop = new ItemStack(Material.matchMaterial(itemInfo.getString("drop")));
                            ItemMeta meta = drop.getItemMeta();
                            if (!dropInfo.getString("name").isEmpty())
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', dropInfo.getString("name")));
                            if (!dropInfo.getString("lore").isEmpty())
                                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', dropInfo.getString("lore"))));
                            drop.setItemMeta(meta);
                            dropLoc.getWorld().dropItemNaturally(dropLoc, drop);
                        }
                    }else{
                        String deletionQuery = "DELETE FROM gens WHERE location='" + gens.getString("location") + "'";
                        PreparedStatement delete = conn.prepareStatement(deletionQuery);
                        delete.execute();
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        },0,getConfig().getInt("drop_frequency"));
    }
}