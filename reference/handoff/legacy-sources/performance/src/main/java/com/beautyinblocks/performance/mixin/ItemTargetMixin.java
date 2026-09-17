package com.beautyinblocks.performance.mixin;
import com.beautyinblocks.performance.*;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(targets="com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems",remap=false)
public class ItemTargetMixin {
    @Redirect(method="m_8036_",at=@At(value="INVOKE",target="Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V",remap=false),remap=false)
    private void choose(List<Object> candidates,Comparator<Object> comparator) {
        if(Performance.ITEM_SELECTION.get()) NearestSelection.moveMinimumFirst(candidates,comparator);
        else candidates.sort(comparator);
    }
}
