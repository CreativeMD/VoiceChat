package com.creativemd.voicechat.core;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.client.GuiVoiceWalkie;
import com.creativemd.voicechat.packets.ConfigPacket;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.relauncher.Side;

public class EventHandlerBoth {
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if(event.player instanceof EntityPlayerMP && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !MinecraftServer.getServer().isSinglePlayer())
		{
			PacketHandler.sendPacketToPlayer(new ConfigPacket(VoiceChat.distance, VoiceChat.sampleRate, VoiceChat.delay), (EntityPlayerMP) event.player);
		}
	}
	
	@SubscribeEvent
    public void PlayerInteractEvent(PlayerInteractEvent event)
    {
		if(event.action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) &&
				event.entityPlayer.getCurrentEquippedItem() != null &&
				event.entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemTalkie)
		{
			FMLCommonHandler.instance().showGuiScreen(new GuiVoiceWalkie());
		}
    }
	
}
