package gollorum.signpost.blocks;

import gollorum.signpost.SPEventHandler;
import gollorum.signpost.blocks.PostPost.Hit;
import gollorum.signpost.blocks.PostPost.HitTarget;
import gollorum.signpost.blocks.PostPost.PostType;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.SendAllPostBasesMessage;
import gollorum.signpost.network.messages.SendPostBasesMessage;
import gollorum.signpost.util.BoolRun;
import gollorum.signpost.util.DoubleBaseInfo;
import gollorum.signpost.util.MyBlockPos;
import gollorum.signpost.util.Sign;
import gollorum.signpost.util.Sign.OverlayType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PostPostTile extends SuperPostPostTile {

	public PostType type = PostType.OAK;

	@Deprecated
	public DoubleBaseInfo bases = null;
	
	public PostPostTile(){}

	public PostPostTile(PostType type){
		this();
		this.type = type;
	}

	public DoubleBaseInfo getBases(){
		DoubleBaseInfo bases = PostHandler.posts.get(toPos());
		if(bases==null){
			bases = new DoubleBaseInfo(type.texture);
			PostHandler.posts.put(toPos(), bases);
		}
		this.bases = bases;
		return bases;
	}
	
	@Override
	public void onBlockDestroy(MyBlockPos pos) {
		isCanceled = true;
		DoubleBaseInfo bases = getBases();
		if(bases.sign1.overlay!=null){
			EntityItem item = new EntityItem(world, pos.x, pos.y, pos.z, new ItemStack(bases.sign1.overlay.item, 1));
			world.spawnEntity(item);
		}
		if(bases.sign2.overlay!=null){
			EntityItem item = new EntityItem(world, pos.x, pos.y, pos.z, new ItemStack(bases.sign2.overlay.item, 1));
			world.spawnEntity(item);
		}
		if(PostHandler.posts.remove(pos)!=null){
			NetworkHandler.netWrap.sendToAll(new SendAllPostBasesMessage());
		}
	}

	@Override
	public void save(NBTTagCompound tagCompound) {
		DoubleBaseInfo bases = getBases();
		tagCompound.setString("base1", ""+bases.sign1.base);
		tagCompound.setString("base2", ""+bases.sign2.base);
		tagCompound.setInteger("rot1", bases.sign1.rotation);
		tagCompound.setInteger("rot2", bases.sign2.rotation);
		tagCompound.setBoolean("flip1", bases.sign1.flip);
		tagCompound.setBoolean("flip2", bases.sign2.flip);
		tagCompound.setString("overlay1", ""+bases.sign1.overlay);
		tagCompound.setString("overlay2", ""+bases.sign2.overlay);
		tagCompound.setBoolean("point1", bases.sign1.point);
		tagCompound.setBoolean("point2", bases.sign2.point);
		tagCompound.setString("paint1", SuperPostPostTile.LocToString(bases.sign1.paint));
		tagCompound.setString("paint2", SuperPostPostTile.LocToString(bases.sign2.paint));
	}

	@Override
	public void load(NBTTagCompound tagCompound) {
		final String base1 = tagCompound.getString("base1");
		final String base2 = tagCompound.getString("base2");

		final int rotation1 = tagCompound.getInteger("rot1");
		final int rotation2 = tagCompound.getInteger("rot2");

		final boolean flip1 = tagCompound.getBoolean("flip1");
		final boolean flip2 = tagCompound.getBoolean("flip2");

		final OverlayType overlay1 = OverlayType.get(tagCompound.getString("overlay1"));
		final OverlayType overlay2 = OverlayType.get(tagCompound.getString("overlay2"));

		final boolean point1 = tagCompound.getBoolean("point1");
		final boolean point2 = tagCompound.getBoolean("point2");

		final String paint1 = tagCompound.getString("paint1");
		final String paint2 = tagCompound.getString("paint2");
		
		final PostPostTile self = this;

		SPEventHandler.scheduleTask(new BoolRun(){
			@Override
			public boolean run() {
				if(world==null){
					return false;
				}else{
					if(world.isRemote){
						return true;
					}
					DoubleBaseInfo bases = getBases();
					bases.sign1.base = PostHandler.getForceWSbyName(base1);
					bases.sign2.base = PostHandler.getForceWSbyName(base2);
					bases.sign1.rotation = rotation1;
					bases.sign2.rotation = rotation2;
					bases.sign1.flip = flip1;
					bases.sign2.flip = flip2;
					bases.sign1.overlay = overlay1;
					bases.sign2.overlay = overlay2;
					bases.sign1.point = point1;
					bases.sign2.point = point2;
					bases.sign1.paint = stringToLoc(paint1);
					bases.sign2.paint = stringToLoc(paint2);
					NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(self, bases));
					return true;
				}
			}
		});
	}
	
	@Override
	public Sign getSign(EntityPlayer player) {
		DoubleBaseInfo bases = getBases();
		Hit hit = (Hit) ((PostPost)blockType).getHitTarget(world, pos.getX(), pos.getY(), pos.getZ(), player);
		if(hit.target.equals(HitTarget.BASE1)){
			return bases.sign1;
		}else if(hit.target.equals(HitTarget.BASE2)){
			return bases.sign2;
		}else{
			return null;
		}
	}
}
