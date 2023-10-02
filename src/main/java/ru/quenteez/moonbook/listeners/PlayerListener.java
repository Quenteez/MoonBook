package ru.quenteez.moonbook.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.quenteez.moonbook.MoonBook;
import ru.quenteez.moonbook.utils.MCUtils;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new BukkitRunnable() {
            public void run() {
                MCUtils.book(e.getPlayer(), true);
            }
        }.runTaskLater(MoonBook.getInstance(), 10);

    }
}
