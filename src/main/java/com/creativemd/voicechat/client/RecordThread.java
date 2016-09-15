package com.creativemd.voicechat.client;

import javax.sound.sampled.TargetDataLine;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.packets.AudioPacket;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RecordThread extends Thread{
	
	public boolean active;
	public static Minecraft mc = Minecraft.getMinecraft();
	public TargetDataLine line;
	
	public RecordThread()
	{
		System.out.println("Create recording Thread");
		active = true;
		start();
	}
	
	@Override
	public void run()
	{
		long last = 0;
		while(active)// && !mc.isSingleplayer())
		{
			if(mc.theWorld != null)
			{
				try{
					if(!line.isActive())
						line.start();
					//if(last == 0)
						//last = System.currentTimeMillis();
					//long last = System.currentTimeMillis();
					byte[] data = new byte[line.getBufferSize()];
					int numBytesRead =  line.read(data, 0, data.length);
					//long now = System.currentTimeMillis();
					//int numFramesRead = numBytesRead/line.getFormat().getFrameSize();
					//double numSecondsRead = numFramesRead/line.getFormat().getFrameRate();
					//System.out.println("recorded " + numFramesRead + " frames = " + numSecondsRead + " seconds at " + (now-last));
					if(mc.thePlayer != null && mc.theWorld != null) //&& !mc.isSingleplayer())
					{
						try{
							//PacketHandler.sendPacketToServer(new AudioPacket(data/*, numBytesRead*/, mc.thePlayer.getName()/*, (now-last)*/));
							mc.addScheduledTask(new Runnable() {
								
								@Override
								public void run() {
									new AudioPacket(data/*, numBytesRead*/, mc.thePlayer.getName()).executeClient(mc.thePlayer);
									
								}
							});
							/*long timeBetween = System.currentTimeMillis();
							
							System.out.println("time between:" + (System.currentTimeMillis()-timeBetween));*/
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					//QueThread.bits.add(data);
					//last = now;
					//System.out.println("Sending sound data");
					/*try {
						sleep(VoiceChat.delay);
					} catch (InterruptedException e) {
					}*/
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Closing recording Thread");
	}

}
