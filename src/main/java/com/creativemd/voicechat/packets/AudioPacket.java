package com.creativemd.voicechat.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.voicechat.client.AudioConsumer;
import com.creativemd.voicechat.core.ItemTalkie;
import com.creativemd.voicechat.core.VoiceChat;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AudioPacket extends CreativeCorePacket{
	
	public byte[] data = null;
	//public int length = 0;
	public String player;
	//public long timestamp = 0;
	public boolean isBoundToPlayer = true;
	public int x;
	public int y;
	public int z;
	public float noise;
	
	public AudioPacket()
	{
		
	}
	
	/*public AudioPacket(byte[] data, int length, String player)
	{
		
	}*/
	
	public AudioPacket(byte[] data/*, int length*/, String player, /*long timestamp,*/ int x, int y, int z, float noise)
	{
		this.data = data;
		//this.length = length;
		this.player = player;
		//this.timestamp = timestamp;
		this.x = x;
		this.y = y;
		this.z = z;
		this.noise = noise;
	}
	
	public AudioPacket(byte[] data/*, int length*/, String player/*, long timestamp*/)
	{
		this(data/*, length*/, player/*, timestamp*/, 0, 0, 0, 0);
		this.isBoundToPlayer = false;
	}
	
	public void readBytes(ByteBuf bytes)
	{
		//timestamp = bytes.readLong();
		data = new byte[bytes.readInt()];
		bytes.readBytes(data);
		//length = bytes.readInt();// wrong order
		player = ByteBufUtils.readUTF8String(bytes);
		if(bytes.readBoolean())
		{
			isBoundToPlayer = false;
			x = bytes.readInt();
			y = bytes.readInt();
			z = bytes.readInt();
			noise = bytes.readFloat();
		}
	}
	
    public void writeBytes(ByteBuf bytes)
	{
    	//bytes.writeLong(timestamp);
    	bytes.writeInt(data.length);
    	bytes.writeBytes(data);
		//bytes.writeInt(length);
		ByteBufUtils.writeUTF8String(bytes, player);
		bytes.writeBoolean(isBoundToPlayer);
		if(isBoundToPlayer)
		{
			bytes.writeInt(x);
			bytes.writeInt(y);
			bytes.writeInt(z);
			bytes.writeFloat(noise);
		}
	}
    
    public static boolean loaded = false;
    
    @SideOnly(Side.CLIENT)
    public static AudioConsumer consumer;
    
    public static HashMap<String, Long> lastplayed = new HashMap<String, Long>(); //This is the list containg a player as key and the timestamp as value
    
    public void executeClient(EntityPlayer player)
	{    	
    	EntityPlayer otherPlayer = null;
    	Minecraft mc = Minecraft.getMinecraft();
    	for (int i = 0; i < mc.theWorld.playerEntities.size(); i++) {
			if(((EntityPlayer)mc.theWorld.playerEntities.get(i)).getName().equals(this.player))
				otherPlayer = (EntityPlayer)mc.theWorld.playerEntities.get(i);
		}
    	
    	float volume = 1.0F;
    	
    	if(otherPlayer != null)
    	{
    		float distance = 0;
    		if(isBoundToPlayer)
    			distance = player.getDistanceToEntity(otherPlayer);
    		else
    			player.getDistance(x, y, z);
    		volume = 1-distance/(float)VoiceChat.distance;
    	}
    	
    	short[] shortarray = new short[data.length/2];
    	ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortarray);
    	
    	if(noise > 0)
    	{
    		for (int i = 0; i < shortarray.length; i++) {
        		shortarray[i] += Math.random()*Short.MAX_VALUE*noise;
    		}
    	}
    	
    	for (int i = 0; i < shortarray.length; i++) {
    		shortarray[i] *= volume;
		}
    	
    	double millisInPacket = data.length / VoiceChat.format.getFrameSize() * 1000.0 / VoiceChat.format.getSampleRate();
    	//millisInPacket = 100000;
    	
    	long time = 0;
    	long max = (long) (consumer.position()+(float)millisInPacket);
    	if(lastplayed.containsKey(this.player))
    		time = (long) Math.max(consumer.position()+4000, Math.min(lastplayed.get(this.player), max));
    	else
    		time = consumer.position()+4000;
    	
    	//if(time == max)
    		//System.out.println("Used max!");
    	
    	//int test = VoiceChat.format.getFrameSize();
    	//long whenInMillis = (long) (time);
    	long whenInMillis = (long) (time);
    	//long whenInMillis = (long) (time + frameDelay);
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
    	
    	[12:26:02 AM] Sjoerd van Kreel: plenty to choose from
    	[12:26:12 AM] Sjoerd van Kreel: K-NN being one of the simplest
    	[12:26:27 AM] Sjoerd van Kreel: just set each sample to the avg of it's K nearest neighbours
    	[12:26:27 AM] Sjoerd van Kreel: eg
    	[12:26:40 AM] Sjoerd van Kreel: seq is 0 1 2 3 2 1
    	[12:26:43 AM] Sjoerd van Kreel: use 2-NN
    	[12:27:40 AM] Sjoerd van Kreel: becomes (undef) (0 + 1 + 2) /3 (1 + 2 + 3) / 3 ( 2 + 3 + 2 ) / 3 (3 + 2 + 1) / 3 (undef)
    	[12:28:08 AM] Sjoerd van Kreel: very simple, but will definitively cut out the high frequencies
    	[12:28:18 AM] Sjoerd van Kreel: more K = more filtering
    	try
    	{
			consumer.mix(whenInMillis, shortarray); // Is this line right? i'm the consumer.position()
			System.out.println("Playing sound with " + (whenInMillis - consumer.position()) + " offset; framedelay=" + millisInPacket + ";"); // length=" + length + ";");
			
		}catch(Exception e){
			e.printStackTrace();
		}
    	
    	lastplayed.put(this.player, whenInMillis + (long)millisInPacket);
    	
	}
	
    
    
    public void executeServer(EntityPlayer player)
	{
    	playAudioAt(player, this.player, data/*, length, timestamp*/);
    	/*ArrayList<Integer> frequenzes = VoiceChat.getFrequenzes(player);
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
    	}*/
	}
    
    public static boolean checkIfLoud(ArrayList<ItemStack> stacks)
    {
    	for(int zahl = 0; zahl < stacks.size(); zahl++)
    		if(stacks.get(zahl) != null && ItemTalkie.isLoud(stacks.get(zahl)))
    			return true;
    	return false;
    }
    
    public static void playAudioAt(EntityPlayer Audioplayer, String playerName, byte[] data/*, int length, long timestamp*/)
    {
    	ArrayList<Integer> frequenzes = ItemTalkie.getActiveFrequenzes(Audioplayer);
    	List players = Audioplayer.worldObj.playerEntities;
		for(int zahl = 0; zahl < players.size(); zahl++)
		{
			EntityPlayer player = (EntityPlayer) players.get(zahl);
			if(player != Audioplayer)
			{
				if(VoiceChat.isInRange((int)Audioplayer.posX, (int)Audioplayer.posY, (int)Audioplayer.posZ, player))
					PacketHandler.sendPacketToPlayer(new AudioPacket(data/*, length*/, playerName/*, timestamp*/), (EntityPlayerMP) player);
				if(frequenzes.size() > 0 && ItemTalkie.canReceivePlayerFrequenzes(player, frequenzes))
					PacketHandler.sendPacketToPlayer(new AudioPacket(data/*, length*/, "talkie" + frequenzes.get(0)/*, timestamp*/, (int)player.posX, (int)player.posY, (int)player.posZ, 0.01F), (EntityPlayerMP) player);
				//System.out.println("Sending sound to player: " + player.getCommandSenderName());
			}
		}
    }
}
