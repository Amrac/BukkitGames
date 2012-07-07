package me.ftbastler.BukkitGames;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.permission.Permission;

public class BGMain extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");

	public String HELP_MESSAGE = null;
	public String SERVER_FULL_MSG = "";
	public String WORLD_BORDER_MSG = "";
	public String GAME_IN_PROGRESS_MSG = "";
	public String KIT_BUY_WEB = "";
	public String NEW_WINNER = "";
	public String MOTD_PROGRESS_MSG = "";
	public String MOTD_COUNTDOWN_MSG = "";
	public String NO_KIT_MSG = "";
	public String SERVER_TITLE = null;
	public Boolean ADV_CHAT_SYSTEM = true;
	public static Integer COUNTDOWN_SECONDS = Integer.valueOf(30);
	public Integer FINAL_COUNTDOWN_SECONDS = Integer.valueOf(20);
	public Integer MAX_GAME_RUNNING_TIME = Integer.valueOf(30);
	public Integer MINIMUM_PLAYERS = Integer.valueOf(1);
	public final Integer WINNER_PLAYERS = Integer.valueOf(1);
	public Integer END_GAME_TIME = Integer.valueOf(25);
	public final String WORLD_TEMPOARY_NAME = "world";
	public static Permission perms = null;
	public Boolean REGEN_WORLD = false;
	public Boolean RANDOM_START = false;
	public Boolean SPAWN_CHESTS = true;
	public Boolean DENY_CHECK_WORLDBORDER = Boolean.valueOf(false);
	public Boolean DENY_LOGIN = false;
	public Boolean DENY_BLOCKPLACE = false;
	public Boolean DENY_BLOCKBREAK = false;
	public Boolean DENY_ITEMPICKUP = false;
	public Boolean DENY_ITEMDROP = false;
	public Boolean DENY_DAMAGE_PLAYER = false;
	public Boolean DENY_DAMAGE_ENTITY = false;
	public Boolean DENY_SHOOT_BOW = false;
	public Boolean QUIT_MSG = false;
	public Boolean DEATH_MSG = false;
	public Boolean COMPASS = false;
	public Location spawn;
	public String STOP_CMD = "";
	public String LAST_WINNER = "";
	public static Integer COUNTDOWN = Integer.valueOf(0);
	public Integer FINAL_COUNTDOWN = Integer.valueOf(0);
	public Integer GAME_RUNNING_TIME = Integer.valueOf(0);
	HashMap<World, ArrayList<Border>> BORDERS = new HashMap<World, ArrayList<Border>>();
	public static Integer WORLDRADIUS = Integer.valueOf(250);
	public Boolean SQL_USE = false;

	public Integer SQL_GAMEID = null;
	public String SQL_HOST = null;
	public String SQL_PORT = null;
	public String SQL_USER = null;
	public String SQL_PASS = null;
	public String SQL_DATA = null;
	public Connection con = null;

	final Timer timer1 = new Timer();
	TimerTask task1 = new TimerTask() {
		public void run() {

			if (BGMain.COUNTDOWN.intValue() > 0) {
				if (BGMain.COUNTDOWN >= 10 & BGMain.COUNTDOWN % 10 == 0) {
					BGChat.printTimeChat("The game will start in "
							+ BGMain.this.TIME(BGMain.COUNTDOWN) + ".");
					for (Player pl : getGamers()) {
						pl.setHealth(20);
						pl.setFoodLevel(20);
						pl.setExp(0);
						pl.setRemainingAir(20);
					}
				} else if (BGMain.COUNTDOWN < 10) {
					BGChat.printTimeChat("The game will start in "
							+ BGMain.this.TIME(BGMain.COUNTDOWN) + ".");
				}

				BGMain.COUNTDOWN--;
			} else if (BGMain.this.getGamers().length < BGMain.this.MINIMUM_PLAYERS
					.intValue()) {
				BGChat.printTimeChat("There are too few players on, restarting countdown.");
				BGMain.COUNTDOWN = BGMain.COUNTDOWN_SECONDS;
			} else {
				BGMain.this.startgame();
			}
		}
	};

	
	final Timer timer3 = new Timer();
	TimerTask task3 = new TimerTask() {
		public void run() {
			if (BGMain.this.FINAL_COUNTDOWN > 0) {
				if (BGMain.this.FINAL_COUNTDOWN >= 10
						& BGMain.this.FINAL_COUNTDOWN % 10 == 0) {
					BGChat.printTimeChat("Invincibility wears off in "
							+ BGMain.this.TIME(BGMain.this.FINAL_COUNTDOWN)
							+ ".");
				} else if (BGMain.this.FINAL_COUNTDOWN < 10) {
					BGChat.printTimeChat("Invincibility wears off in "
							+ BGMain.this.TIME(BGMain.this.FINAL_COUNTDOWN)
							+ ".");
				}
				BGMain.this.FINAL_COUNTDOWN--;
			} else {
				BGChat.printTimeChat("");
				BGChat.printTimeChat("Invincibility was worn off.");
				BGChat.printTipChat();
				BGMain.this.DENY_DAMAGE_PLAYER = Boolean.valueOf(false);
				BGMain.this.DEATH_MSG = Boolean.valueOf(true);
				BGMain.this.timer3.cancel();
				BGMain.this.timer2.scheduleAtFixedRate(BGMain.this.task2, 0L,
						60000L);
			}
		}
	};

	final Timer timer2 = new Timer();
	TimerTask task2 = new TimerTask() {
		public void run() {
			BGMain.this.GAME_RUNNING_TIME = Integer
					.valueOf(BGMain.this.GAME_RUNNING_TIME.intValue() + 1);

			BGMain.this.checkwinner();
			BGVanish.updateVanished();

			if (BGMain.this.GAME_RUNNING_TIME % 10 == 0) {
				if (BGMain.this.SPAWN_CHESTS == true) {
					BGMain.this.spawnChest();
				}
			}
			if ((BGMain.this.GAME_RUNNING_TIME % 5 == 0)
					& (BGMain.this.GAME_RUNNING_TIME % 10 != 0)) {
				if (BGMain.this.SPAWN_CHESTS == true) {
					BGMain.this.spawnTable();
				}
			}
			if ((BGMain.this.GAME_RUNNING_TIME % 5 != 0)
					& (BGMain.this.GAME_RUNNING_TIME % 10 != 0)) {
				if (BGMain.this.SPAWN_CHESTS == true) {
					BGChat.printTipChat();
				}
			}
			if (BGMain.this.GAME_RUNNING_TIME == (BGMain.this.END_GAME_TIME - 1)) {
				BGChat.printInfoChat("Final battle ahead. Teleporting everybody to spawn in 1 minute!");
			}
			if (BGMain.this.GAME_RUNNING_TIME == BGMain.this.END_GAME_TIME) {
				World w = Bukkit.getWorld("world");
				w.setDifficulty(Difficulty.HARD);
				w.strikeLightning(BGMain.this.spawn.add(0.0D, 100.0D, 0.0D));
				BGChat.printInfoChat("Final battle! Teleported everybody to spawn.");
				BGMain.this.endgame();
			}

			if (BGMain.this.GAME_RUNNING_TIME.intValue() == BGMain.this.MAX_GAME_RUNNING_TIME
					.intValue() - 1) {
				BGChat.printInfoChat("Final battle! 1 minute left.");
				BGMain.this.endgame();
			}

			if (BGMain.this.GAME_RUNNING_TIME.intValue() >= BGMain.this.MAX_GAME_RUNNING_TIME
					.intValue())
				Bukkit.getServer().shutdown();
		}
	};

	public void onLoad() {
		this.log.info("[BukkitGames] Deleting old world...");
		Bukkit.getServer().unloadWorld("world", false);
		deleteDir(new File("world"));

		this.REGEN_WORLD = getConfig().getBoolean("REGEN_WORLD");
		if (this.REGEN_WORLD == false) {
			this.log.info("[BukkitGames] Copying saved world...");
			try {
				copyDirectory(new File(this.getDataFolder(), "world"),
						new File("world"));
			} catch (IOException e) {
				log.warning("[BukkitGames] Error: " + e.toString());
			}
		} else {
			this.log.info("[BukkitGames] Generating new world...");
		}
	}

	public void onEnable() {

		new BGKit(this);
		new BGListener(this);
		new BGChat(this);
		new BGCommand(this);
		new BGVanish(this);

		BGCommand bgcmd = new BGCommand(this);
		getCommand("chest").setExecutor(bgcmd);
		getCommand("rchest").setExecutor(bgcmd);
		getCommand("help").setExecutor(bgcmd);
		getCommand("kit").setExecutor(bgcmd);
		getCommand("kitinfo").setExecutor(bgcmd);
		getCommand("start").setExecutor(bgcmd);
		getCommand("gamemaker").setExecutor(bgcmd);
		getCommand("spawn").setExecutor(bgcmd);

		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.KIT_BUY_WEB = getConfig().getString("MESSAGE.KIT_BUY_WEBSITE");
		this.SERVER_TITLE = getConfig().getString("MESSAGE.SERVER_TITLE");
		this.HELP_MESSAGE = getConfig().getString("MESSAGE.HELP_MESSAGE");
		this.RANDOM_START = getConfig().getBoolean("RANDOM_START");
		this.SPAWN_CHESTS = getConfig().getBoolean("SPAWN_CHESTS");
		this.REGEN_WORLD = getConfig().getBoolean("REGEN_WORLD");
		this.NO_KIT_MSG = getConfig().getString("MESSAGE.NO_KIT_PERMISSION");
		this.GAME_IN_PROGRESS_MSG = getConfig().getString(
				"MESSAGE.GAME_PROGRESS");
		this.SERVER_FULL_MSG = getConfig().getString("MESSAGE.SERVER_FULL");
		this.WORLD_BORDER_MSG = getConfig().getString("MESSAGE.WORLD_BORDER");
		this.MOTD_PROGRESS_MSG = getConfig().getString("MESSAGE.MOTD_PROGRESS");
		this.MOTD_COUNTDOWN_MSG = getConfig().getString(
				"MESSAGE.MOTD_COUNTDOWN");
		this.ADV_CHAT_SYSTEM = getConfig().getBoolean("ADVANCED_CHAT");

		this.SQL_USE = getConfig().getBoolean("MYSQL");
		this.SQL_HOST = getConfig().getString("HOST");
		this.SQL_PORT = getConfig().getString("PORT");
		this.SQL_USER = getConfig().getString("USERNAME");
		this.SQL_PASS = getConfig().getString("PASSWORD");
		this.SQL_DATA = getConfig().getString("DATABASE");

		this.MINIMUM_PLAYERS = Integer.valueOf(getConfig().getInt(
				"MINIMUM_PLAYERS_START"));
		BGMain.WORLDRADIUS = Integer.valueOf(getConfig().getInt(
				"WORLD_BORDER_RADIUS"));

		this.MAX_GAME_RUNNING_TIME = Integer.valueOf(getConfig().getInt(
				"TIME.MAX_GAME-MIN"));
		COUNTDOWN_SECONDS = Integer.valueOf(getConfig().getInt(
				"TIME.COUNTDOWN-SEC"));
		this.FINAL_COUNTDOWN_SECONDS = Integer.valueOf(getConfig().getInt(
				"TIME.FINAL_COUNTDOWN-SEC"));
		this.END_GAME_TIME = Integer.valueOf(getConfig().getInt(
				"TIME.INCREASE_DIFFICULTY-MIN"));

		this.COMPASS = Boolean.valueOf(getConfig().getBoolean("COMPASS"));
		this.STOP_CMD = getConfig().getString("RESTART_SERVER_COMMAND");

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(getDataFolder(),
					"leaderboard.yml")));
		} catch (FileNotFoundException e) {
			this.log.warning(e.toString());
		}

		String line = null;
		String merke = null;
		try {
			while ((line = br.readLine()) != null)
				merke = line;
		} catch (IOException e) {
			this.log.warning(e.toString());
		}
		try {
			br.close();
		} catch (IOException e) {
			this.log.warning(e.toString());
		}

		this.LAST_WINNER = merke;

		BGChat.tip1 = getConfig().getString("TIP.1");
		BGChat.tip2 = getConfig().getString("TIP.2");
		BGChat.tip3 = getConfig().getString("TIP.3");
		BGChat.tip4 = getConfig().getString("TIP.4");
		BGChat.tip5 = getConfig().getString("TIP.5");
		BGChat.tip6 = getConfig().getString("TIP.6");

		World thisWorld = getServer().getWorld("world");
		this.spawn = thisWorld.getSpawnLocation();

		Border newBorder = new Border(this.spawn.getX(), this.spawn.getZ(),
				BGMain.WORLDRADIUS.intValue());
		if (!this.BORDERS.containsKey(thisWorld)) {
			ArrayList<Border> newArray = new ArrayList<Border>();
			this.BORDERS.put(thisWorld, newArray);
		}
		((ArrayList<Border>) this.BORDERS.get(thisWorld)).add(newBorder);

		COUNTDOWN = COUNTDOWN_SECONDS;
		this.FINAL_COUNTDOWN = this.FINAL_COUNTDOWN_SECONDS;
		this.GAME_RUNNING_TIME = Integer.valueOf(0);

		this.DENY_BLOCKBREAK = Boolean.valueOf(true);
		this.DENY_BLOCKPLACE = Boolean.valueOf(true);
		this.DENY_ITEMDROP = Boolean.valueOf(true);
		this.DENY_ITEMPICKUP = Boolean.valueOf(true);
		this.DENY_DAMAGE_PLAYER = Boolean.valueOf(true);
		this.DENY_DAMAGE_ENTITY = Boolean.valueOf(true);
		this.DENY_SHOOT_BOW = Boolean.valueOf(true);

		if (SQL_USE) {
			SQLconnect();
			SQLquery("CREATE TABLE IF NOT EXISTS `GAMES` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `STARTTIME` datetime NOT NULL, `ENDTIME` datetime, `REF_WINNER` int(10), PRIMARY KEY (`ID`)) ENGINE=MyISAM DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
			SQLquery("CREATE TABLE IF NOT EXISTS `PLAYERS` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `NAME` varchar(255) NOT NULL, PRIMARY KEY (`ID`)) ENGINE=MyISAM DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
			SQLquery("CREATE TABLE IF NOT EXISTS `PLAYS` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `REF_PLAYER` int(10), `REF_GAME` int(10), `KIT` varchar(255), `DEATHTIME` datetime, `REF_KILLER` int(10), `DEATH_REASON` varchar(255), PRIMARY KEY (`ID`)) ENGINE=MyISAM DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
		}

		Location loc = randomLocation(this.spawn.getChunk()).add(0.0D, 30.0D,
				0.0D);
		Bukkit.getServer().getWorld("world").loadChunk(loc.getChunk());
		log.info("[BukkitGames] Setting up permissions...");
		setupPermissions();
		this.timer1.scheduleAtFixedRate(this.task1, 0L, 1000L);

		PluginDescriptionFile pdfFile = getDescription();
		this.log.info("[BukkitGames] Plugin enabled");
		this.log.info("[BukkitGames] Author: " + pdfFile.getAuthors());
		this.log.info("[BukkitGames] Version: " + pdfFile.getVersion());
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer()
				.getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		this.log.info("[BukkitGames] Plugin disabled");
		this.log.info("[BukkitGames] Author: " + pdfFile.getAuthors());
		this.log.info("[BukkitGames] Version: " + pdfFile.getVersion());

		if (SQL_USE) {
			if (SQL_GAMEID != null) {
				Integer PL_ID = getPlayerID(NEW_WINNER);
				SQLquery("UPDATE `GAMES` SET `ENDTIME` = NOW(), `REF_WINNER` = "
						+ PL_ID + " WHERE `ID` = " + SQL_GAMEID + " ;");
				SQLdisconnect();
			}
		}

		this.timer2.cancel();
		this.timer1.cancel();
		this.timer3.cancel();

		for (Player p : getPlayers()) {
			p.kickPlayer(ChatColor.YELLOW + "Server is restarting!");
		}
		Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(),
				this.STOP_CMD);
	}

	private void firstRun() throws Exception {
		File configFile = new File(getDataFolder(), "config.yml");
		File kitFile = new File(getDataFolder(), "kit.yml");
		File leaderboardFile = new File(getDataFolder(), "leaderboard.yml");

		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
			this.log.info("[BukkitGames] 'config.yml' didn't exist. Created it.");
		}
		if (!kitFile.exists()) {
			kitFile.getParentFile().mkdirs();
			copy(getResource("kit.yml"), kitFile);
			this.log.info("[BukkitGames] 'kit.yml' didn't exist. Created it.");
		}
		if (!leaderboardFile.exists()) {
			leaderboardFile.getParentFile().mkdirs();
			copy(getResource("leaderboard.yml"), leaderboardFile);
			this.log.info("[BukkitGames] 'leaderboard.yml' didn't exist. Created it.");
		}
	}

	private void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean inBorder(Location checkHere) {
		if (!this.BORDERS.containsKey(checkHere.getWorld()))
			return true;
		for (Border amIHere : this.BORDERS.get(checkHere.getWorld())) {
			int X = (int) Math.abs(amIHere.centerX - checkHere.getBlockX());
			int Z = (int) Math.abs(amIHere.centerZ - checkHere.getBlockZ());
			if ((X < amIHere.definiteSq) && (Z < amIHere.definiteSq))
				return true;
			if ((X > amIHere.radius) || (Z > amIHere.radius))
				continue;
			if (X * X + Z * Z < amIHere.radiusSq)
				return true;
		}
		return false;
	}

	public void spawnChest() {
		spawnChest(randomLocation(this.spawn.getChunk()));
	}

	public void spawnChest(Location l) {
		l.getBlock().setType(Material.CHEST);
		Chest c = (Chest) l.getBlock().getState();
		Random r = new Random();
		c.getInventory().addItem(
				new ItemStack[] {
						new ItemStack(r.nextInt(22) + 298, 1, (short) r
								.nextInt(100)),
						new ItemStack(r.nextInt(4) + 283, 1, (short) r
								.nextInt(100)),
						new ItemStack(r.nextInt(15) + 352, r.nextInt(5)) });
		c.update(true);
		DecimalFormat df = new DecimalFormat("##.#");
		BGChat.printInfoChat("Chest spawned at X: " + df.format(l.getX())
				+ " | Y: " + df.format(l.getY()) + " | Z: "
				+ df.format(l.getZ()));
	}

	public void spawnTable() {
		spawnTable(randomLocation(this.spawn.getChunk()));
	}

	public void spawnTable(Location l) {
		l.getBlock().setType(Material.ENCHANTMENT_TABLE);
		DecimalFormat df = new DecimalFormat("##.#");
		BGChat.printInfoChat("Enchantment Table spawned at X: "
				+ df.format(l.getX()) + " | Y: " + df.format(l.getY())
				+ " | Z: " + df.format(l.getZ()));

	}

	public Location getSpawn() {
		Location loc = Bukkit.getWorld("world").getSpawnLocation();
		loc.setY(Bukkit.getWorld("world").getHighestBlockYAt(
				Bukkit.getWorld("world").getSpawnLocation()) + 1.5);
		return loc;
	}

	public Player[] getGamers() {
		ArrayList<Player> gamers = new ArrayList<Player>();
		Player[] list = Bukkit.getOnlinePlayers();
		for (Player p : list) {
			if (p.getGameMode() == GameMode.SURVIVAL) {
				gamers.add(p);
			}
		}
		return (Player[]) gamers.toArray(new Player[0]);
	}

	public Player[] getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		Player[] onlineplayers = Bukkit.getOnlinePlayers();
		for (int i = 0; i < onlineplayers.length; i++) {
			players.add(onlineplayers[i]);
		}
		return (Player[]) players.toArray(new Player[0]);
	}

	public void startgame() {
		this.timer1.cancel();
		this.timer3.scheduleAtFixedRate(this.task3, 3000, 1000);

		this.DENY_LOGIN = Boolean.valueOf(true);
		this.DENY_BLOCKBREAK = Boolean.valueOf(false);
		this.DENY_BLOCKPLACE = Boolean.valueOf(false);
		this.DENY_ITEMDROP = Boolean.valueOf(false);
		this.DENY_ITEMPICKUP = Boolean.valueOf(false);
		this.DENY_DAMAGE_ENTITY = Boolean.valueOf(false);
		this.DENY_SHOOT_BOW = Boolean.valueOf(false);
		this.QUIT_MSG = Boolean.valueOf(true);

		if (SQL_USE) {
			PreparedStatement statement = null;
			ResultSet generatedKeys = null;

			try {
				statement = con.prepareStatement(
						"INSERT INTO `GAMES` (`STARTTIME`) VALUES (NOW()) ;",
						Statement.RETURN_GENERATED_KEYS);

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Error!");
				}

				generatedKeys = statement.getGeneratedKeys();
				if (generatedKeys.next()) {
					SQL_GAMEID = (int) generatedKeys.getLong(1);
				} else {
					throw new SQLException("Error!");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (generatedKeys != null)
					try {
						generatedKeys.close();
					} catch (SQLException logOrIgnore) {
					}
				if (statement != null)
					try {
						statement.close();
					} catch (SQLException logOrIgnore) {
					}
			}

		}

		this.DENY_CHECK_WORLDBORDER = Boolean.valueOf(true);
		Bukkit.getServer().getWorld("world").loadChunk(getSpawn().getChunk());

		Bukkit.getWorld("world").setDifficulty(Difficulty.HARD);
		for (Player p : getPlayers()) {
			if (this.RANDOM_START == false) {
				p.teleport(getSpawn());
			} else {
				Location tploc = getSpawn();
				Random r = new Random();
				tploc.setX(tploc.getX() + 2);
				tploc.setZ(tploc.getZ() + 2);
				tploc.setX(tploc.getX() - Double.parseDouble(r.nextInt(5) + ""));
				tploc.setZ(tploc.getZ() - Double.parseDouble(r.nextInt(5) + ""));
				tploc.setY(Bukkit.getWorld(WORLD_TEMPOARY_NAME)
						.getHighestBlockYAt(tploc));
				p.teleport(tploc);
			}
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setExp(0);
			BGKit.giveKit(p);

			if (SQL_USE & p.getGameMode() == GameMode.SURVIVAL) {
				Integer PL_ID = getPlayerID(p.getName());
				SQLquery("INSERT INTO `PLAYS` (`REF_PLAYER`, `REF_GAME`, `KIT`) VALUES ("
						+ PL_ID
						+ ","
						+ SQL_GAMEID
						+ ",'"
						+ BGKit.getKit(p)
						+ "') ;");
			}
		}

		Bukkit.getServer().getWorld("world").setTime(0L);
		this.DENY_CHECK_WORLDBORDER = Boolean.valueOf(false);
		if (ADV_CHAT_SYSTEM) {
			BGChat.printInfoChat(" --- The games have begun! ---");
			BGChat.printDeathChat("§e\"May the odds be ever in your favor!\"");
		} else {
			BGChat.printTimeChat("");
			BGChat.printTimeChat("The games have begun!");
		}
		BGChat.printTimeChat("Everyone is invincible for "
				+ TIME(this.FINAL_COUNTDOWN_SECONDS) + ".");
	}

	public boolean hasPerm(Player p, String s) {
		return BGMain.perms.has(p, s);
	}

	public static Location randomLocation(Chunk c) {
		Random random = new Random();
		Location startFrom = Bukkit.getWorld("world").getSpawnLocation();
		Location loc = startFrom.clone();
		loc.add((random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS),
				60,
				(random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS));
		int newY = Bukkit.getWorld("world").getHighestBlockYAt(loc);
		loc.setY(newY);
		return loc;
	}

	public static Location getRandomLocation() {
		Random random = new Random();
		Location startFrom = Bukkit.getWorld("world").getSpawnLocation();
		Location loc = startFrom.clone();
		loc.add((random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS),
				60,
				(random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS));
		int newY = Bukkit.getWorld("world").getHighestBlockYAt(loc);
		loc.setY(newY);
		return loc;
	}

	public void endgame() {
		Bukkit.getServer().getWorld("world").loadChunk(getSpawn().getChunk());
		for (Player p : getPlayers()) {
			if (this.RANDOM_START == false) {
				p.teleport(getSpawn());
			} else {
				p.teleport(BGMain.getRandomLocation());
			}
		}
	}

	public void checkwinner() {
		if (getGamers().length <= this.WINNER_PLAYERS.intValue())
			if (getGamers().length == 0) {
				this.timer2.cancel();
				Bukkit.getServer().shutdown();
			} else {
				this.timer2.cancel();
				String winnername = getGamers()[0].getName();
				this.NEW_WINNER = winnername;
				try {
					String contents = winnername;
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							new File(getDataFolder(), "leaderboard.yml"), true));
					writer.newLine();
					writer.write(contents);
					writer.flush();
					writer.close();
				} catch (Exception ex) {
					this.log.warning(ex.toString());
				}

				getGamers()[0].kickPlayer(ChatColor.GOLD
						+ "You are the winner of this game!");
				Bukkit.getServer().shutdown();
			}
	}

	public static void deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]));
			}
		}
		dir.delete();
	}

	public String TIME(Integer i) {
		if (i.intValue() >= 60) {
			Integer time = Integer.valueOf(i.intValue() / 60);
			String add = "";
			if (time > 1) {
				add = "s";
			}
			return time + " minute" + add;
		}
		Integer time = i;
		String add = "";
		if (time > 1) {
			add = "s";
		}
		return time + " second" + add;
	}

	public boolean winner(Player p) {
		if (LAST_WINNER == null) {
			return false;
		}
		if (LAST_WINNER.equals(p.getName())) {
			return true;
		} else {
			return false;
		}
	}

	public Integer getPlayerID(String playername) {
		try {
			Statement stmt = con.createStatement();
			ResultSet r = stmt
					.executeQuery("SELECT `ID`, `NAME` FROM `PLAYERS` WHERE `NAME` = '"
							+ playername + "' ;");
			r.last();
			if (r.getRow() == 0) {
				stmt.close();
				r.close();
				return null;
			}
			Integer PL_ID = r.getInt("ID");
			stmt.close();
			r.close();
			return PL_ID;
		} catch (SQLException ex) {
			System.err.println("[BukkitGames] Error with following query: "
					+ "SELECT `ID`, `NAME` FROM `PLAYERS` WHERE `NAME` = '"
					+ playername + "' ;");
			System.err.println("[BukkitGames] MySQL-Error: " + ex.getMessage());
			return null;
		} catch (NullPointerException ex) {
			System.err
					.println("[BukkitGames] Error while performing a query. (NullPointerException)");
			return null;
		}
	}

	public void SQLconnect() {
		try {
			System.out.println("[BukkitGames] Connecting to MySQL database...");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String conn = "jdbc:mysql://" + SQL_HOST + ":" + SQL_PORT + "/"
					+ SQL_DATA;
			con = DriverManager.getConnection(conn, SQL_USER, SQL_PASS);
		} catch (ClassNotFoundException ex) {
			System.err.println("[BukkitGames] No MySQL driver found!");
		} catch (SQLException ex) {
			System.err
					.println("[BukkitGames] Error while fetching MySQL connection!");
		} catch (Exception ex) {
			System.err
					.println("[BukkitGames] Unknown error while fetchting MySQL connection.");
		}
	}

	public Connection SQLgetConnection() {
		return con;
	}

	public void SQLquery(String sql) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException ex) {
			System.err.println("[BukkitGames] Error with following query: "
					+ sql);
			System.err.println("[BukkitGames] MySQL-Error: " + ex.getMessage());
		} catch (NullPointerException ex) {
			System.err
					.println("[BukkitGames] Error while performing a query. (NullPointerException)");
		}
	}

	public void SQLdisconnect() {
		try {
			System.out
					.println("[BukkitGames] Disconnecting from MySQL database...");
			con.close();
		} catch (SQLException ex) {
			System.err
					.println("[BukkitGames] Error while closing the connection...");
		} catch (NullPointerException ex) {
			System.err
					.println("[BukkitGames] Error while closing the connection...");
		}

	}
}