package gollorum.signpost.util;

import cpw.mods.fml.common.network.ByteBufUtils;
import gollorum.signpost.Signpost;
import gollorum.signpost.blocks.tiles.BigPostPostTile;
import gollorum.signpost.blocks.tiles.PostPostTile;
import gollorum.signpost.management.ConfigHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MyBlockPos {
	
	public int x, y, z;
	public String world;
	public int dim;

	public MyBlockPos(World world, int x, int y, int z, int dim){
		this((world==null||world.isRemote)?"":world.getWorldInfo().getWorldName(), x,
				y, z, dim);
	}

	public MyBlockPos(String world, double x, double y, double z, int dim){
		this(world, (int)x, (int)y, (int)z, dim);
	}

	public MyBlockPos(String world, int x, int y, int z, int dim){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dim = dim;
	}

	public MyBlockPos(MyBlockPos pos) {
		this(pos.world, pos.x, pos.y, pos.z, pos.dim);
	}

	public MyBlockPos(Entity entity){
		this(entity.worldObj, (int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ), dim(entity.worldObj));
	}

	public static int dim(World world){
		if(world==null||world.provider==null){
			return Integer.MIN_VALUE;
		}else
			return world.provider.dimensionId;
	}
	
	public Connection canConnectTo(BaseInfo inf){
		if(inf==null){
			return Connection.VALID;
		}
		if(ConfigHandler.deactivateTeleportation){
			return Connection.VALID;
		}
		if(!(ConfigHandler.interdimensional||(sameWorld(inf.pos) && sameDim(inf.pos)))){
			return Connection.WORLD;
		}
		if(ConfigHandler.maxDist>-1&&distance(inf.pos)>ConfigHandler.maxDist){
			return Connection.DIST;
		}
		return Connection.VALID;
	}

	public static enum Connection{VALID, WORLD, DIST}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tC){
		int[] arr = {x, y, z, dim};
		tC.setIntArray("Position", arr);
		tC.setString("WorldName", world);
		return tC;
	}
	
	public static MyBlockPos readFromNBT(NBTTagCompound tC){
		int[] arr = tC.getIntArray("Position");
		return new MyBlockPos(tC.getString("WorldName"), arr[0], arr[1], arr[2], arr[3]);
	}

	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, world);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(dim);
	}
	
	public static MyBlockPos fromBytes(ByteBuf buf) {
		String world = ByteBufUtils.readUTF8String(buf);
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		int dim = buf.readInt();
		return new MyBlockPos(world, x, y, z, dim);
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof MyBlockPos)){
			return false;
		}
		MyBlockPos other = (MyBlockPos)obj;
			if(other.x!=this.x){
				return false;
			}else if(other.y!=this.y){
				return false;
			}else if(other.z!=this.z){
				return false;
			}else if(!sameWorld(other)){
				return false;
			}else if(!sameDim(other)){
				return false;
			}else return true;
	}
	
	public boolean sameWorld(MyBlockPos other){
		if(other.world.equals("")){
			other.world = this.world;
		}else if(this.world.equals("")){
			this.world = other.world;
		}else if(!this.world.equals(other.world)){
			return false;
		}
		return true;
	}
	
	public boolean sameDim(MyBlockPos other){
		if(other.dim==Integer.MIN_VALUE || this.dim == Integer.MIN_VALUE){
			other.dim = this.dim = Math.max(other.dim, this.dim);
		}else if(other.dim!=this.dim){
				return false;
		}
		return true;
	}
	
	public MyBlockPos update(MyBlockPos newPos){
		x = newPos.x;
		y = newPos.y;
		z = newPos.z;
		if(!(newPos.dim==Integer.MIN_VALUE)){
			dim = newPos.dim;
		}else{
			newPos.dim = dim;
		}
		if(!newPos.world.equals("")){
			world = newPos.world;
		}else{
			newPos.world = world;
		}
		return this;
	}
	
	public double distance(MyBlockPos other){
		int dx = this.x-other.x;
		int dy = this.y-other.y;
		int dz = this.z-other.z;
		return Math.sqrt(dx*dx+dy*dy+dz*dz);
	}

	@Override
	public String toString(){
		return world+": "+x+"|"+y+"|"+z+" in "+dim;
	}

	public World getWorld(){
		return Signpost.proxy.getWorld(this.world, this.dim);
	}

	public TileEntity getTile(){
		World world = getWorld();
		if(world!=null){
			TileEntity tile = world.getTileEntity(x, y, z);
			if(tile instanceof PostPostTile){
				((PostPostTile) tile).getBases();
			}else if(tile instanceof PostPostTile){
				((BigPostPostTile) tile).getBases();
			}
			return tile;
		}else{
			return null;
		}
	}
}