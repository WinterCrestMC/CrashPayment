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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VirtualTokenProvider implements PaymentProvider {
    private final CrashPayment plugin;
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Gson gson = new Gson();

    public VirtualTokenProvider() {
        this.plugin = CrashPayment.getInstance();
        this.load();
    }

    public void setTokens(UUID uuid, int amount) {
        tokens.put(uuid, amount);
    }

    public void addTokens(UUID uuid, int amount) {
        tokens.put(uuid, tokens.getOrDefault(uuid, 0) + amount);
    }

    public void removeTokens(UUID uuid, int amount) {
        tokens.put(uuid, tokens.getOrDefault(uuid, 0) - amount);
    }

    public int getOrDefault(UUID uuid, int amount) {
        return tokens.getOrDefault(uuid, amount);
    }


    /**
     * Get Top users
     * @param amount amount of users to get
     * @return top users
     */
    public List<UUID> getTopUsers(int amount) {
        return tokens.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(amount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get Top 5 users
     * @return top 5 users
     */
    public List<UUID> getTopUsers() {
        return getTopUsers(5);
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

    public void setNegativeToZero() {
        if (tokens.isEmpty()) {
            return;
        }
        for (UUID uuid : tokens.keySet()) {
            if (tokens.get(uuid) < 0) {
                tokens.put(uuid, 0);
            }
        }
        this.save();
    }

    public void setNegativeToZero(UUID uuid) {
        if (!tokens.containsKey(uuid)) return;
        if (tokens.get(uuid) < 0) {
            tokens.put(uuid, 0);
        }
        this.save();
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
        if (amount < 0) {
            callback.accept(new TransactionRecipe(user, amount, comment, "Negative amounts are not allowed."));
        }
        switch (type) {
            case DEPOSIT:
                addTokens(user, (int) amount);
                callback.accept(new TransactionRecipe(user, amount, comment));
                break;
            case WITHDRAW:
                if (amount > tokens.get(user)) {
                    // Error
                    callback.accept(new TransactionRecipe(user, amount, comment, "Insufficient funds"));
                    return;
                }
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
