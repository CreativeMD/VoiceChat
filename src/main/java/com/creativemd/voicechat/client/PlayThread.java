package com.creativemd.voicechat.client;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;


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
