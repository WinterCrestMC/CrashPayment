package net.crashcraft.crashpayment.payment.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TokenCommands {
    private final CrashPayment plugin = CrashPayment.getInstance();
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Gson gson = new Gson();
    private final Material tokenMaterial = Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("token-material")));
    private final int tokenCMD = plugin.getConfig().getInt("token-cmi-data");

    public void load() {
        // Check if funds.json exists
        File file = new File(plugin.getDataFolder(), "funds.json");
        if (!file.exists()) {
            // Create funds.json file
            try {
                boolean output = file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create funds.json");
                plugin.getLogger().severe(e.getMessage());
            }
        }
        // Read tokens from file
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(file));
            if (jsonObject == null) {
                return;
            }
            for (Object key : jsonObject.keySet()) {
                tokens.put(UUID.fromString((String) key), ((Long) jsonObject.get(key)).intValue());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to read funds.json");
            plugin.getLogger().severe(e.getMessage());
        } catch (ParseException e) {
            plugin.getLogger().severe("Failed to parse funds.json");
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void save() {
        if (tokens.isEmpty()) {
            return;
        }
        // Create funds.json file
        File file = new File(plugin.getDataFolder(), "funds.json");
        // Write tokens to file
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(tokens));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save funds.json");
            plugin.getLogger().severe(e.getMessage());
        }
    }


    @Suggestions("player")
    public List<String> playerSuggestions(final CommandContext<CommandSender> sender, final String input) {
        return CrashPayment.getInstance().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("DataFlowIssue")  // We know that the various items aren't NULL else the plugin wouldn't load.
    @CommandMethod("crashpayments give <player> <amount>")
    @CommandDescription("Give a player tokens")
    @CommandPermission("payments.give")
    public void giveTokens(final CommandSender sender,
                           @NonNull @Argument(value = "player", suggestions = "player") final String player,
                           @NonNull @Argument("amount") final int amount) {
        final OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        this.load();
        tokens.put(target.getUniqueId(), tokens.getOrDefault(target.getUniqueId(), 0) + amount);
        this.save();
        sender.sendMessage("Gave " + target.getName() + " " + amount + " tokens");
    }

    @CommandMethod("crashpayments take <player> <amount>")
    @CommandDescription("Take tokens from a player")
    @CommandPermission("payments.take")
    public void takeTokens(final CommandSender sender,
                           @NonNull @Argument(value = "player", suggestions = "player") final String player,
                           @NonNull @Argument("amount") final int amount) {
        final OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        this.load();
        tokens.put(target.getUniqueId(), tokens.getOrDefault(target.getUniqueId(), 0) - amount);
        this.save();
        sender.sendMessage("Took " + amount + " tokens from " + target.getName());
    }

    @CommandMethod("crashpayments set <player> <amount>")
    @CommandDescription("Set a player's tokens")
    @CommandPermission("payments.set")
    public void setTokens(final CommandSender sender,
                          @NonNull @Argument(value = "player", suggestions = "player") final String player,
                          @NonNull @Argument("amount") final int amount) {
        final OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        this.load();
        tokens.put(target.getUniqueId(), amount);
        this.save();
        sender.sendMessage("Set " + target.getName() + "'s tokens to " + amount);
    }

    @CommandMethod("crashpayments check [player]")
    @CommandDescription("Check a player's tokens")
    @CommandPermission("payments.check")
    public void checkTokens(final CommandSender sender,
                            @Nullable @Argument(value = "player", suggestions = "player") String player) {
        if (player == null) {
            player = sender.getName();
        }
        final OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        this.load();
        sender.sendMessage(target.getName() + " has " + tokens.getOrDefault(target.getUniqueId(), 0) + " tokens");
    }

    @CommandMethod("crashpayments convert <player>")
    @CommandDescription("Convert a player's physical tokens to their virtual tokens")
    @CommandPermission("payments.convert")
    public void convertTokens(final CommandSender sender,
                              @NonNull @Argument(value = "player", suggestions = "player") final String player) {
        final Player target = Bukkit.getPlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        Inventory inv = target.getInventory();
        int amount = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() != tokenMaterial ||
                    (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()
                            && item.getItemMeta().getCustomModelData() != tokenCMD)) {
                continue;
            }
            amount += item.getAmount();
            inv.remove(item);
        }
        this.load();
        tokens.put(target.getUniqueId(), tokens.getOrDefault(target.getUniqueId(), 0) + amount);
        this.save();
        sender.sendMessage("Converted " + amount + " tokens from " + target.getName());
    }

    @CommandMethod("crashpayments fix-negatives [player]")
    @CommandDescription("Fix negative amounts from older versions")
    @CommandPermission("payments.fix-negatives")
    public void fixNegatives(final CommandSender sender,
                             @Nullable @Argument(value = "player", suggestions = "player") final String player) {
        PaymentProvider provider = plugin.getProcessorManager().getProcessor().getProvider();
        if (provider instanceof VirtualTokenProvider) {
            VirtualTokenProvider vtProvider = (VirtualTokenProvider) provider;
            if (player == null) {
                vtProvider.setNegativeToZero();
            } else{
                final OfflinePlayer player1 = Bukkit.getOfflinePlayer(player);
                vtProvider.setNegativeToZero(player1.getUniqueId());
            }
        } else {
            sender.sendMessage("This command it only needed for the virtual token provider, you are not using this," +
                    "therefore you do not need it.");
        }

    }
}
