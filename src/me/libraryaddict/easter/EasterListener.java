package me.libraryaddict.easter;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.Currency.CreditsApi;
import me.libraryaddict.Inventory.InventoryApi;
import me.libraryaddict.Loader.Rank;
import me.libraryaddict.Loader.RankLoader;
import me.libraryaddict.hub.HubReturn;
import me.libraryaddict.scoreboard.ScoreboardManager;

public class EasterListener implements Listener {
    private EasterHunt mainPlugin;
    private ItemStack returnToSpawn = InventoryApi.setNameAndLore(new ItemStack(Material.COMPASS), ChatColor.DARK_GREEN
            + "Return to spawn", ChatColor.GREEN + "Right click a block with", ChatColor.GREEN + "this to return to spawn!");;

    public EasterListener(EasterHunt plugin) {
        this.mainPlugin = plugin;
    }

    public int getEggsFound(String player) {
        int eggsFound = 0;
        for (Egg egg : mainPlugin.getEggs()) {
            if (egg.hasFound(player)) {
                eggsFound++;
            }
        }
        return eggsFound;
    }

    public String getTime(Integer i) {
        i = Math.abs(i);
        int remainder = i % 3600, minutes = remainder / 60, seconds = remainder % 60;
        String time = "";
        if (minutes > 0) {
            time += minutes + " minute";
            if (minutes > 1)
                time += "s";
        }
        if (seconds > 0) {
            if (minutes > 0)
                time += ", ";
            time += seconds + " second";
            if (seconds > 1)
                time += "s";
        }
        if (time.equals(""))
            time = "no time at all";
        return time;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBurnSpread(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onChange(EntityChangeBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(BlockDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
        if (event.getCause() == DamageCause.VOID) {
            event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
        }
        if (event.getEntity().getFireTicks() > 0) {
            event.getEntity().setFireTicks(0);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExpCollect(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            switch (event.getClickedBlock().getType()) {
            case STONE_BUTTON:
            case LEVER:
            case WOOD_DOOR:
            case WOOD_BUTTON:
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case TRAP_DOOR:
            case NOTE_BLOCK:
                break;
            default:
                event.setCancelled(true);
            }
        }
        if (event.getAction().name().contains("RIGHT") && event.getItem() != null && event.getItem().equals(this.returnToSpawn)) {
            if (event.getPlayer().getExp() > 0) {
                event.getPlayer().setExp(0);
                event.getPlayer().sendMessage(ChatColor.GOLD + "Teleport to spawn cancelled!");
            } else {
                final Player p = event.getPlayer();
                p.sendMessage(ChatColor.GOLD + "Teleport to spawn started! Right click again to cancel!");
                p.setExp(1);
                BukkitRunnable run = new BukkitRunnable() {
                    public void run() {
                        if (p.getExp() <= 0) {
                            cancel();
                        } else {
                            p.setExp(p.getExp() - .05F);
                            if (p.getExp() <= 0) {
                                p.teleport(p.getWorld().getSpawnLocation());
                                p.setFallDistance(0);
                                p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 10000, 0);
                                cancel();
                            }
                        }
                    }
                };
                run.runTaskTimer(mainPlugin, 4, 4);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        ScoreboardManager.registerScoreboard(event.getPlayer());
        setupStats(event.getPlayer());
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setItem(0, returnToSpawn);
        event.getPlayer().getInventory().setItem(8, HubReturn.getHubIcon());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (mainPlugin.getSecondsInHunt() < 0 || mainPlugin.getSecondsInHunt() == Integer.MAX_VALUE) {
            if (!Rank.MOD.hasRank(event.getPlayer())) {
                event.setResult(Result.KICK_OTHER);
                if (mainPlugin.getSecondsInHunt() < 0) {
                    event.setKickMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "The hunt will begin in "
                            + getTime(-mainPlugin.getSecondsInHunt()) + "!");
                } else {
                    event.setKickMessage(ChatColor.DARK_RED + "The hunt has ended!");
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
        if (event.getItem().getItemStack().getType() == Material.MONSTER_EGG) {
            for (Egg egg : mainPlugin.getEggs()) {
                if (egg.getItem().equals(event.getItem())) {
                    if (!egg.hasFound(event.getPlayer().getName())) {
                        event.getPlayer().setMetadata("PickedUp", new FixedMetadataValue(mainPlugin, System.currentTimeMillis()));
                        mainPlugin.addFoundEgg(egg, event.getPlayer().getName());
                        if (getEggsFound(event.getPlayer().getName()) == mainPlugin.getEggs().size()) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + event.getPlayer().getName()
                                    + " has found all " + mainPlugin.getEggs().size() + " eggs!");
                            CreditsApi.payWithoutSender(event.getPlayer().getName(), 50, true);
                        }
                        setupStats(event.getPlayer());
                        if (new Random().nextInt(3) != 0) {
                            CreditsApi.payWithoutSender(event.getPlayer().getName(), new Random().nextInt(10) + 5, true);
                        } else {
                            if (Rank.VIP.hasRank(event.getPlayer()) && Rank.VIP.getExpires(event.getPlayer().getName()) == 0) {
                                CreditsApi.payWithoutSender(event.getPlayer().getName(), new Random().nextInt(10) + 15, true);
                            } else {
                                RankLoader.addRank(Rank.VIP, event.getPlayer().getName(), 60 * 60 * 24 * 3);
                                event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Scored 3 days of VIP! Woo hoo!");
                            }
                        }
                    } else {
                        if (!event.getPlayer().hasMetadata("PickedUp")
                                || event.getPlayer().getMetadata("PickedUp").get(0).asLong() + 5000 < System.currentTimeMillis()) {
                            event.getPlayer().setMetadata("PickedUp",
                                    new FixedMetadataValue(mainPlugin, System.currentTimeMillis()));
                            event.getPlayer().sendMessage(
                                    ChatColor.RED + "You have already collected egg #" + (1 + mainPlugin.getEggs().indexOf(egg))
                                            + "!");
                        }
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    public void setupStats(Player player) {
        int eggsFound = getEggsFound(player.getName());
        mainPlugin.makeScore(player, mainPlugin.getMyEggsLooted()[0], String.format(mainPlugin.getMyEggsLooted()[1], eggsFound));
        mainPlugin.makeScore(player, mainPlugin.getMyEggsToLoot()[0],
                String.format(mainPlugin.getMyEggsToLoot()[1], (mainPlugin.getEggs().size() - eggsFound)));
    }
}
