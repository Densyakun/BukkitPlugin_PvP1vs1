package io.github.densyakun.bukkit.pvp1vs1;

import java.io.Serializable;
import java.util.UUID;

public class PlayerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	UUID uuid;
	int win = 0;
	int loss = 0;

	public PlayerData(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getWin() {
		return win;
	}

	public int getLoss() {
		return loss;
	}

	public void clear() {
		win = 0;
		loss = 0;
	}
}
