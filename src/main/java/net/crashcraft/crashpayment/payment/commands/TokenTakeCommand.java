package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenTakeCommand {

    private static final String TAKE_PERMISSION = "payments.take";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("take").withPermission(TAKE_PERMISSION),
                new PlayerArgument("target").withPermission(TAKE_PERMISSION),
                new IntegerArgument("amount", 1).withPermission(TAKE_PERMISSION)
            )
            .executes((sender, args) -> {
                int amount = (int) args.get("amount");
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                final UUID targetId = target.getUniqueId();
                provider.load();
                provider.removeTokens(targetId, amount);
                provider.save();
                sender.sendMessage("Took " + amount + " tokens from " + target.getName());
            })
            .register(plugin);

    }
}
