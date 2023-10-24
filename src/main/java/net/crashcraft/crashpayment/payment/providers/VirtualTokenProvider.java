package net.crashcraft.crashpayment.payment.providers;

import com.google.gson.Gson;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.ProviderInitializationException;
import net.crashcraft.crashpayment.payment.TransactionRecipe;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class VirtualTokenProvider implements PaymentProvider {
    private final CrashPayment plugin;
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Gson gson = new Gson();

    public VirtualTokenProvider() {
        this.plugin = CrashPayment.getInstance();
        this.load();
    }

    public void addTokens(UUID uuid, int amount) {
        tokens.put(uuid, tokens.getOrDefault(uuid, 0) + amount);
    }

    public void removeTokens(UUID uuid, int amount) {
        tokens.put(uuid, tokens.getOrDefault(uuid, 0) - amount);
    }


    public void load() {
        // Check if funds.json exists
        File file = new File(plugin.getDataFolder(), "funds.json");
        if (!file.exists()) {
            // Create funds.json file
            try {
                boolean output = file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create funds.json");
                plugin.getLogger().severe(e.getMessage());
            }
        }
        // Read tokens from file
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(file));
            if (jsonObject == null) {
                return;
            }
            for (Object key : jsonObject.keySet()) {
                tokens.put(UUID.fromString((String) key), ((Long) jsonObject.get(key)).intValue());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to read funds.json");
            plugin.getLogger().severe(e.getMessage());
        } catch (ParseException e) {
            plugin.getLogger().severe("Failed to parse funds.json");
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void save() {
        if (tokens.isEmpty()) {
            return;
        }
        // Create funds.json file
        File file = new File(plugin.getDataFolder(), "funds.json");
        // Write tokens to file
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(tokens));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save funds.json");
            plugin.getLogger().severe(e.getMessage());
        }
    }

    @Override
    public String getProviderIdentifier() {
        return "VirtualTokenProvider";
    }

    @Override
    public boolean checkRequirements() {
        return true;
    }

    @Override
    public void setup() throws ProviderInitializationException {

    }

    @Override
    public void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback) {
        // Due to having no way to obtain a payment processor from the main plugin, we have to load the funds.json file here.
        this.load();
        switch (type) {
            case DEPOSIT:
                addTokens(user, (int) amount);
                callback.accept(new TransactionRecipe(user, amount, comment));
                break;
            case WITHDRAW:
                removeTokens(user, (int) amount);
                callback.accept(new TransactionRecipe(user, amount, comment));
                break;
        }
        this.save();
    }

    @Override
    public void getBalance(UUID user, Consumer<Double> callback) {
        callback.accept((double) tokens.getOrDefault(user, 0));
    }
}
