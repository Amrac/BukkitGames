package me.ftbastler.BukkitGames;

import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Logger;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BGListener implements Listener {
	Logger log = Logger.getLogger("Minecraft");
	BGMain plugin;
	private String last_quit;
	private String last_headshot;

	public BGListener(BGMain instance) {
		this.plugin = instance;
		this.plugin.getServer().getPluginManager()
				.registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		if (this.plugin.DENY_CHECK_WORLDBORDER.booleanValue()) {
			return;
		}
		if (this.plugin.inBorder(event.getTo()))
			return;
		Vehicle s = event.getVehicle();
		if (s.isEmpty())
			return;
		Entity Passenger = s.getPassenger();
		if (!(Passenger instanceof Player))
			return;

		BGChat.printPlayerChat((Player) Passenger, this.plugin.WORLD_BORDER_MSG);
		s.teleport(event.getFrom());
		this.plugin.getClass();
		Bukkit.getServer().getWorld("world")
				.playEffect(s.getLocation(), Effect.ENDER_SIGNAL, 5);
		this.plugin.getClass();
		Bukkit.getServer().getWorld("world")
				.playEffect(s.getLocation(), Effect.SMOKE, 5);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();

		if (this.plugin.DENY_BLOCKBREAK.booleanValue()
				& (!plugin.hasPerm(p, "bg.admin.editblocks") || !plugin
						.hasPerm(p, "bg.admin.*"))) {
			event.setCancelled(true);
			return;
		}

		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			if ((BGKit.hasAbility(p, Integer.valueOf(5)) & p.getItemInHand()
					.getType() == Material.COOKIE)) {
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INCREASE_DAMAGE, 10 * 20, 0));
				p.getInventory().removeItem(
						new ItemStack[] { new ItemStack(Material.COOKIE, 1) });
			}
		}

		if (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR) {
			if ((BGKit.hasAbility(p, Integer.valueOf(4)) & p.getItemInHand()
					.getType() == Material.FIREBALL)) {
				Player player = p;
				EntityFireball fball;
				CraftPlayer craftPlayer = (CraftPlayer) player;
				EntityLiving playerEntity = craftPlayer.getHandle();
				Vector lookat = player.getLocation().getDirection()
						.multiply(10);
				Location loc = player.getLocation();
				fball = new EntityFireball(
						((CraftWorld) player.getWorld()).getHandle(),
						playerEntity, lookat.getX(), lookat.getY(),
						lookat.getZ());
				fball.locX = (loc.getX() + lookat.getX() / 5.0D + 0.25D);
				fball.locY = (loc.getY() + player.getEyeHeight() / 2.0D + 0.5D);
				fball.locZ = (loc.getZ() + lookat.getZ() / 5.0D);
				((CraftWorld) player.getWorld()).getHandle().addEntity(fball);

				p.getInventory()
						.removeItem(
								new ItemStack[] { new ItemStack(
										Material.FIREBALL, 1) });
			}

			if ((p.getItemInHand().getType() == Material.COMPASS & this.plugin.COMPASS
					.booleanValue())) {
				Boolean found = Boolean.valueOf(false);
				for (int i = 0; i < 300; i++) {
					List<Entity> entities = p.getNearbyEntities(i, 64.0D, i);
					for (Entity e : entities) {
						if ((!e.getType().equals(EntityType.PLAYER))
								|| (((Player) e).getGameMode() != GameMode.SURVIVAL))
							continue;
						p.setCompassTarget(e.getLocation());
						Double distance = p.getLocation().distance(
								e.getLocation());
						DecimalFormat df = new DecimalFormat("##.#");
						BGChat.printPlayerChat(p, "Tracking player \""
								+ ((Player) e).getName() + "\" | Distance: "
								+ df.format(distance));
						found = Boolean.valueOf(true);
						break;
					}

					if (found.booleanValue())
						break;
				}
				if (!found.booleanValue()) {
					BGChat.printPlayerChat(p,
							"No players in range. Compass points to spawn.");
					p.setCompassTarget(this.plugin.spawn);
				}
			}
		}
	}

	@EventHandler
	public void onServerPing(ServerListPingEvent event) {
		if (this.plugin.DENY_LOGIN.booleanValue())
			event.setMotd(this.plugin.MOTD_PROGRESS_MSG);
		else
			event.setMotd(this.plugin.MOTD_COUNTDOWN_MSG.replace("<time>",
					this.plugin.TIME(BGMain.COUNTDOWN)));
	}

	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (this.plugin.DENY_BLOCKBREAK.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		if (this.plugin.DENY_BLOCKPLACE.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityShootArrow(EntityShootBowEvent event) {
		if (((event.getEntity() instanceof Player))
				&& (this.plugin.DENY_SHOOT_BOW.booleanValue())) {
			event.getBow().setDurability((short) 0);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Entity entity = event.getEntity();

		if ((entity instanceof Arrow)) {
			Arrow arrow = (Arrow) entity;
			Entity shooter = arrow.getShooter();
			if ((shooter instanceof Player)) {
				Player player = (Player) shooter;
				if (BGKit.hasAbility(player, Integer.valueOf(1))) {
					Bukkit.getServer().getWorld("world")
							.createExplosion(arrow.getLocation(), 2.0F);
					arrow.remove();
				} else {
					return;
				}
			} else {
				return;
			}
		}

		if ((entity instanceof Snowball)) {
			Snowball ball = (Snowball) entity;
			Entity shooter = ball.getShooter();
			if ((shooter instanceof Player)) {
				Player player = (Player) shooter;
				if (BGKit.hasAbility(player, Integer.valueOf(3)).booleanValue()) {
					Bukkit.getServer().getWorld("world")
							.createExplosion(ball.getLocation(), 0.0F);
					for (Entity e : ball.getNearbyEntities(3.0D, 3.0D, 3.0D))
						if ((e instanceof Player)) {
							Player pl = (Player) e;
							if (pl.getName() != player.getName()) {
								pl.addPotionEffect(new PotionEffect(
										PotionEffectType.BLINDNESS, 100, 1));
								pl.addPotionEffect(new PotionEffect(
										PotionEffectType.CONFUSION, 160, 1));
							}
						}
				}
			} else {
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (this.plugin.DENY_ITEMDROP.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (this.plugin.DENY_DAMAGE_ENTITY.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (this.plugin.DENY_ITEMPICKUP.booleanValue())
			event.setCancelled(true);
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (this.plugin.DENY_LOGIN.booleanValue() || plugin.ADV_CHAT_SYSTEM)
			event.setLeaveMessage(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();

		if (this.plugin.DENY_LOGIN.booleanValue()
				& (!plugin.hasPerm(p, "bg.admin.logingame") || !plugin.hasPerm(
						p, "bg.admin.*"))) {
			event.setKickMessage(ChatColor.RED
					+ this.plugin.GAME_IN_PROGRESS_MSG);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
					event.getKickMessage());
		} else if (event.getResult() == Result.KICK_FULL) {
			if (plugin.hasPerm(p, "bg.vip.full")
					|| plugin.hasPerm(p, "bg.admin.full")
					|| plugin.hasPerm(p, "bg.admin.*"))
				event.allow();
			else {
				event.setKickMessage(ChatColor.RED
						+ this.plugin.SERVER_FULL_MSG
								.replace("<players>", Integer.toString(Bukkit
										.getOnlinePlayers().length)));
			}
		}

		if (this.plugin.DENY_LOGIN.booleanValue())
			BGVanish.updateVanished();
	}

	@EventHandler
	public void onPlayerOutBorder(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (this.plugin.DENY_CHECK_WORLDBORDER.booleanValue()) {
			return;
		}
		if (!this.plugin.inBorder(event.getTo())) {
			if (this.plugin.inBorder(event.getFrom())) {
				BGChat.printPlayerChat(p, this.plugin.WORLD_BORDER_MSG);
				event.setTo(event.getFrom());
				p.teleport(event.getFrom());
				this.plugin.getClass();
				Bukkit.getServer().getWorld("world")
						.playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
				this.plugin.getClass();
				Bukkit.getServer().getWorld("world")
						.playEffect(p.getLocation(), Effect.SMOKE, 5);
				return;
			}
			p.teleport(p.getWorld().getSpawnLocation().add(0.0D, 20.0D, 0.0D));
			this.plugin.getClass();
			Bukkit.getServer().getWorld("world")
					.playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
			this.plugin.getClass();
			Bukkit.getServer().getWorld("world")
					.playEffect(p.getLocation(), Effect.SMOKE, 5);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!this.plugin.COMPASS.booleanValue())
			return;
		Player p = event.getPlayer();
		Boolean found = Boolean.valueOf(false);
		for (int i = 0; i < 300; i++) {
			List<Entity> entities = p.getNearbyEntities(i, 64.0D, i);
			for (Entity e : entities) {
				if ((e.getType().equals(EntityType.PLAYER))
						&& (((Player) e).getGameMode() == GameMode.SURVIVAL)) {
					p.setCompassTarget(e.getLocation());
					found = Boolean.valueOf(true);
					break;
				}
			}
			if (found.booleanValue())
				break;
		}
		if (!found.booleanValue()) {
			p.setCompassTarget(this.plugin.spawn);
		}
		if (BGVanish.isVanished(p)) {
			p.setFoodLevel(20);
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player p = e.getEntity().getKiller();
		if (BGKit.hasAbility(p, Integer.valueOf(7)).booleanValue()) {
			if (e.getEntityType().getName().equalsIgnoreCase("pig")) {
				e.getDrops().clear();
				e.getDrops().add(new ItemStack(Material.PORK, 2));
			}
		}
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (BGKit.hasAbility(p, Integer.valueOf(8))) {
				if (e.getCause() == DamageCause.FALL) {
					if (e.getDamage() > 4) {
						e.setCancelled(true);
						p.damage(4);
					}
					List<Entity> nearbyEntities = e.getEntity()
							.getNearbyEntities(5, 5, 5);
					for (Entity target : nearbyEntities) {
						if (target instanceof Player) {
							Player t = (Player) target;
							if (t.isSneaking())
								t.damage(e.getDamage() / 2, e.getEntity());
							else
								t.damage(e.getDamage(), e.getEntity());
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		BGVanish.updateVanished();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!plugin.DENY_LOGIN.booleanValue() & plugin.ADV_CHAT_SYSTEM) {
			BGChat.printDeathChat("§e" + event.getJoinMessage());
		}

		if (this.plugin.DENY_LOGIN.booleanValue() || plugin.ADV_CHAT_SYSTEM) {
			event.setJoinMessage(null);
		}

		Player p = event.getPlayer();
		if (plugin.DENY_LOGIN) {
			if (plugin.hasPerm(p, "bg.admin.gamemaker")
					|| plugin.hasPerm(p, "bg.admin.*")) {
				if (p.getGameMode() == GameMode.SURVIVAL) {
					p.setGameMode(GameMode.CREATIVE);
					BGVanish.makeVanished(p);

					BGChat.printPlayerChat(p, "§2You are now a GameMaker.");
				}
			}
		} else {
			if (!plugin.ADV_CHAT_SYSTEM)
				BGChat.printKitChat(p);
		}

		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);

		if (plugin.winner(p)) {
			p.setPlayerListName(ChatColor.GOLD + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.GOLD + p.getName() + ChatColor.RESET);
		} else if (plugin.hasPerm(p, "bg.admin.color")
				|| plugin.hasPerm(p, "bg.admin.*")) {
			p.setPlayerListName(ChatColor.RED + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.RED + p.getName() + ChatColor.RESET);
		} else if (plugin.hasPerm(p, "bg.vip.color")
				|| plugin.hasPerm(p, "bg.vip.*")) {
			p.setPlayerListName(ChatColor.BLUE + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.BLUE + p.getName() + ChatColor.RESET);
		} else {
			p.setPlayerListName(p.getName());
			p.setDisplayName(p.getName());
		}

		if (plugin.SQL_USE) {
			Integer PL_ID = plugin.getPlayerID(p.getName());
			if (PL_ID == null) {
				plugin.SQLquery("INSERT INTO `PLAYERS` (`NAME`) VALUES ('"
						+ p.getName() + "') ;");
			}
		}
	}

	private String getShortStr(String s) {
		if (s.length() == 16) {
			String shorts = s.substring(0, s.length() - 4);
			return shorts;
		}
		if (s.length() == 15) {
			String shorts = s.substring(0, s.length() - 3);
			return shorts;
		}
		if (s.length() == 14) {
			String shorts = s.substring(0, s.length() - 2);
			return shorts;
		}
		if (s.length() == 13) {
			String shorts = s.substring(0, s.length() - 1);
			return shorts;
		}
		return s;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if ((this.plugin.DENY_BLOCKBREAK.booleanValue() & (!plugin.hasPerm(p,
				"bg.admin.editblocks") || !plugin.hasPerm(p, "bg.admin.*")))) {
			event.setCancelled(true);
			return;
		}

		Block b = event.getBlock();
		if (BGKit.hasAbility(p, 2) & b.getType() == Material.LOG) {
			World w = Bukkit.getServer().getWorld(plugin.WORLD_TEMPOARY_NAME);
			Double y = b.getLocation().getY() + 1;
			Location l = new Location(w, b.getLocation().getX(), y, b
					.getLocation().getZ());
			while (l.getBlock().getType() == Material.LOG) {
				l.getBlock().breakNaturally();
				y++;
				l.setY(y);
			}
		}

	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		if ((this.plugin.DENY_BLOCKPLACE.booleanValue() & (!plugin.hasPerm(p,
				"bg.admin.editblocks") || !plugin.hasPerm(p, "bg.admin.*"))))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		if (plugin.ADV_CHAT_SYSTEM) {
			BGChat.playerChatMsg(String.format(event.getFormat(), event
					.getPlayer().getDisplayName(), event.getMessage()));
			event.setCancelled(true);
		} else {
			return;
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if (!plugin.DENY_LOGIN.booleanValue() & plugin.ADV_CHAT_SYSTEM) {
			BGChat.printDeathChat("§e" + event.getQuitMessage());
		}

		if (this.plugin.DENY_LOGIN.booleanValue() || plugin.ADV_CHAT_SYSTEM) {
			event.setQuitMessage(null);
		}

		if (p.getGameMode() == GameMode.CREATIVE) {
			event.setQuitMessage(null);
			return;
		}

		if (this.plugin.QUIT_MSG.booleanValue() & !p.isDead()) {
			BGChat.printDeathChat(p.getName() + " left the game.");
			if (!plugin.ADV_CHAT_SYSTEM) {
				BGChat.printDeathChat(this.plugin.getGamers().length - 1
						+ " players remaining.");
				BGChat.printDeathChat("");
			}
			Location light = p.getLocation();
			last_quit = p.getName();
			p.setHealth(0);
			Bukkit.getServer().getWorld("world")
					.strikeLightningEffect(light.add(0.0D, 100.0D, 0.0D));
		}

		if (this.plugin.NEW_WINNER != p.getName()
				& this.plugin.DENY_LOGIN.booleanValue()) {
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(this.plugin, new Runnable() {
						public void run() {
							plugin.checkwinner();

							if (plugin.SQL_USE) {
								Integer PL_ID = plugin.getPlayerID(last_quit);
								if (last_quit == plugin.NEW_WINNER) {
									plugin.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `DEATH_REASON` = 'WINNER' WHERE `REF_PLAYER` = "
											+ PL_ID
											+ " AND `REF_GAME` = "
											+ plugin.SQL_GAMEID + " ;");
								} else {
									plugin.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `DEATH_REASON` = 'QUIT' WHERE `REF_PLAYER` = "
											+ PL_ID
											+ " AND `REF_GAME` = "
											+ plugin.SQL_GAMEID + " ;");
								}
							}
						}
					}, 60L);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (this.plugin.DENY_DAMAGE_ENTITY.booleanValue()
				& !(event.getEntity() instanceof Player)) {
			return;
		}
		if (this.plugin.DENY_DAMAGE_PLAYER.booleanValue()
				& event.getEntity() instanceof Player) {
			return;
		}
		if (event.getEntity().getLastDamageCause().getCause() == DamageCause.CUSTOM
				& event.getEntity().getLastDamageCause().getDamage() == 0) {
			return;
		}
		if (event.getEntity().isDead()) {
			return;
		}

		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				if (!BGKit.hasAbility(p, 9)) {
					return;
				}
				if (p.getLocation().distance(event.getEntity().getLocation()) >= 30) {
					if (event.getEntity() instanceof LivingEntity) {
						LivingEntity victom = (LivingEntity) event.getEntity();
						if (victom instanceof Player) {
							Player v = (Player) victom;
							ItemStack helmet = v.getInventory().getHelmet();
							if (helmet == null) {
								BGChat.printDeathChat(v.getName()
										+ " was headshotted by " + p.getName()
										+ ".");
								if (!plugin.ADV_CHAT_SYSTEM) {
									BGChat.printDeathChat((this.plugin
											.getGamers().length - 1)
											+ " players remaining.");
									BGChat.printDeathChat("");
								}
								Location light = v.getLocation();
								Bukkit.getServer()
										.getWorld("world")
										.strikeLightningEffect(
												light.add(0.0D, 100.0D, 0.0D));
								last_headshot = v.getName();
								v.setHealth(0);
								v.kickPlayer(ChatColor.RED + v.getName()
										+ " was headshotted by " + p.getName()
										+ ".");

								if (plugin.SQL_USE) {
									Integer PL_ID = plugin.getPlayerID(v
											.getName());
									Integer KL_ID = plugin.getPlayerID(p
											.getName());
									plugin.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `REF_KILLER` = "
											+ KL_ID
											+ ", `DEATH_REASON` = 'HEADSHOT' WHERE `REF_PLAYER` = "
											+ PL_ID
											+ " AND `REF_GAME` = "
											+ plugin.SQL_GAMEID + " ;");
								}
							} else {
								helmet.setDurability((short) (helmet
										.getDurability() + 20));
								v.getInventory().setHelmet(helmet);
							}
						} else {
							BGChat.printPlayerChat(p, "Headshot!");
							victom.setHealth(0);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if ((this.plugin.DENY_DAMAGE_PLAYER.booleanValue() & event.getEntity() instanceof Player)) {
			event.setCancelled(true);
			return;
		}

		if ((this.plugin.DENY_DAMAGE_ENTITY.booleanValue() & !(event
				.getEntity() instanceof Player))) {
			event.setCancelled(true);
			return;
		}

		if ((event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK & event
				.getEntity() instanceof Player)) {
			Player p = (Player) event.getEntity();
			if ((BGKit.hasAbility(p, Integer.valueOf(6)).booleanValue() & !p
					.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))) {
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INCREASE_DAMAGE, 200, 1));
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.FIRE_RESISTANCE, 260, 1));
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getGameMode() == GameMode.CREATIVE) {
			event.setDeathMessage(null);
			return;
		}

		if (last_quit == event.getEntity().getName()
				|| last_headshot == event.getEntity().getName()) {
			event.setDeathMessage(null);
			return;
		}

		if ((event.getEntity() instanceof Player & this.plugin.DEATH_MSG
				.booleanValue())) {
			Player p = event.getEntity();

			if (plugin.SQL_USE) {
				Integer PL_ID = plugin.getPlayerID(p.getName());

				Integer KL_ID = null;
				if (p.getKiller() != null) {
					KL_ID = plugin.getPlayerID(p.getKiller().getName());
				} else {
					KL_ID = null;
				}

				plugin.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `REF_KILLER` = "
						+ KL_ID
						+ ", `DEATH_REASON` = '"
						+ p.getLastDamageCause().getCause().name().toString()
						+ "' WHERE `REF_PLAYER` = "
						+ PL_ID
						+ " AND `REF_GAME` = " + plugin.SQL_GAMEID + " ;");
			}

			p.kickPlayer(ChatColor.RED + event.getDeathMessage() + ".");
			Location light = p.getLocation();

			BGChat.printDeathChat(event.getDeathMessage() + ".");
			if (!plugin.ADV_CHAT_SYSTEM) {
				BGChat.printDeathChat(this.plugin.getGamers().length
						+ " players remaining.");
				BGChat.printDeathChat("");
			}
			Bukkit.getServer().getWorld("world")
					.strikeLightningEffect(light.add(0.0D, 100.0D, 0.0D));
		}

		event.setDeathMessage(null);
	}
}