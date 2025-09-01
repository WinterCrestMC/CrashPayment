package net.crashcraft.crashpayment.payment.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import java.util.UUID;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.providers.VirtualTokenProvider;
import org.bukkit.entity.Player;

public class TokenGiveCommand {

    private static final String GIVE_PERMISSION = "payments.give";

    public static void register(CrashPayment plugin) {

        new CommandAPICommand("claimtokens")
            .withArguments(
                LiteralArgument.of("give").withPermission(GIVE_PERMISSION),
                new PlayerArgument("target").withPermission(GIVE_PERMISSION),
                new IntegerArgument("amount", 1).withPermission(GIVE_PERMISSION)
            )
            .executes((sender, args) -> {
                VirtualTokenProvider provider = (VirtualTokenProvider) plugin.getProcessorManager().getProcessor().getProvider();

                int amount = (int) args.get("amount");
                Player target = (Player) args.get("target");

                final UUID targetId = target.getUniqueId();
                provider.load();
                provider.addTokens(targetId, amount);
                provider.save();
                sender.sendMessage("Gave " + target.getName() + " " + amount + " tokens");
            })
            .register(plugin);

    }
}
