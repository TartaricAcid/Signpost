package gollorum.signpost.commands;

import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SetWaystoneCount extends CommandBase {

	@Override
	public String getCommandName() {
		return "setwaystonesleft";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/setwaystonesleft <amount> [<player>]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		if(sender instanceof EntityPlayerMP){
			EntityPlayerMP player = (EntityPlayerMP) sender;
			if(args.length==1){
				try{
					PostHandler.playerKnownWaystones.get(player.getUniqueID()).b.a = Integer.parseInt(args[0]);
				}catch(Exception e){
					sender.addChatMessage(new TextComponentString("Error: '"+args[0]+"' is not a numer"));
				}
			}else if(args.length==2){
				EntityPlayerMP p = (EntityPlayerMP) PostHandler.getPlayerByName(args[1]);
				if(p==null){
					NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.playerNotFound", "<player>", args[1]), player);
					return;
				}else{
					try{
						PostHandler.playerKnownWaystones.get(p.getUniqueID()).b.a = Integer.parseInt(args[0]);
					}catch(Exception e){
						sender.addChatMessage(new TextComponentString("Error: '"+args[0]+"' is not a numer"));
					}
				}
			}else{
				sender.addChatMessage(new TextComponentString("Error: wrong argument count, use "+getCommandUsage(null)));
			}
		}
	}

	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return sender instanceof EntityPlayerMP && ConfigHandler.isOp((EntityPlayerMP)sender);
    }

}