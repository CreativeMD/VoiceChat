package com.creativemd.voicechat.core;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.voicechat.client.AudioConsumer;
import com.creativemd.voicechat.client.RecordThread;
import com.creativemd.voicechat.config.ConfigLoader;
import com.creativemd.voicechat.packets.AudioPacket;
import com.creativemd.voicechat.packets.ConfigPacket;
import com.creativemd.voicechat.packets.GuiPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = VoiceChat.modid, version = VoiceChat.version, name = "VoiceChat")
public class VoiceChat {//extends DummyModContainer{
	
	public static final String modid = "voicechat"; 
	public static final String version = "0.3";
	
	public static Configuration config;
	
	/*public VoiceChat() {
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = "VoiceChat";
		meta.version = "0.1"; //String.format("%d.%d.%d.%d", majorVersion, minorVersion, revisionVersion, buildVersion);
		meta.credits = "CreativeMD";
		meta.authorList = Arrays.asList("CreativeMD");
		meta.description = "This mod adds the ability to talk with people in a server.";
		meta.url = "";
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void modConstruction(FMLConstructionEvent evt){

	}*/
	
	public static void load()
	{
		config.load();
		distance = config.get("Voice", "range", distance).getInt(distance);
		sampleRate = (float) config.get("Voice", "sampleRate", sampleRate).getDouble(sampleRate);
		delay = config.get("Voice", "delay", delay).getInt(delay);
		config.save();
	}
	
	public static void save()
	{
		if(FMLCommonHandler.instance().getMinecraftServerInstance() != null && !FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer())
		{
			config.load();
			config.get("Voice", "range", distance).set(distance);
			config.get("Voice", "sampleRate", sampleRate).set(sampleRate);
			config.get("Voice", "delay", delay).set(delay);
			config.save();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static RecordThread thread;
	
	/** 8000,11025,16000,22050,44100,48000**/
	public static float sampleRate = 16000.0F;
	public static int delay = 4000; 
	public static int distance = 50; //50m
	
	public static AudioFormat format;
	
	public static void refreshAudioFormat()
	{
		int sampleSizeInBits = 16; //8,16
		int channels = 2; //1,2
		boolean signed = true; //true,false
		boolean bigEndian = false; //true,false
		VoiceChat.format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		
		AudioConsumer.CHANNELS = VoiceChat.format.getChannels();
		AudioConsumer.SAMPLE_RATE = (int) VoiceChat.format.getSampleRate();
		AudioConsumer.NUM_PRODUCERS = 1;
		AudioConsumer.BUFFER_SIZE_FRAMES = (int) (VoiceChat.format.getSampleRate() / 4);
		
		if(AudioPacket.consumer != null)
		{
			AudioPacket.consumer.close();
		}
			
		AudioPacket.consumer = new AudioConsumer();
		AudioPacket.consumer.start();
		
		if(VoiceChat.thread != null)
		{
			VoiceChat.thread.active = false;
			VoiceChat.thread.interrupt();
			VoiceChat.thread.line.close();
		}
		
		VoiceChat.thread = new RecordThread();
		
		//Initializate Microphone
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, VoiceChat.format);
		try { 
			VoiceChat.thread.line = (TargetDataLine) AudioSystem.getLine(info);     
			VoiceChat.thread.line.open(VoiceChat.format); 
			/*float bits = VoiceChat.format.getSampleSizeInBits();
			bits = VoiceChat.format.getFrameRate();
			bits = VoiceChat.format.getSampleRate();
			bits = VoiceChat.format.getFrameSize();
			//RecordThread.line.
			RecordThread.line.open(VoiceChat.format, VoiceChat.format.getSampleSizeInBits()); */
		} catch (LineUnavailableException ex) { 
			ex.printStackTrace();
		}
		
		//VoiceChat.thread.start();
	}
	
	//public static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static Item talkie;
	
	@EventHandler
	public void init(FMLInitializationEvent evt) {
		CreativeCorePacket.registerPacket(AudioPacket.class, "VCaudio");
		CreativeCorePacket.registerPacket(ConfigPacket.class, "VCconfig");
		CreativeCorePacket.registerPacket(GuiPacket.class, "VGUI");
		MinecraftForge.EVENT_BUS.register(new EventHandlerBoth());
		talkie = new ItemTalkie().setUnlocalizedName("walkietalkie");
		GameRegistry.registerItem(talkie, talkie.getUnlocalizedName());
		FMLCommonHandler.instance().bus().register(new EventHandlerBoth());
		
		if(Loader.isModLoaded("ingameconfigmanager"))
			ConfigLoader.startConfig();
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			loadClient();
	}
	
	/*@SideOnly(Side.CLIENT)
	public static void refreshFormat()
	{
		RecordThread.line.close();
		try {
			RecordThread.line.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		for(int zahl = 0; zahl < PlayThread.threads.size(); zahl++)
		{
			PlayThread.threads.get(zahl).interrupt();
		}
		PlayThread.threads.clear();
	}*/
	
	@SideOnly(Side.CLIENT)
	public void loadClient()
	{
		refreshAudioFormat();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
		load();
	}

	/*@Subscribe
	public void postInit(FMLPostInitializationEvent evt) {

	}*/
	
	public static boolean isInRange(int X, int Y, int Z, EntityPlayer player)
	{
		return distance >= Math.sqrt(Math.pow(player.posX-X, 2) + Math.pow(player.posY-Y, 2) + Math.pow(player.posZ-Z, 2));
	}
	
	/*public static ItemStack getItemStack(EntityPlayer player, int frequenz)
	{
		ArrayList<Integer> frequenzes = new ArrayList<Integer>();
		frequenzes.add(frequenz);
		for(int zahl = 0; zahl < player.inventory.mainInventory.length; zahl++)
			if(player.inventory.mainInventory[zahl] != null &&
				player.inventory.mainInventory[zahl].getItem() instanceof ItemTalkie &&
				ItemTalkie.isActive(player.inventory.mainInventory[zahl]) &&
				hasFrequenzes(player, frequenzes))
					return player.inventory.mainInventory[zahl];
		return null;
	}*/
	
	/*public static ArrayList<ItemStack> getItemStacks(EntityPlayer player, ArrayList<Integer> frequenzes)
	{
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		for(int zahl = 0; zahl < frequenzes.size(); zahl++)
		{
			stacks.add(getItemStack(player, frequenzes.get(zahl)));
		}
		return stacks;
	}*/
	
	/*public static boolean hasFrequenzes(EntityPlayer player, ArrayList<Integer> frequenzes)
	{
		 ArrayList<Integer> newFrequnezes = getFrequenzes(player);
		 for(int zahl = 0; zahl < frequenzes.size(); zahl++)
			 if(newFrequnezes.contains(frequenzes.get(zahl)))
				 return true;
		 return false;
	}
	
	public static ArrayList<Integer> getFrequenzes(EntityPlayer player)
	{
		ArrayList<Integer> frequenzes = new ArrayList<Integer>();
		for(int zahl = 0; zahl < player.inventory.mainInventory.length; zahl++)
			if(player.inventory.mainInventory[zahl] != null &&
				player.inventory.mainInventory[zahl].getItem() instanceof ItemTalkie &&
				ItemTalkie.isActive(player.inventory.mainInventory[zahl]))
					frequenzes.add(ItemTalkie.getFrequenz(player.inventory.mainInventory[zahl]));
		return frequenzes;
	}*/
}
