package net.crashcraft.crashpayment;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.ProcessorManager;
import net.crashcraft.crashpayment.payment.ProviderInitializationException;
import net.crashcraft.crashpayment.payment.commands.TokenCheckCommand;
import net.crashcraft.crashpayment.payment.commands.TokenConvertCommand;
import net.crashcraft.crashpayment.payment.commands.TokenFixNegativesCommand;
import net.crashcraft.crashpayment.payment.commands.TokenGiveCommand;
import net.crashcraft.crashpayment.payment.commands.TokenSetCommand;
import net.crashcraft.crashpayment.payment.commands.TokenTakeCommand;
import net.crashcraft.crashpayment.payment.commands.TokenTopCommand;
import net.crashcraft.crashpayment.payment.commands.TokenTransferCommand;
import net.crashcraft.crashpayment.payment.expansions.VirtualTokenExpansion;
import net.crashcraft.crashpayment.payment.rewards.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CrashPayment extends JavaPlugin {
    private ProcessorManager processorManager;
    private RewardManager rewardManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).setNamespace("claimtokens").silentLogs(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        saveDefaultConfig();

        TokenGiveCommand.register(this);
        TokenTakeCommand.register(this);
        TokenSetCommand.register(this);
        TokenCheckCommand.register(this);
        TokenConvertCommand.register(this);
        TokenFixNegativesCommand.register(this);
        TokenTransferCommand.register(this);
        TokenTopCommand.register(this);

        // Register papi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VirtualTokenExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin){
        return setupPaymentProvider(plugin, "");
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin, String providerOverride){
        try {
            processorManager = new ProcessorManager(plugin, providerOverride);
            rewardManager = new RewardManager(this);
            return processorManager;
        } catch (ProviderInitializationException e){
            getLogger().severe("Unable to find a valid payment provider, " +
                    "payments will be reverted to a fake payment provider where all transactions will be approved.");
            getLogger().severe(e.getMessage());
        }
        return null;
    }

    public void register(JavaPlugin plugin, ServicePriority priority, PaymentProvider provider){
        Bukkit.getServicesManager().register(PaymentProvider.class, provider, plugin, priority);
        plugin.getLogger().info("Registered Payment Provider [" + provider.getProviderIdentifier() + "] with priority " + priority.name());
    }

    static public CrashPayment getInstance(){
        return JavaPlugin.getPlugin(CrashPayment.class);
    }

    static public ItemStack setCMD(Integer num, ItemStack items) {
        ItemMeta meta = items.getItemMeta();
        if (meta == null) {
            getInstance().getLogger()
                    .warning("Itemmeta for this material is null, please change this in the config.");
            return items;
        }
        meta.setCustomModelData(num);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getInstance().getConfig().getString("token-name", "Claim Token")));
        items.setItemMeta(meta);
        return items;
    }

    public ProcessorManager getProcessorManager() {
        return processorManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
