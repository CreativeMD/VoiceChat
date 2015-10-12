package com.creativemd.voicechat.client;

public class AudioData {
	
	public byte[] data = null;
	public int length = 0;
	
	public AudioData(byte[] data, int length)
	{
		this.data = data;
		this.length = length;
	}
}
