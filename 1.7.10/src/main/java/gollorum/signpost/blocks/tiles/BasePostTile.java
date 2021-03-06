package gollorum.signpost.blocks.tiles;

import gollorum.signpost.SPEventHandler;
import gollorum.signpost.blocks.WaystoneContainer;
import gollorum.signpost.event.UpdateWaystoneEvent;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.BaseUpdateClientMessage;
import gollorum.signpost.network.messages.BaseUpdateServerMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.BoolRun;
import gollorum.signpost.util.MyBlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class BasePostTile extends TileEntity implements WaystoneContainer {

	public boolean isCanceled = false;

	public BasePostTile() {super();}
	
	public BasePostTile setup(){
		return this;
	}
	
	public BaseInfo getBaseInfo(){
		return PostHandler.allWaystones.getByPos(toPos());
	}

	public MyBlockPos toPos(){
		if(worldObj==null||worldObj.isRemote){
			return new MyBlockPos("", xCoord, yCoord, zCoord, dim());
		}else{
			return new MyBlockPos(worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord, dim());
		}
	}

	public int dim(){
		if(worldObj==null||worldObj.provider==null){
			return Integer.MIN_VALUE;
		}else
			return worldObj.provider.dimensionId;
	}
	
	public void onBlockDestroy(MyBlockPos pos) {
		isCanceled = true;
		BaseInfo base = getBaseInfo();
		if(PostHandler.allWaystones.remove(base)){
			MinecraftForge.EVENT_BUS.post(new UpdateWaystoneEvent(UpdateWaystoneEvent.WaystoneEventType.DESTROYED, worldObj, xCoord, yCoord, zCoord, base==null?"":base.name));
			NetworkHandler.netWrap.sendToAll(new BaseUpdateClientMessage());
		}
	}

	@Override
	public void setName(String name) {
		BaseInfo bi = getBaseInfo();
		bi.name = name;
		NetworkHandler.netWrap.sendToServer(new BaseUpdateServerMessage(bi, false));
	}

	@Override
	public String getName() {
		BaseInfo ws = getBaseInfo();
		return ws == null ? "null" : getBaseInfo().toString();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	 public int getBlockMetadata(){
		try{
			return super.getBlockMetadata();
		}catch(NullPointerException e){return 0;}
	}
}
