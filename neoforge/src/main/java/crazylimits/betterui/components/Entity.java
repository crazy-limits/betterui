package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

@KJSBindings
@LDLRegister(name = "entity", group = "inventory", registry = "ldlib2:ui_element")
public class Entity extends UIElement {

    private int scale = 30;
    private float yOffset = 0.0625f;
    private boolean followsMouse = true;

    @Nullable
    private LivingEntity entity;

    // runtime
//    private float xMouse;
//    private float yMouse;

    public Entity() {
        super();
        getLayout().setWidth(50);
        getLayout().setHeight(70);

//        getRoot().addEventListener(UIEvents.MOUSE_MOVE, this::onMouseMove);
        addEventListener(UIEvents.MUI_CHANGED, this::onModularUIChanged);
    }

    protected void onModularUIChanged(UIEvent event) {
        var mui = getModularUI();
        if (mui != null && event.customData != mui) {
            var root = mui.getAllElements().getFirst();
            if (root.hasEventListener(UIEvents.MOUSE_MOVE, this::onMouseMove)) {
                return;
            }
            root.addEventListener(UIEvents.MOUSE_MOVE, this::onMouseMove);
        }
    }

    public UIElement getRoot() {
        var parent = getParent();
        if (parent == null) {
            return this;
        }
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    public int getScale() {
        return scale;
    }

    public Entity setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public float getYOffset() {
        return yOffset;
    }

    public Entity setYOffset(float yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public boolean isFollowsMouse() {
        return followsMouse;
    }

    public Entity setFollowsMouse(boolean followsMouse) {
        this.followsMouse = followsMouse;
        return this;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    public Entity setEntity(@Nullable LivingEntity entity) {
        this.entity = entity;
        return this;
    }

    public Entity setEntityType(EntityType<? extends LivingEntity> entityType) {
        var mc = Minecraft.getInstance();
        if (mc.level != null) {
            this.entity = entityType.create(mc.level);
        }
        return this;
    }

    protected void onMouseMove(UIEvent event) {
        if (followsMouse) {
//            this.xMouse = (float) event.x;
//            this.yMouse = (float) event.y;
        }
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        super.drawBackgroundAdditional(guiContext);

        if (entity == null) return;

        int x1 = (int) getPositionX();
        int y1 = (int) getPositionY();
        int x2 = (int) (getPositionX() + getSizeWidth());
        int y2 = (int) (getPositionY() + getSizeHeight());

        renderEntityInInventoryFollowsMouse(
                guiContext.graphics,
                x1, y1, x2, y2,
                scale, yOffset,
                followsMouse ? guiContext.mouseX : (x1 + x2) / 2f,
                followsMouse ? guiContext.mouseY : (y1 + y2) / 2f,
                entity
        );
    }

    public static void renderEntityInInventoryFollowsMouse(
            GuiGraphics graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yOffset,
            float mouseX,
            float mouseY,
            LivingEntity entity
    ) {
        float cx = (float) (x1 + x2) / 2.0F;
        float cy = (float) (y1 + y2) / 2.0F;

        float angleX = (float) Math.atan((cx - mouseX) / 40.0F);
        float angleY = (float) Math.atan((cy - mouseY) / 40.0F);

        renderEntityInInventoryFollowsAngle(
                graphics,
                x1,
                y1,
                x2,
                y2,
                scale,
                yOffset,
                angleX,
                angleY,
                entity
        );
    }

    public static void renderEntityInInventoryFollowsAngle(
            GuiGraphics graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yOffset,
            float angleXComponent,
            float angleYComponent,
            LivingEntity entity
    ) {
        float cx = (float) (x1 + x2) / 2.0F;
        float cy = (float) (y1 + y2) / 2.0F;

        graphics.enableScissor(x1, y1, x2, y2);

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf()
                .rotateX(angleYComponent * 20.0F * ((float) Math.PI / 180F));
        pose.mul(camera);

        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;

        entity.yBodyRot = 180.0F + angleXComponent * 20.0F;
        entity.setYRot(180.0F + angleXComponent * 40.0F);
        entity.setXRot(-angleYComponent * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float entityScale = entity.getScale();
        Vector3f translate = new Vector3f(
                0.0F,
                entity.getBbHeight() / 2.0F + yOffset * entityScale,
                0.0F
        );
        float finalScale = (float) scale / entityScale;

        renderEntityInInventory(graphics, cx, cy, finalScale, translate, pose, camera, entity);

        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;

        graphics.disableScissor();
    }

    public static void renderEntityInInventory(
            GuiGraphics graphics,
            float x,
            float y,
            float scale,
            Vector3f translate,
            Quaternionf pose,
            @Nullable Quaternionf cameraOrientation,
            LivingEntity entity
    ) {
        graphics.pose().pushPose();

        graphics.pose().translate((double) x, (double) y, (double) 50.0F);
        graphics.pose().scale(scale, scale, -scale);
        graphics.pose().translate(translate.x, translate.y, translate.z);
        graphics.pose().mulPose(pose);

        // --- Force correct 3D depth state for entity + armor layers ---
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515); // GL_LEQUAL (vanilla default for 3D)
        RenderSystem.depthMask(true);
        // If your UI pipeline uses blending, keep it; armor still needs depth test.
        // RenderSystem.enableBlend();

        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            dispatcher.overrideCameraOrientation(
                    cameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI)
            );
        }

        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() ->
                dispatcher.render(
                        entity,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0F,
                        1.0F,
                        graphics.pose(),
                        graphics.bufferSource(),
                        15728880
                )
        );

        graphics.flush();

        dispatcher.setRenderShadow(true);

        // --- Restore UI-ish state (most UI expects no depth) ---
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        graphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    public void loadXml(Element element) {
        if (element.hasAttribute("scale")) {
            scale = Integer.parseInt(element.getAttribute("scale"));
        }
        if (element.hasAttribute("yOffset")) {
            yOffset = Float.parseFloat(element.getAttribute("yOffset"));
        }
        if (element.hasAttribute("followsMouse")) {
            followsMouse = Boolean.parseBoolean(element.getAttribute("followsMouse"));
        }
        super.loadXml(element);
    }
}