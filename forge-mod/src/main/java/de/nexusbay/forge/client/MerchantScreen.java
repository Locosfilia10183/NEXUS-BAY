package de.nexusbay.forge.client;

import de.nexusbay.forge.network.MerchantNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MerchantScreen extends Screen {
    private final String island,name;
    private final long balance;
    private final String[] ids,names;
    private final long[] prices;
    private MerchantScreen(String island,String name,long balance,String[] ids,String[] names,long[] prices){
        super(Component.literal("Händler • "+name));this.island=island;this.name=name;this.balance=balance;this.ids=ids;this.names=names;this.prices=prices;
    }
    public static void open(String island,String name,long balance,String[] ids,String[] names,long[] prices){
        net.minecraft.client.Minecraft.getInstance().setScreen(new MerchantScreen(island,name,balance,ids,names,prices));
    }
    @Override protected void init(){
        int width=Math.min(340,this.width-24),left=(this.width-width)/2;
        for(int i=0;i<ids.length;i++){final int index=i;int y=105+i*48;
            addRenderableWidget(Button.builder(Component.literal("Kaufen • "+prices[i]+" Beli"),b->MerchantNetwork.buy(island,ids[index]))
                    .bounds(left+width-142,y,132,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Schließen"),b->onClose()).bounds(this.width/2-50,this.height-30,100,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);int width=Math.min(340,this.width-24),left=(this.width-width)/2;
        g.fill(left,24,left+width,this.height-20,0xEE101D2B);g.fill(left,24,left+width,68,0xFF263C50);
        g.drawCenteredString(font,name,this.width/2,38,0xFFFFD36A);
        g.drawCenteredString(font,"Dein Guthaben: "+balance+" Beli",this.width/2,54,0xFF83D69A);
        for(int i=0;i<ids.length;i++){int y=94+i*48;g.fill(left+10,y,left+width-10,y+38,0xAA26394B);
            g.drawString(font,names[i],left+18,y+13,0xFFFFFFFF,false);
        }
        super.render(g,mx,my,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
