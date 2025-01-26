package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenFixNegativesCommand {

    private static final String FIX_NEGATIVES_PERMISSION = "payments.fix-negatives";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("fix-negatives").withPermission(FIX_NEGATIVES_PERMISSION),
                new PlayerArgument("target").withPermission(FIX_NEGATIVES_PERMISSION).setOptional(true)
            )
            .executes((sender, args) -> {
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                if (target == null) {
                    provider.setNegativeToZero();
                } else {
                    provider.setNegativeToZero(target.getUniqueId());
                }

                sender.sendMessage("Action complete.");
            })
            .register(plugin);

    }
}
