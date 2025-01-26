package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenCheckCommand {

    private static final String CHECK_PERMISSION = "payments.check";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("check").withPermission(CHECK_PERMISSION),
                new PlayerArgument("target").withPermission(CHECK_PERMISSION).setOptional(true)
            )
            .executes((sender, args) -> {
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                if (target == null) {
                    if (sender instanceof Player) {
                        target = (Player) sender;
                    } else {
                        sender.sendMessage("Player not found!");
                        return;
                    }
                }

                final UUID targetId = target.getUniqueId();
                provider.load();
                sender.sendMessage(target.getName() + " has " + provider.getOrDefault(targetId, 0) + " tokens");
            })
            .register(plugin);

    }
}
