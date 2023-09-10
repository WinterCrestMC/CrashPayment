package net.crashcraft.crashpayment.payment.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.crashcraft.crashpayment.CrashPayment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TokenCommands {

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
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }
        PlayerInventory inventory = target.getInventory();
        ItemStack items = new ItemStack(Material.getMaterial(Objects.requireNonNull(CrashPayment.getInstance().getConfig().getString("token-material"))));
        items.setAmount(amount);
        CrashPayment.setCMD(CrashPayment.getInstance().getConfig().getInt("token-cmi-data"), items);
        inventory.addItem(items);
        sender.sendMessage("Gave " + target.getName() + " " + amount + " tokens");
    }
}
