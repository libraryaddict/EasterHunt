package me.libraryaddict.easter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.Currency.CreditsApi;
import me.libraryaddict.Inventory.InventoryApi;
import me.libraryaddict.Loader.Rank;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.scoreboard.ScoreboardManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

public class EasterHunt extends JavaPlugin {
    private ArrayList<Egg> eggs = new ArrayList<Egg>();
    private Object properties;
    private int secondsInHunt;
    private int[] types = new int[] { 50, 51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 65, 66, 90, 91, 92, 93, 94, 95, 96, 98,
            100, 120 };
    private int i = 0;

    public void addFoundEgg(Egg egg, String name) {
        Bukkit.broadcastMessage(ChatColor.GOLD + name + " has just found egg #" + (eggs.indexOf(egg) + 1) + "!");
        egg.addFound(name);
        ScoreboardManager.addToTeam(this.getTotalEggs()[0], "TotalEggs", null,
                String.format(this.getTotalEggs()[1], getTotalEggsFound()), true);
        saveEgg(egg);
        egg.getLocation().getWorld().playEffect(egg.getItem().getLocation(), Effect.EXPLOSION_HUGE, 1);
        egg.getLocation().getWorld().playSound(egg.getItem().getLocation(), Sound.FIREWORK_TWINKLE, 3, 0);
    }

    private short getData(int index) {
        return (short) types[(i + index) % types.length];
    }

    public ArrayList<Egg> getEggs() {
        return eggs;
    }

    public int getLength(String msg) {
        int index = msg.indexOf("%,d");
        if (index == -1) {
            index = msg.indexOf("%s");
        }
        if (index > 16) {
            index = 16;
        }
        return index;
    }

    public String[] getMyEggsLooted() {
        String str = ChatColor.GOLD + "Eggs looted " + ChatColor.YELLOW + "%s";
        return new String[] { str.substring(0, getLength(str)), str.substring(getLength(str)) };
    }

    public String[] getMyEggsToLoot() {
        String str = ChatColor.GOLD + "Eggs to loot " + ChatColor.YELLOW + "%s";
        return new String[] { str.substring(0, getLength(str)), str.substring(getLength(str)) };
    }

    public int getSecondsInHunt() {
        return secondsInHunt;
    }

    public String[] getTimeToLoot() {
        String str = ChatColor.DARK_RED + "Time left " + ChatColor.RED + "%s";
        return new String[] { str.substring(0, getLength(str)), str.substring(getLength(str)) };
    }

    public String[] getTotalEggs() {
        String str = ChatColor.DARK_AQUA + "Total Eggs " + ChatColor.AQUA + "%,d";
        return new String[] { str.substring(0, getLength(str)), str.substring(getLength(str)) };
    }

    public int getTotalEggsFound() {
        int total = 0;
        for (Egg egg : eggs) {
            total += egg.getFinders().size();
        }
        return total;
    }

