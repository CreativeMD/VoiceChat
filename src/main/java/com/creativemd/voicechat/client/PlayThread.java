package com.creativemd.voicechat.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class PlayThread extends Thread{
	
	public AudioConsumer consumer;
	
	public PlayThread(AudioConsumer consumer)
	{
		this.consumer = consumer;
	}
	
	@Override
	public void run()
	{
		consumer.run();
	}
}
