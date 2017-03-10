package gollorum.signpost.render;

import org.lwjgl.opengl.GL11;

import gollorum.signpost.Signpost;
import gollorum.signpost.blocks.PostPostTile;
import gollorum.signpost.util.DoubleBaseInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class PostRenderer extends TileEntitySpecialRenderer<PostPostTile>{

	private ModelPost model;
	
	public PostRenderer(){
		model = new ModelPost();
	}
	
	@Override
	public void renderTileEntityAt(PostPostTile tile, double x, double y, double z, float partialTicks, int destroyStage) {
		DoubleBaseInfo tilebases = tile.getBases();
		GL11.glPushMatrix();
		GL11.glTranslated(x+0.5, y, z+0.5);
		bindTexture(new ResourceLocation(Signpost.MODID + ":textures/blocks/"+tile.type.texture+".png"));
		model.render((Entity)null, 0, 0.1f, 0, 0, 0, 0.0625f, tilebases, tile.isItem);
        
        FontRenderer fontrenderer = this.getFontRenderer();
		GL11.glTranslated(0, 0.8d, 0);
		GL11.glRotated(180, 0, 0, 1);
		GL11.glRotated(180, 0, 1, 0);
		double sc = 0.013d;
		double ys = 1.3d*sc;
        
		int color =(1<<16) + (1<<8);
		
        if(!tile.isItem){
        	if(tilebases.base1!=null&&!tilebases.base1.name.equals("null")&&!tilebases.base1.name.equals("")){
        		double lurch = tilebases.flip1?0.45-fontrenderer.getStringWidth(tilebases.base1.name)*sc:-0.45;
            	double alpha = Math.atan(lurch*16/3.1);
            	double d = Math.sqrt(Math.pow(3.1/16, 2)+Math.pow(lurch, 2));
            	double beta = alpha + Math.toRadians(tilebases.rotation1);
            	double dx = Math.sin(beta)*d;
            	double dz = -Math.cos(beta)*d;
        		GL11.glTranslated(dx, 0, dz);
        		GL11.glScaled(sc, ys, sc);
        		GL11.glRotated(-tilebases.rotation1, 0, 1, 0);
        		String s = tilebases.base1.name;
        		double sc2 = 100d/fontrenderer.getStringWidth(s);
        		if(sc2>=1){
        			sc2 = 1;
        		}
        		GL11.glScaled(sc2,  sc2,  sc2);
                fontrenderer.drawString(s, 0, 0, color);
        		GL11.glScaled(1/sc2,  1/sc2,  1/sc2);
        		GL11.glRotated(tilebases.rotation1, 0, 1, 0);
        		GL11.glScaled(1/sc, 1/ys, 1/sc);
        		GL11.glTranslated(-dx, 0, -dz);
        	}

        	if(tilebases.base2!=null&&!tilebases.base2.name.equals("null")&&!tilebases.base2.name.equals("")){
        		GL11.glTranslated(0, 0.5d, 0);
        		double lurch = tilebases.flip2?0.45-fontrenderer.getStringWidth(tilebases.base2.name)*sc:-0.45;
            	double alpha = Math.atan(lurch*16/3.1);
            	double d = Math.sqrt(Math.pow(3.1/16, 2)+Math.pow(lurch, 2));
            	double beta = alpha + Math.toRadians(tilebases.rotation2);
            	double dx = Math.sin(beta)*d;
            	double dz = -Math.cos(beta)*d;
        		GL11.glTranslated(dx, 0, dz);
        		GL11.glScaled(sc, ys, sc);
        		GL11.glRotated(-tilebases.rotation2, 0, 1, 0);
        		String s = tilebases.base2.name;
        		double sc2 = 100d/fontrenderer.getStringWidth(s);
        		if(sc2>=1){
        			sc2 = 1;
        		}
        		GL11.glScaled(sc2,  sc2,  sc2);
                fontrenderer.drawString(s, 0, 0, color);
        		GL11.glScaled(1/sc2,  1/sc2,  1/sc2);
        		GL11.glRotated(tilebases.rotation1, 0, 1, 0);
        		GL11.glScaled(1/sc, 1/ys, 1/sc);
        		GL11.glTranslated(-dx, 0, -dz);
        	}
        }
        GL11.glPopMatrix();

	}

}