package gollorum.signpost.blocks;

import gollorum.signpost.SPEventHandler;
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

public class BasePostTile extends TileEntity {

	public boolean isCanceled = false;

	public BasePostTile() {}
	
	public BasePostTile setup(){
		SPEventHandler.scheduleTask(new BoolRun() {
			@Override
			public boolean run() {
				if(isCanceled){
					return true;
				}
				if(world==null){
					return false;
				}
				init();
				return true;
			}
		});
		return this;
	}
	
	public BaseInfo getBaseInfo(){
		return PostHandler.allWaystones.getByPos(toPos());
	}

	public void init(){
		boolean found = false;
		if(getBaseInfo()!=null){
			return;
		}
		PostHandler.allWaystones.add(new BaseInfo(null, toPos(), null));
	}

	public MyBlockPos toPos(){
		if(world==null||world.isRemote){
			return new MyBlockPos("", pos.getX(), pos.getY(), pos.getZ(), dim());
		}else{
			return new MyBlockPos(world.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ(), dim());
		}
	}

	public int dim(){
		if(world==null||world.provider==null){
			return Integer.MIN_VALUE;
		}else
			return world.provider.getDimension();
	}
	
	public void onBlockDestroy(MyBlockPos pos) {
		isCanceled = true;
		BaseInfo base = PostHandler.allWaystones.getByPos(pos);
		if(PostHandler.allWaystones.removeByPos(pos)){
			MinecraftForge.EVENT_BUS.post(new UpdateWaystoneEvent(UpdateWaystoneEvent.WaystoneEventType.DESTROYED, world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), base==null?"":base.name));
			NetworkHandler.netWrap.sendToAll(new BaseUpdateClientMessage());
		}
	}

	public void setName(String name) {
		BaseInfo ws = getBaseInfo();
		ws.name = name;
		NetworkHandler.netWrap.sendToServer(new BaseUpdateServerMessage(ws, false));
	}

	public String getName() {
		BaseInfo ws = getBaseInfo();
		return ws == null ? "null" : getBaseInfo().toString();
	}

	@Override
	public String toString() {
		return getName();
	}

}
