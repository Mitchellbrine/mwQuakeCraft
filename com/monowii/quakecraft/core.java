package com.monowii.quakecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


public class core extends JavaPlugin implements Listener
{
	
	//pInfos example:    monowii    : 7895423402_0_1
	//                   PlayerName : LastShotTime_Kills_Deaths
	
	
	Location spawn = null;
	
	ArrayList<Player> Players = new ArrayList<Player>();
	ArrayList<Player> redTeam = new ArrayList<Player>();
	ArrayList<Player> blueTeam = new ArrayList<Player>();
	HashMap<String, Long> pReloadRailgun = new HashMap<String, Long>();
	HashMap<String, Long> pReloadShotgun = new HashMap<String, Long>();
	HashMap<String, Long> pReloadRocket = new HashMap<String, Long>();

	int blueScore = 0;
	int redScore = 0;
	
	
	public void onEnable() 
	{
		getServer().getPluginManager().registerEvents(this, this);
		
		spawn = new Location(getServer().getWorld("world"), 598, 4, 1119);
		spawn.setPitch(6);
		spawn.setYaw(180);
		
		spawnBonusWeapon();
		newScore();
	}
	
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent e)
	{
		if (e.getBlock().getTypeId() == 39) 
		{
			e.setCancelled(true);
		}
	}
	
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) 
	{
		if (e.getEntity() instanceof Arrow)
		{
			Player shooter = (Player)e.getEntity().getShooter();
			
			getServer().getWorld("world").createExplosion(e.getEntity().getLocation(), 0, false);
			getServer().getWorld("world").playEffect(e.getEntity().getLocation(), Effect.SMOKE, 5);
			List<Entity> eList = e.getEntity().getNearbyEntities(2, 2, 2);
			
			for (Entity pl : eList)
			{
				System.out.println(pl);
				
				if (pl instanceof Player) 
				{
					Player target = (Player)pl;
					
					
					if ( (( redTeam.contains(shooter) && blueTeam.contains(target) ) || ( blueTeam.contains(shooter) && redTeam.contains(target) )) && !target.isDead())
					{
						target.setHealth(0);
						
						addFrag(shooter, target.getName(), "Rocket Launcher");
					}
				}
			}
			
			e.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) 
	{
		//IF THE PLAYER IS HIT BY AN ARROW (FROM THE ROCKET LAUNCHER)
		if (e.getDamager() instanceof Arrow && e.getEntity() instanceof Player)
		{
			Player shooter = (Player) ((Snowball) e.getDamager()).getShooter();
			Player target = (Player) e.getEntity();
			
			e.setCancelled(true);
			
			target.setHealth(0);
			
			addFrag(shooter, target.getName(), "Rocket Launcher");
		}
		//IF THE PLAYER IS HIT BY AN SNOWBALL (FROM THE SHOTGUN)
		if (e.getDamager() instanceof Snowball && e.getEntity() instanceof Player)
		{
			Player shooter = (Player) ((Snowball) e.getDamager()).getShooter();
			Player target = (Player) e.getEntity();
			
			e.setCancelled(true);
			
			if ( ( redTeam.contains(shooter) && blueTeam.contains(target) ) || ( blueTeam.contains(shooter) && redTeam.contains(target) ) )
			{
				if (target.getHealth() - 6 >= 0)
				{
					target.setHealth(target.getHealth()-6);
				}
				else
				{
					target.setHealth(0);
					
					
					addFrag(shooter, target.getName(), "Shotgun");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) 
	{
		e.getPlayer().teleport(spawn);
		e.getPlayer().setGameMode(GameMode.ADVENTURE);
	}
	
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		if (Players.contains(p)) 
		{
			Players.remove(p);
		}
		if (redTeam.contains(p))
		{
			redTeam.remove(p);
		}			
		if (blueTeam.contains(p))
		{
			blueTeam.remove(p);
		}
		
	    for (PotionEffect effect : p.getActivePotionEffects()) 
	    {
	    	p.removePotionEffect(effect.getType());
	    }
	        
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args)
	{
    	Player p = null;
    	if (sender instanceof Player) { p = (Player) sender; }
    	
    	if (cmd.getName().equalsIgnoreCase("test"))
    	{
    		p.sendMessage("Pos: x: "+p.getLocation().getX()+" y: "+p.getLocation().getY()+" z: "+p.getLocation().getZ());
    		p.sendMessage("Yaw: "+p.getLocation().getYaw()+" Pitch: "+p.getLocation().getPitch());
    		
    		p.sendMessage("Red: "+redTeam.size()+" / Blue: "+blueTeam.size());
    		
    	}
    	
    	if (cmd.getName().equalsIgnoreCase("quake"))
    	{
    		if (args.length == 0)
    		{
    			//p.sendMessage("§8----=[ §6mwQuakeCraft §8]=----");
    			p.sendMessage("§cYou must choose a team ! §7/join red §for §7/join blue");
    		} 
    		else 
    		{
    			if (args[0].equalsIgnoreCase("red"))
    			{
    				
    				if (redTeam.contains(p)) {
    					redTeam.remove(p);
    				} if (blueTeam.contains(p)) {
    					blueTeam.remove(p);
    				}
    				
    				p.getInventory().clear();
    				p.sendMessage("§aYou join red team !");
    				Players.add(p);
    				redTeam.add(p);
    				pReloadRailgun.put(p.getName(), System.currentTimeMillis()-3000);
    				pReloadShotgun.put(p.getName(), System.currentTimeMillis()-3000);
    				pReloadRocket.put(p.getName(), System.currentTimeMillis()-3000);
    				p.teleport(redSpawn());
					p.getInventory().addItem(new ItemStack(Material.STICK, 1));
					p.getInventory().addItem(new ItemStack(Material.BLAZE_ROD, 1));
    				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
    				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
    				p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
    			}
    			if (args[0].equalsIgnoreCase("blue"))
    			{
    				if (redTeam.contains(p)) {
    					redTeam.remove(p);
    				} if (blueTeam.contains(p)) {
    					blueTeam.remove(p);
    				}
    				
    				p.getInventory().clear();
    				p.sendMessage("§aYou join blue team !");
    				Players.add(p);
    				blueTeam.add(p);
    				pReloadRailgun.put(p.getName(), System.currentTimeMillis());
    				pReloadShotgun.put(p.getName(), System.currentTimeMillis()-3000);
    				pReloadRocket.put(p.getName(), System.currentTimeMillis()-3000);
    				p.teleport(blueSpawn());
					p.getInventory().addItem(new ItemStack(Material.STICK, 1));
					p.getInventory().addItem(new ItemStack(Material.BLAZE_ROD, 1));
    				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
    				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
    				p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 9));
    			}
    		}
    	}
		return false;
	}
	
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) 
	{
		
		if (e.getEntity() instanceof Player) 
		{
			Player p = (Player)e.getEntity();
			
			if (!p.isDead())
			{
				if (e.getCause() == DamageCause.VOID)
				{
					p.setHealth(0);
				}
				
				if (e.getCause() == DamageCause.FALL) 
				{
					e.setCancelled(true);
				}
			}
			

		}
	}
	
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) 
	{
		e.getDrops().clear();
		
		e.setDeathMessage("");
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		final Player p = e.getPlayer();
		
		if (redTeam.contains(p))
		{
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
			{
				public void run()
				{
					p.getInventory().addItem(new ItemStack(Material.STICK, 1));
					p.getInventory().addItem(new ItemStack(Material.BLAZE_ROD, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
					p.teleport(redSpawn());
					p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
				}
			}, 2);
		}
		
		if (blueTeam.contains(p))
		{
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
			{
				public void run()
				{
					p.getInventory().addItem(new ItemStack(Material.STICK, 1));
					p.getInventory().addItem(new ItemStack(Material.BLAZE_ROD, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
					p.teleport(blueSpawn());
					p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 9));
				}
			}, 2);
		}
		
	}
	
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		
		//                                                                     =====TELEPORTERS=====
		if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.STONE_PLATE) {
			Location bLoc = e.getClickedBlock().getLocation();
			
			//BluePort
			if ((bLoc.getX() == 731 && bLoc.getY() == 18) && (bLoc.getZ() == 1271 || bLoc.getZ() == 1272)) {
				bluePort(e.getPlayer());
			}
			
			//redPort
			if ((bLoc.getX() == 573 && bLoc.getY() == 4) && (bLoc.getZ() == 1258 || bLoc.getZ() == 1259)) {
				redPort(e.getPlayer());
			}
		}
		
		
		
		
        //    													         	   =====ROCKET LAUNCHER=====
		
		if (Players.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getType() == Material.FLINT && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
		{
			if (System.currentTimeMillis() - (pReloadRocket.get(e.getPlayer().getName())) >= 2000)
			{
				Player p = e.getPlayer();
				
				if (p.getInventory().contains(Material.GLOWSTONE_DUST))
{
					Arrow arrow = p.getWorld().spawn(p.getEyeLocation(), Arrow.class);
					arrow.setShooter(p);
					arrow.setVelocity(p.getLocation().getDirection().multiply(4));
					
					pReloadRocket.put(p.getName(), System.currentTimeMillis());
					
					p.getInventory().removeItem(new ItemStack(Material.GLOWSTONE_DUST, 1));
					
					p.getWorld().playSound(p.getLocation(), Sound.CREEPER_HISS, 1, 1);
				}
				else
				{
					p.sendMessage("§cVous n'avez pas de munitions !");
				}
			}
			else
			{
				e.getPlayer().sendMessage("§5"+reloadTime("Rocket", e.getPlayer().getName()));
			}
		}
		
		
		
		
		
		//                                                                          =====SHOTGUN=====
		
		
		if (Players.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
		{
			if (System.currentTimeMillis() - (pReloadShotgun.get(e.getPlayer().getName())) >= 1000)
			{
				Player p = e.getPlayer();
				Vector vec = p.getLocation().getDirection().multiply(4);
				
				double yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				double xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				double zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				
				
				//Ball 1
				vec.setY(vec.getY()+yRand);
				vec.setX(vec.getX()+xRand);
				vec.setZ(vec.getZ()+zRand);
				
				Snowball ball = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
				ball.setShooter(p);
				ball.setVelocity(vec);
				
				//Ball 2
				yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				vec = p.getLocation().getDirection().multiply(4);
				vec.setY(vec.getY()+yRand);
				vec.setX(vec.getX()+xRand);
				vec.setZ(vec.getZ()+zRand);
				
				Snowball ball2 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
				ball2.setShooter(p);
				ball2.setVelocity(vec.setY(vec.getY()-0.3));
				
				//Ball 3
				yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				vec = p.getLocation().getDirection().multiply(4);
				vec.setY(vec.getY()+yRand);
				vec.setX(vec.getX()+xRand);
				vec.setZ(vec.getZ()+zRand);
				
				Snowball ball3 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
				ball3.setShooter(p);
				ball3.setVelocity(vec.setY(vec.getY()-0.3));
				
				//Ball 4
				yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				vec = p.getLocation().getDirection().multiply(4);
				vec.setY(vec.getY()+yRand);
				vec.setX(vec.getX()+xRand);
				vec.setZ(vec.getZ()+zRand);
				
				Snowball ball4 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
				ball4.setShooter(p);
				ball4.setVelocity(vec.setY(vec.getY()-0.3));
				
				//Ball 5
				yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
				vec = p.getLocation().getDirection().multiply(4);
				vec.setY(vec.getY()+yRand);
				vec.setX(vec.getX()+xRand);
				vec.setZ(vec.getZ()+zRand);
				
				Snowball ball5 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
				ball5.setShooter(p);
				ball5.setVelocity(vec.setY(vec.getY()-0.3));
				
				getServer().getWorld("world").playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1, 1);
				
				pReloadShotgun.put(p.getName(), System.currentTimeMillis());
			}
			else
			{
				e.getPlayer().sendMessage("§5"+reloadTime("Shotgun", e.getPlayer().getName()));
			}
			

		}
		
		
		
		//                                                                    =====RAILGUN=====
		
		if (Players.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getType() == Material.STICK && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
		{
			if ((System.currentTimeMillis() - (pReloadRailgun.get(e.getPlayer().getName())) >= 1500))
			{
				Player p = e.getPlayer();
				
				if (p.getLocation().getY() < -2) { return; }
				
				final List<Block> blocks = p.getLineOfSight(null, 75);
				blocks.remove(0);
				int LaserNombreBlocks = blocks.size();
				long timer = System.currentTimeMillis();
				
				HashMap<Location, String> pLoc = new HashMap<Location, String>();
				
				pReloadRailgun.put(p.getName(), timer);
				
				
				for (Player ply : Players) 
				{
					
					if (ply != p) 
					{
						pLoc.put(new Location(ply.getWorld(), ply.getLocation().getBlockX(), ply.getLocation().getBlockY(), ply.getLocation().getBlockZ()), ply.getName());
					}
					
				}
				
				for (int i = LaserNombreBlocks; i > 0 ; i--)
				{
					if (blocks.get((i-1)).getTypeId() == 0)
					{
						blocks.get((i-1)).setTypeId(39);
						
						//If the laser hit the legs
						if (pLoc.containsKey(blocks.get((i-1)).getLocation()))
						{
							String PlayerToKill = pLoc.get(blocks.get((i-1)).getLocation());
							
							if ( (redTeam.contains(p) && blueTeam.contains(getServer().getPlayer(PlayerToKill))) || (blueTeam.contains(p) && redTeam.contains(getServer().getPlayer(PlayerToKill))) ) 
							{
								getServer().getPlayer(PlayerToKill).setHealth(0);
								
								addFrag(p, PlayerToKill, "Railgun");
								
							}
							
						}
						//If the laser hit the body/head
						else if (pLoc.containsKey(  new Location(blocks.get((i-1)).getWorld(), blocks.get((i-1)).getX(), blocks.get((i-1)).getY() - 1, blocks.get((i-1)).getZ())  ))
						{
							String PlayerToKill = pLoc.get(new Location(blocks.get((i-1)).getWorld(), blocks.get((i-1)).getX(), blocks.get((i-1)).getY() - 1, blocks.get((i-1)).getZ()));
							
							if ( ((redTeam.contains(p) && blueTeam.contains(getServer().getPlayer(PlayerToKill))) || (blueTeam.contains(p) && redTeam.contains(getServer().getPlayer(PlayerToKill)))) && !getServer().getPlayer(PlayerToKill).isDead() ) 
							{
								getServer().getPlayer(PlayerToKill).setHealth(0);
								
								addFrag(p, PlayerToKill, "Railgun");
							}
							
						}
					}
					else 
					{
						blocks.remove(i-1);
					}

				}
				
				getServer().getWorld("world").playSound(p.getLocation(), Sound.WITHER_HURT, 1, 1);
				
				
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
				{
					public void run()
					{
						for (Block b : blocks)
						{
							b.setTypeId(0);
						}
					}
					
				}, 10L);
				
				
			}
			else
			{
				e.getPlayer().sendMessage("§5"+reloadTime("Railgun", e.getPlayer().getName()));
			}
		
		}
		
	}
    
	
	public String reloadTime(String gun, String player)
	{
		String toReturn = "Error !";
		long time = 0;
		int reloadTimeDividePer10 = 0;
		
		if (gun == "Railgun") {
			time = System.currentTimeMillis() - pReloadRailgun.get(player);
			toReturn = "ok";
			gun = "Railgun";
			reloadTimeDividePer10 = 150;
		}
		if (gun == "Shotgun") {
			time = System.currentTimeMillis() - pReloadShotgun.get(player);
			toReturn = "ok";
			gun = "Shotgun";
			reloadTimeDividePer10 = 100;
		}
		if (gun == "Rocket") {
			time = System.currentTimeMillis() - pReloadRocket.get(player);
			toReturn = "ok";
			gun = "Rocket Launcher";
			reloadTimeDividePer10 = 200;
		}
		
		if (reloadTimeDividePer10 != 0) 
		{
			if (time < reloadTimeDividePer10) {
				toReturn = gun+" reload §f[§7__________§f] §e0%";
			} else if (time < reloadTimeDividePer10 * 2) {
				toReturn = gun+" reload §f[§a=§7_________§f] §e10%";
			} else if (time < reloadTimeDividePer10 * 3) {
				toReturn = gun+" reload §f[§a==§7________§f] §e20%";
			} else if (time < reloadTimeDividePer10 * 4) {
				toReturn = gun+" reload §f[§a===§7_______§f] §e30%";
			} else if (time < reloadTimeDividePer10 * 5) {
				toReturn = gun+" reload §f[§a====§7______§f] §e40%";
			} else if (time < reloadTimeDividePer10 * 6) {
				toReturn = gun+" reload §f[§a=====§7_____§f] §e50%";
			} else if (time < reloadTimeDividePer10 * 7) {
				toReturn = gun+" reload §f[§a======§7____§f] §e60%";
			} else if (time < reloadTimeDividePer10 * 8) {
				toReturn = gun+" reload §f[§a=======§7___§f] §e70%";
			} else if (time < reloadTimeDividePer10 * 9) {
				toReturn = gun+" reload §f[§a========§7__§f] §e80%";
			} else if (time < reloadTimeDividePer10 * 10) {
				toReturn = gun+" reload §f[§a=========§7_§f] §e90%";
			}
		}
		return toReturn;
	}
	
	
	public String convertToTime(long ms)
	{
		int ms1 = (int)ms;
	    int secs = ms1 / 1000;
	    int mins = secs / 60;
	    int hours = mins / 60;
	    
	    hours %= 24;
	    secs %= 60;
	    mins %= 60;
	    ms1 %= 1000;
	    
	    
	    String hoursS = Integer.toString(hours);
	    String secsS = Integer.toString(secs);
	    String minsS = Integer.toString(mins);
	    String ms2 = Integer.toString(ms1);
	    
	    if (secs < 10) secsS = "0" + secsS;
	    if (mins < 10) minsS = "0" + minsS;
	    if (hours < 10) hoursS = "0" + hoursS;
	    
	    return hoursS + "h:" + minsS + "m:" + secsS + "s:" + ms2 + "ms";
	}
	
	public Location redSpawn()
	{
		Random random = new Random();
		int zmin = 1153;
		int zmax = 1172;
		int xmin = 586;
		int xmax = 593;
		
		double finalZ = (random.nextInt(zmax - zmin) + zmin) + 0.50;
		double finalX = (random.nextInt(xmax - xmin) + xmin) + 0.50;
		
		Location redSpawn = new Location(getServer().getWorld("world"), finalX, 4, finalZ);
		redSpawn.setPitch(12);
		redSpawn.setYaw(-90);
		
		return redSpawn;
	}

	public Location blueSpawn()
	{
		Random random = new Random();
		int zmin = 1160;
		int zmax = 1184;
		int xmin = 687;
		int xmax = 696;
		
		double finalZ = (random.nextInt(zmax - zmin) + zmin) + 0.50;
		double finalX = (random.nextInt(xmax - xmin) + xmin) + 0.50;
		
		Location blueSpawn = new Location(getServer().getWorld("world"), finalX, 4, finalZ);
		
		blueSpawn.setPitch(9);
		blueSpawn.setYaw(90);
		
		return blueSpawn;
	}
	
	public void redPort(Player p)
	{
		Location portLoc = new Location(p.getWorld(), 580.5, 52.5, 1271.5);
		portLoc.setPitch(-7);
		portLoc.setYaw(242);
		
		p.teleport(portLoc);
		p.setVelocity(p.getLocation().getDirection().multiply(2));
	}
	
	public void bluePort(Player p) 
	{
		Location portLoc = new Location(p.getWorld(), 739.5, 50.5, 1264.5);
		portLoc.setPitch(-17);
		portLoc.setYaw(90);
		
		p.teleport(portLoc);
		p.setVelocity(p.getLocation().getDirection().multiply(2));
		
	}
	
	public void spawnBonusWeapon() 
	{
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			public void run() 
			{
				getServer().getWorld("world").dropItem(new Location(getServer().getWorld("world"), 645, 4, 1150), new ItemStack(Material.GLOWSTONE_DUST, 16));
				getServer().getWorld("world").dropItem(new Location(getServer().getWorld("world"), 634, 4, 1195), new ItemStack(Material.FLINT, 1));
			}
			
		}, 300L, 1200L);
	}
	
	public void newScore() 
	{
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			public void run() 
			{
				getServer().broadcastMessage("§8-----=[§6 Scores §8]=-----");
				getServer().broadcastMessage("§3blueTeam:§7 "+blueScore+" points");
				getServer().broadcastMessage("§4redTeam:§7 "+redScore+" points");
			}
			
		}, 300L, 300L);
	}
	
	public void addFrag(Player p, String target, String weapon) 
	{
		if (redTeam.contains(p))
		{
			getServer().broadcastMessage("§4"+p.getName()+" §8has fragged §3"+target+" §7(§d"+weapon+"§7)");
			redScore++; 
		}
		if (blueTeam.contains(p)) 
		{
			getServer().broadcastMessage("§3"+p.getName()+" §8has fragged §4"+target+" §7(§d"+weapon+"§7)");
			blueScore++; 
		}
		
		for (Player pl : getServer().getOnlinePlayers())
		{
			pl.playSound(pl.getLocation(), Sound.GHAST_DEATH, 1, 1);
		}
	}
}
