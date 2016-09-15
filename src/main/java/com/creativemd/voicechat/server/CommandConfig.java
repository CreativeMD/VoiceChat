package com.creativemd.voicechat.server;


import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public class CommandConfig implements ICommand{
	

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "VoiceChat";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		
		return null;
	}

	@Override
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if(icommandsender.getCommandSenderName() != "Rcon")
		{
			
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
		return true;
		//TODO Update return MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(icommandsender.getCommandSenderName());
	}

	@Override
	public List addTabCompletionOptions(ICommandSender icommandsender,
			String[] astring) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}

}
