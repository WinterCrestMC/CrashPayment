package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenTransferCommand {

    private static final String TRANSFER_PERMISSION = "payments.transfer";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("transfer").withPermission(TRANSFER_PERMISSION),
                new PlayerArgument("target").withPermission(TRANSFER_PERMISSION),
                new IntegerArgument("amount", 1).withPermission(TRANSFER_PERMISSION)
            )
            .executesPlayer((sender, args) -> {
                int amount = (int) args.get("amount");
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();


                if (target == sender) {
                    sender.sendMessage("You cannot transfer to yourself!");
                    return;
                }

                final UUID targetId = target.getUniqueId();
                final UUID senderId = sender.getUniqueId();
                provider.load();

                if (provider.getOrDefault(senderId, 0) < amount) {
                    sender.sendMessage("You do not have enough tokens");
                    return;
                }

                provider.addTokens(targetId, amount);
                provider.removeTokens(senderId, amount);
                provider.save();

                sender.sendMessage("Transferred " + amount + " tokens to " + target.getName());
            })
            .register(plugin);

    }
}
