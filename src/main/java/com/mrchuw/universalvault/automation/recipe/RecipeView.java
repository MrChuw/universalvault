package com.mrchuw.universalvault.automation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public sealed interface RecipeView {

    String id();

    ItemKey primaryOutput();

    int primaryOutputCount();

    record Crafting(
            String id,
            Map<Integer, ItemKey> grid,
            ItemKey output,
            int outputCount
    ) implements RecipeView {

        public static final MapCodec<Crafting> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(Crafting::id),
                Codec.unboundedMap(
                        Codec.STRING.xmap(Integer::parseInt, String::valueOf),
                        ItemKey.CODEC
                ).fieldOf("grid").forGetter(Crafting::grid),
                ItemKey.CODEC.fieldOf("output").forGetter(Crafting::output),
                Codec.INT.optionalFieldOf("output_count", 1).forGetter(Crafting::outputCount)
        ).apply(inst, Crafting::new));

        public static final Codec<Crafting> CODEC = MAP_CODEC.codec();

        public Crafting {
            grid = Map.copyOf(grid);
        }

        @Override public ItemKey primaryOutput() { return output; }
        @Override public int primaryOutputCount() { return outputCount; }
    }

    record SlotSpec(int slotIndex, ItemKey item, int count) {
        public static final Codec<SlotSpec> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("slot").forGetter(SlotSpec::slotIndex),
                ItemKey.CODEC.fieldOf("item").forGetter(SlotSpec::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(SlotSpec::count)
        ).apply(inst, SlotSpec::new));
    }

    record Custom(
            String id,
            List<SlotSpec> inputs,
            List<SlotSpec> outputs
    ) implements RecipeView {

        public static final MapCodec<Custom> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(Custom::id),
                SlotSpec.CODEC.listOf().fieldOf("inputs").forGetter(Custom::inputs),
                SlotSpec.CODEC.listOf().fieldOf("outputs").forGetter(Custom::outputs)
        ).apply(inst, Custom::new));

        public static final Codec<Custom> CODEC = MAP_CODEC.codec();

        public Custom {
            inputs = List.copyOf(inputs);
            outputs = List.copyOf(outputs);
            if (inputs.isEmpty()) throw new IllegalArgumentException("custom recipe without inputs");
            if (outputs.isEmpty()) throw new IllegalArgumentException("custom recipe without outputs");
        }

        @Override public ItemKey primaryOutput() { return outputs.get(0).item(); }
        @Override public int primaryOutputCount() { return outputs.get(0).count(); }
    }

    Codec<RecipeView> CODEC = Codec.STRING.dispatch(
            "type",
            view -> view instanceof Crafting ? "crafting" : "custom",
            type -> switch (type) {
                case "crafting" -> Crafting.MAP_CODEC.xmap(v -> (RecipeView) v, v -> (Crafting) v);
                case "custom" -> Custom.MAP_CODEC.xmap(v -> (RecipeView) v, v -> (Custom) v);
                default -> throw new IllegalArgumentException("unknown recipe type: " + type);
            }
    );

    default Map<ItemKey, Long> aggregateInputs() {
        Map<ItemKey, Long> out = new HashMap<>();
        if (this instanceof Crafting c) {
            for (ItemKey k : c.grid().values()) {
                out.merge(k, 1L, Long::sum);
            }
        } else if (this instanceof Custom c) {
            for (SlotSpec s : c.inputs()) {
                out.merge(s.item(), (long) s.count(), Long::sum);
            }
        }
        return out;
    }
}