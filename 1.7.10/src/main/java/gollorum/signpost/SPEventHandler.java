package gollorum.signpost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gollorum.signpost.blocks.BasePost;
import gollorum.signpost.blocks.PostPost;
import gollorum.signpost.blocks.PostPostTile;
import gollorum.signpost.items.PostWrench;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PlayerStore;
import gollorum.signpost.management.WorldSigns;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.InitPlayerResponseMessage;
import gollorum.signpost.network.messages.SendAllPostBasesMessage;
import gollorum.signpost.util.BlockPos;
import gollorum.signpost.util.BoolRun;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;

public class SPEventHandler {

	private static HashMap<Runnable, Integer> tasks = new HashMap<Runnable, Integer>();
	private static HashSet<BoolRun> predicatedTasks = new HashSet<BoolRun>();

	/**
	 * Schedules a task
	 * 
	 * @param task
	 *            The task to execute
	 * @param delay
	 *            The delay in ticks (1s/20)
	 */
	public static void scheduleTask(Runnable task, int delay) {
		tasks.put(task, delay);
	}

	public static void scheduleTask(BoolRun task){
		predicatedTasks.add(task);
	}
	
	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!(event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent)) {
			return;
		}
		// time++;
		HashMap<Runnable, Integer> remainingTasks = new HashMap<Runnable, Integer>();
		for (Entry<Runnable, Integer> now : tasks.entrySet()) {
			int val = now.getValue()-1;
			if (val < 2) {
				now.getKey().run();
			}else{
				remainingTasks.put(now.getKey(), val);
			}
		}
		tasks = remainingTasks;
		
		HashSet<BoolRun> remainingPreds = new HashSet<BoolRun>();
		for(BoolRun now: predicatedTasks){
			if(!now.run()){
				remainingPreds.add(now);
			}
		}
		predicatedTasks = remainingPreds;
	}

	// ServerSide
	@SubscribeEvent
	public void loggedIn(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP) {
			NetworkHandler.netWrap.sendTo(new InitPlayerResponseMessage(), (EntityPlayerMP) event.player);
			NetworkHandler.netWrap.sendTo(new SendAllPostBasesMessage(), (EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public void entConst(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityPlayerMP) {
			event.entity.registerExtendedProperties("KnownWaystones", new PlayerStore());
		}
	}
	
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		if(!event.world.isRemote) {
			WorldSigns.worldSigns(event.world);
		}
	}

	@SubscribeEvent
	public void onSave(WorldEvent.Save event) {
		if(!event.world.isRemote) {
			WorldSigns.worldSigns(event.world);
		}
	}
	
	@SubscribeEvent
	public void oBlockPlace(PlaceEvent event){
		if(!(event.player instanceof EntityPlayerMP)){
			if(event.block instanceof BasePost){
				BasePost.placeClient(event.world, new BlockPos("", event.x, event.y, event.z, event.player.dimension), event.player);
			}else if(event.block instanceof PostPost){
				PostPost.placeClient(event.world, new BlockPos("", event.x, event.y, event.z, event.player.dimension), event.player);
			}
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP)event.player;
		if(event.block instanceof BasePost){
			if(!ConfigHandler.securityLevelWaystone.canUse(player)){
				BasePost.getWaystoneRootTile(event.world, event.x, event.y, event.z).onBlockDestroy(new BlockPos(event.world, event.x, event.y, event.z, player.dimension));
				event.setCanceled(true);
			}else{
				BasePost.placeServer(event.world, new BlockPos(event.world.getWorldInfo().getWorldName(), event.x, event.y, event.z, event.player.dimension), (EntityPlayerMP) event.player);
			}
		}else if(event.block instanceof PostPost){
			if(!ConfigHandler.securityLevelSignpost.canUse(player)){
				PostPost.getWaystonePostTile(event.world, event.x, event.y, event.z).onBlockDestroy(new BlockPos(event.world, event.x, event.y, event.z, player.dimension));
				event.setCanceled(true);
			}else{
				PostPost.placeServer(event.world, new BlockPos(event.world.getWorldInfo().getWorldName(), event.x, event.y, event.z, event.player.dimension), (EntityPlayerMP) event.player);
			}
			
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event){
		if(!(event.getPlayer() instanceof EntityPlayerMP)){
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP)event.getPlayer();
		if(event.block instanceof BasePost){
			if(!ConfigHandler.securityLevelWaystone.canUse(player)){
				event.setCanceled(true);
			}else{
				BasePost.getWaystoneRootTile(event.world, event.x, event.y, event.z).onBlockDestroy(new BlockPos(event.world, event.x, event.y, event.z, player.dimension));
			}
		}else if(event.block instanceof PostPost){
			if(!ConfigHandler.securityLevelSignpost.canUse(player)){
				event.setCanceled(true);
			}else{
				PostPost.getWaystonePostTile(event.world, event.x, event.y, event.z).onBlockDestroy(new BlockPos(event.world, event.x, event.y, event.z, player.dimension));
			}
		}
		TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
		if(tile instanceof PostPostTile){
			if(event.getPlayer().getHeldItem()!=null&&event.getPlayer().getHeldItem().getItem() instanceof PostWrench){
				event.setCanceled(true);
			}
		}
	}
}