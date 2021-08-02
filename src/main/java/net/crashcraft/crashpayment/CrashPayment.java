package net.crashcraft.crashpayment;

import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.ProcessorManager;
import net.crashcraft.crashpayment.payment.ProviderInitializationException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CrashPayment extends JavaPlugin {
    public ProcessorManager setupPaymentProvider(JavaPlugin plugin){
        return setupPaymentProvider(plugin, "");
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin, String providerOverride){
        try {
            return new ProcessorManager(plugin, providerOverride);
        } catch (ProviderInitializationException e){
            e.printStackTrace();
        }
        return null;
    }

    public void register(JavaPlugin plugin, ServicePriority priority, PaymentProvider provider){
        Bukkit.getServicesManager().register(PaymentProvider.class, provider, plugin, priority);
        plugin.getLogger().info("Registered Payment Provider [" + provider.getProviderIdentifier() + "] with priority " + priority.name());
    }
}
