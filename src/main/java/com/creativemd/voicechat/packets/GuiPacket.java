package com.creativemd.voicechat.packets;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.voicechat.client.GuiVoiceConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GuiPacket extends CreativeCorePacket{

	@Override
	public void readBytes(ByteBuf bytes) {
	}

	@Override
	public void writeBytes(ByteBuf bytes) {
	}

	@Override
	public void executeClient(EntityPlayer player) {
		FMLCommonHandler.instance().showGuiScreen(new GuiVoiceConfig());
	}

	@Override
	public void executeServer(EntityPlayer player) {
	}

}
