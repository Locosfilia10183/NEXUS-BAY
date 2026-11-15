package de.nexusbay.forge.client;

import de.nexusbay.forge.network.QuestNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuestScreen extends Screen {
    private final String islandId,islandName,npcName,greeting,activeId;
    private final int progress;
    private final String[] ids,titles,descriptions;
    private final int[] kills;
    private final long[] beli,xp;
    private final boolean[] completed;
    private QuestScreen(String islandId,String islandName,String npcName,String greeting,String activeId,int progress,String[] ids,String[] titles,String[] descriptions,int[] kills,long[] beli,long[] xp,boolean[] completed){
        super(Component.literal("Questgeber • "+islandName));this.islandId=islandId;this.islandName=islandName;this.npcName=npcName;this.greeting=greeting;this.activeId=activeId;this.progress=progress;this.ids=ids;this.titles=titles;this.descriptions=descriptions;this.kills=kills;this.beli=beli;this.xp=xp;this.completed=completed;
    }
    public static void open(String islandId,String islandName,String npcName,String greeting,String activeId,int progress,String[] ids,String[] titles,String[] descriptions,int[] kills,long[] beli,long[] xp,boolean[] completed){
        net.minecraft.client.Minecraft.getInstance().setScreen(new QuestScreen(islandId,islandName,npcName,greeting,activeId,progress,ids,titles,descriptions,kills,beli,xp,completed));
    }
    @Override protected void init(){
        int width=Math.min(380,this.width-24),left=(this.width-width)/2;
        for(int i=0;i<ids.length;i++){final int index=i;int y=148+i*76;
            if(activeId.equals(ids[i])&&progress>=kills[i])addRenderableWidget(Button.builder(Component.literal("Belohnung abholen"),b->QuestNetwork.claim(islandId,ids[index])).bounds(left+width-146,y,136,20).build());
            else if(activeId.isBlank()&&!completed[i])addRenderableWidget(Button.builder(Component.literal("Quest annehmen"),b->QuestNetwork.accept(islandId,ids[index])).bounds(left+width-146,y,136,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Schließen"),b->onClose()).bounds(this.width/2-50,this.height-30,100,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);int width=Math.min(380,this.width-24),left=(this.width-width)/2;
        g.fill(left,18,left+width,this.height-18,0xEE101D2B);g.fill(left,18,left+width,52,0xFF263C50);
        g.drawCenteredString(font,"QUESTGEBER • "+islandName.toUpperCase(),this.width/2,30,0xFFFFD36A);
        g.drawCenteredString(font,npcName,this.width/2,58,0xFFFFFFFF);\n        g.drawCenteredString(font,greeting,this.width/2,72,0xFFE1EAF2);\n        g.drawCenteredString(font,activeId.isBlank()?"Wähle deine nächste Aufgabe":"Aktive Quest: "+activeId,this.width/2,92,0xFF74D8FF);
        for(int i=0;i<ids.length;i++){int y=106+i*76;g.fill(left+12,y-5,left+width-12,y+67,0xAA26394B);
            g.drawString(font,titles[i],left+20,y,0xFFFFD36A,false);g.drawString(font,descriptions[i],left+20,y+13,0xFFE1EAF2,false);
            String state=completed[i]?"ABGESCHLOSSEN":activeId.equals(ids[i])?"FORTSCHRITT "+Math.min(progress,kills[i])+"/"+kills[i]:!activeId.isBlank()?"GESPERRT – andere Quest aktiv":"Ziel: "+kills[i]+" Gegner";
            g.drawString(font,state,left+20,y+27,completed[i]?0xFF83D69A:0xFFB8D5E8,false);
            g.drawString(font,"Belohnung: "+beli[i]+" Beli  •  "+xp[i]+" Frucht-XP",left+20,y+41,0xFFD9C48C,false);
        }super.render(g,mx,my,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
