package gollorum.signpost;

import java.util.Map.Entry;
import java.util.UUID;

import gollorum.signpost.blocks.BasePost;
import gollorum.signpost.blocks.BasePostTile;
import gollorum.signpost.blocks.SuperPostPost;
import gollorum.signpost.blocks.SuperPostPostTile;
import gollorum.signpost.items.CalibratedPostWrench;
import gollorum.signpost.items.PostWrench;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PlayerProvider;
import gollorum.signpost.management.PlayerStore;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.management.WorldSigns;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.InitPlayerResponseMessage;
import gollorum.signpost.network.messages.SendAllBigPostBasesMessage;
import gollorum.signpost.network.messages.SendAllPostBasesMessage;
import gollorum.signpost.util.BoolRun;
import gollorum.signpost.util.MyBlockPos;
import gollorum.signpost.util.StringSet;
import gollorum.signpost.util.collections.Lurchpaerchensauna;
import gollorum.signpost.util.collections.Lurchsauna;
import gollorum.signpost.util.collections.Pair;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SPEventHandler {

	private static Lurchpaerchensauna<Runnable, Integer> tasks = new Lurchpaerchensauna<Runnable, Integer>();
	private static Lurchsauna<BoolRun> predicatedTasks = new Lurchsauna<BoolRun>();

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

	public static boolean cancelTask(BoolRun task){
		return predicatedTasks.remove(task);
	}
	
	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!(event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent)) {
			return;
		}
		// time++;
		Lurchpaerchensauna<Runnable, Integer> remainingTasks = new Lurchpaerchensauna<Runnable, Integer>();
		for (Entry<Runnable, Integer> now : tasks.entrySet()) {
			int val = now.getValue()-1;
			if (val < 2) {
				now.getKey().run();
			}else{
				remainingTasks.put(now.getKey(), val);
			}
		}
		tasks = remainingTasks;
		
		Lurchsauna<BoolRun> remainingPreds = new Lurchsauna<BoolRun>();
		for(BoolRun now: predicatedTasks){int a = 0;
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
			NetworkHandler.netWrap.sendTo(new SendAllBigPostBasesMessage(), (EntityPlayerMP) event.player);
			PlayerStore store = event.player.getCapability(PlayerProvider.STORE_CAP, null);
			store.init((EntityPlayerMP) event.player);
//			if(store.player==null){
//				store.init((EntityPlayerMP) event.player);
//			}else{
//				PostHandler.addAllDiscoveredByName(event.player.getUniqueID(), store.);
//				store.init((EntityPlayerMP) event.player);
//			}
		}
	}

	public static final ResourceLocation PLAYER_CAP = new ResourceLocation(Signpost.MODID, "playerstore");
	 
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent.Entity event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			PlayerProvider provider = new PlayerProvider((EntityPlayerMP) event.getEntity());
			event.addCapability(PLAYER_CAP, provider);
		}
	}
	
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		if(!event.getWorld().isRemote) {
			WorldSigns.worldSigns(event.getWorld());
		}
	}

	@SubscribeEvent
	public void onSave(WorldEvent.Save event) {
		if(!event.getWorld().isRemote) {
			WorldSigns.worldSigns(event.getWorld());
		}
	}

	@SubscribeEvent
	public void oBlockPlace(PlaceEvent event){
		if(!(event.getPlayer() instanceof EntityPlayerMP)){
			if(event.getState().getBlock() instanceof BasePost){
				BasePost.placeClient(event.getWorld(), new MyBlockPos("", event.getPos(), event.getPlayer().dimension), event.getPlayer());
			}else if(event.getState().getBlock() instanceof SuperPostPost){
				SuperPostPost.placeClient(event.getWorld(), new MyBlockPos("", event.getPos(), event.getPlayer().dimension), event.getPlayer());
			}
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP)event.getPlayer();
		if(event.getState().getBlock() instanceof BasePost){
			BasePostTile tile = BasePost.getWaystoneRootTile(event.getWorld(), event.getPos());
			if(!(ConfigHandler.securityLevelWaystone.canPlace(player) && checkWaystoneCount(player))){
				tile.onBlockDestroy(new MyBlockPos(event.getWorld(), event.getPos(), player.dimension));
				event.setCanceled(true);
			}else{
				BasePost.placeServer(event.getWorld(), new MyBlockPos(event.getWorld().getWorldInfo().getWorldName(), event.getPos(), event.getPlayer().dimension), (EntityPlayerMP) event.getPlayer());
			}
		}else if(event.getState().getBlock() instanceof SuperPostPost){
			SuperPostPostTile tile = SuperPostPost.getSuperTile(event.getWorld(), event.getPos());
			if(!(ConfigHandler.securityLevelSignpost.canPlace(player) && checkSignpostCount(player))){
				tile.onBlockDestroy(new MyBlockPos(event.getWorld(), event.getPos(), player.dimension));
				event.setCanceled(true);
			}else{
				SuperPostPost.placeServer(event.getWorld(), new MyBlockPos(event.getWorld().getWorldInfo().getWorldName(), event.getPos(), event.getPlayer().dimension), (EntityPlayerMP) event.getPlayer());
			}
		}
	}

	private boolean checkWaystoneCount(EntityPlayerMP player){
		Pair<StringSet, Pair<Integer, Integer>> pair = PostHandler.playerKnownWaystones.get(player.getUniqueID());
		int remaining = pair.b.a;
		if(remaining == 0){
			player.addChatMessage(new TextComponentString("You are not allowed to place more waystones"));
			return false;
		}else{
			if(remaining > 0){
				pair.b.a--;
			}
			return true;
		}
	}
	
	private void updateWaystoneCount(BasePostTile tile){
		if(tile == null || tile.getBaseInfo() == null){
			return;
		}
		UUID owner = tile.getBaseInfo().owner;
		if(owner == null){
			return;
		}
		Pair<StringSet, Pair<Integer, Integer>> pair = PostHandler.playerKnownWaystones.get(owner);
		if(pair!=null && pair.b.a>=0){
			pair.b.a++;
		}
	}

	private boolean checkSignpostCount(EntityPlayerMP player){
		Pair<StringSet, Pair<Integer, Integer>> pair = PostHandler.playerKnownWaystones.get(player.getUniqueID());
		int remaining = pair.b.b;
		if(remaining == 0){
			player.addChatMessage(new TextComponentString("You are not allowed to place more signposts"));
			return false;
		}else{
			if(remaining > 0){
				pair.b.b--;
			}
			return true;
		}
	}
	
	private void updateSignpostCount(SuperPostPostTile tile){
		if(tile == null || tile.owner == null){
			return;
		}
		Pair<StringSet, Pair<Integer, Integer>> pair = PostHandler.playerKnownWaystones.get(tile.owner);
		if(pair.b.b>=0){
			pair.b.b++;
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event){
		TileEntity tile = event.getWorld().getTileEntity(event.getPos());
		if(tile instanceof SuperPostPostTile 
				&& !PostHandler.isHandEmpty(event.getPlayer()) 
				&& (event.getPlayer().getHeldItemMainhand().getItem() instanceof PostWrench 
						|| event.getPlayer().getHeldItemMainhand().getItem() instanceof CalibratedPostWrench
						|| event.getPlayer().getHeldItemMainhand().getItem().equals(Items.WHEAT_SEEDS)
						|| event.getPlayer().getHeldItemMainhand().getItem().equals(Items.SNOWBALL)
						|| event.getPlayer().getHeldItemMainhand().getItem().equals(Item.getItemFromBlock(Blocks.VINE)))){
			event.setCanceled(true);
			((SuperPostPost)tile.getBlockType()).onBlockClicked(event.getWorld(), event.getPos(), event.getPlayer());
			return;
		}
		if(!(event.getPlayer() instanceof EntityPlayerMP)){
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP)event.getPlayer();
		if(event.getState().getBlock() instanceof BasePost){
			BasePostTile t = BasePost.getWaystoneRootTile(event.getWorld(), event.getPos());
			if(!ConfigHandler.securityLevelWaystone.canUse(player, ""+t.getBaseInfo().owner)){
				event.setCanceled(true);
			}else{
				updateWaystoneCount(t);
				t.onBlockDestroy(new MyBlockPos(event.getWorld(), event.getPos(), player.dimension));
			}
		}else if(event.getState().getBlock() instanceof SuperPostPost){
			SuperPostPostTile t = SuperPostPost.getSuperTile(event.getWorld(), event.getPos());
			if(!ConfigHandler.securityLevelSignpost.canUse(player, ""+t.owner)){
				event.setCanceled(true);
			}else{
				updateSignpostCount(t);
				t.onBlockDestroy(new MyBlockPos(event.getWorld(), event.getPos(), player.dimension));
			}
		}
	}
}
