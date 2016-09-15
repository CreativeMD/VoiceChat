package com.creativemd.voicechat.core;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.packets.ConfigPacket;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;

public class EventHandlerBoth {
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if(event.player instanceof EntityPlayerMP && !Loader.isModLoaded("ingameconfigmanager") && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer())
		{
			PacketHandler.sendPacketToPlayer(new ConfigPacket(VoiceChat.distance, VoiceChat.sampleRate, VoiceChat.delay), (EntityPlayerMP) event.player);
		}
	}
	
}
