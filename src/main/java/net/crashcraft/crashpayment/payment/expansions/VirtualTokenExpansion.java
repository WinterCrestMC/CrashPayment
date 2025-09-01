package net.crashcraft.crashpayment.payment.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
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
        return "crashpayment";
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
        VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

        return provider.getOrDefault(uuid, 0);
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
