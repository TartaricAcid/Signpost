package gollorum.signpost.util;

import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.util.math.tracking.DDDVector;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class Sign {

	public BaseInfo base;
	public int rotation;
	public boolean flip;
	public OverlayType overlay;
	public boolean point;
	public ResourceLocation paint;
	
	public Sign(ResourceLocation texture){
		base = null;
		rotation = 0;
		flip = false;
		overlay = null;
		point = false;
		paint = texture;
	}

	public Sign(BaseInfo base, int rotation, boolean flip, OverlayType overlay, boolean point, ResourceLocation paint) {
		this.base = base;
		this.rotation = rotation;
		this.flip = flip;
		this.overlay = overlay;
		this.point = point;
		this.paint = paint;
	}

	public static enum OverlayType{
		GRAS(	"grass",	Items.wheat_seeds),
		VINE(	"vine",		Item.getItemFromBlock(Blocks.vine)),
		SNOW(	"snow",		Items.snowball);
		public String texture;
		public Item item;
		OverlayType(String texture, Item item){
			this.texture = texture;
			this.item = item;
		}
		public static OverlayType get(String arg){
			try{
				return valueOf(arg);
			}catch(IllegalArgumentException e){
				return null;
			}
		}
	}

	public final double calcRot(int x, int z) {
 		if(point&&!(base==null||base.blockPos==null||ConfigHandler.deactivateTeleportation)){
			int dx = x-base.blockPos.x;
			int dz = z-base.blockPos.z;
			return DDDVector.genAngle(dx, dz)+Math.toRadians(-90+(flip?0:180)+(dx<0&&dz>0?180:0));
		}else{
			return Math.toRadians(rotation);
		}
	}
	
	public boolean isValid(){
		return base!=null && base.hasName();
	}

	public void rot(int i, int x, int z) {
		if(point){
			rotation = (int) Math.round(Math.toDegrees(calcRot(x, z))/15)*15;
			point = false;
		}
		rotation = (rotation+i)%360;
	}

	@Override
	public String toString(){
		return ""+base;
	}

}