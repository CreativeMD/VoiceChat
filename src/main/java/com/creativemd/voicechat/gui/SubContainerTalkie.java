package com.creativemd.voicechat.gui;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.voicechat.core.ItemTalkie;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerTalkie extends SubContainer{
	
	public ItemStack stack;
	
	public SubContainerTalkie(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		ItemTalkie.setFrequenz(stack, nbt.getInteger("frequenz"));
	}

}
