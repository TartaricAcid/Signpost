package gollorum.signpost.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatAllowedCharacters;

public class SignInputBox extends Gui{
	
	private static final double pWidth = 25.0;
	private static final double pHeight = 6.0;
	private static final double verh = pWidth/pHeight;

	public int x;
	public int y;
	public int width;
	public int height;
	private boolean isFocused = false;
    private int cursorPosition;
    
    public int color = 0;
	
	private FontRenderer fontRend;
	
	public String text = "";
	
	public SignInputBox(FontRenderer p_i1032_1_, int x, int y, int width) {
		this.fontRend = p_i1032_1_;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = (int) (width/verh);
//		super(p_i1032_1_, x, y, width, (int) (width/verh));
	}
	
	public void setFocused(boolean bool){
		isFocused = bool;
	}
	
	public boolean isFocused(){
		return isFocused;
	}
	
	public void drawSignBox(){
		drawTexturedModalRect(this.x, this.y, 0, 0, width, height);
		drawText();
	}
	
	@Override  
	public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height){
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x), 		(double)(y+height), (double)this.zLevel, 0.0, 1.0);
        tessellator.addVertexWithUV((double)(x+width), 	(double)(y+height), (double)this.zLevel, 1.0, 1.0);
        tessellator.addVertexWithUV((double)(x+width), 	(double)(y), 		(double)this.zLevel, 1.0, 0.0);
        tessellator.addVertexWithUV((double)(x),		(double)(y), 		(double)this.zLevel, 0.0, 0.0);
        tessellator.draw();
    }
	
	public void drawText(){
		double scale = this.width/pWidth;
		double x = this.x+(this.width)/2.0-scale;
		double y = this.y+(this.height)/2.0+scale/4;
		double sc2 = 100d/fontRend.getStringWidth(text);
		if(sc2>=1){
			sc2 = 1;
		}
		GL11.glPushMatrix();
		GL11.glScaled(sc2, sc2, 1);
//		GL11.glTranslated(x+width/2.0, 0, y+height/2.0);
//		GL11.glScaled(1, 1, 1);
//		GL11.glTranslated(-fontRend.getStringWidth(getText())/2.0, 0, -fontRend.FONT_HEIGHT/2.0);
//		fontRend.drawString(getText(), 0, 0, color);
		if(sc2==1.0){
			x = (x-fontRend.getStringWidth(getText())/2.0);
		}else{
			x = (x-50)/sc2;
		}
//		sc2*=2.0;
		fontRend.drawString(getText(), (int) x, (int) ((y-fontRend.FONT_HEIGHT/2.0*sc2)/sc2), color);
		GL11.glPopMatrix();
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public String getText(){
		return text;
	}

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_){
        if (!this.isFocused){
            return false;
        }else{
        	switch (p_146201_1_){
                case 1:
                    cursorPosition = text.length();
                    return true;
                case 22:
                    this.writeText(GuiScreen.getClipboardString());
                    return true;
                default:
                    switch (p_146201_2_){
                    case 14:
                        if (GuiScreen.isCtrlKeyDown()){
                            this.deleteWords(-1);
                        }else{
                            this.deleteFromCursor(-1);
                        }
                        return true;
                        case 199:
                            cursorPosition = 0;
                            return true;
                        case 203:
                            if (GuiScreen.isCtrlKeyDown()){
                                this.setCursorPosition(this.getNthWordFromCursor(-1));
                            }else{
                                this.moveCursorBy(-1);
                            }
                            return true;
                        case 205:
                            if (GuiScreen.isCtrlKeyDown()){
                                this.setCursorPosition(this.getNthWordFromCursor(1));
                            }else{
                                this.moveCursorBy(1);
                            }
                            return true;
                        case 207:
                            cursorPosition = text.length();
                            return true;
                        case 211:
                            if (GuiScreen.isCtrlKeyDown()){
                                this.deleteWords(1);
                            }
                            else{
                            	this.deleteFromCursor(1);
                            }
                            return true;
                        default:
                            if (ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)){
                            	this.writeText(Character.toString(p_146201_1_));
                                return true;
                            }else{
                                return false;
                            }
                    }
            }
        }
    }

    public void writeText(String p_146191_1_){
//        String s1 = "";
        String s2 = ChatAllowedCharacters.filerAllowedCharacters(p_146191_1_);
//        int i = this.cursorPosition;
//        int j = this.cursorPosition;
//        boolean flag = false;

//        if (this.text.length() > 0){
//            s1 = this.text.substring(0, i);
//        }
//
//        int l;
//
//        s1 = s1 + s2;
//        l = s2.length();
//
//        if (this.text.length() > 0 && j < this.text.length()){
//            s1 = s1 + this.text.substring(j);
//        }

        this.text = text.substring(0, cursorPosition)+s2+text.substring(cursorPosition);
        this.moveCursorBy(s2.length());
    }

    public void moveCursorBy(int p_146182_1_){
        this.setCursorPosition(cursorPosition + p_146182_1_);
    }

    public void setCursorPosition(int p_146190_1_){
        this.cursorPosition = p_146190_1_;
        int j = this.text.length();

        if (this.cursorPosition < 0){
            this.cursorPosition = 0;
        }else if (this.cursorPosition > j){
            this.cursorPosition = j;
        }
    }

    public void deleteFromCursor(int p_146175_1_){
        if (this.text.length() != 0){
			boolean flag = p_146175_1_ < 0;
			int j = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
			int k = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
			String s = "";
			
			if (j >= 0){
			    s = this.text.substring(0, j);
			}
			
			if (k < this.text.length()){
			    s = s + this.text.substring(k);
			}
			
			this.text = s;
			
			if (flag){
			    this.moveCursorBy(p_146175_1_);
			}
        }
    }

    public void deleteWords(int p_146177_1_){
        if (this.text.length() != 0){
            this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
        }
    }

    public int getNthWordFromCursor(int p_146187_1_){
        return this.getNthWordFromPos(p_146187_1_, this.cursorPosition);
    }

    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_){
        return this.func_146197_a(p_146183_1_, this.cursorPosition, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_){
        int k = p_146197_2_;
        boolean flag1 = p_146197_1_ < 0;
        int l = Math.abs(p_146197_1_);

        for (int i1 = 0; i1 < l; ++i1)
        {
            if (flag1)
            {
                while (p_146197_3_ && k > 0 && this.text.charAt(k - 1) == 32)
                {
                    --k;
                }

                while (k > 0 && this.text.charAt(k - 1) != 32)
                {
                    --k;
                }
            }
            else
            {
                int j1 = this.text.length();
                k = this.text.indexOf(32, k);

                if (k == -1)
                {
                    k = j1;
                }
                else
                {
                    while (p_146197_3_ && k < j1 && this.text.charAt(k) == 32)
                    {
                        ++k;
                    }
                }
            }
        }

        return k;
    }

    public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_){
        isFocused = (p_146192_1_ >= this.x && p_146192_1_ < this.x + this.width && p_146192_2_ >= this.y && p_146192_2_ < this.y + this.height);

        if (this.isFocused && p_146192_3_ == 0){
            int l = p_146192_1_ - this.x;
            this.setCursorPosition(this.fontRend.trimStringToWidth(text, l).length());
        }
    }

}