package com.creativemd.voicechat.core;

import java.io.File;
import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
@MCVersion(value = "1.7.10")
public class VoiceChatLoader implements IFMLLoadingPlugin {
	
	public static File location;

	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{VoiceChatTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return VoiceChat.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
