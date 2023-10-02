package ru.quenteez.moonbook.utils;

import com.vk.api.sdk.objects.wall.WallpostFull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.quenteez.moonbook.MoonBook;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCUtils {
    static int maxPageVolume = 256;
    static String author = "MoonStudio";
    public static void book(Player player, boolean joined) {
        boolean contains = MoonBook.users.contains(player.getUniqueId());
        boolean openBook = false;

        if (joined) {
            if (!contains) {
                openBook = true;
                MoonBook.users.add(player.getUniqueId());
                MoonBook.getDatabase().execute("INSERT INTO `users` (uuid) VALUES ('" + player.getUniqueId() + "');");
            }
        } else {
            openBook = true;
        }

        if (openBook) {
            WallpostFull post = MoonBook.getVkUtils().getLastPost();
            if (post == null) {
                return;
            }

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookmeta = (BookMeta) book.getItemMeta();
            if (bookmeta == null) {
                return;
            }

            bookmeta.setTitle(author + " News");
            bookmeta.setPages(splitString(ChatColor.DARK_RED +
                    getDateFromUnix(post.getDate()) + "\n \n" +
                    ChatColor.BLACK + post.getText()));
            bookmeta.setAuthor(author);
            book.setItemMeta(bookmeta);
            player.openBook(book);
        }
    }


    public static String getDateFromUnix(int unix) {
        Date date = new Date(unix * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return dateFormat.format(date);
    }
    public static List<String> splitString(String input) {
        List<String> result = new ArrayList<>();
        int length = input.length();

        for (int startIndex = 0; startIndex < length; startIndex += maxPageVolume) {
            int endIndex = Math.min(startIndex + maxPageVolume, length);
            String chunk = input.substring(startIndex, endIndex);
            result.add(chunk);
        }

        return result;
    }
}
