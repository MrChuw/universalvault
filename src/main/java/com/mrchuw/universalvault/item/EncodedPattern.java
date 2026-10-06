package com.mrchuw.universalvault.item;

import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.registry.ModRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.mrchuw.universalvault.storage.ItemKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class EncodedPattern extends Item {

    public EncodedPattern(Properties properties) {
        super(properties);
    }

    public static ItemStack create(VaultPattern pattern) {
        ItemStack stack = new ItemStack(ModRegistry.ENCODED_PATTERN.get());
        stack.set(ModRegistry.ENCODED_PATTERN_DATA.get(), pattern);
        return stack;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nonnull TooltipContext context,
                                @Nonnull TooltipDisplay display,
                                @Nonnull Consumer<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        VaultPattern p = stack.get(ModRegistry.ENCODED_PATTERN_DATA.get());
        if (p == null) {
            tooltip.accept(Component.translatable("item.universal_vault.encoded_pattern.invalid")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack outputStack = p.output().toStack(1);
        Component outputName = outputStack.getHoverName();
        Component outputLine = p.outputCount() > 1
                ? Component.literal(p.outputCount() + " × ").append(outputName)
                : outputName;
        tooltip.accept(Component.literal("→ ").append(outputLine)
                .withStyle(ChatFormatting.WHITE));

        Map<ItemKey, Long> inputs = p.recipe().aggregateInputs();
        if (!inputs.isEmpty()) {
            List<Map.Entry<ItemKey, Long>> sorted = new ArrayList<>(inputs.entrySet());
            sorted.sort(Comparator.comparing(e -> e.getKey().itemId().toString()));

            MutableComponent line = Component.literal("← ");
            int shown = 0;
            int maxShow = 4;
            for (Map.Entry<ItemKey, Long> e : sorted) {
                if (shown > 0) line.append(Component.literal(", "));
                if (shown >= maxShow) {
                    line.append(Component.literal("+" + (sorted.size() - maxShow) + " more"));
                    break;
                }
                ItemStack in = e.getKey().toStack(1);
                line.append(Component.literal(e.getValue() + " × ")).append(in.getHoverName());
                shown++;
            }
            tooltip.accept(line.withStyle(ChatFormatting.GRAY));
        }

        Component status;
        if (p.paused()) {
            status = Component.translatable("item.universal_vault.encoded_pattern.status.paused");
        } else if (p.triggerMode() == VaultPattern.TriggerMode.MANUAL) {
            status = Component.translatable("item.universal_vault.encoded_pattern.status.manual");
        } else {
            status = Component.translatable("item.universal_vault.encoded_pattern.status.auto",
                    p.min(), p.max(), p.batch());
        }
        tooltip.accept(status.copy().withStyle(ChatFormatting.GRAY));

        if (p.priority() != 0) {
            tooltip.accept(Component.translatable("item.universal_vault.encoded_pattern.priority",
                            p.priority())
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.accept(Component.translatable("item.universal_vault.encoded_pattern.kind."
                        + (p.recipe() instanceof com.mrchuw.universalvault.automation.recipe.RecipeView.Crafting
                        ? "crafting" : "custom"))
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
