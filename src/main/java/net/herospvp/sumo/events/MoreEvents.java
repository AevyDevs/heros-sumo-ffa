package net.herospvp.sumo.events;

import net.herospvp.base.utils.StringFormat;
import net.herospvp.sumo.Main;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class MoreEvents implements Listener {

    private final Main instance;
    private final StringFormat stringFormat;
    private final Map<String, String> player1v1;
    private final Map<Player, Integer> playerAndHits;

    public MoreEvents(Main instance) {
        this.instance = instance;
        this.player1v1 = new HashMap<>();
        this.playerAndHits = new HashMap<>();
        this.stringFormat = instance.getBase().getStringFormat();
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        player1v1.put(event.getPlayer().getName(), null);
        playerAndHits.put(event.getPlayer(), 0);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        player1v1.remove(event.getPlayer().getName());
        playerAndHits.remove(event.getPlayer());
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager(), damaged = (Player) event.getEntity();

        if (damager.getLocation().getY() > instance.getBase().getWorldConfiguration().getPvpDisabledOver()) {
            return;
        }

        String damagerName = damager.getName(), damagedName = damaged.getName();

        if (damager.getItemInHand() == null) {
            return;
        }

        if (damager.getItemInHand().getItemMeta() == null) {
            return;
        }

        damaged.setHealth(20D);

        if (player1v1.get(damagerName) == null) {
            player1v1.replace(damagerName, damagedName);
        }

        ItemStack itemStack = damager.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (player1v1.get(damagerName).equals(damagedName)) {

            int value = playerAndHits.get(damager);

            if (value >= 5) {

                playerAndHits.replace(damager, value + 1);

                switch (itemMeta.getDisplayName()) {
                    case "§7Livello §e1": {
                        if (value != 10) {
                            return;
                        }
                        itemMeta.setDisplayName(stringFormat.translate("&7Livello &e2"));
                        itemStack.setItemMeta(itemMeta);

                        itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                        damager.setItemInHand(itemStack);

                        damager.sendMessage(ChatColor.GREEN + "Lo stick e' aumentato al livello 2!");
                        break;
                    }
                    case "§7Livello §e0": {
                        itemMeta.setDisplayName(stringFormat.translate("&7Livello &e1"));
                        itemStack.setItemMeta(itemMeta);

                        itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                        damager.setItemInHand(itemStack);

                        damager.sendMessage(ChatColor.GREEN + "Lo stick e' aumentato al livello 1!");
                        break;
                    }
                }
                return;
            }
            playerAndHits.replace(damager, value + 1);
        } else {
            itemMeta.setDisplayName(stringFormat.translate("&7Livello &e0"));

            itemStack.setItemMeta(itemMeta);
            itemStack.removeEnchantment(Enchantment.KNOCKBACK);

            damager.setItemInHand(itemStack);
            player1v1.replace(damagerName, damagedName);
            playerAndHits.replace(damager, 0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        playerAndHits.replace(event.getEntity(), 0);
    }

}
