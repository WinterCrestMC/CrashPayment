package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.Objects;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TokenConvertCommand {

    private static final String CONVERT_PERMISSION = "payments.convert";

    public static void register(CrashPayment plugin) {
        Material tokenMaterial = Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("token-material")));
        int tokenCMD = plugin.getConfig().getInt("token-cmi-data");

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("convert").withPermission(CONVERT_PERMISSION),
                new PlayerArgument("target").withPermission(CONVERT_PERMISSION)
            )
            .executes((sender, args) -> {
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                provider.load();
                Inventory inv = target.getInventory();
                UUID targetId = target.getUniqueId();

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
                provider.addTokens(targetId, amount);
                provider.save();
                sender.sendMessage("Converted " + amount + " tokens from " + target.getName());
            })
            .register(plugin);

    }
}
