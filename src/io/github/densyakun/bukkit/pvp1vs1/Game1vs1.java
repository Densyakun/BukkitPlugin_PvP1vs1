package io.github.densyakun.bukkit.pvp1vs1;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;

import io.github.densyakun.bukkit.minigamemanager.Game;

public class Game1vs1 extends Game {
	public static final String name = "PvP1vs1";
	public Map1vs1 map;
	//public Map<UUID, Kit> setkits = new HashMap<UUID, Kit>();
	public boolean determined = false;

	public Game1vs1(Map1vs1 map) {
		super(name);
		this.map = map;
		setEntrytime(0);
		setStarttime(Main.main.starttime);
		setEndtime(Main.main.endtime);
		setStoptime(Main.main.stoptime);
		setMinplayers(2);
		setMaxplayers(2);
	}

	@Override
	public void stop() {
		List<Player> players = getPlayers();
		for (int a = 0; a < players.size(); a++) {
			players.get(a).getInventory().clear();
			players.get(a).getInventory().setArmorContents(null);
			players.get(a).setGameMode(GameMode.ADVENTURE);
		}
		if (Main.main.lobby != null) {
			for (int a = 0; a < players.size(); a++) {
				players.get(a).teleport(Main.main.lobby.getSpawnLocation());
			}
		}
		super.stop();
	}

	@Override
	public void removePlayer(UUID uuid) {
		Player player = Main.main.getServer().getPlayer(uuid);
		if (player != null) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setGameMode(GameMode.ADVENTURE);
			if (isEntered()) {
				loss(player);
				if (Main.main.lobby != null) {
					player.teleport(Main.main.lobby.getSpawnLocation());
				}
			}
		}
		super.removePlayer(uuid);
	}

	@Override
	public void entered() {
		super.entered();
		List<Player> players = getPlayers();
		Collections.shuffle(players);
		for (int a = 0; a < players.size(); a++) {
			players.get(a).getInventory().clear();
			Kit kit = Main.main.getKit(Main.main.defaultkit);
			//if ((kit = setkits.get(players.get(a).getUniqueId())) == null) {
				if (kit == null) {
					kit = Kit.getDefaultKit();
				}
			//}
			kit.applyKit(players.get(a).getInventory());
			players.get(a).leaveVehicle();
			players.get(a).resetMaxHealth();
			players.get(a).setFireTicks(0);
			players.get(a).setFoodLevel(20);
			players.get(a).setGameMode(GameMode.SURVIVAL);
			players.get(a).setHealth(players.get(a).getMaxHealth());
			if (a == 0) {
				if (map.redspawn != null && !players.get(a).teleport(map.redspawn)) {
					stop();
				}
			} else if (map.bluespawn != null && !players.get(a).teleport(map.bluespawn)) {
				stop();
			}
		}
	}

	@Override
	public void end() {
		super.end();
		if (!determined) {
			List<Player> players = getPlayers();
			for (int a = 0; a < players.size(); a++) {
				players.get(a)
						.sendMessage(ChatColor.GREEN + "[" + Main.main.getName() + "] " + ChatColor.RED + "引き分けです");
			}
		}
	}

	public void loss(Player player) {
		determined = true;
		PlayerData data = Main.main.getPlayerData(player.getUniqueId());
		data.loss += 1;
		Main.main.UpdatePlayerData(data);
		Player winner = null;
		List<Player> players = getPlayers();
		for (int a = 0; a < players.size(); a++) {
			Player p = players.get(a);
			if (!p.getUniqueId().equals(player.getUniqueId())) {
				winner = p;
				break;
			}
		}
		if (winner != null) {
			for (int a = 0; a < players.size(); a++) {
				players.get(a).sendMessage(Main.main.prefix + ChatColor.WHITE
						+ winner.getDisplayName() + ChatColor.AQUA + "が" + ChatColor.WHITE
						+ player.getDisplayName() + ChatColor.AQUA + "に勝利しました");
			}
			PlayerData killerdata = Main.main.getPlayerData(winner.getUniqueId());
			killerdata.win += 1;
			Main.main.UpdatePlayerData(killerdata);
		}
		if (Main.main.getServer().getPluginManager().getPlugin("iConomy") != null) {
			Account account = new Account(player.getName());
			if (0.1 <= ((!Main.main.minus && account.getHoldings().getBalance() < Main.main.lost)
					? account.getHoldings().getBalance() : Main.main.lost)) {
				iConomy.Template.set(Template.Node.PLAYER_DEBIT);
				if (!Main.main.minus && account.getHoldings().getBalance() < Main.main.lost) {
					iConomy.Template.add("amount", iConomy.format(account.getHoldings().getBalance()));
					account.getHoldings().subtract(account.getHoldings().getBalance());
				} else {
					account.getHoldings().subtract(Main.main.lost);
					iConomy.Template.add("amount", iConomy.format(Main.main.lost));
				}
				iConomy.Template.add("name", player.getName());
				Messaging.send(player, iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
			}
			if (0.1 <= Main.main.prize) {
				new Account(winner.getName()).getHoldings().add(Main.main.prize);
				iConomy.Template.set(Template.Node.PLAYER_CREDIT);
				iConomy.Template.add("name", winner.getName());
				iConomy.Template.add("amount", iConomy.format(Main.main.prize));
				Messaging.send(winner,
						iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
			}
		}
		stop();
	}
}
