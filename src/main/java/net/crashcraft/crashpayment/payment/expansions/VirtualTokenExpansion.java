package net.crashcraft.crashpayment.payment.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.crashcraft.crashpayment.CrashPayment;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualTokenExpansion extends PlaceholderExpansion {
    private final CrashPayment plugin = CrashPayment.getInstance();
    @Override
    public @NotNull String getIdentifier() {
        return "crashpayment_virtualtokens";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Scot_Survivor";
    }

    @Override
    public @NotNull String getVersion() {
        return "v1.0.0";
    }

    private int getTokens(UUID uuid) {
        Map<UUID, Integer> tokens = new HashMap<>();
        File file = new File(plugin.getDataFolder(), "funds.json");
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(file));
            if (jsonObject == null) {
                return 0;
            }
            for (Object key : jsonObject.keySet()) {
                tokens.put(UUID.fromString((String) key), ((Long) jsonObject.get(key)).intValue());
            }
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Failed to read funds.json");
            plugin.getLogger().severe(e.getMessage());
        }
        return tokens.getOrDefault(uuid, 0);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        return getTokens(player.getUniqueId()) + "";
    }
}
