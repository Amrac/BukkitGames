package me.ftbastler.BukkitGames;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class BGChat {
	private static BGMain plugin;
	static String tip1 = "";
	static String tip2 = "";
	static String tip3 = "";
	static String tip4 = "";
	static String tip5 = "";
	static String tip6 = "";
	static Integer TIP_COUNT = Integer.valueOf(0);

	static String TIMER_MSG = ""; 
	static String DEATH_MSG = ""; 
	static String INFO_MSG = "Welcome to the HungerGames! | Get your kit now: §f/kit"; 
	static HashMap<Player, String> PLAYER_MSG = new HashMap<Player, String>();
	static HashMap<Player, Boolean> KIT_CHAT = new HashMap<Player, Boolean>();
	static HashMap<Player, Boolean> HELP_CHAT = new HashMap<Player, Boolean>();
	static HashMap<Player, String> KITINFO_CHAT = new HashMap<Player, String>();
	static String[] CHAT_MSG = new String[6];
	static HashMap<Integer, String> ABILITY_DESC = new HashMap<Integer, String>();

	static Boolean update = true;

	public BGChat(BGMain ins) {
		plugin = ins;
		CHAT_MSG[0] = "";
		CHAT_MSG[1] = "";
		CHAT_MSG[2] = "";
		CHAT_MSG[3] = "";
		CHAT_MSG[4] = "";
		CHAT_MSG[5] = "";

		ABILITY_DESC.put(1, "Arrows fired at the ground at explode.");
		ABILITY_DESC.put(2,
				"When breaking a log, all other logs above will break, too.");
		ABILITY_DESC.put(3, "Throwing snowballs causes confusion to players.");
		ABILITY_DESC.put(4, "Fire charges get launched via left click.");
		ABILITY_DESC.put(5, "When eating a cookie, you get strength.");
		ABILITY_DESC.put(6, "Gain strength when in fire and no fire damage.");
		ABILITY_DESC.put(7, "Pigs always drop 2 porkchop.");
		ABILITY_DESC
				.put(8,
						"Take max of 2 fall damage. Damage you would have taken is transfered to those within a 5 block radius of you.");
		ABILITY_DESC
				.put(9,
						"Headshot (instant kill) a player, if you shoot him from afar.");
	}

	public static void playerChatMsg(String message) {
		CHAT_MSG[5] = CHAT_MSG[4];
		CHAT_MSG[4] = CHAT_MSG[3];
		CHAT_MSG[3] = CHAT_MSG[2];
		CHAT_MSG[2] = CHAT_MSG[1];
		CHAT_MSG[1] = CHAT_MSG[0];
		CHAT_MSG[0] = message;
		updateChat();
	}

	public static void printInfoChat(String text) {
		if (plugin.ADV_CHAT_SYSTEM) {
			INFO_MSG = text;
			updateChat();
		} else {
			Bukkit.getServer().broadcastMessage("§2" + text);
		}
	}

	public static void printDeathChat(String text) {
		if (plugin.ADV_CHAT_SYSTEM) {
			DEATH_MSG = text;
			updateChat();
		} else {
			Bukkit.getServer().broadcastMessage("§c" + text);
		}
	}

	public static void printTimeChat(String text) {
		if (plugin.ADV_CHAT_SYSTEM) {
			TIMER_MSG = text;
			updateChat();
		} else {
			Bukkit.getServer().broadcastMessage("§a" + text);
		}
	}

	public static void printPlayerChat(Player player, String text) {
		if (plugin.ADV_CHAT_SYSTEM) {
			PLAYER_MSG.put(player, "§7" + text);
			updateChat(player);
			final Player pl = player;
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							BGChat.PLAYER_MSG.remove(pl);
						}
					}, 100);
		} else {
			player.sendMessage("§a" + text);
		}
	}

	public static void printHelpChat(Player player) {
		if (plugin.ADV_CHAT_SYSTEM) {
			HELP_CHAT.put(player, true);
			updateChat();
			final Player pl = player;
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							BGChat.HELP_CHAT.remove(pl);
						}
					}, 100);
		} else {
			BGChat.printPlayerChat(player, plugin.SERVER_TITLE);
			String are = "are";
			String players = "players";
			if (plugin.getGamers().length == 1) {
				are = "is";
				players = "player";
			}

			Integer timeleft = plugin.MAX_GAME_RUNNING_TIME
					- plugin.GAME_RUNNING_TIME;
			String is = "are";
			String minute = "minutes";
			if (timeleft <= 1) {
				is = "minute";
				minute = "minute";
			}
			player.sendMessage("§7 - There " + are + " "
					+ plugin.getGamers().length + " " + players + " online.");
			player.sendMessage("§7 - There " + is + " " + timeleft + " "
					+ minute + " left.");
			if (plugin.HELP_MESSAGE != null && plugin.HELP_MESSAGE != "")
				player.sendMessage("§7 - " + plugin.HELP_MESSAGE);
		}
	}

	public static void printKitChat(Player player) {
		if (plugin.ADV_CHAT_SYSTEM) {
			KIT_CHAT.put(player, true);
			updateChat();
			final Player pl = player;
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							BGChat.KIT_CHAT.remove(pl);
						}
					}, 100);
		} else {
			FileConfiguration kitConfig = YamlConfiguration
					.loadConfiguration(new File(plugin.getDataFolder(),
							"kit.yml"));
			List<String> kitname = kitConfig.getStringList("KITS");

			String yourkits = "";
			String otherkits = "";
			for (String name : kitname) {
				if (BGMain.perms.has(player, "bg.kit." + name)
						|| BGMain.perms.has(player, "bg.kit.*")
						|| plugin.winner(player)) {
					yourkits = name + ", " + yourkits;
				} else {
					otherkits = name + ", " + otherkits;
				}
			}
			player.sendMessage("");
			player.sendMessage("§bAvailable kits: §7(/kit [KitName])");
			player.sendMessage("");
			player.sendMessage("§aYour kits: §f" + yourkits);
			player.sendMessage("§aOther kits: §f" + otherkits);
			player.sendMessage("§bMore kits available at: "
					+ plugin.KIT_BUY_WEB);
			player.sendMessage("");
		}
	}

	public static void printKitInfo(Player player, String kitname) {
		String kitinfoname = kitname;
		kitname = kitname.toLowerCase();
		FileConfiguration kitConfig = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder(), "kit.yml"));
		ConfigurationSection kit = kitConfig.getConfigurationSection(kitname);
		if (kit == null) {
			printPlayerChat(player,
					"That kit doesn't exist! View all kits with /kit");
			return;
		}

		if (plugin.ADV_CHAT_SYSTEM) {
			KITINFO_CHAT.put(player, kitname);
			updateChat();
			final Player pl = player;
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							BGChat.KITINFO_CHAT.remove(pl);
						}
					}, 100);
		} else {

			char[] stringArray = kitinfoname.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			kitinfoname = new String(stringArray);

			player.sendMessage("§a" + kitinfoname + " kit includes:");

			List<String> kititems = kit.getStringList("ITEMS");
			for (String item : kititems) {
				String[] oneitem = item.split(",");

				String itemstring = null;
				Integer id = null;
				Integer amount = null;
				String enchantment = null;
				String ench_numb = null;

				if (oneitem[0].contains(":")) {
					String[] ITEM_ID = oneitem[0].split(":");
					id = Integer.valueOf(Integer.parseInt(ITEM_ID[0]));
					amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				} else {
					id = Integer.valueOf(Integer.parseInt(oneitem[0]));
					amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				}

				itemstring = " - "
						+ amount
						+ "x "
						+ Material.getMaterial(id.intValue()).toString()
								.replace("_", " ").toLowerCase();

				if (oneitem.length == 4) {
					enchantment = Enchantment
							.getById(Integer.parseInt(oneitem[2])).getName()
							.toLowerCase();
					ench_numb = oneitem[3];

					itemstring = itemstring + " with " + enchantment + " "
							+ ench_numb;
				}

				player.sendMessage("§f" + itemstring);
			}

			String abils = getAbilityDesc(kit.getInt("ABILITY"));

			if (abils != "None") {
				player.sendMessage("§f - " + abils);

			}
		}
	}

	public static void printTipChat() {
		String tip = "";

		switch (TIP_COUNT.intValue()) {
		case 0:
			tip = tip1;
			TIP_COUNT = Integer.valueOf(TIP_COUNT.intValue() + 1);
			break;
		case 1:
			tip = tip2;
			TIP_COUNT = Integer.valueOf(TIP_COUNT.intValue() + 1);
			break;
		case 2:
			tip = tip3;
			TIP_COUNT = Integer.valueOf(TIP_COUNT.intValue() + 1);
			break;
		case 3:
			tip = tip4;
			TIP_COUNT = Integer.valueOf(TIP_COUNT.intValue() + 1);
			break;
		case 4:
			tip = tip5;
			TIP_COUNT = Integer.valueOf(TIP_COUNT.intValue() + 1);
			break;
		case 5:
			tip = tip6;
			TIP_COUNT = Integer.valueOf(0);
		}

		if (plugin.ADV_CHAT_SYSTEM) {
			INFO_MSG = "[TIP] " + tip;
			updateChat();
		} else {
			if (tip != "" || tip != null)
				Bukkit.getServer().broadcastMessage("§7[TIP] " + tip);
		}
	}

	private static void updateChat() {
		for (Player pl : plugin.getPlayers()) {
			updateChat(pl);
		}
	}

	private static void updateChat(Player p) {

		// Clear chat
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");

		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("");

		if (HELP_CHAT.containsKey(p)) {
			Integer line = 5;
			String are = "are";
			String players = "players";
			if (plugin.getGamers().length == 1) {
				are = "is";
				players = "player";
			}

			Integer timeleft = plugin.MAX_GAME_RUNNING_TIME
					- plugin.GAME_RUNNING_TIME;
			String is = "are";
			String minute = "minutes";
			if (timeleft <= 1) {
				is = "minute";
				minute = "minute";
			}
			Integer help_length = 0;
			if (plugin.HELP_MESSAGE != null && plugin.HELP_MESSAGE != "")
				help_length = plugin.HELP_MESSAGE.length();

			line++;
			while (help_length > 50) {
				line++;
				help_length = help_length - 50;
			}

			p.sendMessage("§b" + plugin.SERVER_TITLE);
			p.sendMessage("");
			p.sendMessage("§7 - There " + are + " " + plugin.getGamers().length
					+ " " + players + " online.");
			p.sendMessage("§7 - There " + is + " " + timeleft + " " + minute
					+ " left.");
			if (plugin.HELP_MESSAGE != null && plugin.HELP_MESSAGE != "")
				p.sendMessage("§7 - " + plugin.HELP_MESSAGE);

			if (PLAYER_MSG.containsKey(p))
				line++;

			while (line <= 10) {
				p.sendMessage("");
				line++;
			}

			if (PLAYER_MSG.containsKey(p))
				p.sendMessage(PLAYER_MSG.get(p));

		} else if (KIT_CHAT.containsKey(p)) {
			Integer line = 5;
			FileConfiguration kitConfig = YamlConfiguration
					.loadConfiguration(new File(plugin.getDataFolder(),
							"kit.yml"));
			List<String> kitname = kitConfig.getStringList("KITS");

			String yourkits = "";
			String otherkits = "";
			for (String name : kitname) {
				if (BGMain.perms.has(p, "bg.kit." + name)
						|| BGMain.perms.has(p, "bg.kit.*") || plugin.winner(p)) {
					yourkits = name + ", " + yourkits;
				} else {
					otherkits = name + ", " + otherkits;
				}
			}
			Integer otherline = otherkits.length();
			Integer yourline = yourkits.length();
			yourline = yourline + 11;
			otherline = otherline + 12;

			line++;
			while (otherline > 50) {
				line++;
				otherline = otherline - 50;
			}
			line++;
			while (yourline > 50) {
				line++;
				yourline = yourline - 50;
			}
			p.sendMessage("§bAvailable kits: §7(/kit [KitName])");
			p.sendMessage("");
			p.sendMessage("§aYour kits: §f" + yourkits);
			p.sendMessage("§aOther kits: §f" + otherkits);
			p.sendMessage("");
			p.sendMessage("§bMore kits available at: " + plugin.KIT_BUY_WEB);

			if (PLAYER_MSG.containsKey(p))
				line++;

			while (line <= 10) {
				p.sendMessage("");
				line++;
			}

			if (PLAYER_MSG.containsKey(p))
				p.sendMessage(PLAYER_MSG.get(p));

		} else if (KITINFO_CHAT.containsKey(p)) {
			Integer line = 0;
			String kitname = KITINFO_CHAT.get(p);
			String kitinfoname = kitname;
			kitname = kitname.toLowerCase();
			FileConfiguration kitConfig = YamlConfiguration
					.loadConfiguration(new File(plugin.getDataFolder(),
							"kit.yml"));
			ConfigurationSection kit = kitConfig
					.getConfigurationSection(kitname);

			char[] stringArray = kitinfoname.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			kitinfoname = new String(stringArray);

			p.sendMessage("§a" + kitinfoname + " kit includes:");
			p.sendMessage("");
			line++;
			line++;
			line++;
			List<String> kititems = kit.getStringList("ITEMS");
			for (String item : kititems) {
				String[] oneitem = item.split(",");

				String itemstring = null;
				Integer id = null;
				Integer amount = null;
				String enchantment = null;
				String ench_numb = null;

				if (oneitem[0].contains(":")) {
					String[] ITEM_ID = oneitem[0].split(":");
					id = Integer.valueOf(Integer.parseInt(ITEM_ID[0]));
					amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				} else {
					id = Integer.valueOf(Integer.parseInt(oneitem[0]));
					amount = Integer.valueOf(Integer.parseInt(oneitem[1]));
				}

				itemstring = " - "
						+ amount
						+ "x "
						+ Material.getMaterial(id.intValue()).toString()
								.replace("_", " ").toLowerCase();

				if (oneitem.length == 4) {
					enchantment = Enchantment
							.getById(Integer.parseInt(oneitem[2])).getName()
							.toLowerCase();
					ench_numb = oneitem[3];

					itemstring = itemstring + " with " + enchantment + " "
							+ ench_numb;
				}
				line++;
				p.sendMessage("§f" + itemstring);
			}

			String abils = getAbilityDesc(kit.getInt("ABILITY"));
			if (abils != null) {
				p.sendMessage("§f - " + abils);
				line++;
			}
			if (PLAYER_MSG.containsKey(p))
				line++;

			while (line <= 10) {
				p.sendMessage("");
				line++;
			}
			if (PLAYER_MSG.containsKey(p))
				p.sendMessage("§7" + PLAYER_MSG.get(p));

		} else {

			// Print chat
			p.sendMessage("§9" + INFO_MSG);
			p.sendMessage("§c" + DEATH_MSG);
			if (plugin.DENY_DAMAGE_PLAYER)
				p.sendMessage("§a" + TIMER_MSG);
			else {
				Integer timeleft = plugin.MAX_GAME_RUNNING_TIME
						- plugin.GAME_RUNNING_TIME;
				String minute = "minutes";
				if (timeleft <= 1) {
					minute = "minute";
				}

				p.sendMessage("§a" + "Players remaining: §f"
						+ plugin.getGamers().length + " §a| Time remaining: §f"
						+ timeleft + " " + minute);
			}
			p.sendMessage("-----------------------------------------------------");
			if (!PLAYER_MSG.containsKey(p))
				p.sendMessage(CHAT_MSG[5]);
			p.sendMessage(CHAT_MSG[4]);
			p.sendMessage(CHAT_MSG[3]);
			p.sendMessage(CHAT_MSG[2]);
			p.sendMessage(CHAT_MSG[1]);
			p.sendMessage(CHAT_MSG[0]);
			if (PLAYER_MSG.containsKey(p))
				p.sendMessage(PLAYER_MSG.get(p));
		}

	}

	public static String getAbilityDesc(Integer ability) {
		if (ability == 0)
			return null;

		if (ABILITY_DESC.containsKey(ability))
			return ABILITY_DESC.get(ability);
		else
			return null;
	}

	public static void setAbilityDesc(Integer ability, String description)
			throws Error {
		if (ABILITY_DESC.containsKey(ability))
			throw new Error(
					"Cannot overwrite existing descriptions of abilties.");
		else
			ABILITY_DESC.put(ability, description);
	}

}