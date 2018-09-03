package io.github.densyakun.bukkit.pvp1vs1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Kit defaultkit;
	public String name;
	public transient HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
	public transient ItemStack helmet;
	public transient ItemStack chestplate;
	public transient ItemStack leggings;
	public transient ItemStack boots;

	public Kit(String name) {
		this.name = name;
	}

	public void setKit(PlayerInventory playerinv) {
		for (int a = 0; a < playerinv.getSize(); a++) {
			ItemStack item = playerinv.getItem(a);
			if (item != null) {
				items.put(a, item);
			}
		}
		helmet = playerinv.getHelmet();
		chestplate = playerinv.getChestplate();
		leggings = playerinv.getLeggings();
		boots = playerinv.getBoots();
	}

	public void applyKit(PlayerInventory inventory) {
		inventory.clear();
		Iterator<Integer> a = items.keySet().iterator();
		while (a.hasNext()) {
			Integer b = a.next();
			inventory.setItem(b, items.get(b));
		}
		inventory.setHelmet(helmet);
		inventory.setChestplate(chestplate);
		inventory.setLeggings(leggings);
		inventory.setBoots(boots);
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		HashMap<Integer, HashMap<String, Object>> a = new HashMap<Integer, HashMap<String, Object>>();
		Iterator<Integer> b = items.keySet().iterator();
		while (b.hasNext()) {
			Integer key = b.next();
			a.put(key, new HashMap<String, Object>(items.get(key).serialize()));
		}
		stream.writeObject(a);
		stream.writeObject(helmet == null ? null : helmet.serialize());
		stream.writeObject(chestplate == null ? null : chestplate.serialize());
		stream.writeObject(leggings == null ? null : leggings.serialize());
		stream.writeObject(boots == null ? null : boots.serialize());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		items = new HashMap<Integer, ItemStack>();
		HashMap<Integer, HashMap<String, Object>> b = (HashMap<Integer, HashMap<String, Object>>) stream.readObject();
		Iterator<Integer> c = b.keySet().iterator();
		while (c.hasNext()) {
			Integer key = c.next();
			items.put(key, ItemStack.deserialize(b.get(key)));
		}
		Map<String, Object> d = (Map<String, Object>) stream.readObject();
		if (d != null) {
			helmet = ItemStack.deserialize(d);
		}
		d = (Map<String, Object>) stream.readObject();
		if (d != null) {
			chestplate = ItemStack.deserialize(d);
		}
		d = (Map<String, Object>) stream.readObject();
		if (d != null) {
			leggings = ItemStack.deserialize(d);
		}
		d = (Map<String, Object>) stream.readObject();
		if (d != null) {
			boots = ItemStack.deserialize(d);
		}
	}

	public static Kit getDefaultKit() {
		return defaultkit == null ? defaultkit = new Kit("default") : defaultkit;
	}
}
