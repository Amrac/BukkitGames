package me.ftbastler.BukkitGames;

import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BGCommand implements CommandExecutor {
	Logger log = Logger.getLogger("Minecraft");
	private BGMain plugin;

	public BGCommand(BGMain plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			this.log.info("[BukkitGames] This command can only be accessed by players!");
			return true;
		}
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("start")) {
			if (plugin.hasPerm(p, "bg.admin.start")
					|| plugin.hasPerm(p, "bg.admin.*")) {
				if (this.plugin.DENY_LOGIN.booleanValue())
					BGChat.printPlayerChat(p, "The game has already begun!");
				else
					this.plugin.startgame();
			} else {
				BGChat.printPlayerChat(p, "You are not allowed to do this.");
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("gamemaker")) {
			if (plugin.hasPerm(p, "bg.admin.gamemaker")
					|| plugin.hasPerm(p, "bg.admin.*")) {
				if (p.getGameMode() == GameMode.CREATIVE) {
					p.setGameMode(GameMode.SURVIVAL);
					BGVanish.makeVisible(p);

					BGChat.printPlayerChat(p,
							"§2You are no longer a GameMaker.");
				} else {
					p.setGameMode(GameMode.CREATIVE);
					BGVanish.makeVanished(p);

					BGChat.printPlayerChat(p, "§2You are now a GameMaker.");
				}
			} else
				BGChat.printPlayerChat(p, "You are not allowed to do this.");

			return true;
		}

		if (cmd.getName().equalsIgnoreCase("help")) {
			BGChat.printHelpChat(p);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("chest")) {
			if (plugin.hasPerm(p, "bg.admin.chest")
					|| plugin.hasPerm(p, "bg.admin.*")) {
				this.plugin.spawnChest(p.getLocation());
				return true;
			}
			BGChat.printPlayerChat(p, "You are not allowed to do this.");
			return false;
		}

		if (cmd.getName().equalsIgnoreCase("rchest")) {
			if (plugin.hasPerm(p, "bg.admin.rchest")
					|| plugin.hasPerm(p, "bg.admin.*")) {
				this.plugin.spawnChest();
				return true;
			}
			BGChat.printPlayerChat(p, "You are not allowed to do this.");
			return false;
		}

		if (cmd.getName().equalsIgnoreCase("kitinfo")) {
			if (args.length != 1) {
				BGChat.printPlayerChat(p,
						"Must include a kit name! (/kitinfo [kitName])");
				return true;
			}
			BGChat.printKitInfo(p, args[0]);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("kit")) {
			if (this.plugin.DENY_LOGIN.booleanValue()) {
				if (args.length != 1) {
					BGChat.printKitChat(p);
					return true;
				}
				BGChat.printPlayerChat(p, "The game has already started!");
				return true;
			}
			if (args.length != 1) {
				BGChat.printKitChat(p);
				return true;
			}
			BGKit.setKit(p, args[0]);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("spawn")) {
			if (this.plugin.DENY_LOGIN.booleanValue()
					& !(plugin.hasPerm(p, "bg.admin.spawn") || plugin.hasPerm(
							p, "bg.admin.*"))) {
				BGChat.printPlayerChat(p, "The game has already started!");
				return true;
			} else {
				p.teleport(plugin.getSpawn());
				BGChat.printPlayerChat(p, "Teleported to the spawn location.");
				return true;
			}
		}
		return true;
	}
}