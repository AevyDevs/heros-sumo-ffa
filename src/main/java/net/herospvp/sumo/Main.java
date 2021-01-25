package net.herospvp.sumo;

import lombok.Getter;
import net.herospvp.base.Base;
import net.herospvp.base.commands.Spawn;
import net.herospvp.base.events.CombatEvents;
import net.herospvp.base.events.PlayerEvents;
import net.herospvp.base.utils.StringFormat;
import net.herospvp.base.utils.lambdas.SpawnLambda;
import net.herospvp.sumo.events.MoreEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Main extends JavaPlugin {

    @Getter
    private Base base;
    private StringFormat stringFormat;
    private Spawn spawn;
    private PlayerEvents playerEvents;
    private CombatEvents combatEvents;

    @Override
    public void onEnable() {

        base = Base.getInstance();
        stringFormat = base.getStringFormat();

        new MoreEvents(this);

        ItemStack[] hotBar = new ItemStack[1];
        hotBar[0] = new ItemStack(Material.STICK);

        ItemMeta itemMeta = hotBar[0].getItemMeta();
        itemMeta.setLore(Arrays.asList(" ", stringFormat.translate("&e* &75 hit(*) &8» &elivello 1"),
                stringFormat.translate("&e* &710 hit(*) &8» &elivello 2"), " ",
                stringFormat.translate("&7(*) » consecutive allo stesso avversario"), " ",
                stringFormat.translate("&7&7NOTA: colpire altri avversari comporta la"),
                stringFormat.translate("&7&7perdita del livello"), " "));

        itemMeta.setDisplayName(stringFormat.translate("&7Livello &e0"));
        hotBar[0].setItemMeta(itemMeta);

        base.getWorldConfiguration().setPvpDisabledOver(58);

        spawn = base.getSpawn();
        playerEvents = base.getPlayerEvents();
        combatEvents = base.getCombatEvents();

        SpawnLambda spawnLambda = (player) -> {

            PlayerInventory playerInventory = player.getInventory();
            playerInventory.clear();
            playerInventory.setItem(0, hotBar[0]);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.getActivePotionEffects().size() == 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 172800000, 0));
                } else {
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
                            continue;
                        }
                        player.removePotionEffect(effect.getType());
                    }
                }
            }, 5L);
        };

        spawn.setSpawnLambda(spawnLambda);
        playerEvents.setSpawnLambda(spawnLambda);
        combatEvents.setCombatEventsLambda((player, killer) -> {});

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.getLocation().getY() < 21) {
                        player.setHealth(0D);
                    }
                }, 4L);
            }
        }, 20L, 20L);

    }

    @Override
    public void onDisable() {

    }

}
