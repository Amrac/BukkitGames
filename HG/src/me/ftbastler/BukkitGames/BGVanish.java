package me.ftbastler.BukkitGames;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public class BGVanish {
	private static BGMain plugin;
	Logger log = Logger.getLogger("Minecraft");
	static ArrayList<String> vanished = new ArrayList<String>();

	public BGVanish(BGMain ins) {
		plugin = ins;
	}

	public static void makeVanished(Player p) {
		for (Player player : plugin.getPlayers()) {
			if (player.getName().equals(p.getName())) {
				continue;
			}
			if (!player.isOp()) {
				player.hidePlayer(p);
			}
		}
		vanished.add(p.getName());
	}

	public static boolean isVanished(Player p) {
		return vanished.contains(p.getName());
	}

	public static void updateVanished() {
		for (Player p : plugin.getPlayers())
			if (isVanished(p)) {
				makeVanished(p);
			} else {
				makeVisible(p);
			}
	}

	public static void makeVisible(Player p) {
		for (Player player : plugin.getPlayers()) {
			player.showPlayer(p);
		}
		vanished.remove(p.getName());
	}
}