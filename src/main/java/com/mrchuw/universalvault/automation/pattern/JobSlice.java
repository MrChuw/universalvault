package com.mrchuw.universalvault.automation.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;

public record JobSlice(
        UUID sliceId,
        UUID patternId,
        RecipeView recipe,
        ItemKey output,
        int units,
        Map<ItemKey, Long> reservedIngredients,
        Map<ItemKey, Long> buffer,
        @Nullable BlockPos stationPos,
        State state
) {

    public enum State { WAITING, ACTIVE, DONE }

    public static final Codec<JobSlice> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("slice_id").forGetter(JobSlice::sliceId),
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("pattern_id").forGetter(JobSlice::patternId),
            RecipeView.CODEC.fieldOf("recipe").forGetter(JobSlice::recipe),
            ItemKey.CODEC.fieldOf("output").forGetter(JobSlice::output),
            Codec.INT.fieldOf("units").forGetter(JobSlice::units),
            kvMapCodec().fieldOf("reserved").forGetter(JobSlice::reservedIngredients),
            kvMapCodec().optionalFieldOf("buffer", Map.of()).forGetter(JobSlice::buffer),
            BlockPos.CODEC.optionalFieldOf("station")
                    .forGetter(s -> Optional.ofNullable(s.stationPos())),
            Codec.STRING.xmap(State::valueOf, Enum::name)
                    .optionalFieldOf("state", State.WAITING).forGetter(JobSlice::state)
    ).apply(inst, (sliceId, patternId, recipe, output, units, reserved, buffer, station, state) ->
            new JobSlice(sliceId, patternId, recipe, output, units, reserved, buffer,
                    station.orElse(null), state)));

    public JobSlice {
        reservedIngredients = Map.copyOf(reservedIngredients);
        buffer = Map.copyOf(buffer);
    }
    public JobSlice withState(State s) {
        return new JobSlice(sliceId, patternId, recipe, output, units,
                reservedIngredients, buffer, stationPos, s);
    }

    public JobSlice withStation(@Nullable BlockPos pos) {
        return new JobSlice(sliceId, patternId, recipe, output, units,
                reservedIngredients, buffer, pos, state);
    }

    public JobSlice withBuffer(Map<ItemKey, Long> newBuffer) {
        return new JobSlice(sliceId, patternId, recipe, output, units,
                reservedIngredients, newBuffer, stationPos, state);
    }

    private static Codec<Map<ItemKey, Long>> kvMapCodec() {
        return Kv.CODEC.listOf().xmap(
                list -> {
                    Map<ItemKey, Long> m = new HashMap<>();
                    for (Kv kv : list) m.put(kv.key(), kv.value());
                    return m;
                },
                map -> {
                    var l = new java.util.ArrayList<Kv>(map.size());
                    for (var e : map.entrySet()) l.add(new Kv(e.getKey(), e.getValue()));
                    return l;
                });
    }

    private record Kv(ItemKey key, long value) {
        static final Codec<Kv> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemKey.CODEC.fieldOf("k").forGetter(Kv::key),
                Codec.LONG.fieldOf("v").forGetter(Kv::value)
        ).apply(inst, Kv::new));
    }
}