package com.creativemd.voicechat.packets;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.voicechat.core.VoiceChat;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public class ConfigPacket extends CreativeCorePacket{
	
	public int range = VoiceChat.distance;
	public float sampleRate = VoiceChat.sampleRate;
	public int delay = VoiceChat.delay; //10ms
	
	public ConfigPacket()
	{
		
	}
	
	public ConfigPacket(int range, float sampleRate, int delay)
	{
		this.range = range;
		this.sampleRate = sampleRate;
		this.delay = delay;
	}
	
	@Override
	public void readBytes(ByteBuf bytes) {
		range = bytes.readInt();
		sampleRate = bytes.readFloat();
		delay = bytes.readInt();
	}

	@Override
	public void writeBytes(ByteBuf bytes) {
		bytes.writeInt(range);
		bytes.writeFloat(sampleRate);
		bytes.writeInt(delay);
	}

	@Override
	public void executeClient(EntityPlayer player) {
		VoiceChat.distance = range;
		VoiceChat.sampleRate = sampleRate;
		VoiceChat.delay = delay;
		player.addChatComponentMessage(new TextComponentTranslation("Recieved Voice Chat config"));
		
		VoiceChat.refreshAudioFormat();
	}

	@Override
	public void executeServer(EntityPlayer player) {
		VoiceChat.distance = range;
		VoiceChat.sampleRate = sampleRate;
		VoiceChat.delay = delay;
	}

}
