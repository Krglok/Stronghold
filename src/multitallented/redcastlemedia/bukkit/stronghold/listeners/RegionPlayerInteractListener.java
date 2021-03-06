package multitallented.redcastlemedia.bukkit.stronghold.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import multitallented.redcastlemedia.bukkit.stronghold.Stronghold;
import multitallented.redcastlemedia.bukkit.stronghold.region.RegionCondition;
import multitallented.redcastlemedia.bukkit.stronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.stronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.stronghold.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.stronghold.region.SuperRegionType;
import multitallented.redcastlemedia.bukkit.stronghold.region.Region;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

/**
 * 
 * @author Multitallented
 */
public class RegionPlayerInteractListener implements Listener
{
	private final RegionManager rm;
	private final Map<Player, String> channels = new HashMap<Player, String>();
	private final Stronghold plugin;

	public RegionPlayerInteractListener(Stronghold plugin)
	{
		this.rm = plugin.getRegionManager();
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String channel = channels.get(player);
		if (channel == null || channel.equals(""))
		{
			return;
		}
		event.setCancelled(true);
		String title = null;
		try
		{
			for (String s : rm.getSuperRegion(channel).getMember(
					player.getName()))
			{
				if (s.contains("title:"))
				{
					title = s.replace("title:", "");
				}
			}
		} catch (NullPointerException npe)
		{

		}
		SendMessageThread smt = new SendMessageThread(plugin, channel,
				channels, title, player, event.getMessage());
		String message = "[" + channel + "] " + player.getDisplayName() + ": "
				+ event.getMessage();
		Logger.getLogger("Minecraft").log(Level.INFO, message);
		try
		{
			smt.run();
		} catch (Exception e)
		{

		}
	}

	public void setPlayerChannel(Player p, String s)
	{
		if (s.equals(""))
		{
			String prevChannel = channels.get(p);
			String title = null;
			try
			{
				for (String sn : rm.getSuperRegion(prevChannel).getMember(
						p.getName()))
				{
					if (sn.contains("title:"))
					{
						title = sn.replace("title:", "");
					}
				}
			} catch (NullPointerException npe)
			{

			}
			channels.remove(p);
			if (prevChannel != null && !prevChannel.endsWith(s))
			{
				SendMessageThread smt = new SendMessageThread(plugin,
						prevChannel, channels, title, p, p.getDisplayName()
								+ " has left channel " + s);
				try
				{
					smt.run();
				} catch (Exception e)
				{

				}
			}
			return;
		}
		channels.put(p, s);
		String title = null;
		try
		{
			for (String sn : rm.getSuperRegion(s).getMember(p.getName()))
			{
				if (sn.contains("title:"))
				{
					title = sn.replace("title:", "");
				}
			}
		} catch (NullPointerException npe)
		{

		}
		SendMessageThread smt = new SendMessageThread(plugin, s, channels,
				title, p, p.getDisplayName() + " has joined channel " + s);
		try
		{
			smt.run();
		} catch (Exception e)
		{

		}
	}

