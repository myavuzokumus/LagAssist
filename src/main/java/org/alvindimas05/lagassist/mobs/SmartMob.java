package org.alvindimas05.lagassist.mobs;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import org.alvindimas05.lagassist.utils.PaperOnly;
import org.alvindimas05.lagassist.utils.WorldMgr;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.stacker.StackManager;
import org.alvindimas05.lagassist.utils.VersionMgr;

public class SmartMob implements Listener {

	EventPriority prio;
	
	private static SplittableRandom rand = new SplittableRandom();

	public static boolean Spawning;
	public static List<String> mobs = new ArrayList<String>();
	public static boolean Whitelist;

	private static boolean nogravityastand;
	private static boolean simpleslime;

	public static void Enabler(boolean reload) {
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new SmartMob(), Main.p);
		}
		
		Bukkit.getLogger().info("    §e[§a✔§e] §fSmart Mob Tools.");
		mobs = Main.config.getStringList("smart-cleaner.mobs");

		Whitelist = Main.config.getBoolean("smart-cleaner.whitelist");
		Spawning = true;

		nogravityastand = Main.config.getBoolean("mob-manager.no-armorstand-gravity");
		simpleslime = Main.config.getBoolean("mob-manager.simple-slime");

	}

	@SuppressWarnings("deprecation")
	public static void MobCuller() {
		for (World w : Bukkit.getWorlds()) {
			if (!WorldMgr.blacklist.contains(w.getName())) {
				for (Entity e : w.getEntities()) {
					if (Whitelist) {
						if (isRemovable(e)) {
							e.remove();
						}
					} else if (mobs.contains(e.getType().toString())) {
						if (isRemovable(e)) {
							e.remove();
						}
					}
				}
			}
		}
	}
	
	private static boolean isRemovable(Entity ent) {
		String name = ent.getCustomName();
		
		if (VersionMgr.hasPassengers(ent)) return false;
		
		if (name != null && !StackManager.isStacked(ent)) return false;
		
		if (ent instanceof Player) return false;
		
		return true;
		
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void DeathListener(EntityDeathEvent e) {
		Entity ent = e.getEntity();
		if (simpleslime && ent.getType() == EntityType.SLIME) {
			Slime slm = (Slime) ent;
			e.getDrops().clear();

			int items = rand.nextInt(slm.getSize() * 2, slm.getSize() * 4);
			int xp = rand.nextInt(slm.getSize() * 4, slm.getSize() * 7);

			e.setDroppedExp(xp);
			e.getDrops().add(new ItemStack(Material.SLIME_BALL, items));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void SpawnListener(CreatureSpawnEvent e) {
		
		if (!WorldMgr.blacklist.contains(e.getEntity().getWorld().getName())) {
			return;
		}
		
		if (!Spawning) {
			e.setCancelled(true);
			return;
		}
		Entity ent = e.getEntity();
		SpawnReason reason = e.getSpawnReason();

		if (nogravityastand && (ent.getType() == EntityType.ARMOR_STAND)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.p, () -> {

                if (Main.paper) {
                    PaperOnly.freezeArmorstand((ArmorStand) e.getEntity());
                } else {
                    ent.setGravity(false);

                }
            }, 40L);
		}

		if (simpleslime && (reason == SpawnReason.SLIME_SPLIT) && ent.getType() == EntityType.SLIME) {
			e.setCancelled(true);
		}

	}

}
