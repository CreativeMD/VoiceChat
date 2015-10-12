package com.creativemd.voicechat.core;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.item.ItemTossEvent;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.voicechat.client.PlayThread;
import com.creativemd.voicechat.client.RecordThread;
import com.creativemd.voicechat.packets.AudioPacket;
import com.creativemd.voicechat.packets.ConfigPacket;
import com.creativemd.voicechat.packets.GuiPacket;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class VoiceChat extends DummyModContainer{
	
	public static final String modid = "voicechat"; 
	
	public static Configuration config;
	
	public VoiceChat() {
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

	}
	
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
		if(MinecraftServer.getServer() != null && !MinecraftServer.getServer().isSinglePlayer())
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
	public static int delay = 50; //10ms
	public static int distance = 50; //50m
	
	public static AudioFormat format;
	
	public static AudioFormat refreshAudioFormat()
	{
		int sampleSizeInBits = 16; //8,16
		int channels = 2; //1,2
		boolean signed = true; //true,false
		boolean bigEndian = false; //true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	
	public static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static Item talkie;
	
	@Subscribe
	public void init(FMLInitializationEvent evt) {
		CreativeCorePacket.registerPacket(AudioPacket.class, "VCaudio");
		CreativeCorePacket.registerPacket(ConfigPacket.class, "VCconfig");
		CreativeCorePacket.registerPacket(GuiPacket.class, "VGUI");
		MinecraftForge.EVENT_BUS.register(new EventHandlerBoth());
		talkie = new ItemTalkie().setUnlocalizedName("walkietalkie");
		GameRegistry.registerItem(talkie, talkie.getUnlocalizedName());
		FMLCommonHandler.instance().bus().register(new EventHandlerBoth());
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
		format = refreshAudioFormat();
		//Initializate Microphone
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, VoiceChat.format);
		try { 
			RecordThread.line = (TargetDataLine) AudioSystem.getLine(info);          
			RecordThread.line.open(VoiceChat.format); 
		} catch (LineUnavailableException ex) { 
			ex.printStackTrace();
		}
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent evt) {

	}
	
	public static boolean isInRange(int X, int Y, int Z, EntityPlayer player)
	{
		return distance >= Math.sqrt(Math.pow(player.posX-X, 2) + Math.pow(player.posY-Y, 2) + Math.pow(player.posZ-Z, 2));
	}
	
	public static ItemStack getItemStack(EntityPlayer player, int frequenz)
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
	}
	
	public static ArrayList<ItemStack> getItemStacks(EntityPlayer player, ArrayList<Integer> frequenzes)
	{
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		for(int zahl = 0; zahl < frequenzes.size(); zahl++)
		{
			stacks.add(getItemStack(player, frequenzes.get(zahl)));
		}
		return stacks;
	}
	
	public static boolean hasFrequenzes(EntityPlayer player, ArrayList<Integer> frequenzes)
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
	}
}
