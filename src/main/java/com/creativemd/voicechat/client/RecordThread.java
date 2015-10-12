package com.creativemd.voicechat.client;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import net.minecraft.client.Minecraft;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.core.VoiceChat;
import com.creativemd.voicechat.packets.AudioPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RecordThread extends Thread{
	
	public Minecraft mc;
	@SideOnly(Side.CLIENT)
	public static TargetDataLine line;
	
	public RecordThread()
	{
		mc = Minecraft.getMinecraft();
		System.out.println("Create recording Thread");
	}
	
	@Override
	public void run()
	{
		long last = 0;
		while(mc.theWorld != null && !mc.isSingleplayer())
		{
			if(!line.isActive())
				line.start();
			if(last == 0)
				last = System.currentTimeMillis();
			byte[] data = new byte[line.getBufferSize()];
			int numBytesRead =  line.read(data, 0, data.length);
			long now = System.currentTimeMillis();
			int numFramesRead = numBytesRead/line.getFormat().getFrameSize();
			double numSecondsRead = numFramesRead/line.getFormat().getFrameRate();
			System.out.println("recorded " + numFramesRead + " frames = " + numSecondsRead + " seconds at " + (now-last));
			PacketHandler.sendPacketToServer(new AudioPacket(data, numBytesRead, mc.thePlayer.getCommandSenderName(), (now-last)));
			last = now;
			//System.out.println("Sending sound data");
			/*try {
				sleep(VoiceChat.delay);
			} catch (InterruptedException e) {
			}*/
		}
	}

}
