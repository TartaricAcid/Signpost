package gollorum.signpost.network.messages;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import gollorum.signpost.blocks.tiles.BigPostPostTile;
import gollorum.signpost.blocks.tiles.SuperPostPostTile;
import gollorum.signpost.util.BigBaseInfo;
import gollorum.signpost.util.MyBlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;

public class SendBigPostBasesMessage implements IMessage {

	public MyBlockPos pos;
	public String base;
	public int baserot;
	public boolean flip;
	public String overlay;
	public boolean point;
	public String[] description;
	public ResourceLocation paint;
	public ResourceLocation postPaint;

	public SendBigPostBasesMessage(){}
	
	public SendBigPostBasesMessage(BigPostPostTile tile, BigBaseInfo base) {
		tile.markDirty();
		this.pos = tile.toPos();
		this.base = ""+base.sign.base;
		baserot = base.sign.rotation;
		flip = base.sign.flip;
		overlay = ""+base.sign.overlay;
		point = base.sign.point;
		description = base.description;
		paint = base.sign.paint;
		postPaint = base.postPaint;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		pos.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, base);
		buf.writeInt(baserot);
		buf.writeBoolean(flip);
		ByteBufUtils.writeUTF8String(buf, overlay);
		buf.writeBoolean(point);
		buf.writeInt(description.length);
		for(String now: description){
			ByteBufUtils.writeUTF8String(buf, now);
		}
		ByteBufUtils.writeUTF8String(buf, SuperPostPostTile.locToString(paint));
		ByteBufUtils.writeUTF8String(buf, SuperPostPostTile.locToString(postPaint));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = MyBlockPos.fromBytes(buf);
		base = ByteBufUtils.readUTF8String(buf);
		baserot = buf.readInt();
		flip = buf.readBoolean();
		overlay = ByteBufUtils.readUTF8String(buf);
		point = buf.readBoolean();
		description = new String[buf.readInt()];
		for(int i=0; i<description.length; i++){
			description[i] = ByteBufUtils.readUTF8String(buf);
		}
		paint = SuperPostPostTile.stringToLoc(ByteBufUtils.readUTF8String(buf)); //Kann mich bitte jemand verprügeln? Solch Dummheit muss bestraft werden. AARGH
		postPaint = SuperPostPostTile.stringToLoc(ByteBufUtils.readUTF8String(buf));
	}

}
