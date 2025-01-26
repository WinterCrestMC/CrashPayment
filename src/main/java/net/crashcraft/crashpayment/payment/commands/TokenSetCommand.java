package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenSetCommand {

    private static final String SET_PERNMISSION = "payments.set";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("set").withPermission(SET_PERNMISSION),
                new PlayerArgument("target").withPermission(SET_PERNMISSION),
                new IntegerArgument("amount").withPermission(SET_PERNMISSION)
            )
            .executes((sender, args) -> {
                int amount = (int) args.get("amount");
                Player target = (Player) args.get("target");
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                final UUID targetId = target.getUniqueId();
                provider.load();
                provider.setTokens(targetId, amount);
                provider.save();
                sender.sendMessage("Set " + target.getName() + "'s tokens to " + amount);
            })
            .register(plugin);

    }
}
