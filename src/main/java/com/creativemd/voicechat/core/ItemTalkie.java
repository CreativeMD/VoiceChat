package com.creativemd.voicechat.core;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.voicechat.gui.SubContainerTalkie;
import com.creativemd.voicechat.gui.SubGuiTalkie;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTalkie extends Item implements IGuiCreator{
	
	public static final int min = 400;
	public static final int max = 800;
	
	public ItemTalkie() {
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
	}
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
    	if(!world.isRemote)
    	{
    		if(player.isSneaking())
    			GuiHandler.openGuiItem(player, world);
    		else
    		{
    			if(stack.hasTagCompound())
    				stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setBoolean("active", !isActive(stack));
    		}
    	}
    	return new ActionResult(EnumActionResult.SUCCESS, stack);
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
    
    public static boolean canReceivePlayerFrequenzes(EntityPlayer player, ArrayList<Integer> frequenzes)
    {
    	ArrayList<Integer> playerfrequenzes = getReceivingFrequenzes(player);
    	for (int i = 0; i < frequenzes.size(); i++) {
			if(playerfrequenzes.contains(frequenzes.get(i)))
				return true;
		}
    	return false;
    }
    
    public static ArrayList<Integer> getReceivingFrequenzes(EntityPlayer player)
    {
    	ArrayList<Integer> frequenzes = new ArrayList<Integer>();
    	for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if(stack != null && stack.getItem() instanceof ItemTalkie)
				frequenzes.add(getFrequenz(stack));
		}
    	return frequenzes;
    }
    
    public static ArrayList<Integer> getActiveFrequenzes(EntityPlayer player)
    {
    	ArrayList<Integer> frequenzes = new ArrayList<Integer>();
    	for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if(stack != null && stack.getItem() instanceof ItemTalkie && isActive(stack))
				frequenzes.add(getFrequenz(stack));
		}
    	return frequenzes;
    }
    
    public static boolean isLoud(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.hasTagCompound())
    		par1ItemStack.setTagCompound(new NBTTagCompound());
    	return par1ItemStack.getTagCompound().getBoolean("loud");
    }
    
    public static boolean isActive(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.hasTagCompound())
    		par1ItemStack.setTagCompound(new NBTTagCompound());
    	return par1ItemStack.getTagCompound().getBoolean("active");
    }
    
    
    public static void setFrequenz(ItemStack stack, int frequenz)
    {
    	frequenz = Math.min(max, frequenz);
    	frequenz = Math.max(min, frequenz);
    	if(stack.hasTagCompound())
    		stack.setTagCompound(new NBTTagCompound());
    	stack.getTagCompound().setInteger("frequenz", frequenz);
    }
    
    public static int getFrequenz(ItemStack par1ItemStack)
    {
    	if(par1ItemStack.hasTagCompound())
    		par1ItemStack.setTagCompound(new NBTTagCompound());
    	int frequenz = par1ItemStack.getTagCompound().getInteger("frequenz");
    	if(frequenz < min)
    		frequenz = min;
    	if(frequenz > max)
    		 frequenz = max;
    	return frequenz;
    }

	@Override
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiTalkie(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos,
			IBlockState state) {
		return new SubContainerTalkie(player, stack);
	}

}