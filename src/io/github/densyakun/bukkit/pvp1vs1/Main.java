package io.github.densyakun.bukkit.pvp1vs1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.densyakun.bukkit.minigamemanager.Game;
import io.github.densyakun.bukkit.minigamemanager.MiniGameCommandListener;
import io.github.densyakun.bukkit.minigamemanager.MiniGameManager;

public class Main extends JavaPlugin implements Listener, MiniGameCommandListener {
	public static final String param_is_not_enough = "パラメータが足りません";
	public static final String param_wrong_cmd = "パラメータが間違っています";
	public static final String cmd_player_only = "このコマンドはプレイヤーのみ実行できます";

	public static Main main;
	String prefix;
	World lobby;
	int starttime = 10;
	int endtime = 180;
	int stoptime = 5;
	double prize = 100.0;
	double lost = 10.0;
	boolean minus = false;
	String defaultkit = "default";
	private File mapsfile;
	private File datafile;
	private File kitsfile;
	private ArrayList<Map1vs1> maps;
	private ArrayList<PlayerData> pdata;
	private ArrayList<Kit> kits = new ArrayList<Kit>();

	@Override
	public void onEnable() {
		Main.main = this;
		prefix = ChatColor.GREEN + "[" + getName() + "]";
		mapsfile = new File(getDataFolder(), "maps.dat");
		datafile = new File(getDataFolder(), "data.dat");
		kitsfile = new File(getDataFolder(), "kits.dat");
		load();
		getServer().getPluginManager().registerEvents(this, this);
		MiniGameManager.minigamemanager.addMiniGameCommandListener(this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "]有効");
	}

