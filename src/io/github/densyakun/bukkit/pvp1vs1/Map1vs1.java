package io.github.densyakun.bukkit.pvp1vs1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.bukkit.Location;

public class Map1vs1 implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String mapname;
	public transient Location redspawn;
	public transient Location bluespawn;

	public Map1vs1(String mapname, Location redspawn, Location bluespawn) {
		this.mapname = mapname;
		this.redspawn = redspawn;
		this.bluespawn = bluespawn;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		stream.writeObject(redspawn == null ? null : redspawn.serialize());
		stream.writeObject(bluespawn == null ? null : bluespawn.serialize());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		Map<String, Object> spawn = (Map<String, Object>) stream.readObject();
		if (spawn != null) {
			redspawn = Location.deserialize(spawn);
		}
		spawn = (Map<String, Object>) stream.readObject();
		if (spawn != null) {
			bluespawn = Location.deserialize(spawn);
		}
	}
}
