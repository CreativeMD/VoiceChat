package com.creativemd.voicechat.client;

import net.minecraft.client.gui.GuiButton;

public class GuiButtonState extends GuiButton{
	
	public String[] states;
	
	public GuiButtonState(int par1, int par2, int par3, int par4, int par5, String... states) {
		super(par1, par2, par3, par4, par5, states[0]);
		this.states = states;
	}
	
	public int buttonClicked()
	{
		int index = getIndex()+1;
		if(index < 0 | index >= states.length)
			index = 0;
		displayString = states[index];
		return index;
	}
	
	public int getIndex()
	{
		for(int zahl = 0; zahl < states.length; zahl++)
			if(displayString.equals(states[zahl]))
				return zahl;
		return -1;
	}
	
	public void setIndex(int index)
	{
		if(index < 0 | index >= states.length)
			index = 0;
		displayString = states[index];
	}

}
