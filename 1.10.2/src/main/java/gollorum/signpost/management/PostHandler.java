package gollorum.signpost.management;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import gollorum.signpost.SPEventHandler;
import gollorum.signpost.blocks.tiles.BigPostPostTile;
import gollorum.signpost.blocks.tiles.PostPostTile;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;
import gollorum.signpost.network.messages.TeleportRequestMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.BigBaseInfo;
import gollorum.signpost.util.BoolRun;
import gollorum.signpost.util.DoubleBaseInfo;
import gollorum.signpost.util.MyBlockPos;
import gollorum.signpost.util.StonedHashSet;
import gollorum.signpost.util.StringSet;
import gollorum.signpost.util.collections.Lurchpaerchensauna;
import gollorum.signpost.util.collections.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PostHandler {

	public static StonedHashSet allWaystones = new StonedHashSet();	
	private static Lurchpaerchensauna<MyBlockPos, DoubleBaseInfo> posts = new Lurchpaerchensauna<MyBlockPos, DoubleBaseInfo>();
	private static Lurchpaerchensauna<MyBlockPos, BigBaseInfo> bigPosts = new Lurchpaerchensauna<MyBlockPos, BigBaseInfo>();
	//ServerSide
	public static Lurchpaerchensauna<UUID, TeleportInformation> awaiting =  new Lurchpaerchensauna<UUID, TeleportInformation>(); 

	/**
	 * UUID = the player;
	 * Pair.StringSet = the discovered waystones;
	 * Pair.b.a = the waystones left to place;
	 * Pair.b.b = the signposts left to place;
	 */
	public static Lurchpaerchensauna<UUID, Pair<StringSet, Pair<Integer, Integer>>> playerKnownWaystones = new Lurchpaerchensauna<UUID, Pair<StringSet, Pair<Integer, Integer>>>(){
		@Override
		public Pair<StringSet, Pair<Integer, Integer>> get(Object obj){
			Pair<StringSet, Pair<Integer, Integer>> pair = super.get(obj);
			if(pair == null){
				Pair<StringSet, Pair<Integer, Integer>> p = new Pair<StringSet, Pair<Integer, Integer>>();
				p.a  = new StringSet();
				p.b = new Pair<Integer, Integer>();
				p.b.a = ConfigHandler.maxWaystones;
				p.b.b = ConfigHandler.maxSignposts;
				return put((UUID) obj, p);
			}else{
				return pair;
			}
		}
	};
	
	public static boolean doesPlayerKnowWaystone(EntityPlayerMP player, BaseInfo waystone){
		return playerKnownWaystones.get(player.getUniqueID()).a.contains(waystone.name);
	}
	
	public static void init(){
		allWaystones = new StonedHashSet();
		playerKnownWaystones = new Lurchpaerchensauna<UUID, Pair<StringSet, Pair<Integer, Integer>>>(){
			@Override
			public Pair<StringSet, Pair<Integer, Integer>> get(Object obj){
				Pair<StringSet, Pair<Integer, Integer>> pair = super.get(obj);
				if(pair == null){
					Pair<StringSet, Pair<Integer, Integer>> p = new Pair<StringSet, Pair<Integer, Integer>>();
					p.a  = new StringSet();
					p.b = new Pair<Integer, Integer>();
					p.b.a = ConfigHandler.maxWaystones;
					p.b.b = ConfigHandler.maxSignposts;
					return put((UUID) obj, p);
				}else{
					return pair;
				}
			}
		};
		posts = new Lurchpaerchensauna<MyBlockPos, DoubleBaseInfo>();
		bigPosts = new Lurchpaerchensauna<MyBlockPos, BigBaseInfo>();
		awaiting = new Lurchpaerchensauna<UUID, TeleportInformation>();
	}

	public static Lurchpaerchensauna<MyBlockPos, DoubleBaseInfo> getPosts() {
		return posts;
	}

	public static void setPosts(Lurchpaerchensauna<MyBlockPos, DoubleBaseInfo> posts) {
		PostHandler.posts = posts;
		refreshDoublePosts();
	}

	public static Lurchpaerchensauna<MyBlockPos, BigBaseInfo> getBigPosts() {
		return bigPosts;
	}

	public static void setBigPosts(Lurchpaerchensauna<MyBlockPos, BigBaseInfo> bigPosts) {
		PostHandler.bigPosts = bigPosts;
		refreshBigPosts();
	}

	private static void refreshPosts(){
		refreshDoublePosts();
		refreshBigPosts();
	}
	
	public static void refreshDoublePosts(){
		for(Entry<MyBlockPos, DoubleBaseInfo> now: posts.entrySet()){
			PostPostTile tile = (PostPostTile) now.getKey().getTile();
			if(tile!=null){
				tile.isWaystone();
				tile.getBases();
			}
		}
	}
	
	public static void refreshBigPosts(){
		for(Entry<MyBlockPos, BigBaseInfo> now: bigPosts.entrySet()){
			BigPostPostTile tile = (BigPostPostTile) now.getKey().getTile();
			if(tile!=null){
				tile.isWaystone();
				tile.getBases();
			}
		}
	}
	
	public static BaseInfo getWSbyName(String name){
		if(ConfigHandler.deactivateTeleportation){
			return new BaseInfo(name, null, null);
		}else{
			for(BaseInfo now:allWaystones){
				if(name.equals(now.name)){
					return now;
				}
			}
			return null;
		}
	}

	public static BaseInfo getForceWSbyName(String name){
		if(name==null || name.equals("null")){
			return null;
		}
		for(BaseInfo now:allWaystones){
			if(name.equals(now.name)){
				return now;
			}
		}
		return new BaseInfo(name, null, null);
	}
	
	public static class TeleportInformation{
		public BaseInfo destination;
		public int stackSize;
		public WorldServer world;
		public BoolRun boolRun;
		public TeleportInformation(BaseInfo destination, int stackSize, WorldServer world, BoolRun boolRun) {
			this.destination = destination;
			this.stackSize = stackSize;
			this.world = world;
			this.boolRun = boolRun;
		}
	}

	/**
	 * @return whether the player could pay
	 */
	public static boolean pay(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2){
		if(canPay(player, x1, y1, z1, x2, y2, z2)){
			doPay(player, x1, y1, z1, x2, y2, z2);
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean canPay(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2){
		if(ConfigHandler.cost == null || ConfigHandler.isCreative(player)){
			return true;
		}else{
			int playerItemCount = 0;
			for(ItemStack now: player.inventory.mainInventory){
				if(now != null && now.getItem() !=null && now.getItem().getClass() == ConfigHandler.cost.getClass()){
					playerItemCount += now.stackSize;
				}
			}
			return playerItemCount>=getStackSize(x1, y1, z1, x2, y2, z2);
		}
	}

	public static void doPay(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2){
		if(ConfigHandler.cost == null || ConfigHandler.isCreative(player)){
			return;
		}else{
			int stackSize = getStackSize(x1, y1, z1, x2, y2, z2);
			player.inventory.clearMatchingItems(ConfigHandler.cost, 0, stackSize, null);
		}
	}
	
	public static int getStackSize(int x1, int y1, int z1, int x2, int y2, int z2){
		if(ConfigHandler.costMult==0){
			return 1;
		}else{
			int dx = x1-x2; int dy = y1-y2; int dz = z1-z2;
			return (int) Math.sqrt(dx*dx+dy*dy+dz*dz) / ConfigHandler.costMult + 1;
		}
	}
	
	public static int getStackSize(MyBlockPos pos1, MyBlockPos pos2){
		return getStackSize(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
	}
	
	public static void confirm(final EntityPlayerMP player){
		final TeleportInformation info = awaiting.get(player.getUniqueID());
		SPEventHandler.scheduleTask(new Runnable(){
			@Override
			public void run() {
				if(info==null){
					NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.noConfirm"), player);
					return;
				}else{
					doPay(player, (int)player.posX, (int)player.posY, (int)player.posZ, info.destination.pos.x, info.destination.pos.y+1, info.destination.pos.z);
					SPEventHandler.cancelTask(info.boolRun);
					if(!(player.getServerWorld().getWorldInfo().getWorldName().equals(info.world.getWorldInfo().getWorldName()))){
						player.mcServer.getPlayerList().transferEntityToWorld(player, player.dimension, player.getServerWorld(), info.world, new SignTeleporter(info.world));
					}
					if(!(player.dimension==info.destination.pos.dim)){
						player.mcServer.getPlayerList().transferPlayerToDimension(player, info.destination.pos.dim, new SignTeleporter(info.world));
//						player.changeDimension(info.destination.pos.dim);
					}
					player.setPositionAndUpdate(info.destination.pos.x+0.5, info.destination.pos.y+1, info.destination.pos.z+0.5);
				}
			}
		}, 1);
	}

	public static void teleportMe(BaseInfo destination, final EntityPlayerMP player, int stackSize){
		if(ConfigHandler.deactivateTeleportation){
			return;
		}
		if(canTeleport(player, destination)){
			WorldServer world = (WorldServer) destination.pos.getWorld();
			if(world == null){
				NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.errorWorld", "<world>", destination.pos.world), player);
			}else{
				SPEventHandler.scheduleTask(awaiting.put(player.getUniqueID(), new TeleportInformation(destination, stackSize, world, new BoolRun(){
					private short ticksLeft = 2400;
					@Override
					public boolean run() {
						if(ticksLeft--<=0){
							awaiting.remove(player.getUniqueID());
							return true;
						}
						return false;
					}
				})).boolRun);
				NetworkHandler.netWrap.sendTo(new TeleportRequestMessage(stackSize, destination.name), player);
			}
		}else{
			NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.notDiscovered", "<Waystone>", destination.name), player);
		}
	}
	
	public static StonedHashSet getByWorld(String world){
		StonedHashSet ret = new StonedHashSet();
		for(BaseInfo now: allWaystones){
			if(now.pos.world.equals(world)){
				ret.add(now);
			}
		}
		return ret;
	}
	
	public static boolean updateWS(BaseInfo newWS, boolean destroyed){
		if(destroyed){
			if(allWaystones.remove(getWSbyName(newWS.name))){
				for(Map.Entry<UUID, Pair<StringSet, Pair<Integer, Integer>>> now: playerKnownWaystones.entrySet()){
					now.getValue().a.remove(newWS);
				}
				return true;
			}
			return false;
		}
		for(BaseInfo now: allWaystones){
			if(now.update(newWS)){
				return true;
			}
		}
		return allWaystones.add(newWS);
	}
	
	public static boolean addAllDiscoveredByName(UUID player, StringSet ws){
		if(playerKnownWaystones.containsKey(player)){
			return playerKnownWaystones.get(player).a.addAll(ws);
		}else{
			StringSet newSet = new StringSet();
			boolean ret = newSet.addAll(ws);
			Pair<StringSet, Pair<Integer, Integer>> pair = new Pair<StringSet, Pair<Integer, Integer>>();
			pair.a  = newSet;
			pair.b = new Pair<Integer, Integer>();
			pair.b.a = ConfigHandler.maxWaystones;
			pair.b.b = ConfigHandler.maxSignposts;
			playerKnownWaystones.put(player, pair);
			return ret;
		}
	}
	
	public static boolean addDiscovered(UUID player, BaseInfo ws){
		if(ws==null){
			return false;
		}
		if(playerKnownWaystones.containsKey(player)){
			return playerKnownWaystones.get(player).a.add(ws+"");
		}else{
			StringSet newSet = new StringSet();
			newSet.add(""+ws);
			Pair<StringSet, Pair<Integer, Integer>> pair = new Pair<StringSet, Pair<Integer, Integer>>();
			pair.a  = newSet;
			pair.b = new Pair<Integer, Integer>();
			pair.b.a = ConfigHandler.maxWaystones;
			pair.b.b = ConfigHandler.maxSignposts;
			playerKnownWaystones.put(player, pair);
			return true;
		}
	}
	
	public static boolean canTeleport(EntityPlayerMP player, BaseInfo target){
		StringSet playerKnows = PostHandler.playerKnownWaystones.get(player.getUniqueID()).a;
		if(ConfigHandler.disableDiscovery){
			if(!(playerKnows==null||playerKnows.contains(target.name))){
				playerKnows.add(target.name);
			}
			return true;
		}
		if(playerKnows==null){
			return false;
		}
		return playerKnows.contains(target.name);
	}
	
	public static WorldServer getWorldByName(String world, int dim){
		WorldServer ret = null;
		forLoop:
		for(WorldServer now: FMLCommonHandler.instance().getMinecraftServerInstance().worldServers){
			if(now.getWorldInfo().getWorldName().equals(world)){
				ret = now;
				continue forLoop;
			}
		}
		if(dim!=0){
			ret = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim);
		}
		return ret;
	}

	public static boolean addRep(BaseInfo ws) {
		BaseInfo toDelete = allWaystones.getByPos(ws.blockPos);
		allWaystones.removeByPos(toDelete.blockPos);
		allWaystones.add(ws);
		return true;
	}
	
	public static EntityPlayer getPlayerByName(String name){
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
	}
	
	public static boolean isHandEmpty(EntityPlayer player){
		return player.getHeldItemMainhand()==null || player.getHeldItemMainhand().getItem()==null || player.getHeldItemMainhand().getItem().equals(Item.getItemFromBlock(Blocks.AIR));
	}

	private static class SignTeleporter extends Teleporter{

		public SignTeleporter(WorldServer worldIn) {super(worldIn);}
		
		@Override
		public void placeInPortal(Entity entityIn, float rotationYaw){}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float rotationYaw){return true;}
		
		@Override
		public boolean makePortal(Entity entityIn){return true;}
		
		@Override
		public void removeStalePortalLocations(long worldTime){}
	}
}
