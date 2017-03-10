package gollorum.signpost.blocks;

import java.util.UUID;

import gollorum.signpost.Signpost;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.BaseUpdateClientMessage;
import gollorum.signpost.network.messages.ChatMessage;
import gollorum.signpost.network.messages.OpenGuiMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.BlockPos;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BasePost extends BlockContainer {

	public BasePost() {
		super(Material.rock);
		this.setHardness(2);
		this.setResistance(100000);
		setBlockName("SignpostBase");
		setCreativeTab(CreativeTabs.tabTransport);
		setBlockTextureName(Signpost.MODID + ":base");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (ConfigHandler.deactivateTeleportation) {
			return false;
		}
		if (!world.isRemote) {
			BaseInfo ws = getWaystoneRootTile(world, x, y, z).getBaseInfo();
			if (!player.isSneaking()) {
				if (!ConfigHandler.deactivateTeleportation) {
					NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.discovered", "<Waystone>", ws.name), (EntityPlayerMP) player);
				}
				PostHandler.addDiscovered(player.getUniqueID(), ws);
			} else {
				if (!ConfigHandler.deactivateTeleportation
						&& ConfigHandler.securityLevelWaystone.canUse((EntityPlayerMP) player)) {
					NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiBaseID, x, y, z), (EntityPlayerMP) player);
				}
			}
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		return new BasePostTile().setup();
	}

	public static BasePostTile getWaystoneRootTile(World world, int x, int y, int z) {
		TileEntity ret = world.getTileEntity(x, y, z);
		if (ret instanceof BasePostTile) {
			return (BasePostTile) ret;
		} else {
			return null;
		}
	}

	public static String generateName() {
		int i = 0;
		String ret;
		do {
			ret = "Waystone " + (PostHandler.allWaystones.size() + (i++));
		} while (PostHandler.allWaystones.nameTaken(ret));
		return ret;
	}

	public static void placeServer(World world, BlockPos pos, EntityPlayerMP player) {
		BasePostTile tile = getWaystoneRootTile(world, pos.x, pos.y, pos.z);
		String name = generateName();
		UUID owner = player.getUniqueID();
		BaseInfo ws = new BaseInfo(name, pos, owner);
		if(tile.getBaseInfo()==null){
			PostHandler.allWaystones.add(ws);
		}else{
			tile.getBaseInfo().setAll(ws);
		}
		PostHandler.addDiscovered(player.getUniqueID(), tile.getBaseInfo());
		NetworkHandler.netWrap.sendToAll(new BaseUpdateClientMessage());
	}

	public static void placeClient(final World world, final BlockPos pos, final EntityPlayer player) {
		BasePostTile tile = getWaystoneRootTile(world, pos.x, pos.y, pos.z);
		if (tile != null && tile.getBaseInfo() == null) {
			PostHandler.allWaystones.add(new BaseInfo("", pos, player.getUniqueID()));
		}
	}
}