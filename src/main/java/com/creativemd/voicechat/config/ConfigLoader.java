package com.creativemd.voicechat.config;

import com.creativemd.ingameconfigmanager.api.core.TabRegistry;
import com.creativemd.ingameconfigmanager.api.tab.ModTab;
import com.creativemd.voicechat.core.VoiceChat;

import net.minecraft.item.ItemStack;

public class ConfigLoader {
	
	public static ModTab tab;
	
	public static void startConfig()
	{
		tab = new ModTab("VoiceChat", new ItemStack(VoiceChat.talkie));
		tab.addBranch(new VoiceBranch());
		TabRegistry.registerModTab(tab);
	}
	
}
