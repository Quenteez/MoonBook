package ru.quenteez.moonbook.utils;

import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.GetFilter;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.quenteez.moonbook.MoonBook;

public class VKUtils {
    private final JavaPlugin instance;
    @Getter
    private boolean isDisabled = false;
    private ServiceActor actor;
    private Integer clientId;
    private Integer groupId;
    private String serviceToken;
    private Integer lastPostId;
    @Getter
    private WallpostFull lastPost;

    public VKUtils(MoonBook instance) {
        this.instance = instance;
        if (!this.checkValidates("clientId", "serviceToken", "groupId")) {
            this.isDisabled = true;
            return;
        }
        clientId = this.instance.getConfig().getInt("vkontakte.clientId");
        serviceToken = this.instance.getConfig().getString("vkontakte.serviceToken");
        groupId = this.instance.getConfig().getInt("vkontakte.groupId");

        new BukkitRunnable() {
            @Override
            public void run() {
                actor = new ServiceActor(clientId, serviceToken);
                lastPost = getLatestPost();
                if (lastPost != null) {
                    lastPostId = lastPost.getId();
                }
            }
        }.runTaskAsynchronously(instance);
    }

    private WallpostFull getLatestPost() {
        try {
            GetResponse response = MoonBook.vk.wall().get(this.actor)
                    .ownerId(-this.groupId)
                    .count(3)
                    .filter(GetFilter.ALL)
                    .execute();
            if (response == null || response.getItems().isEmpty()) {
                return null;
            }

            WallpostFull latestPost = null;
            for (WallpostFull item : response.getItems()) {
                Integer isPinned = item.getIsPinned();
                if (isPinned != null && isPinned == 1) {
                    continue;
                }
                latestPost = item;
                break;
            }

            return latestPost;
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void listenGroup() {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (lastPost == null) {
                    return;
                }

                WallpostFull post = getLatestPost();
                if (post == null) {
                    return;
                }

                if (lastPostId != null && lastPostId.equals(post.getId())) {
                    return;
                }

                MoonBook.clearUsers();
                lastPostId = post.getId();
                lastPost = post;

                Bukkit.getScheduler().runTaskLater(instance,
                        () -> Bukkit.getServer().getOnlinePlayers().forEach(player -> MCUtils.book(player, true)),
                        20L * 2);
            }
        }.runTaskTimerAsynchronously(instance, 0L, 20L * 10L);
    }

    public boolean checkValidates(String... parameters) {
        for (String param : parameters) {
            if (this.instance.getConfig().get("vkontakte." + param) == null) {
                Bukkit.getConsoleSender().sendMessage("    §eWarnings");
                Bukkit.getConsoleSender().sendMessage("    §7You must specify the data to authorize");
                Bukkit.getConsoleSender().sendMessage("    §7to Vkontakte in configuration.");
                Bukkit.getConsoleSender().sendMessage("    §7'" + param + "' return null");
                Bukkit.getConsoleSender().sendMessage("    §4The Vkontakte is not authorized.");
                return false;
            }
        }
        return true;
    }
}
