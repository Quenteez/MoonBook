package ru.quenteez.moonbook;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.quenteez.moonbook.commands.NewsCommand;
import ru.quenteez.moonbook.database.MySQL;
import ru.quenteez.moonbook.listeners.PlayerListener;
import ru.quenteez.moonbook.utils.VKUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MoonBook extends JavaPlugin {
    private Logger logger;
    @Getter
    @Setter
    private static MySQL database;
    public static TransportClient transportClient;
    public static VkApiClient vk;
    @Getter
    private static VKUtils vkUtils;
    @Getter
    private static MoonBook instance;
    public static List<UUID> users = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEnable() {
        instance = this;
        logger = LoggerFactory.getLogger(this.getName());

        transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);

        Bukkit.getPluginCommand("news").setExecutor(new NewsCommand());
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        saveDefaultConfig();

        database = new MySQL(this, this.getName());
        if (database.isDisabled()) {
            onDisable();
        }

        vkUtils = new VKUtils(this);
        if (vkUtils.isDisabled()) {
            onDisable();
        }

        setupDatabase();
        vkUtils.listenGroup();
        logger.info("§aEnabled " + this.getName());
    }

    @Override
    public void onDisable() {
        database.getSQL().close();
        logger.info("§cDisabled " + this.getName());
    }

    public void setupDatabase() {
        database.createTable("users", "id INT AUTO_INCREMENT",
                "uuid VARCHAR(36) NOT NULL", "PRIMARY KEY (id)");
        users.clear();
        logger.info("§eSync users...");
        List<String> uuids = database.getStringList("SELECT uuid FROM `users`", "uuid");
        if (!uuids.isEmpty()) {
            uuids.forEach(uuid -> users.add(UUID.fromString(uuid)));
        }
    }

    public static void clearUsers() {
        users.clear();
        database.execute("DELETE FROM `users`;");
    }
}
