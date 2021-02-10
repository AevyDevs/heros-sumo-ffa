package net.herospvp.sumo;

import lombok.Getter;
import net.herospvp.base.Base;
import net.herospvp.base.utils.StringFormat;
import net.herospvp.sumo.events.MoreEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@Getter
public class Main extends JavaPlugin {

    private Base base;
    private StringFormat stringFormat;
    private ItemStack[] hotBar;

    @Override
    public void onEnable() {

        base = getPlugin(Base.class);
        stringFormat = base.getStringFormat();

        new MoreEvents(this);

        hotBar = new ItemStack[2];
        hotBar[0] = new ItemStack(Material.STICK);
        hotBar[1] = new ItemStack(Material.STAINED_CLAY, 8);

        ItemMeta itemMeta = hotBar[0].getItemMeta();
        itemMeta.setLore(Arrays.asList(" ", stringFormat.translate("&e* &75 hit(*) &8» &elivello 1"),
                stringFormat.translate("&e* &710 hit(*) &8» &elivello 2"), " ",
                stringFormat.translate("&7(*) » consecutive allo stesso avversario"), " ",
                stringFormat.translate("&7&7NOTA: colpire altri avversari comporta la"),
                stringFormat.translate("&7&7perdita del livello"), " "));

        itemMeta.setDisplayName(stringFormat.translate("&7Livello &e0"));
        hotBar[0].setItemMeta(itemMeta);

        base.getWorldConfiguration().setPvpDisabledOver(58);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.getLocation().getY() < 21) {
                        player.setHealth(0D);
                    }
                }, 3L);
            }
        }, 17L, 17L);

    }

    @Override
    public void onDisable() {

    }

}
