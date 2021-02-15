package net.herospvp.sumo.events;

import lombok.Getter;
import net.herospvp.base.events.custom.CombatKillEvent;
import net.herospvp.base.events.custom.MapChangeEvent;
import net.herospvp.base.events.custom.SpawnEvent;
import net.herospvp.base.utils.StringFormat;
import net.herospvp.heroscore.objects.HPlayer;
import net.herospvp.sumo.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class MoreEvents implements Listener {

    private final Main instance;
    private final StringFormat stringFormat;
    private final Map<String, String> player1v1;
    private final Map<Player, Integer> playerAndHits;
    @Getter
    private final Map<Block, Long> blockTimings;

    public MoreEvents(Main instance) {
        this.instance = instance;
        this.player1v1 = new HashMap<>();
        this.playerAndHits = new HashMap<>();
        this.blockTimings = new HashMap<>();
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

        damaged.setHealth(20D);

        if (damager.getItemInHand() == null || damager.getItemInHand().getItemMeta() == null ||
                !damager.getItemInHand().getItemMeta().hasDisplayName()) {
            return;
        }

        ItemStack itemStack = damager.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (player1v1.get(damagerName) == null) {
            player1v1.replace(damagerName, damagedName);
        }

        if (player1v1.get(damagerName).equals(damagedName)) {

            int value = playerAndHits.get(damager);
            playerAndHits.replace(damager, value + 1);

            if (itemMeta.getDisplayName().startsWith("§7Livello §e1")) {
                if (value != 5) {
                    return;
                }
                itemMeta.setDisplayName(stringFormat.translate("&7Livello &e3 &c(" +  damagedName + ")"));
                itemStack.setItemMeta(itemMeta);

                itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                damager.setItemInHand(itemStack);

                damager.sendMessage(ChatColor.GREEN + "Lo stick e' aumentato al livello 3!");
            } else if (itemMeta.getDisplayName().startsWith("§7Livello §e2")) {
                if (value != 10) {
                    return;
                }
                itemMeta.setDisplayName(stringFormat.translate("&7Livello &e3 &c(" +  damagedName + ")"));
                itemStack.setItemMeta(itemMeta);

                itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
                damager.setItemInHand(itemStack);

                damager.sendMessage(ChatColor.GREEN + "Lo stick e' aumentato al livello 3!");
            } else {
                itemMeta.setDisplayName(stringFormat.translate("&7Livello &e1 &c(" +  damagedName + ")"));
                itemStack.setItemMeta(itemMeta);
            }
        } else {
            itemMeta.setDisplayName(stringFormat.translate("&7Livello &e1 &c(" +  damagedName + ")"));

            itemStack.setItemMeta(itemMeta);
            itemStack.removeEnchantment(Enchantment.KNOCKBACK);

            itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
            damager.setItemInHand(itemStack);

            player1v1.replace(damagerName, damagedName);
            playerAndHits.replace(damager, 0);

            event.setCancelled(true);
            damaged.damage(0D);
        }
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        playerAndHits.replace(event.getEntity(), 0);
    }

    @EventHandler
    public void on(BlockPlaceEvent event) {
        blockTimings.put(event.getBlockPlaced(), System.currentTimeMillis());
    }

    @EventHandler
    public void on(MapChangeEvent event) {
        for (Map.Entry<Block, Long> entry : blockTimings.entrySet()) {
            entry.getKey().breakNaturally();
        }
        blockTimings.clear();
    }

    @EventHandler
    public void on(SpawnEvent event) {
        Player player = event.getPlayer();

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        playerInventory.setItem(0, instance.getHotBar()[0]);
        playerInventory.setItem(1, instance.getHotBar()[1]);

        Bukkit.getScheduler().runTaskLater(instance, () -> {
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
    }

    @EventHandler
    public void on(CombatKillEvent event) {
        Player victim = event.getVictim(), killer = event.getKiller();

        HPlayer hKiller = instance.getBase().getHerosCore().getPlayersHandler().getPlayer(killer.getUniqueId());
        hKiller.setCoins(hKiller.getCoins() + 2);

        PlayerInventory playerInventory = killer.getInventory();

        for (ItemStack itemStack : playerInventory) {

            if (itemStack == null || itemStack.getItemMeta() == null || itemStack.getType() != Material.STICK) continue;

            if (!itemStack.getItemMeta().getDisplayName().contains(stringFormat.translate("&7Livello &e1"))) {

                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(stringFormat.translate("&7Livello &e1"));
                itemStack.setItemMeta(meta);

                itemStack.removeEnchantment(Enchantment.KNOCKBACK);
                itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

                player1v1.replace(killer.getName(), victim.getName());
                playerAndHits.replace(killer, 0);
            }
        }
        playerInventory.addItem(instance.getHotBar()[1]);

    }

}
