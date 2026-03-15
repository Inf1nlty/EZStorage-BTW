package com.zerofall.ezstorage.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.GuiButton;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.network.C2S.C2SClearCraftingGridPacket;
import emi.dev.emi.emi.network.EmiNetwork;

import org.lwjgl.opengl.GL11;

public class GuiCraftingCore extends GuiStorageCore {

    private static final int OUTPUT_SLOT_VIS_X = 118;
    private static final int OUTPUT_SLOT_VIS_Y = 132;
    private static final int CLEAR_BTN_REL_X = 99;
    private static final int CLEAR_BTN_REL_Y = 114;
    private static final int CLEAR_BTN_W = 8;
    private static final int CLEAR_BTN_H = 8;

    protected GuiButton btnClearCraftingPanel;

    public GuiCraftingCore(EntityPlayer player, World world, int x, int y, int z) {
        super(new ContainerStorageCoreCrafting(player, world, x, y, z), world, x, y, z);
        this.xSize = 195;
        this.ySize = 256;
    }

    public GuiCraftingCore(ContainerStorageCoreCrafting container, World world, int x, int y, int z) {
        super(container, world, x, y, z);
        this.xSize = 195;
        this.ySize = 256;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        btnClearCraftingPanel = new GuiButton(10, -100, -100, 1, 1, "");
        buttonList.add(btnClearCraftingPanel);
    }

    /** Returns true if the mouse is over the clear button. */
    private boolean isOverClearBtn(int mouseX, int mouseY) {
        int bx = this.guiLeft + CLEAR_BTN_REL_X;
        int by = this.guiTop + CLEAR_BTN_REL_Y;
        return mouseX >= bx && mouseX < bx + CLEAR_BTN_W
            && mouseY >= by && mouseY < by + CLEAR_BTN_H;
    }

    /** Draw the clear button using the side-button background texture (same as the 4 side buttons). */
    private void drawClearButton(int mouseX, int mouseY)
    {
        int bx = this.guiLeft + CLEAR_BTN_REL_X;
        int by = this.guiTop + CLEAR_BTN_REL_Y;
        boolean hovered = isOverClearBtn(mouseX, mouseY);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        this.mc.getTextureManager().bindTexture(resSideButton);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(bx, by + CLEAR_BTN_H, this.zLevel, 0.0, 1.0);
        tessellator.addVertexWithUV(bx + CLEAR_BTN_W, by + CLEAR_BTN_H, this.zLevel, 1.0, 1.0);
        tessellator.addVertexWithUV(bx + CLEAR_BTN_W, by, this.zLevel, 1.0, 0.0);
        tessellator.addVertexWithUV(bx, by, this.zLevel, 0.0, 0.0);
        tessellator.draw();

        if (hovered)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
            tessellator.startDrawingQuads();
            tessellator.addVertex(bx, by + CLEAR_BTN_H, this.zLevel);
            tessellator.addVertex(bx + CLEAR_BTN_W, by + CLEAR_BTN_H, this.zLevel);
            tessellator.addVertex(bx + CLEAR_BTN_W, by, this.zLevel);
            tessellator.addVertex(bx, by, this.zLevel);
            tessellator.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Draw clear button
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        drawClearButton(mouseX, mouseY);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0 && isOverClearBtn(mouseX, mouseY))
        {
            EmiNetwork.sendToServer(new C2SClearCraftingGridPacket());
            this.mc.sndManager.playSound("random.click", (float) this.mc.thePlayer.posX, (float) this.mc.thePlayer.posY, (float) this.mc.thePlayer.posZ, 0.25F, 1.0F);
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderMouseOverTooltip(int mouseX, int mouseY)
    {
        super.renderMouseOverTooltip(mouseX, mouseY);

        if (!(inventorySlots instanceof ContainerStorageCoreCrafting container)) return;

        int x = this.guiLeft + OUTPUT_SLOT_VIS_X;
        int y = this.guiTop + OUTPUT_SLOT_VIS_Y;

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
        {
            ItemStack result = container.craftResult.getStackInSlot(0);

            if (result != null)
            {
                List<String> tooltip = new ArrayList<String>(result.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips));
                func_102021_a(tooltip, mouseX, mouseY);
            }
            return;
        }

        for (int i = 0; i < 9; i++)
        {
            int col = i % 3;
            int row = i / 3;
            int sx = this.guiLeft + 44 + col * 18;
            int sy = this.guiTop + 114 + row * 18;

            if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16)
            {
                ItemStack stack = container.craftMatrix.getStackInSlot(i);

                if (stack != null)
                {
                    func_102021_a(stack.getTooltip(this.mc.thePlayer,
                            this.mc.gameSettings.advancedItemTooltips),
                            mouseX, mouseY);
                }
                return;
            }
        }
    }


    @Override
    public int rowsVisible() {
        return 5;
    }

    @Override
    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageCraftingGui.png");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Handled via mouseClicked + direct packet send
    }
}