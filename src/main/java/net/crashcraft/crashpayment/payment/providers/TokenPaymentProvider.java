package net.crashcraft.crashpayment.payment.providers;

import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.ProviderInitializationException;
import net.crashcraft.crashpayment.payment.TransactionRecipe;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class TokenPaymentProvider implements PaymentProvider {
    private int tokenValue;
    private int tokenCMD;
    private Material tokenMaterial;

    @Override
    public String getProviderIdentifier() {
        return "TokenPaymentProvider";
    }

    @Override
    public boolean checkRequirements() {
        this.tokenValue = CrashPayment.getInstance().getConfig().getInt("token-value");
        if (tokenValue == 0 || tokenValue < 0) {
            CrashPayment.getInstance().getLogger()
                    .warning("Token value is 0 or less than 0, please change this in the config.");
            return false;
        }
        this.tokenCMD = CrashPayment.getInstance().getConfig().getInt("token-cmi-data");
        String temp = CrashPayment.getInstance().getConfig().getString("token-material");
        if (temp == null) {
            CrashPayment.getInstance().getLogger()
                    .warning("Token material is null, please change this in the config.");
            return false;
        }
        this.tokenMaterial = Material.getMaterial(temp);
        if (tokenMaterial == null) {
            CrashPayment.getInstance().getLogger()
                    .warning("Token material is null, please change this in the config.");
            return false;
        }
        return true;
    }

    @Override
    public void setup() throws ProviderInitializationException {

    }

    @Override
    public void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback) {
        Player player = Bukkit.getOfflinePlayer(user).getPlayer();
        if (player == null) {
            callback.accept(new TransactionRecipe(user, amount, comment, "Player is null"));
            return;
        }
        if (!player.isOnline()) {
            callback.accept(new TransactionRecipe(user, amount, comment, "Player is not online"));
            return;
        }

        switch (type){
            case DEPOSIT:
                Inventory inventory = player.getInventory();
                ItemStack tokens = new ItemStack(tokenMaterial, (int) amount);
                CrashPayment.setCMD(tokenCMD, tokens);
                inventory.addItem(tokens);
                callback.accept(new TransactionRecipe(user, amount, comment));
                break;
            case WITHDRAW:
                inventory = player.getInventory();
                // Find tokens that match the amount
                int amountLeft = (int) amount;
                for (ItemStack item : inventory.getContents()) {
                    if (item == null || item.getType() != tokenMaterial ||
                            (item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() != tokenCMD)) {
                        continue;
                    }
                    if (item.getAmount() > amountLeft) {
                        item.setAmount(item.getAmount() - amountLeft);
                        amountLeft = 0;
                        break;
                    } else {
                        amountLeft -= item.getAmount();
                        inventory.remove(item);
                    }
                }
                if (amountLeft > 0) {
                    callback.accept(new TransactionRecipe(user, amount, comment, "Not enough tokens"));
                    // Put tokens back
                    if (amountLeft == amount) {
                        break;
                    }
                    inventory.addItem(CrashPayment.setCMD(tokenCMD, new ItemStack(tokenMaterial, amountLeft + 1)));
                    break;
                }
        }
    }

    @Override
    public void getBalance(UUID user, Consumer<Double> callback) {
        Player player = Bukkit.getOfflinePlayer(user).getPlayer();
        if (player == null) {
            callback.accept(0.0);
            return;
        }
        if (!player.isOnline()) {
            callback.accept(0.0);
            return;
        }
        Inventory inventory = player.getInventory();
        double amount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != tokenMaterial ||
                    (item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() != tokenCMD)) {
                continue;
            }
            amount += item.getAmount();
        }
        callback.accept(amount);
    }
}
