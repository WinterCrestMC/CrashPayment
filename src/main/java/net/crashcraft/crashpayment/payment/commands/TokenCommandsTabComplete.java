package net.crashcraft.crashpayment.payment.commands;

import net.crashcraft.crashpayment.CrashPayment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TokenCommandsTabComplete implements TabCompleter {

    private static final String GIVE_PERMISSION = "payments.give";
    private static final String TAKE_PERMISSION = "payments.take";
    private static final String SET_PERMISSION = "payments.set";
    private static final String CHECK_PERMISSION = "payments.check";
    private static final String CONVERT_PERMISSION = "payments.convert";
    private static final String FIX_NEGATIVES_PERMISSION = "payments.fix-negatives";
    private static final String TRANSFER_PERMISSION = "payments.transfer";
    private static final String TOP_PERMISSION = "payments.top";

    public List<String> playerSuggestions(final String input) {
        return CrashPayment.getInstance().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> finalCompletions = new ArrayList<>();
        if (args.length == 0) {
            return List.of();
        }

        if (args.length == 1) {
            if (commandSender.hasPermission(GIVE_PERMISSION)) completions.add("give");
            if (commandSender.hasPermission(TAKE_PERMISSION)) completions.add("take");
            if (commandSender.hasPermission(SET_PERMISSION)) completions.add("set");
            if (commandSender.hasPermission(CHECK_PERMISSION)) completions.add("check");
            if (commandSender.hasPermission(CONVERT_PERMISSION)) completions.add("convert");
            if (commandSender.hasPermission(FIX_NEGATIVES_PERMISSION)) completions.add("fix-negatives");
            if (commandSender.hasPermission(TRANSFER_PERMISSION)) completions.add("transfer");
            if (commandSender.hasPermission(TOP_PERMISSION)) completions.add("top");
            StringUtil.copyPartialMatches(args[0], completions, finalCompletions);
        }

        if (args.length == 2) {
            String input = args[0].toLowerCase();
            switch (input) {
                case "give" -> {
                    if (commandSender.hasPermission(GIVE_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "take" -> {
                    if (commandSender.hasPermission(TAKE_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "set" -> {
                    if (commandSender.hasPermission(SET_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "check" -> {
                    if (commandSender.hasPermission(CHECK_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "convert" -> {
                    if (commandSender.hasPermission(CONVERT_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "fix-negatives" -> {
                    if (commandSender.hasPermission(FIX_NEGATIVES_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "transfer" -> {
                    if (commandSender.hasPermission(TRANSFER_PERMISSION)) finalCompletions.addAll(playerSuggestions(args[1]));
                }
                case "top" -> {
                    completions.add("<number>");
                }
            }
        }

        if (args.length == 3) {
            String input = args[1].toLowerCase();
            switch (input) {
                case "give" -> {
                    if (commandSender.hasPermission(GIVE_PERMISSION)) completions.add("<amount>");
                }
                case "take" -> {
                    if (commandSender.hasPermission(TAKE_PERMISSION)) completions.add("<amount>");
                }
                case "set" -> {
                    if (commandSender.hasPermission(SET_PERMISSION)) completions.add("<amount>");
                }
                case "transfer" -> {
                    if (commandSender.hasPermission(TRANSFER_PERMISSION)) completions.add("<amount>");
                }
            }
        }
        Collections.sort(finalCompletions);
        return finalCompletions;
    }
}
