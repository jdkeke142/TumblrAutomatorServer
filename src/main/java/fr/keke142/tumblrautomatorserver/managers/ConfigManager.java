package fr.keke142.tumblrautomatorserver.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.keke142.tumblrautomatorserver.TumblrAutomatorServer;
import fr.keke142.tumblrautomatorserver.objects.Account;
import fr.keke142.tumblrautomatorserver.objects.Autofollow;
import fr.keke142.tumblrautomatorserver.objects.Autopost;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final TumblrAutomatorServer server;

    private final File accountsFile;
    private final File autofollowFile;
    private final File autopostFile;

    private final ObjectMapper objectMapper;

    private Map<String, Autofollow> autofollow;
    private Map<String, HashMap<String, Autopost>> autopost;
    private Map<String, Account> accounts;

    public ConfigManager(TumblrAutomatorServer server) throws IOException {
        this.server = server;

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        autofollowFile = new File(server.getDataFolder(), "autofollow.json");
        server.createDefaultConfiguration(autofollowFile, "autofollow.json");
        autofollow = objectMapper.readValue(autofollowFile, new TypeReference<HashMap<String, Autofollow>>() {
        });

        autopostFile = new File(server.getDataFolder(), "autopost.json");
        server.createDefaultConfiguration(autopostFile, "autopost.json");
        autopost = objectMapper.readValue(autopostFile, new TypeReference<HashMap<String, HashMap<String, Autopost>>>() {
        });

        accountsFile = new File(server.getDataFolder(), "accounts.json");
        server.createDefaultConfiguration(accountsFile, "accounts.json");

        accounts = objectMapper.readValue(accountsFile, new TypeReference<HashMap<String, Account>>() {
        });
    }

    public void reload() throws IOException {
        server.createDefaultConfiguration(autofollowFile, "autofollow.json");
        autofollow = objectMapper.readValue(autofollowFile, new TypeReference<HashMap<String, Autofollow>>() {
        });

        server.createDefaultConfiguration(autopostFile, "autopost.json");
        autopost = objectMapper.readValue(autopostFile, new TypeReference<HashMap<String, HashMap<String, Autopost>>>() {
        });

        server.createDefaultConfiguration(accountsFile, "accounts.json");
        accounts = objectMapper.readValue(accountsFile, new TypeReference<HashMap<String, Account>>() {
        });
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public Map<String, Autofollow> getAutofollow() {
        return autofollow;
    }

    public Map<String, HashMap<String, Autopost>> getAutopost() {
        return autopost;
    }
}
