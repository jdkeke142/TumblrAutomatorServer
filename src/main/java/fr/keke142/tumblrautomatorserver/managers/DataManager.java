package fr.keke142.tumblrautomatorserver.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.keke142.tumblrautomatorserver.TumblrAutomatorServer;
import fr.keke142.tumblrautomatorserver.objects.AutofollowData;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataManager {
    private final TumblrAutomatorServer server;

    private final File dataFile;

    private final ObjectMapper objectMapper;

    private final Map<String, Object> data;
    private final Map<String, AutofollowData> autofollowData;

    private final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    private static final int MAX_FOLLOW = 200;

    public DataManager(TumblrAutomatorServer server) throws IOException {
        this.server = server;

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.setDateFormat(df);

        dataFile = new File(server.getDataFolder(), "data.json");
        server.createDefaultConfiguration(dataFile, "data.json");

        data = objectMapper.readValue(dataFile, new TypeReference<HashMap<String, Object>>() {
        });

        autofollowData = objectMapper.readValue(objectMapper.readTree(dataFile).get("autofollow").toString(), new TypeReference<HashMap<String, AutofollowData>>() {
        });
    }

    public boolean isFollowed(String accountName, String followed) {
        return autofollowData.get(accountName).getFollowed().contains(followed);
    }

    public boolean isFollowLimitReached(String accountName) {
        if (autofollowData.containsKey(accountName)) {
            return autofollowData.get(accountName).getFollowAmount() >= MAX_FOLLOW;
        } else {
            autofollowData.put(accountName, new AutofollowData());

            data.put("autofollow", autofollowData);

            try {
                objectMapper.writeValue(dataFile, data);
            } catch (IOException ex) {
                TumblrAutomatorServer.getLogger().error("Failed to write data", ex);
            }

            return false;
        }
    }

    public void incrementFollowAmount(String accountName) {
        if (autofollowData.containsKey(accountName)) {
            AutofollowData autofollow = this.autofollowData.get(accountName);

            String lastDay = df.format(autofollow.getLastDate());
            String nowDay = df.format(new Date());

            if (lastDay.equals(nowDay)) {
                autofollow.setFollowAmount(autofollow.getFollowAmount() + 1);
            } else {
                autofollow.setLastDate(new Date());
                autofollow.setFollowAmount(0);
            }

            autofollowData.put(accountName, autofollow);
        } else {
            autofollowData.put(accountName, new AutofollowData());
        }

        data.put("autofollow", autofollowData);

        try {
            objectMapper.writeValue(dataFile, data);
        } catch (IOException ex) {
            TumblrAutomatorServer.getLogger().error("Failed to write data", ex);
        }
    }

    public void addFollowed(String accountName, String followed) {
        if (autofollowData.containsKey(accountName)) {
            AutofollowData autofollow = this.autofollowData.get(accountName);
            List<String> followedAccounts = new ArrayList<>(autofollow.getFollowed());

            followedAccounts.add(followed);
            autofollow.setFollowed(followedAccounts);

            autofollowData.put(accountName, autofollow);
        } else {
            autofollowData.put(accountName, new AutofollowData());
        }

        data.put("autofollow", autofollowData);

        try {
            objectMapper.writeValue(dataFile, data);
        } catch (IOException ex) {
            TumblrAutomatorServer.getLogger().error("Failed to write data", ex);
        }
    }
}
