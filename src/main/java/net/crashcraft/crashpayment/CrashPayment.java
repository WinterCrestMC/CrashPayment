package net.crashcraft.crashpayment;

import net.crashcraft.crashpayment.Payment.PaymentProvider;
import net.crashcraft.crashpayment.Payment.ProcessorManager;
import net.crashcraft.crashpayment.Payment.ProviderInitializationException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CrashPayment extends JavaPlugin {
    @Override
    public void onLoad(){
        getLogger().info("Loading...");
    }

    @Override
    public void onEnable(){
        getLogger().info("Loaded");
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin){
        try {
            return new ProcessorManager(plugin);
        } catch (ProviderInitializationException e){
            e.printStackTrace();
        }
        return null;
    }

    public void register(JavaPlugin plugin, ServicePriority priority, PaymentProvider provider){
        Bukkit.getServicesManager().register(PaymentProvider.class, provider, plugin, priority);
        plugin.getLogger().info("Registering payment provider " + provider.getProviderIdentifier());
    }
}
