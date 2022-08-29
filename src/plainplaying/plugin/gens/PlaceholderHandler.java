package plainplaying.plugin.gens;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PlaceholderHandler extends PlaceholderExpansion {
    SimpleGenerators plugin;
    public PlaceholderHandler(SimpleGenerators plugin){
        this.plugin = plugin;
    }
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        switch (params){
            case "gen_amount" -> {
                try {
                    return Integer.toString(plugin.getPlacedGens(player));
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
            case "max_gens" -> {
                if (player.isOnline()) {
                    return Integer.toString(plugin.getMaxGens((Player) player));
                }else{
                    plugin.getLogger().warning("trying to access the max gens of an offline player is forbidden!");
                }
            }
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "sg";
    }

    @Override
    public String getAuthor() {
        return "PlainPlaying";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}