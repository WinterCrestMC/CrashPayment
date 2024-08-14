package net.crashcraft.crashpayment.payment.commands;

import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class TokenCommands implements CommandExecutor {
    private final CrashPayment plugin = CrashPayment.getInstance();
    private final Material tokenMaterial = Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("token-material")));
    private final int tokenCMD = plugin.getConfig().getInt("token-cmi-data");
    private static final String GIVE_PERMISSION = "payments.give";
    private static final String TAKE_PERMISSION = "payments.take";
    private static final String SET_PERMISSION = "payments.set";
    private static final String CHECK_PERMISSION = "payments.check";
    private static final String CONVERT_PERMISSION = "payments.convert";
    private static final String FIX_NEGATIVES_PERMISSION = "payments.fix-negatives";
    private static final String TRANSFER_PERMISSION = "payments.transfer";
    private static final String TOP_PERMISSION = "payments.top";

    public VirtualTokenProvider getProvider() {
        return (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();
    }

    /*
    crashpayments give <player> <amount>
    crashpayments take <player> <amount>
    crashpayments set <player> <amount>
    crashpayments check [player]
    crashpayments convert <player>
    crashpayments fix-negatives [player]
    crashpayments transfer <player> <amount>
    crashpayments top [amount]
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!sender.hasPermission(GIVE_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                player = plugin.getServer().getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found");
                    return true;
                }
                final UUID target = player.getUniqueId();
                provider.load();
                provider.addTokens(target, Integer.parseInt(args[2]));
                provider.save();
                sender.sendMessage("Gave " + args[1] + " " + args[2] + " tokens");
                return true;
            }
            case "take" -> {
                if (args.length != 3) {
                    return false;
                }
                if (!sender.hasPermission(TAKE_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                player = plugin.getServer().getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found");
                    return true;
                }
                final UUID target = player.getUniqueId();
                provider.load();
                provider.removeTokens(target, Integer.parseInt(args[2]));
                provider.save();
                sender.sendMessage("Took " + args[2] + " tokens from " + args[1]);
                return true;
            }
            case "set" -> {
                if (args.length != 3) {
                    return false;
                }
                if (!sender.hasPermission(SET_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                player = plugin.getServer().getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found");
                    return true;
                }
                final UUID target = player.getUniqueId();
                provider.load();
                provider.setTokens(target, Integer.parseInt(args[2]));
                provider.save();
                sender.sendMessage("Set " + args[1] + "'s tokens to " + args[2]);
                return true;
            }
            case "check" -> {
                if (!sender.hasPermission(CHECK_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();

                if (args.length >= 2) {
                    player = plugin.getServer().getPlayer(args[1]);
                }
                if (player == null) {
                    sender.sendMessage("Player not found!");
                    return true;
                }
                final UUID target = player.getUniqueId();
                provider.load();
                sender.sendMessage(player.getName() + " has " + provider.getOrDefault(target, 0) + " tokens");
                return true;
            }
            case "convert" -> {
                if (!sender.hasPermission(CONVERT_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                if (args.length > 2) {
                    player = plugin.getServer().getPlayer(args[1]);
                }
                if (player == null) {
                    sender.sendMessage("Player not found");
                    return true;
                }
                final UUID target = player.getUniqueId();
                provider.load();
                Inventory inv = player.getInventory();
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
                provider.addTokens(target, amount);
                provider.save();
                sender.sendMessage("Converted " + amount + " tokens from " + player.getName());
                return true;
            }
            case "fix-negatives" -> {
                if (args.length > 2) {
                    return false;
                }
                if (!sender.hasPermission(FIX_NEGATIVES_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                if (args.length == 1) {
                    provider.setNegativeToZero();
                } else {
                    player = plugin.getServer().getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage("Player not found");
                        return true;
                    }
                    final UUID target = player.getUniqueId();
                    provider.setNegativeToZero(target);
                }
                sender.sendMessage("Action complete.");
                return true;
            }
            case "transfer" -> {
                if (!sender.hasPermission(TRANSFER_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found. Please make sure the player is online.");
                    return true;
                }
                if (target.getUniqueId().equals((player.getUniqueId()))) {
                    sender.sendMessage("You cannot transfer to yourself!");
                    return true;
                }
                provider.load();
                final UUID senderUUID = player.getUniqueId();
                if (provider.getOrDefault(senderUUID, 0) < Integer.parseInt(args[2])) {
                    sender.sendMessage("You do not have enough tokens");
                    return true;
                }
                provider.addTokens(target.getUniqueId(), Integer.parseInt(args[2]));
                provider.removeTokens(senderUUID, Integer.parseInt(args[2]));
                provider.save();
                sender.sendMessage("Transferred " + args[2] + " tokens to " + args[1]);
                return true;
            }
            case "top" -> {
                if (!sender.hasPermission(TOP_PERMISSION)) return true;
                VirtualTokenProvider provider = this.getProvider();
                provider.load();
                List<UUID> topUsers;
                if (args.length == 1) {
                    topUsers = provider.getTopUsers();
                } else {
                    int amount = Integer.parseInt(args[1]);
                    if (amount >= 50) {
                        sender.sendMessage("Amount must be less than 50");
                        return true;
                    }
                    topUsers = provider.getTopUsers();
                }
                sender.sendMessage("Top users:");
                for (int i = 0; i < topUsers.size(); i++) {
                    final UUID uuid = topUsers.get(i);
                    player = plugin.getServer().getPlayer(uuid);
                    sender.sendMessage((i + 1) + ". " + player.getName() + " - " + provider.getOrDefault(uuid, 0));
                }
                return true;
            }
        }

        return false;
    }
}
