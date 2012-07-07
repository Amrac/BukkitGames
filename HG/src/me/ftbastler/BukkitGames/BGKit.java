package me.ftbastler.BukkitGames;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BGKit {
	private static BGMain plugin;
	static Logger log = Logger.getLogger("Minecraft");
	static HashMap<Player, String> KIT = new HashMap<Player, String>();

	public BGKit(BGMain instan) {
		plugin = instan;
	}

	public static void giveKit(Player p) {
		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);

		if (!KIT.containsKey(p)) {
			if (plugin.COMPASS.booleanValue()) {
				p.getInventory().addItem(
						new ItemStack[] { new ItemStack(Material.COMPASS, 1) });
			}
			return;
		}

		String kitname = (String) KIT.get(p);
		FileConfiguration kitConfig = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder(), "kit.yml"));
		ConfigurationSection kit = kitConfig.getConfigurationSection(kitname
				.toLowerCase());

		List<String> kititems = kit.getStringList("ITEMS");
		for (String item : kititems) {
			String[] oneitem = item.split(",");
			ItemStack i = null;
			Integer id = null;
			Integer amount = null;
			Short durability = null;
			if (oneitem[0].contains(":")) {
				String[] ITEM_ID = oneitem[0].split(":");
				id = Integer.valueOf(Integer.parseInt(ITEM_ID[0]));
				amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				durability = Short.valueOf(Short.parseShort(ITEM_ID[1]));
				i = new ItemStack(id.intValue(), amount.intValue(),
						durability.shortValue());
			} else {
				id = Integer.valueOf(Integer.parseInt(oneitem[0]));
				amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				i = new ItemStack(id.intValue(), amount.intValue());
			}

			if (oneitem.length == 4) {
				i.addUnsafeEnchantment(
						Enchantment.getById(Integer.parseInt(oneitem[2])),
						Integer.parseInt(oneitem[3]));
			}

			if ((id.intValue() < 298) || (317 < id.intValue())) {
				p.getInventory().addItem(new ItemStack[] { i });
			} else if ((id.intValue() == 298) || (id.intValue() == 302)
					|| (id.intValue() == 306) || (id.intValue() == 310)
					|| (id.intValue() == 314)) {
				i.setAmount(1);
				p.getInventory().setHelmet(i);
			} else if ((id.intValue() == 299) || (id.intValue() == 303)
					|| (id.intValue() == 307) || (id.intValue() == 311)
					|| (id.intValue() == 315)) {
				i.setAmount(1);
				p.getInventory().setChestplate(i);
			} else if ((id.intValue() == 300) || (id.intValue() == 304)
					|| (id.intValue() == 308) || (id.intValue() == 312)
					|| (id.intValue() == 316)) {
				i.setAmount(1);
				p.getInventory().setLeggings(i);
			} else if ((id.intValue() == 301) || (id.intValue() == 305)
					|| (id.intValue() == 309) || (id.intValue() == 313)
					|| (id.intValue() == 317)) {
				i.setAmount(1);
				p.getInventory().setBoots(i);
			}
		}

		String pot = kit.getString("POTION");
		if (pot != null & pot != "") {
			if (!pot.equals(0)) {
				String[] potion = pot.split(",");
				if (Integer.parseInt(potion[0]) != 0) {
					if (Integer.parseInt(potion[1]) == 0) {
						p.addPotionEffect(new PotionEffect(PotionEffectType
								.getById(Integer.parseInt(potion[0])),
								plugin.MAX_GAME_RUNNING_TIME * 1200, Integer
										.parseInt(potion[2])));
					} else {
						p.addPotionEffect(new PotionEffect(PotionEffectType
								.getById(Integer.parseInt(potion[0])), Integer
								.parseInt(potion[1]) * 20, Integer
								.parseInt(potion[2])));
					}
				}
			}
		}

		if (plugin.COMPASS.booleanValue())
			p.getInventory().addItem(
					new ItemStack[] { new ItemStack(Material.COMPASS, 1) });
	}

	public static void setKit(Player player, String kitname) {
		kitname = kitname.toLowerCase();
		FileConfiguration kitConfig = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder(), "kit.yml"));
		ConfigurationSection kit = kitConfig.getConfigurationSection(kitname);

		if (kit == null) {
			BGChat.printPlayerChat(player, "That kit doesn't exist!");
			return;
		}

		if (BGMain.perms.has(player, "bg.kit." + kitname)
				|| BGMain.perms.has(player, "bg.kit.*")
				|| plugin.winner(player)) {
			if (KIT.containsKey(player)) {
				KIT.remove(player);
			}

			KIT.put(player, kitname);
			char[] stringArray = kitname.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			kitname = new String(stringArray);
			BGChat.printPlayerChat(player, "You have chosen " + kitname
					+ " as your kit.");

			if (plugin.winner(player))
				player.setDisplayName("§8[" + kitname + "] §r" + ChatColor.GOLD
						+ player.getName() + ChatColor.WHITE);
			else if (plugin.hasPerm(player, "bg.admin.color")
					|| BGMain.perms.has(player, "bg.admin.*"))
				player.setDisplayName("§8[" + kitname + "] §r" + ChatColor.RED
						+ player.getName() + ChatColor.WHITE);
			else if (plugin.hasPerm(player, "bg.vip.color")
					|| BGMain.perms.has(player, "bg.vip.*"))
				player.setDisplayName("§8[" + kitname + "] §r" + ChatColor.BLUE
						+ player.getName() + ChatColor.WHITE);
			else
				player.setDisplayName("§8[" + kitname + "] §r"
						+ ChatColor.WHITE + player.getName() + ChatColor.WHITE);
		} else {
			BGChat.printPlayerChat(player, plugin.NO_KIT_MSG);
			return;
		}
	}

	public static Boolean hasAbility(Player player, Integer ability) {
		if (!KIT.containsKey(player)) {
			return Boolean.valueOf(false);
		}

		String kitname = (String) KIT.get(player);
		FileConfiguration kitConfig = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder(), "kit.yml"));
		ConfigurationSection kit = kitConfig.getConfigurationSection(kitname);

		Integer i = Integer.valueOf(kit.getInt("ABILITY"));
		if (i == ability) {
			return Boolean.valueOf(true);
		}
		return Boolean.valueOf(false);
	}

	public static String getKit(Player player) {
		String kitname = KIT.get(player);
		return kitname;
	}
}