    public void makeScore(Player player, String key, String score) {
        ScoreboardManager.addToTeam(player, key, key, null, score, false);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("found")) {
            ArrayList<String> found = new ArrayList<String>();
            ArrayList<String> notFound = new ArrayList<String>();
            for (int i = 0; i < eggs.size(); i++) {
                Egg egg = eggs.get(i);
                if (egg.hasFound(sender.getName())) {
                    found.add("#" + (i + 1));
                } else {
                    notFound.add("#" + (i + 1));
                }
            }
            sender.sendMessage(ChatColor.DARK_AQUA + "Found: " + ChatColor.AQUA
                    + StringUtils.join(found, ChatColor.DARK_AQUA + ", " + ChatColor.AQUA));
            sender.sendMessage(ChatColor.DARK_RED + "Not found: " + ChatColor.RED
                    + StringUtils.join(notFound, ChatColor.DARK_RED + ", " + ChatColor.RED));
        } else if (sender.hasPermission("easter.reset")) {
            int radius = 5;
            if (args.length > 0) {
                radius = Integer.parseInt(args[0]);
            }
            for (Egg egg : eggs) {
                if (egg.getLocation().distance(((Player) sender).getLocation()) < radius
                        || egg.getItem().getLocation().distance(((Player) sender).getLocation()) < radius) {
                    egg.getItem().remove();
                    egg.setItem(egg
                            .getLocation()
                            .getWorld()
                            .dropItem(
                                    egg.getLocation(),
                                    InventoryApi.setNameAndLore(
                                            new ItemStack(Material.MONSTER_EGG, 1, getData(eggs.indexOf(egg))),
                                            System.currentTimeMillis() + "")));
                    egg.getItem().teleport(egg.getLocation());
                    egg.getItem().setVelocity(new Vector(0, 0.1, 0));
                    sender.sendMessage(ChatColor.RED + "Reset egg #" + (eggs.indexOf(egg) + 1));
                }
            }
        }
        return true;
    }

    public void onDisable() {
        for (Egg egg : eggs) {
            egg.getItem().remove();
        }
    }

    public void onEnable() {
        GameProfile libProfile = new GameProfile(UUID.fromString("679b97cb-3371-4927-8697-096e6668a1a9"), "libraryslave");
        ReflectionManager.getSkullBlob(libProfile);
        properties = libProfile.getProperties();
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        WrappedGameProfile profile = event.getPacket().getGameProfiles().read(0);
                        GameProfile pro = new GameProfile(UUID.fromString(profile.getId()), profile.getName());
                        try {
                            Field props = GameProfile.class.getDeclaredField("properties");
                            props.setAccessible(true);
                            props.set(pro, properties);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        event.getPacket().getModifier().write(1, pro);
                    }
                });
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new EasterListener(this), this);
        Location center = Bukkit.getWorld("world").getSpawnLocation();
        if (getConfig().contains("TimeTillHunt")) {
            this.secondsInHunt = getConfig().getInt("TimeTillHunt");
        } else {
            this.secondsInHunt = -(10 * 60);
            getConfig().set("TimeTillHunt", secondsInHunt);
            saveConfig();
        }
        if (getConfig().contains("Eggs")) {
            for (String x : getConfig().getConfigurationSection("Eggs").getKeys(false)) {
                for (String y : getConfig().getConfigurationSection("Eggs." + x).getKeys(false)) {
                    for (String z : getConfig().getConfigurationSection("Eggs." + x + "." + y).getKeys(false)) {
                        Location loc = new Location(center.getWorld(), Integer.parseInt(x), Integer.parseInt(y),
                                Integer.parseInt(z)).getBlock().getLocation();
                        Egg egg = new Egg(loc, new ArrayList<String>(getConfig().getStringList("Eggs." + x + "." + y + "." + z)));
                        eggs.add(egg);
                        System.out.print("Loaded egg at " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
                    }
                }
            }
        } else {
            for (int x = -350; x <= 350; x++) {
                for (int y = 0; y <= 254; y++) {
                    for (int z = -350; z <= 350; z++) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.BEACON) {
                            b.setType(Material.AIR);
                            Egg egg = new Egg(b.getLocation());
                            saveEgg(egg);
                            eggs.add(egg);
                            System.out.print("Found egg at " + b.getX() + " " + b.getY() + " " + b.getZ());
                        }
                    }
                }
            }
        }
        System.out.print("Loaded up " + eggs.size() + " eggs!");
        for (Egg egg : eggs) {
            egg.getLocation().getChunk().load();
            egg.setItem(egg
                    .getLocation()
                    .getWorld()
                    .dropItem(
                            egg.getLocation(),
                            InventoryApi.setNameAndLore(new ItemStack(Material.MONSTER_EGG, 1, getData(eggs.indexOf(egg))),
                                    System.currentTimeMillis() + "")));
            egg.getItem().teleport(egg.getLocation());
            egg.getItem().setVelocity(new Vector(0, 0.1, 0));
        }
        if (!getConfig().contains("Rewarded")) {
            HashMap<String, Integer> collected = new HashMap<String, Integer>();
            for (Egg egg : eggs) {
                for (String name : egg.getFinders()) {
                    if (!collected.containsKey(name)) {
                        collected.put(name, 1);
                    } else {
                        collected.put(name, collected.get(name) + 1);
                    }
                }
            }
            for (String name : collected.keySet()) {
                if (collected.get(name) >= 20) {
                    CreditsApi.payWithoutSender(name, 50, true);
                }
            }
            getConfig().set("Rewarded", true);
            saveConfig();
        }
        ScoreboardManager.setDisplayName(DisplaySlot.SIDEBAR, ChatColor.DARK_PURPLE + "Easter Hunt");
        ScoreboardManager.makeScore(DisplaySlot.SIDEBAR, this.getTimeToLoot()[0], 3);
        ScoreboardManager.makeScore(DisplaySlot.SIDEBAR, this.getTotalEggs()[0], 2);
        ScoreboardManager.addToTeam(this.getTotalEggs()[0], "TotalEggs", null,
                String.format(this.getTotalEggs()[1], this.getTotalEggsFound()), true);
        ScoreboardManager.makeScore(DisplaySlot.SIDEBAR, this.getMyEggsToLoot()[0], 1);
        ScoreboardManager.makeScore(DisplaySlot.SIDEBAR, this.getMyEggsLooted()[0], 0);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                Bukkit.getWorlds().get(0).setWeatherDuration(Integer.MAX_VALUE);
                if (secondsInHunt != Integer.MAX_VALUE) {
                    secondsInHunt++;
                    if (secondsInHunt == (60 * 60 * 24 * 2)) {
                        secondsInHunt = Integer.MAX_VALUE;
                    }
                    ScoreboardManager.addToTeam(getTimeToLoot()[0], "HuntEnds", null,
                            String.format(getTimeToLoot()[1], (60 * 24 * 2 * 60) - getSecondsInHunt()), true);
                    getConfig().set("TimeTillHunt", secondsInHunt);
                    saveConfig();
                    if (getSecondsInHunt() == Integer.MAX_VALUE) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (!Rank.MOD.hasRank(p)) {
                                p.kickPlayer(ChatColor.GOLD + "The easter hunt has ended!");
                            }
                        }
                    }
                }
            }
        }, 0, 20);
    }

    public void saveEgg(Egg egg) {
        Location loc = egg.getLocation();
        getConfig().set("Eggs." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ(), egg.getFinders());
        saveConfig();
    }
}
