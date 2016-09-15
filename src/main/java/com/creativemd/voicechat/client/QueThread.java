package com.creativemd.voicechat.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.voicechat.core.VoiceChat;
import com.creativemd.voicechat.packets.AudioPacket;

import net.minecraft.client.Minecraft;

public class QueThread extends Thread{
	
	public static CopyOnWriteArrayList<byte[]> bits = new CopyOnWriteArrayList<>();
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static QueThread thread = new QueThread();
	
	public static final long offset = 10000;
	
	public QueThread() {
		start();
	}
	
	@Override
	public void run()
	{
		while(!isInterrupted())
		{
			if(bits.size() > 0)
			{
				byte[] data = bits.get(0);
				bits.remove(0);
				short[] shortarray = new short[data.length/2];
		    	ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortarray);
		    	
		    	double millisInPacket = data.length / VoiceChat.format.getFrameSize() * 1000.0 / VoiceChat.format.getSampleRate();
				if(mc.isSingleplayer())
				{
					play(AudioPacket.consumer.position()+offset, shortarray);
					System.out.println("Playing bit length=" + millisInPacket + " at " + AudioPacket.consumer.position() + " offset=" + offset);
				}
			}
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void play(long time, short[] play)
	{
		AudioPacket.consumer.mix(time, play);
	}
	
}
