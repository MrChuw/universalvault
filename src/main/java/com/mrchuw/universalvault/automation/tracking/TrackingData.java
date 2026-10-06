package com.mrchuw.universalvault.automation.tracking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class TrackingData extends SavedData {

    private static final int CURRENT_SCHEMA = 1;
    private static final String DATA_NAME = UniversalVault.MOD_ID + "_tracking";

    public record VelocityState(
            double currentMultiplier,
            long[] buckets,
            int head,
            long lastDecayTick
    ) {
        public static final Codec<VelocityState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.fieldOf("multiplier").forGetter(VelocityState::currentMultiplier),
                Codec.LONG.listOf().xmap(
                        list -> list.stream().mapToLong(Long::longValue).toArray(),
                        arr -> {
                            List<Long> out = new ArrayList<>(arr.length);
                            for (long v : arr) out.add(v);
                            return out;
                        }
                ).fieldOf("buckets").forGetter(VelocityState::buckets),
                Codec.INT.fieldOf("head").forGetter(VelocityState::head),
                Codec.LONG.fieldOf("last_decay").forGetter(VelocityState::lastDecayTick)
        ).apply(inst, VelocityState::new));

        public static VelocityState empty() {
            return new VelocityState(1.0,
                    new long[AdaptiveVelocityTracker.BUCKET_COUNT],
                    0, 0L);
        }
    }

    private VelocityState velocity = VelocityState.empty();

    public VelocityState getVelocity() { return velocity; }

    public void setVelocity(VelocityState v) {
        this.velocity = v;
        setDirty();
    }

    public static final Codec<TrackingData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(TrackingData::schema),
            VelocityState.CODEC.optionalFieldOf("velocity", VelocityState.empty())
                    .forGetter(TrackingData::getVelocity)
    ).apply(inst, (schema, velocity) -> {
        TrackingData data = new TrackingData();
        data.schema = schema;
        data.velocity = velocity;
        return data;
    }));

    private int schema = CURRENT_SCHEMA;
    public int schema() { return schema; }

    public static final SavedDataType<TrackingData> TYPE = new SavedDataType<>(
            //? if >= 26.1 {
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "tracking"),
            //?} else {
            /*UniversalVault.MOD_ID + "_tracking",
             *///?}
            TrackingData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static TrackingData get(ServerLevel level) {
        //? if >= 26.1 {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
        //?} else {
        /*return level.getDataStorage().computeIfAbsent(TYPE);
         *///?}
    }
}