	public Map<Player, String> getChannels()
	{
		return channels;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
//    	Block b = event.getClickedBlock();
//    	if (b != null)
//    	{
//	    	if (event.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD)
//	    	{
//	    		cmdBlazeRod(event);
//				event.setCancelled(true);
//	    		return;
//	    	}
//    	}
		if (event.getAction() == Action.PHYSICAL)
		{
			if ((event.getClickedBlock().getType() == Material.CROPS || event
					.getClickedBlock().getTypeId() ==  60))
			{
				ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
				conditions.add(new RegionCondition("denyblockbreak", true, 0));
				conditions.add(new RegionCondition("denyblockbreaknoreagent",
						false, 0));
				if (rm.shouldTakeAction(event.getClickedBlock().getLocation(),
						event.getPlayer(), conditions))
				{
					event.setCancelled(true);
					return;
				}
			} else
			{
				ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
				conditions.add(new RegionCondition("denyplayerinteract", true,
						0));
				conditions.add(new RegionCondition(
						"denyplayerinteractnoreagent", false, 0));
				conditions.add(new RegionCondition("denyusecircuit", true, 0));
				conditions.add(new RegionCondition("denyusecircuitnoreagent",
						false, 0));
				if (rm.shouldTakeAction(event.getClickedBlock().getLocation(),
						event.getPlayer(), conditions))
				{
					event.setCancelled(true);
					return;
				}
			}
			return;
		}

		ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
		conditions.add(new RegionCondition("denyplayerinteract", true, 0));
		conditions.add(new RegionCondition("denyplayerinteractnoreagent",
				false, 0));
		if (event.getClickedBlock().getType() == Material.LEVER
				|| event.getClickedBlock().getType() == Material.STONE_BUTTON)
		{
			conditions.add(new RegionCondition("denyusecircuit", true, 0));
			conditions.add(new RegionCondition("denyusecircuitnoreagent",
					false, 0));
		} else if (event.getClickedBlock().getType() == Material.WOODEN_DOOR
				|| event.getClickedBlock().getType() == Material.TRAP_DOOR
				|| event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK)
		{
			conditions.add(new RegionCondition("denyusedoor", true, 0));
			conditions
					.add(new RegionCondition("denyusedoornoreagent", false, 0));
		} else if (event.getClickedBlock().getType() == Material.CHEST
				|| event.getClickedBlock().getType() == Material.FURNACE
				|| event.getClickedBlock().getType() == Material.DISPENSER)
		{
			conditions.add(new RegionCondition("denyusechest", true, 0));
			conditions.add(new RegionCondition("denyusechestnoreagent", false,
					0));
		}
		if (rm.shouldTakeAction(event.getClickedBlock().getLocation(),
				event.getPlayer(), conditions))
		{
			event.getPlayer().sendMessage(
					ChatColor.GRAY + "[Stronghold] This region is protected");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		if (event.isCancelled()
				|| (!rm.shouldTakeAction(event.getBed().getLocation(),
						event.getPlayer(), 0, "denyplayerinteract", true) && !rm
						.shouldTakeAction(event.getBed().getLocation(),
								event.getPlayer(), 0,
								"denyplayerinteractnoreagent", false)))
		{
			return;
		}

		event.getPlayer().sendMessage(
				ChatColor.GRAY + "[Stronghold] This region is protected");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		if (event.isCancelled()
				|| (!rm.shouldTakeAction(event.getBlockClicked().getLocation(),
						event.getPlayer(), 0, "denybucketuse", true) && !rm
						.shouldTakeAction(
								event.getBlockClicked().getLocation(),
								event.getPlayer(), 0, "denybucketusenoreagent",
								false)))
		{
			return;
		}

		event.getPlayer().sendMessage(
				ChatColor.GRAY + "[Stronghold] This region is protected");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (event.isCancelled()
				|| (!rm.shouldTakeAction(event.getBlockClicked().getLocation(),
						event.getPlayer(), 0, "denybucketuse", true) && !rm
						.shouldTakeAction(
								event.getBlockClicked().getLocation(),
								event.getPlayer(), 0, "denybucketusenoreagent",
								false)))
		{
			return;
		}

		event.getPlayer().sendMessage(
				ChatColor.GRAY + "[Stronghold] This region is protected");
		event.setCancelled(true);
	}

    /**
     * do action with BLAZEROD in settlements
     * @param event
     */
    private void cmdBlazeRod(PlayerInteractEvent event)
    {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
	    	Block target = event.getClickedBlock();
	    	int regionId = 0;
	    	Location pos = target.getLocation();
			ArrayList<Region> targets =  plugin.getRegionManager().getContainingRegions(pos);
	    	ArrayList<String> msg = new ArrayList<String>();
			msg.add("== Stronghold : Region Info");
			if (targets != null)
			{
				if (targets.size() > 0)
				{
		    		for (Region region : targets)
		    		{
	    				regionId = region.getID();
		    			if (region.getOwners().size() > 0)
		    			{
		    				msg.add(region.getID()+":"+region.getType()+":"+region.getOwners().get(0));
		    			} else
		    			{
		    				msg.add(region.getID()+":"+region.getType()+":");
		    			}
		    		}
				} else
				{
		    		msg.add("No Region found ! ");
				}
	
			} else
			{
	    		msg.add("No Region found ! ");
			}
			for (SuperRegion sRegion : plugin.getRegionManager().getContainingSuperRegions(pos))
			{
				if (sRegion.getOwners().size() > 0)
				{
					msg.add(sRegion.getName()+":"+sRegion.getType()+":"+sRegion.getOwners().get(0));
				} else
				{
					msg.add(sRegion.getName()+":"+sRegion.getType()+":");
				}
			}
			
			plugin.getMessageData().printPage(event.getPlayer(), msg, 1);
		}    	
    }

}

