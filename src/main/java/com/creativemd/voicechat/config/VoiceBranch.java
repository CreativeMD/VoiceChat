package com.creativemd.voicechat.config;

import com.creativemd.creativecore.client.avatar.Avatar;
import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.ingameconfigmanager.api.common.branch.ConfigBranch;
import com.creativemd.ingameconfigmanager.api.common.branch.ConfigSegmentCollection;
import com.creativemd.ingameconfigmanager.api.common.segment.IntegerSegment;
import com.creativemd.ingameconfigmanager.api.common.segment.SelectSegment;
import com.creativemd.voicechat.core.VoiceChat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VoiceBranch extends ConfigBranch{

	public VoiceBranch() {
		super("VoiceChat");
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected Avatar getAvatar() {
		return new AvatarItemStack(new ItemStack(VoiceChat.talkie));
	}

	@Override
	public void loadCore() {
		
	}

	@Override
	public void createConfigSegments() {
		segments.add(new SelectSegment("sampleRate", "Sample Rate", "16000", "8000", "11025", "16000"));
		segments.add(new IntegerSegment("distance", "Distance", 50));
	}

	@Override
	public boolean needPacket() {
		return true;
	}

	@Override
	public void onRecieveFrom(boolean isServer, ConfigSegmentCollection collection) {
		VoiceChat.sampleRate = Float.parseFloat((String) collection.getSegmentValue("sampleRate"));
		VoiceChat.distance = (Integer) collection.getSegmentValue("distance");
		if(!isServer)
			VoiceChat.refreshAudioFormat();
	}

}
