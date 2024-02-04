package net.crashcraft.crashpayment.payment.rewards;

import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.TransactionResponse;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Objects;

public class RewardManager {
    private CrashPayment plugin;
    private String timeFormat;
    private int rewardAmount;
    private HashMap<Player, Long> rewardCooldown = new HashMap<>();
    private BukkitTask worker;
    private final PaymentProvider paymentProvider;

    public RewardManager(CrashPayment plugin) {
        this.plugin = plugin;
        this.timeFormat = plugin.getConfig().getString("reward-time-format");
        this.rewardAmount = plugin.getConfig().getInt("reward-amount");
        paymentProvider = plugin.getProcessorManager().getProcessor().getProvider();
        worker = Bukkit.getScheduler().runTaskTimer(plugin, this::task, 0,
                plugin.getConfig().getInt("reward-check-interval"));  // Run every tick.
    }

    private void task() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (rewardCooldown.containsKey(player)) {
                long time = rewardCooldown.get(player);
                if (System.currentTimeMillis()/1000 - time >= timeFormatted()) {
                    rewardCooldown.remove(player);
                    paymentProvider.makeTransaction(
                            player.getUniqueId(),
                            TransactionType.DEPOSIT,
                            "Reward",
                            rewardAmount,
                            recipe -> {
                                if (recipe.getTransactionStatus() == TransactionResponse.SUCCESS) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("reward-message")))
                                            .replace("%amount%", String.valueOf(rewardAmount)));
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("reward-failed-message"))));
                                }
                            }
                    );
                }
            } else {
                rewardCooldown.put(player, System.currentTimeMillis()/1000);
            }
        }
    }


    private long timeFormatted() {
        // h == Hour, m = Minute, s = second, "1h 30m" means 1 hour and 30 minutes. Convert to unix
        // time.
        String[] split = timeFormat.split(" ");
        long time = 0;
        for (String s : split) {
            if (s.contains("h")) {
                time += (long) Integer.parseInt(s.replace("h", "")) * 60 * 60;
            } else if (s.contains("m")) {
                time += (long) Integer.parseInt(s.replace("m", "")) * 60;
            } else if (s.contains("s")) {
                time += Integer.parseInt(s.replace("s", ""));
            }
        }
        return time;
    }
}
