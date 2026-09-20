package com.mrchuw.universalvault.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public record ItemKey(Identifier itemId, DataComponentPatch components) {

    public static final Codec<ItemKey> CODEC = RecordCodecBuilder.create(instance ->
            instance
                    .group(
                            Identifier.CODEC.fieldOf("item").forGetter(ItemKey::itemId),
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                    .forGetter(ItemKey::components)
                    )
                    .apply(instance, ItemKey::new)
    );

    public static @Nullable ItemKey of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new ItemKey(id, stack.getComponentsPatch());
    }

    /** Resolves the backing Item, or null if the mod that registered it is absent. */
    public @Nullable Item resolveItem() {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        return (item == Items.AIR) ? null : item;
    }

    /** True while the mod that registered this item is present; false means it is orphaned. */
    public boolean isResolved() {
        return resolveItem() != null;
    }

    public ItemStack toStack(int count) {
        if (count <= 0) return ItemStack.EMPTY;
        Item item = resolveItem();
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponentsAndValidate(components);
        return stack;
    }
}
