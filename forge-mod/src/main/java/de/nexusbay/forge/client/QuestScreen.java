package de.nexusbay.forge.client;

import de.nexusbay.forge.network.QuestNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuestScreen extends Screen {
    private final String islandId, islandName, activeId;
    private final int progress;
    private final String[] ids, titles, descriptions;
    private final int[] kills;
    private final long[] beli, fruitXp;
    private final boolean[] completed;
    private QuestScreen(String islandId, String islandName, String activeId, int progress,
                        String[] ids, String[] titles, String[] descriptions, int[] kills,
                        long[] beli, long[] fruitXp, boolean[] completed) {
        super(Component.literal("Questgeber • " + islandName));
        this.islandId=islandId; this.islandName=islandName; this.activeId=activeId; this.progress=progress;
        this.ids=ids; this.titles=titles; this.descriptions=descriptions; this.kills=kills;
        this.beli=beli; this.fruitXp=fruitXp; this.completed=completed;
    }
    public static void open(String islandId, String islandName, String activeId, int progress,
                            String[] ids, String[] titles, String[] descriptions, int[] kills,
                            long[] beli, long[] fruitXp, boolean[] completed) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new QuestScreen(islandId,islandName,activeId,
                progress,ids,titles,descriptions,kills,beli,fruitXp,completed));
    }
    @Override protected void init() {
        int panelWidth=Math.min(360,this.width-24), left=(this.width-panelWidth)/2;
        int top=35;
        for(int i=0;i<ids.length;i++){
            final int index=i;
            int y=top+i*76+48;
            if(!activeId.isBlank() && activeId.equals(ids[i]) && progress>=kills[i]){
                addRenderableWidget(Button.builder(Component.literal("Belohnung abholen"),b->QuestNetwork.claim(islandId,ids[index]))
                        .bounds(left+panelWidth-142,y,132,20).build());
            } else if(activeId.isBlank() && !completed[i]){
                addRenderableWidget(Button.builder(Component.literal("Quest annehmen"),b->QuestNetwork.accept(islandId,ids[index]))
                        .bounds(left+panelWidth-142,y,132,20).build());
            }
        }
        addRenderableWidget(Button.builder(Component.literal("Schließen"),b->onClose())
                .bounds(this.width/2-50,this.height-34,100,20).build());
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){
        renderBackground(g);
        int panelWidth=Math.min(360,this.width-24), left=(this.width-panelWidth)/2;
        g.fill(left,18,left+panelWidth,this.height-18,0xEE101D2B);
        g.fill(left,18,left+panelWidth,48,0xFF263C50);
        g.drawCenteredString(font,"QUESTGEBER • "+islandName.toUpperCase(),this.width/2,29,0xFFFFD36A);
        if(!activeId.isBlank()){
            g.drawCenteredString(font,"Aktive Quest: "+activeId,this.width/2,55,0xFF74D8FF);
        } else {
            g.drawCenteredString(font,"Wähle deine nächste Aufgabe",this.width/2,55,0xFFDCE8F2);
        }
        for(int i=0;i<ids.length;i++){
            int y=82+i*76;
            g.fill(left+12,y-5,left+panelWidth-12,y+65,0xAA26394B);
            g.drawString(font,titles[i],left+20,y,0xFFFFD36A,false);
            g.drawString(font,descriptions[i],left+20,y+13,0xFFE1EAF2,false);
            String state=completed[i]?"ABGESCHLOSSEN":activeId.equals(ids[i])?
                    "FORTSCHRITT "+Math.min(progress,kills[i])+"/"+kills[i]:
                    !activeId.isBlank()?"GESPERRT – andere Quest aktiv":"Ziel: "+kills[i]+" Gegner";
            g.drawString(font,state,left+20,y+27,completed[i]?0xFF83D69A:0xFFB8D5E8,false);
            g.drawString(font,"Belohnung: "+beli[i]+" Beli  •  "+fruitXp[i]+" Frucht-XP",left+20,y+41,0xFFD9C48C,false);
        }
        super.render(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean isPauseScreen(){return false;}
}
