package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import java.util.List;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TokenTopCommand {

    private static final String TOP_PERMISSION = "payments.top";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("top").withPermission(TOP_PERMISSION),
                new IntegerArgument("amount").withPermission(TOP_PERMISSION).setOptional(true)
            )
            .executes((sender, args) -> {
                Integer amount = (Integer) args.get("amount");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                provider.load();

                List<UUID> topUsers;
                if (amount == null) {
                    topUsers = provider.getTopUsers();
                } else {
                    topUsers = provider.getTopUsers(amount);
                }

                sender.sendMessage("Top users:");
                for (int i = 0; i < topUsers.size(); i++) {
                    final UUID uuid = topUsers.get(i);
                    OfflinePlayer target = plugin.getServer().getOfflinePlayer(uuid);
                    sender.sendMessage((i + 1) + ". " + target.getName() + " - " + provider.getOrDefault(uuid, 0));
                }
            })
            .register(plugin);

    }
}
