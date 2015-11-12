package com.creativemd.voicechat.packets;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import paulscode.sound.SoundSystem;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.client.AudioConsumer;
import com.creativemd.voicechat.core.ItemTalkie;
import com.creativemd.voicechat.core.VoiceChat;
import com.creativemd.voicechat.core.VoiceChatTransformer;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AudioPacket extends CreativeCorePacket{
	
	public byte[] data = null;
	public int length = 0;
	public String player;
	public long timestamp = 0;
	
	public AudioPacket()
	{
		
	}
	
	/*public AudioPacket(byte[] data, int length, String player)
	{
		
	}*/
	
	public AudioPacket(byte[] data, int length, String player, long timestamp)
	{
		this.data = data;
		this.length = length;
		this.player = player;
		this.timestamp = timestamp;
	}
	
	public void readBytes(ByteBuf bytes)
	{
		timestamp = bytes.readLong();
		data = new byte[bytes.readInt()];
		bytes.readBytes(data);
		length = bytes.readInt();// wrong order
		player = ByteBufUtils.readUTF8String(bytes);
		
	}
	
    public void writeBytes(ByteBuf bytes)
	{
    	bytes.writeLong(timestamp);
    	bytes.writeInt(data.length);
    	bytes.writeBytes(data);
		bytes.writeInt(length);
		ByteBufUtils.writeUTF8String(bytes, player);
	}
    
    public static boolean loaded = false;
    
    @SideOnly(Side.CLIENT)
    public static AudioConsumer consumer;
    
    public static HashMap<String, Long> lastplayed = new HashMap<String, Long>(); //This is the list containg a player as key and the timestamp as value
    
    public void executeClient(EntityPlayer player)
	{
    	if(consumer == null)
    	{
    		
    		consumer = new AudioConsumer();
    		consumer.start();
    	}
    	/*data = new byte[data.length];
    	ThreadLocalRandom rand = ThreadLocalRandom.current();
        for(int i = 0; i < data.length; i++)
        	data[i] = (byte)((rand.nextDouble()-0.5) * Byte.MAX_VALUE);*/
    	
    	//short[] shortarray = new short[data.length/VoiceChat.format.getFrameSize()]; //<-- Is this data.length/2 ok? don't think so, was just a random idea
    	
    	
    	/*Noise:
    	shortarray = new short[8000];
    	ThreadLocalRandom rand = ThreadLocalRandom.current();
        for(int i = 0; i < 8000; i++)
        	shortarray[i] = (short)(rand.nextDouble() * Short.MAX_VALUE*0.2);*/
        
    	// here we go compensate for network delay etc
    	// since if you send at 12 o clock exactly
    	// you will receive a bit later
    	// lets say delay everything by 300 millis
    	// you can finetune later
    	
    	// first we need to know how "late" is consumer position 0 relative to timestamp
    	// hmz lemme think
    	// now need to convert timestamp to consumer position
    	// plus delay
    	//Old: long whenInMillis = timestamp - AudioConsumer.STARTED_AT + 500; //wait a second couldn't we just do this?
    	//long whenInMillis = (long) (consumer.position() + timestamp / 1000.0 * VoiceChat.format.getFrameRate());
    	
    	EntityPlayer otherPlayer = null;
    	Minecraft mc = Minecraft.getMinecraft();
    	for (int i = 0; i < mc.theWorld.playerEntities.size(); i++) {
			if(((EntityPlayer)mc.theWorld.playerEntities.get(i)).getCommandSenderName().equals(this.player))
				otherPlayer = (EntityPlayer)mc.theWorld.playerEntities.get(i);
		}
    	
    	float volume = 1.0F;
    	
    	if(otherPlayer != null)
    	{
    		float distance = player.getDistanceToEntity(otherPlayer);
    		volume = 1-distance/(float)VoiceChat.distance;
    	}
    	
    	/*for (int i = 0; i < data.length; i++) {
			data[i] *= volume;
		}*/
    	
    	short[] shortarray = new short[data.length/2];
    	ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortarray);
    	
    	for (int i = 0; i < shortarray.length; i++) {
    		shortarray[i] *= volume;
		}
    	
    	double frameDelay = timestamp / 1000.0 * VoiceChat.format.getFrameRate();
    	
    	long time = 0;
    	long max = (long) (consumer.position()+(float)frameDelay*2);
    	if(lastplayed.containsKey(this.player))
    		time = (long) Math.max(consumer.position(), Math.min(lastplayed.get(this.player), max));
    	else
    		time = consumer.position();
    	
    	//if(time == max)
    		//System.out.println("Used max!");
    	
    	//int test = VoiceChat.format.getFrameSize();
    	//long whenInMillis = (long) (time);
    	long whenInMillis = (long) (time + frameDelay);
    	//that would be the delay of "300ms"
    	// no that wont work
    	// if you do that youre mixing audio and network latency
    	// meaning network jitter will cause discontinuities in the audio stream
    	// // no ok its millis
    	// now go from target timestamp to buffer position
    	//long whenInFrames = (long)(whenInMillis / 1000.0 * VoiceChat.format.getFrameRate());
    	// now correct for consumer position
    	
    	//whenInFrames-=consumer.position();
    	// ok i hope thats about right
    	// always dificult to wrap my head around this stuff
    	// lets just try - fire it up!
    	//need to add some send code
    	
    	// do some logging
    	// i want to make sure this looks ok
    	
    	//System.out.println ("playing " + player.getCommandSenderName());
    	/*System.out.println ("recorded at = " + timestamp);
    	System.out.println ("received at = " + System.currentTimeMillis());
    	System.out.println ("consumer at = " + consumer.position());
    	System.out.println ("playback at = " + whenInMillis);
    	System.out.println ("engine started at = " + (AudioConsumer.STARTED_AT/1000));*/
    	
    	
    	try
		{
			consumer.mix(whenInMillis, shortarray); // Is this line right? i'm the consumer.position()
			//System.out.println("Playing");
		}catch(Exception e){
			e.printStackTrace();
		}
    	
    	lastplayed.put(this.player, whenInMillis);
    	
	}
	
    
    
    public void executeServer(EntityPlayer player)
	{
    	playAudioAt(player, this.player, data, length, timestamp);
    	ArrayList<Integer> frequenzes = VoiceChat.getFrequenzes(player);
    	if(frequenzes.size() > 0)
    	{
    		List players = player.worldObj.playerEntities;
    		for(int zahl = 0; zahl < players.size(); zahl++)
    		{
    			EntityPlayer newplayer = (EntityPlayer) players.get(zahl);
    			if(!player.getCommandSenderName().equals(newplayer.getCommandSenderName()) && VoiceChat.hasFrequenzes(newplayer, frequenzes))
    			{
    				if(checkIfLoud(VoiceChat.getItemStacks(newplayer, frequenzes)))
    				{
    					playAudioAt(newplayer, this.player, data, length, timestamp);
    				}else{
    					// whats this? test stuff ?
    					PacketHandler.sendPacketToPlayer(this, (EntityPlayerMP) newplayer);
    					//PacketHandler.sendToPlayer(new AudioPacket(data, length, player.getCommandSenderName(), 0), newplayer);
    				}
    			}
    		}
    	}
	}
    
    public static boolean checkIfLoud(ArrayList<ItemStack> stacks)
    {
    	for(int zahl = 0; zahl < stacks.size(); zahl++)
    		if(stacks.get(zahl) != null && ItemTalkie.isLoud(stacks.get(zahl)))
    			return true;
    	return false;
    }
    
    public static void playAudioAt(EntityPlayer Audioplayer, String playerName, byte[] data, int length, long timestamp)
    {
    	List players = Audioplayer.worldObj.playerEntities;
		for(int zahl = 0; zahl < players.size(); zahl++)
		{
			EntityPlayer player = (EntityPlayer) players.get(zahl);
			if(!player.getCommandSenderName().equals(Audioplayer.getCommandSenderName()) && VoiceChat.isInRange((int)Audioplayer.posX, (int)Audioplayer.posY, (int)Audioplayer.posZ, player))
			{
				PacketHandler.sendPacketToPlayer(new AudioPacket(data, length, playerName, timestamp), (EntityPlayerMP) player);
			}
		}
    }
}
