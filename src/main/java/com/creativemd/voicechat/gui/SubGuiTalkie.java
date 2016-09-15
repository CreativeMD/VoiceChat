package com.creativemd.voicechat.gui;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.voicechat.core.ItemTalkie;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiTalkie extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiTalkie(ItemStack stack) {
		super();
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiButton("<", 5, 5, 10){

			@Override
			public void onClicked(int x, int y, int button) {
				GuiSteppedSlider slider = (GuiSteppedSlider) get("frequenz");
				slider.setValue(slider.value-1);
			}
			
		});
		controls.add(new GuiLabel("MHz", 76, 9));
		controls.add(new GuiSteppedSlider("frequenz", 16, 5, 100, 20, ItemTalkie.min, ItemTalkie.max, ItemTalkie.getFrequenz(stack)));
		controls.add(new GuiButton(">", 117, 5, 10){

			@Override
			public void onClicked(int x, int y, int button) {
				GuiSteppedSlider slider = (GuiSteppedSlider) get("frequenz");
				slider.setValue(slider.value+1);
			}
			
		});
	}
	
	@CustomEventSubscribe
	public void onControlChange(GuiControlChangedEvent event)
	{
		if(event.source.is("frequenz"))
		{
			GuiSteppedSlider slider = (GuiSteppedSlider) get("frequenz");
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("frequenz", (int)slider.value);
			sendPacketToServer(nbt);
		}
	}
}
