package com.comze_instancelabs.oitc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static Main m = null;
	IArenaScoreboard scoreboard = new IArenaScoreboard(this);
	ICommandHandler cmdhandler = new ICommandHandler();

	Random r;

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "OneInTheChamber", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), true);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.scoreboardManager = new IArenaScoreboard(this);
		IArenaListener listener = new IArenaListener(this, pinstance, "OneInTheChamber");
		pinstance.setArenaListener(listener);
		MinigamesAPI.getAPI().registerArenaListenerLater(this, listener);
		pli = pinstance;
		try {
			pinstance.getClass().getMethod("setAchievementGuiEnabled", boolean.class);
			pinstance.setAchievementGuiEnabled(true);
		} catch (NoSuchMethodException e) {
			System.out.println("Update your MinigamesLib to the latest version to use the Achievement Gui.");
		}

		this.getConfig().addDefault("config.kills_to_win", 20);

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		r = new Random();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return cmdhandler.handleArgs(this, "mgoitc", "/" + cmd.getName(), sender, args);
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		return a;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (p.getLocation().getY() < 0) {
					Util.teleportPlayerFixed(p, a.getSpawns().get(r.nextInt(a.getSpawns().size())));
					a.onEliminated(p.getName());
					pli.getClassesHandler().getClass(p.getName());
					scoreboard.updateScoreboard(a);
					p.setHealth(20D);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				final Location l = p.getLocation();
				event.getDrops().clear();
				event.setDeathMessage(null);
				final IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					p.setHealth(20D);
					if (p.getKiller() instanceof Player) {
						Player killer = (Player) p.getKiller();
						if (!killer.getName().equalsIgnoreCase(p.getName())) {
							if (a.kills.containsKey(killer.getName())) {
								int i = a.kills.get(killer.getName());
								if (i >= this.getConfig().getInt("config.kills_to_win") - 1) {
									for (String p_ : a.getAllPlayers()) {
										if (!p_.equalsIgnoreCase(killer.getName())) {
											pli.global_lost.put(p_, a);
										}
									}
									a.stop();
									return;
								}
								a.kills.put(killer.getName(), i + 1);
							} else {
								a.kills.put(killer.getName(), 1);
							}
							a.lastdamager.put(p.getName(), killer.getName());
							a.onEliminated(p.getName());
						}
						killer.getInventory().addItem(new ItemStack(Material.ARROW));
						killer.updateInventory();
						Util.teleportPlayerFixed(p, a.getSpawns().get(r.nextInt(a.getSpawns().size())));
						scoreboard.updateScoreboard(a);
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								pli.getClassesHandler().getClass(p.getName());
								p.setHealth(20D);
							}
						}, 15L);
						p.setHealth(20D);
					}
				}
			}
		}
	}

	boolean isSupported = false;
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			Player attacker = null;
			if (event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(!isSupported){
					for (Method m : projectile.getClass().getDeclaredMethods()) {
						if (m.getName().equalsIgnoreCase("getshooter")) {
							isSupported = true;
						}
					}
					for (Method m : projectile.getClass().getMethods()) {
						if (m.getName().equalsIgnoreCase("getshooter")) {
							isSupported = true;
						}
					}
					if(!isSupported){
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your Bukkit version is too old and doesn't support getting the shooter of a projectile, please update!");
						return;
					}
				}
				if (projectile.getShooter() instanceof Player) {
					attacker = (Player) projectile.getShooter();
					if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
						IArena a = (IArena) pli.global_players.get(p.getName());
						if (!attacker.getName().equalsIgnoreCase(p.getName())) {
							if (a.kills.containsKey(attacker.getName())) {
								int i = a.kills.get(attacker.getName());
								if (i >= this.getConfig().getInt("config.kills_to_win") - 1) {
									for (String p_ : a.getAllPlayers()) {
										if (!p_.equalsIgnoreCase(attacker.getName())) {
											pli.global_lost.put(p_, a);
										}
									}
									a.stop();
									return;
								}
								a.kills.put(attacker.getName(), i + 1);
							} else {
								a.kills.put(attacker.getName(), 1);
							}
							a.lastdamager.put(p.getName(), attacker.getName());
							a.onEliminated(p.getName());
						} else {
							event.setCancelled(true);
							return;
						}
						Util.teleportPlayerFixed(p, a.getSpawns().get(r.nextInt(a.getSpawns().size())));
						attacker.getInventory().addItem(new ItemStack(Material.ARROW));
						attacker.updateInventory();
						pli.getClassesHandler().getClass(p.getName());
						scoreboard.updateScoreboard(a);
						p.setHealth(20D);
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								pli.getClassesHandler().getClass(p.getName());
								p.setHealth(20D);
							}
						}, 15L);
						return;
					}
				}
			} else if (event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else {
				return;
			}

			if (p != null && attacker != null) {
				if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
					if (pli.global_lost.containsKey(attacker.getName()) || pli.getSpectatorManager().isSpectating(p)) {
						event.setCancelled(true);
						return;
					}
					Arena a = (Arena) pli.global_players.get(p.getName());
					if (a.getArenaState() == ArenaState.INGAME) {
						// TODO
						// a.lastdamager.put(p.getName(), attacker.getName());
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			event.setCancelled(true);
		}
	}

}
