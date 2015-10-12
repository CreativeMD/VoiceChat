package com.creativemd.voicechat.client;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.FMLConfigGuiFactory.FMLConfigGuiScreen;
import cpw.mods.fml.client.IModGuiFactory.RuntimeOptionCategoryElement;
import cpw.mods.fml.client.IModGuiFactory.RuntimeOptionGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiVoiceConfig extends GuiScreen {
	
	public boolean stopped = false;
    @Override
    public void initGui()
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 38, I18n.format("gui.done")));
        //this.buttonList.ad
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Forge Mod Loader test config screen", this.width / 2, 40, 0xFFFFFF);
        super.drawScreen(par1, par2, par3); 
    }
    
    @Override
    protected void actionPerformed(GuiButton par1GuiButton)
    {
    	if (par1GuiButton.enabled)
        {
    		switch(par1GuiButton.id)
    		{
    		case 1:
    			this.onGuiClosed();
            	break;
    		}
        }
    }
}
