package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class GuiContainerHook {

    private static final int OVERLAY_RED = ConfigColor.RED.getColor(127);
    private static final int OVERLAY_GREEN = ConfigColor.GREEN.getColor(127);

    private static ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static EnchantPair reforgeToRender = null;
    private static Set<EnchantPair> enchantsToRender = new HashSet<>();

    public static void showEnchantments(Slot slotIn, int x, int y, ItemStack item) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.SHOW_ENCHANTMENTS_REFORGES)) {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRendererObj;
            if (item != null && item.hasDisplayName()) {
                if (item.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                    List<String> toolip = item.getTooltip(mc.thePlayer, false);
                    if (toolip.size() > 2) {
                        String enchantLine = toolip.get(2);
                        String[] lines = enchantLine.split(Pattern.quote("* "));
                        if (lines.length >= 2) {
                            String toMatch = lines[1];
                            String enchant;
                            if (main.getUtils().getEnchantmentMatch().size() > 0 &&
                                    main.getUtils().enchantReforgeMatches(toMatch)) {
                                enchant = EnumChatFormatting.RED + toMatch;
                            } else {
                                enchant = EnumChatFormatting.YELLOW + toMatch;
                            }
                            float yOff;
                            if (slotIn.slotNumber == 29 || slotIn.slotNumber == 33) {
                                yOff = 26;
                            } else {
                                yOff = 36;
                            }
                            float scaleMultiplier = 1 / 0.75F;
                            float halfStringWidth = fr.getStringWidth(enchant) / 2;
                            x += 8; // to center it
                            enchantsToRender.add(new EnchantPair(x * scaleMultiplier - halfStringWidth, y * scaleMultiplier + yOff, enchant));
                        }
                    }
                } else if (slotIn.inventory.getDisplayName().getUnformattedText().equals("Reforge Item") && slotIn.slotNumber == 13) {
                    String reforge = main.getUtils().getReforgeFromItem(item);
                    if (reforge != null) {
                        if (main.getUtils().getEnchantmentMatch().size() > 0 &&
                                main.getUtils().enchantReforgeMatches(reforge)) {
                            reforge = EnumChatFormatting.RED + reforge;
                        } else {
                            reforge = EnumChatFormatting.YELLOW + reforge;
                        }
                        x -= 28;
                        y += 22;
                        float halfStringWidth = fr.getStringWidth(reforge) / 2;
                        reforgeToRender = new EnchantPair(x - halfStringWidth, y, reforge);
                    }
                }
            }
            if (slotIn.slotNumber == 53) {
                GlStateManager.pushMatrix();

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                if (reforgeToRender != null) {
                    fr.drawStringWithShadow(reforgeToRender.getEnchant(), reforgeToRender.getX(), reforgeToRender.getY(), new Color(255, 255, 255, 255).getRGB());
                    reforgeToRender = null;
                }
                GlStateManager.scale(0.75, 0.75, 1);
                Iterator<EnchantPair> enchantPairIterator = enchantsToRender.iterator();
                while (enchantPairIterator.hasNext()) {
                    EnchantPair enchant = enchantPairIterator.next();
                    fr.drawStringWithShadow(enchant.getEnchant(), enchant.getX(), enchant.getY(), new Color(255, 255, 255, 255).getRGB());
                    enchantPairIterator.remove();
                }
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();

                GlStateManager.popMatrix();
            }
        }
    }

    public static void drawBackpacks(GuiContainer guiContainer, FontRenderer fontRendererObj) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();
        Minecraft mc = Minecraft.getMinecraft();
        if (backpack != null) {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI) {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length/9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                int textColor = 4210752;
                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)) {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    textColor = color.getTextColor();
                }
                guiContainer.drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                guiContainer.drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                fontRendererObj.drawString(backpack.getBackpackName(), x+8, y+6, textColor);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = x+8 + ((i % 9) * 18);
                        int itemY = y+18 + ((i / 9) * 18);
                        RenderItem renderItem = mc.getRenderItem();
                        setZLevel(guiContainer, 200);
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        setZLevel(guiContainer, 0);
                        renderItem.zLevel = 0;
                    }
                }
            } else {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + (16 * 9) + 3, y + (16 * (length / 9)) + 3, ConfigColor.DARK_GRAY.getColor(250));
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = x + ((i % 9) * 16);
                        int itemY = y + ((i / 9) * 16);
                        RenderItem renderItem = mc.getRenderItem();
                        setZLevel(guiContainer, 200);
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        setZLevel(guiContainer, 0);
                        renderItem.zLevel = 0;
                    }
                }
            }
            main.getUtils().setBackpackToRender(null);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public static void setLastSlot() {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    public static void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor, Slot theSlot) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        net.minecraft.inventory.Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if (theSlot != null) {
            int slotNum = theSlot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                drawRightGradientRect(guiContainer, left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
                return;
            }
        }
        drawRightGradientRect(guiContainer, left, top, right, bottom, startColor, endColor);
    }

    public static void drawSlot(GuiContainer guiContainer, Slot slot) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        Container container = mc.thePlayer.openContainer;

        if(slot != null) {
            // Draw crafting pattern overlays inside the crafting grid
            if(main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS) && main.getUtils().isOnSkyblock()
                    && slot.inventory.getDisplayName().getUnformattedText().equals(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME)
                    && CraftingPatternSelection.selectedPattern != CraftingPattern.FREE) {

                int craftingGridIndex = CraftingPattern.slotToCraftingGridIndex(slot.getSlotIndex());
                if(craftingGridIndex >= 0) {
                    int slotLeft = slot.xDisplayPosition;
                    int slotTop = slot.yDisplayPosition;
                    int slotRight = slotLeft + 16;
                    int slotBottom = slotTop + 16;
                    if(CraftingPatternSelection.selectedPattern.isSlotInPattern(craftingGridIndex)) {
                        if(!slot.getHasStack()) {
                            drawRightGradientRect(guiContainer, slotLeft, slotTop, slotRight, slotBottom, OVERLAY_GREEN, OVERLAY_GREEN);
                        }
                    } else {
                        if(slot.getHasStack()) {
                            drawRightGradientRect(guiContainer, slotLeft, slotTop, slotRight, slotBottom, OVERLAY_RED, OVERLAY_RED);
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock()) {
                int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
                if (main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1,1,1,0.4F);
                    GlStateManager.enableBlend();
                    Minecraft.getMinecraft().getTextureManager().bindTexture(RenderListener.LOCK);
                    mc.ingameGUI.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    private static Method drawGradientRect = null;

    private static void drawRightGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor) {
        if (SkyblockAddonsTransformer.isLabymodClient()) { // There are no access transformers in labymod.
            try {
                if (drawGradientRect == null) {
                    drawGradientRect = guiContainer.getClass().getSuperclass().getSuperclass().getDeclaredMethod("a", int.class, int.class, int.class, int.class, int.class, int.class);
                    drawGradientRect.setAccessible(true);
                }
                if (drawGradientRect != null) {
                    drawGradientRect.invoke(guiContainer, left, top, right, bottom, startColor, endColor);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            guiContainer.drawGradientRect(left, top, right, bottom, startColor, endColor);
        }
    }

    public static void keyTyped(GuiContainer guiContainer, int keyCode, Slot theSlot, ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())) {
                int slot = main.getUtils().getLastHoveredSlot();
                if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                        }
                    }
                }
                if (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5) {
                    if (main.getConfigValues().getLockedSlots().contains(slot)) {
                        if (main.getLockSlot().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        } else {
                            main.getUtils().playLoudSound("note.bass", 0.5);
                            returnValue.cancel(); // slot is locked
                            return;
                        }
                    } else {
                        if (main.getLockSlot().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 0.1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (main.getInventoryUtils().shouldCancelDrop(theSlot)) returnValue.cancel();
            }
        }
    }

    private static Field zLevel = null;

    private static void setZLevel(Gui gui, int zLevelToSet) {
        if (SkyblockAddonsTransformer.isLabymodClient()) { // There are no access transformers in labymod.
            try {
                if (zLevel == null) {
                    zLevel = gui.getClass().getDeclaredField("e");
                    zLevel.setAccessible(true);
                }
                if (zLevel != null) {
                    zLevel.set(gui, zLevelToSet);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            gui.zLevel = zLevelToSet;
        }
    }
}
