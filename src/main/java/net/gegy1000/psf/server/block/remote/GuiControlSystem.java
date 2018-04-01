package net.gegy1000.psf.server.block.remote;

import lombok.Getter;
import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiControlSystem extends GuiContainer {
    
    @Nonnull
    private static final ResourceLocation TEXTURE_LOC = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/control_system.png");
    
    @Nonnull
    private static final ResourceLocation PREVIEW_BG = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/preview_bg.png");
    
    @Getter
    private final ContainerControlSystem container;
    
    private GuiScrollingList craftList;
    
    private int selectedCraft = -1;

    private SpacecraftModel model;
    
    @Nonnull
    private Collection<IModule> modules = new ArrayList<>();
    
    private GuiButton buttonBack;
    
    private GuiTextField tfName;

    public GuiControlSystem(ContainerControlSystem inventorySlotsIn) {
        super(inventorySlotsIn);
        this.container = inventorySlotsIn;
        
        xSize = 256;
        ySize = 201;
    }
    
    @Override
    public void initGui() {
        super.initGui();

        craftList = new GuiCraftList(this, mc, xSize - 20, ySize - 10, guiTop + 10, guiTop + ySize - 10, guiLeft + 10, 20, width, height);
        
        buttonBack = new GuiButtonExt(0, guiLeft + xSize - 50 - 10, guiTop + ySize - 20 - 10, 50, 20, "Back");
        buttonBack.visible = false;
        addButton(buttonBack);
        
        tfName = new GuiTextField(1, mc.fontRenderer, guiLeft + (xSize / 2), guiTop + 10, 115, 20);
        IListedSpacecraft craft = getCraft();
        if (craft != null) {
            tfName.setText(craft.getName());
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if (this.craftList != null)
            this.craftList.handleMouseInput(mouseX, mouseY);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (this.tfName != null) {
            this.tfName.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.tfName != null) {
            if (this.tfName.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (selectedCraft >= 0) {
            updateName();
        }
    }
    
    private void updateName() {
        IListedSpacecraft craft = getCraft();
        if (craft != null) {
            craft.setName(tfName.getText());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button == buttonBack) {
            updateName();
            selectedCraft = -1;
            buttonBack.visible = false;
            model = null;
            modules.clear();
            tfName.setText("");
        }
    }
    
    private void drawAlphaTexturedRect(int x, int y, int tx, int ty, int w, int h, float alpha) {
        float px = 1 / 256f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + 0, y + h, this.zLevel).tex((tx + 0) * px, (ty + h) * px).color(1, 1, 1, alpha).endVertex();
        bufferbuilder.pos(x + w, y + h, this.zLevel).tex((tx + w) * px, (ty + h) * px).color(1, 1, 1, alpha).endVertex();
        bufferbuilder.pos(x + w, y + 0, this.zLevel).tex((tx + w) * px, ty * px).color(1, 1, 1, alpha).endVertex();
        bufferbuilder.pos(x + 0, y + 0, this.zLevel).tex((tx + 0) * px, ty * px).color(1, 1, 1, alpha).endVertex();
        tessellator.draw();
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(TEXTURE_LOC);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (selectedCraft >= 0 && model != null) {
            IListedSpacecraft craft = getCraft();
            if (craft == null) {
                return;
            }
            
            BlockPos from = model.getRenderWorld().getMinPos();
            BlockPos to = model.getRenderWorld().getMaxPos();
            AxisAlignedBB bb = new AxisAlignedBB(new Vec3d(from), new Vec3d(to).addVector(1, 1, 1));

            drawRect(guiLeft + 9, guiTop + 9, guiLeft + (xSize / 2) - 9, guiTop + ySize - 9, 0xFF8A8A8A);
            GlStateManager.color(1, 1, 1);
            
            mc.getTextureManager().bindTexture(PREVIEW_BG);
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
            int craftY = craft.getPosition().getY();
            float alpha = 0;
            if (craftY > 256) {
                alpha = Math.min((craftY - 256) / 500f, 1);
            }
            drawTexturedModalRect(guiLeft + 10, guiTop + 10, 0, 0, (xSize / 2) - 20, ySize - 20);
            GlStateManager.color(1, 1, 1, alpha);
            drawTexturedModalRect(guiLeft + 10, guiTop + 10, 128, 0, (xSize / 2) - 20, ySize - 20);
            GlStateManager.color(1, 1, 1);

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            
            double lengthX = (bb.maxX - bb.minX) * 16;
            double lengthY = (bb.maxY - bb.minY) * 16;
            double lengthZ = (bb.maxZ - bb.minZ) * 16;

            double halfX = lengthX / 2;
            double halfY = lengthY / 2;
            double halfZ = lengthZ / 2;
            
            final double maxW = 6 * 16;
            final double maxH = 11 * 16;
            
            double overW = Math.max(lengthX - maxW, lengthZ - maxW);
            double overH = lengthY - maxH;
            
            double sc = 1;
            
            if (overW > 0 && overW > overH) {
                sc = maxW / (overW + maxW);
            } else if (overH > 0 && overH > overW) {
                sc = maxH / (overH + maxH);
            }
            
//            halfX *= sc;
//            halfY *= sc;
//            halfZ *= sc;
            
            GlStateManager.translate(guiLeft + halfX + (xSize / 4), guiTop + halfY + (ySize / 2), 500);
            GlStateManager.rotate(0, 1, 0, 0);
            
            BlockPos min = model.getRenderWorld().getMinPos();

            GlStateManager.translate(-halfX, -halfY, -halfZ);
            GlStateManager.rotate(mc.world.getTotalWorldTime() + mc.getRenderPartialTicks(), 0, 1, 0);       

            GlStateManager.translate(halfX, halfY, halfZ);

            GlStateManager.translate(min.getX() * 16, min.getY() * 16, min.getZ() * 16);

            GlStateManager.scale(-16, -16, -16);
            
            GlStateManager.scale(sc, sc, sc);

            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            model.render(BlockRenderLayer.SOLID);

            GlStateManager.enableAlpha();
            model.render(BlockRenderLayer.CUTOUT_MIPPED);
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            model.render(BlockRenderLayer.CUTOUT);
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

            GlStateManager.enableBlend();
            model.render(BlockRenderLayer.TRANSLUCENT);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            
            tfName.drawTextBox();
            
            int x = guiLeft + (xSize / 2);
            int y = guiTop + 35;
            int color = 0xFF333333;
            mc.fontRenderer.drawString("Modules:", x, y, color);
            x += 10;
            y += 10;
            Map<ResourceLocation, List<IModule>> grouped = modules.stream().collect(Collectors.groupingBy(IModule::getRegistryName));
            for (val e : grouped.entrySet()) {
                mc.fontRenderer.drawString(e.getKey() + ": " + e.getValue().size(), x, y, color);
                y += 10;
            }
            x -= 10;
            y += 5;
            int energy = modules.stream()
                    .filter(m -> m instanceof IEnergyStorage)
                    .map(m -> (IEnergyStorage) m)
                    .reduce(0, (e, m) -> e + m.getEnergyStored(), (a, b) -> a + b);
            mc.fontRenderer.drawString("Energy Stored: " + energy, x, y, color);
            y += 15;
            mc.fontRenderer.drawString("Position:", x, y, color);
            BlockPos pos = craft.getPosition();
            x += 5;
            y += 10;
            mc.fontRenderer.drawString("X: " + pos.getX(), x, y, color);
            y += 10;
            mc.fontRenderer.drawString("Y: " + pos.getY(), x, y, color);
            y += 10;
            mc.fontRenderer.drawString("Z: " + pos.getZ(), x, y, color);
            
        } else {
            craftList.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
    
    private @Nullable IListedSpacecraft getCraft() {
        if (selectedCraft >= 0) {
            return container.getTe().getCrafts().get(selectedCraft);
        }
        return null;
    }

    public void selectCraft(int index) {
        this.selectedCraft = index;
        IListedSpacecraft craft = getCraft();
        craft.requestVisualData();
        buttonBack.visible = true;
        tfName.setText(craft.getName());
    }

    public void setVisual(IListedSpacecraft.Visual visual) {
        model = SpacecraftModel.build(visual.getBlockAccess());
        modules = visual.getModules();
    }
}
