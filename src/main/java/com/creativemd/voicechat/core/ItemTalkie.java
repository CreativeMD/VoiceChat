package com.creativemd.voicechat.core;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemTalkie extends Item{
	
	@SideOnly(Side.CLIENT)
    private IIcon on;
	@SideOnly(Side.CLIENT)
	private IIcon off;
 
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        on = par1IconRegister.registerIcon(VoiceChat.modid + ":walkie.png");
        off = par1IconRegister.registerIcon(VoiceChat.modid + ":walkieOff.png");
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack par1ItemStack)
    {
    	if(isActive(par1ItemStack))
    		return on;
        return off;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	if(par1ItemStack.stackTagCompound == null)
    		par1ItemStack.stackTagCompound = new NBTTagCompound();
    	par1ItemStack.stackTagCompound.setBoolean("active", !isActive(par1ItemStack));
        return par1ItemStack;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
    	if(isActive(par1ItemStack))
    		par3List.add("On");
    	else
    		par3List.add("Off");
    	par3List.add(getFrequenz(par1ItemStack) + "Mhz");
    	
    }
    
    public static boolean isLoud(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.stackTagCompound == null)
    		par1ItemStack.stackTagCompound = new NBTTagCompound();
    	return par1ItemStack.stackTagCompound.getBoolean("loud");
    }
    
    public static boolean isActive(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.stackTagCompound == null)
    		par1ItemStack.stackTagCompound = new NBTTagCompound();
    	return par1ItemStack.stackTagCompound.getBoolean("active");
    }
    
    public static int getFrequenz(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.stackTagCompound == null)
    		par1ItemStack.stackTagCompound = new NBTTagCompound();
    	int frequenz = par1ItemStack.stackTagCompound.getInteger("frequenz");
    	if(frequenz < 400)
    		frequenz = 400;
    	if(frequenz > 500)
    		 frequenz = 500;
    	return frequenz;
    }

}