	@SuppressWarnings("unchecked")
	public void load() {
		saveDefaultConfig();
		lobby = getServer().getWorld(getConfig().getString("lobby-world", "world"));
		if (lobby != null) {
			getServer().getConsoleSender()
					.sendMessage(ChatColor.GREEN + "[" + getName() + "] Lobby: " + lobby.toString());
		} else {
			getServer().getConsoleSender()
					.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "ロビーのワールドが見つかりません");
		}
		starttime = getConfig().getInt("start-time", starttime);
		endtime = getConfig().getInt("end-time", endtime);
		stoptime = getConfig().getInt("stop-time", stoptime);
		prize = getConfig().getDouble("prize", prize);
		lost = getConfig().getDouble("lost", lost);
		minus = getConfig().getBoolean("minus", minus);
		defaultkit = getConfig().getString("defaultkit", defaultkit);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapsfile));
			maps = (ArrayList<Map1vs1>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			maps = new ArrayList<Map1vs1>();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datafile));
			pdata = (ArrayList<PlayerData>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			pdata = new ArrayList<PlayerData>();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(kitsfile));
			kits = (ArrayList<Kit>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			kits = new ArrayList<Kit>();
		}
	}

	@Override
	public boolean MiniGameCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("1vs1")) {
			if (args.length == 1) {
				sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
				sender.sendMessage(ChatColor.GREEN + "/game 1vs1 (map|kit|join|stats)");
			} else if (args[1].equalsIgnoreCase("map")) {
				if (sender.isOp() || sender.hasPermission("pvp1vs1.admin")) {
					if (args.length == 2) {
						sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map (create|delete|spawn)");
					} else if (args[2].equalsIgnoreCase("create")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map create (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(prefix + ChatColor.RED + "そのマップ名は使用されています");
									return true;
								}
							}
							
							maps.add(new Map1vs1(args[3], null, null));
							sender.sendMessage(prefix + ChatColor.AQUA + "新しいマップを作成しました マップ名: " + args[3]);
							mapsave();
						}
					} else if (args[2].equalsIgnoreCase("delete")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map delete (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(prefix + ChatColor.AQUA + "マップを削除しました マップ名: " + args[3]);
									maps.remove(a);
									mapsave();
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりません");
						}
					} else if (args[2].equalsIgnoreCase("spawn")) {
						if (args.length <= 5) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map) (red|blue)");
						} else if (args[3].equalsIgnoreCase("set")) {
							for (int b = 0; b < maps.size(); b++) {
								Map1vs1 map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									if (args[5].equalsIgnoreCase("red")) {
										map.redspawn = adjustLocation(((Entity) sender).getLocation());
										mapsave();
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(赤)を設定しました");
									} else if (args[5].equalsIgnoreCase("blue")) {
										map.bluespawn = adjustLocation(((Entity) sender).getLocation());
										mapsave();
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(青)を設定しました");
									} else {
										sender.sendMessage(prefix + ChatColor.RED + "\"red\"または\"blue\"を指定して下さい");
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else if (args[3].equalsIgnoreCase("get")) {
							for (int b = 0; b < maps.size(); b++) {
								Map1vs1 map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									if (args[5].equalsIgnoreCase("red")) {
										if (map.redspawn == null) {
											sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(赤)は設定されていません");
										} else {
											((Entity) sender).teleport(map.redspawn);
											sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(赤)に移動しました");
										}
									} else if (args[5].equalsIgnoreCase("blue")) {
										if (map.bluespawn == null) {
											sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(青)は設定されていません");
										} else {
											((Entity) sender).teleport(map.bluespawn);
											sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点(青)に移動しました");
										}
									} else {
										sender.sendMessage(prefix + ChatColor.RED + "\"red\"または\"blue\"を指定して下さい");
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else {
							sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map) (red|blue)");
						}
					} else {
						sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map (create|delete|spawn)");
					}
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "権限がありません");
				}
			} else if (args[1].equalsIgnoreCase("kit")) {
				if (sender instanceof Player && (sender.isOp() || sender.hasPermission("pvp1vs1.admin"))) {
					if (args.length <= 2) {
						sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN + "/game 1vs1 kit (set|get)");
					} else if (args[2].equalsIgnoreCase("set")) {
						if (args.length <= 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 kit set (kit)");
						} else {
							PlayerInventory inv = ((HumanEntity) sender).getInventory();
							Kit kit = new Kit(args[3]);
							kit.setKit(inv);
							putKit(kit);
							kitssave();
							sender.sendMessage(prefix + ChatColor.AQUA + "キット\"" + args[3] + "\"を設定しました");
						}
					} else if (args[2].equalsIgnoreCase("get")) {
						if (args.length <= 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 kit set (kit)");
						} else {
							Kit kit = getKit(args[3]);
							if (kit == null) {
								sender.sendMessage(prefix + ChatColor.RED + "キットが見つかりません");
							} else {
								kit.applyKit(((HumanEntity) sender).getInventory());
								sender.sendMessage(prefix + ChatColor.AQUA + "キット\"" + args[3] + "\"を呼び出しました");
								return true;
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN + "/game 1vs1 kit (set|get)");
					}
				} else {
					sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "権限がありません");
				}
			} else if (args[1].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (MiniGameManager.minigamemanager
							.getPlayingGame(((OfflinePlayer) sender).getUniqueId()) == null) {
						List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
						for (int a = 0; a < games.size(); a++) {
							Game game = games.get(a);
							if (!(game instanceof Game1vs1 && game.isJoinable())) {
								games.remove(a);
							}
						}
						if (args.length != 2) {
							for (int a = 0; a < maps.size(); a++) {
								Map1vs1 map = maps.get(a);
								if (map.mapname.equals(args[2])) {
									for (int c = 0; c < games.size(); c++) {
										Game game = games.get(c);
										if (game instanceof Game1vs1
												&& map.mapname.equals(((Game1vs1) game).map.mapname)) {
											if (!MiniGameManager.minigamemanager.joinGame((Player) sender, game)) {
												sender.sendMessage(prefix
														+ ChatColor.RED + "このマップが使用しているゲームに入ることが出来ません");
											}
											return true;
										}
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりません");
						}
						if (0 < games.size()) {
							int a;
							while (0 < games.size() && !MiniGameManager.minigamemanager.joinGame((Player) sender,
									games.get(a = new Random().nextInt(games.size())))) {
								games.remove(a);
							}
						}
						if (0 == games.size()) {
							List<Map1vs1> b = maps;
							for (int c = 0; c < b.size();) {
								boolean d = true;
								for (int e = 0; e < games.size(); e++) {
									Game game = games.get(e);
									if (game instanceof Game1vs1 && maps.get(c).equals(((Game1vs1) game).map)) {
										b.remove(c);
										d = false;
										break;
									}
								}
								if (d) {
									c++;
								}
							}
							if (0 < b.size()) {
								MiniGameManager.minigamemanager.joinGame((Player) sender,
										new Game1vs1(b.get(new Random().nextInt(b.size()))));
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "使用可能なマップがありません");
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "ゲーム中です");
					}
				}
			} else if (args[1].equalsIgnoreCase("stats")) {
				if (args.length <= 2) {
					if (sender instanceof Player) {
						PlayerData data = getPlayerData(((OfflinePlayer) sender).getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD + ((Player) sender).getDisplayName()
											+ "の情報: \nWin: " + data.getWin() + "\nLoss: " + data.getLoss());
						}
					}
				} else {
					@SuppressWarnings("deprecation")
					OfflinePlayer player = getServer().getOfflinePlayer(args[2]);
					if (player != null) {
						PlayerData data = getPlayerData(player.getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD
									+ (player.getPlayer() != null ? player.getPlayer().getDisplayName()
											: player.getName())
									+ "の情報: \nWin: " + data.getWin() + "\nLoss: " + data.getLoss());
						}
					}
				}
			} else {
				sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
				sender.sendMessage(ChatColor.GREEN + "/game 1vs1 (map|kit|join|stats)");
			}
			return true;
		}
		return false;
	}

	public void mapsave() {
		getDataFolder().mkdirs();
		try {
			mapsfile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mapsfile));
			oos.writeObject(maps);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void kitssave() {
		getDataFolder().mkdirs();
		try {
			kitsfile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(kitsfile));
			oos.writeObject(kits);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pdatasave() {
		getDataFolder().mkdirs();
		try {
			datafile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datafile));
			oos.writeObject(pdata);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Kit getKit(String name) {
		for (int a = 0; a < kits.size(); a++) {
			Kit kit = kits.get(a);
			if (kit.name.equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}

	public void putKit(Kit kit) {
		for (int a = 0; a < kits.size(); a++) {
			if (kits.get(a).name.equalsIgnoreCase(kit.name)) {
				kits.set(a, kit);
				return;
			}
		}
		kits.add(kit);
	}

	public PlayerData getPlayerData(UUID uuid) {
		for (int a = 0; a < pdata.size(); a++) {
			if (pdata.get(a).getUuid().equals(uuid)) {
				return pdata.get(a);
			}
		}
		PlayerData data = new PlayerData(uuid);
		pdata.add(data);
		return data;
	}

	public void UpdatePlayerData(PlayerData data) {
		boolean a = true;
		for (int b = 0; b < pdata.size(); b++) {
			if (pdata.get(b).getUuid().equals(data.getUuid())) {
				a = false;
				pdata.set(b, data);
				break;
			}
		}
		if (a) {
			pdata.add(data);
		}
		pdatasave();
	}

	public List<Map1vs1> getMaps() {
		return maps;
	}

	public List<Map1vs1> getEnabledMaps() {
		List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
		List<Map1vs1> a = maps;
		for (int b = 0; b < games.size(); b++) {
			Game game = games.get(b);
			if (game instanceof Game1vs1) {
				for (int c = 0; c < a.size();) {
					if (((Game1vs1) game).map.mapname.equals(a.get(c))) {
						a.remove(c);
						break;
					} else {
						c++;
					}
				}
			}
		}
		return a;
	}

	public Location adjustLocation(Location location) {
		location.setX((double) (Math.round(location.getX() * 2)) / 2);
		location.setY((double) (Math.round(location.getY() * 2)) / 2);
		location.setZ((double) (Math.round(location.getZ() * 2)) / 2);
		location.setYaw((float) (Math.round(location.getYaw() / 15)) * 15);
		location.setPitch((float) (Math.round(location.getPitch() / 15)) * 15);
		return location;
	}

	@EventHandler
	public void PlayerMove(PlayerMoveEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered() && !game.isStarted()) {
			e.setTo(new Location(e.getPlayer().getWorld(), e.getFrom().getX(), e.getTo().getY(), e.getFrom().getZ(),
					e.getTo().getYaw(), e.getTo().getPitch()));
		}
	}

	@EventHandler
	public void EntityDamage(EntityDamageEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getEntity().getUniqueId());
		if (game != null && game instanceof Game1vs1 && (!game.isStarted() || game.isEnded())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getEntity().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered()) {
			e.setKeepInventory(true);
			e.setDeathMessage(null);
			((Game1vs1) game).loss(e.getEntity());
		}
	}

	@EventHandler
	public void PlayerTeleport(PlayerTeleportEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered() && e.getCause() == TeleportCause.COMMAND) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerPickupItem(PlayerPickupItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerDropItem(PlayerDropItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void BlockBreak(BlockBreakEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void BlockPlace(BlockPlaceEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof Game1vs1 && game.isEntered()) {
			e.setCancelled(true);
		}
	}
}
