package codes.biscuit.skyblockaddons.asm.hooks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

public class RendererLivingEntityHook {

    private static boolean isCoolPerson;

    public static boolean equals(String s, Object anObject) {
        isCoolPerson = s.equals("Biscut") || s.equals("Pinpointed");
        // no don't ask to be added lol
        return s.equals("Dinnerbone") || isCoolPerson;
    } //cough nothing to see here

    public static  boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_));
    }
}
