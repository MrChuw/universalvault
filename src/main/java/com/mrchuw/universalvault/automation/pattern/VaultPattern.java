package com.mrchuw.universalvault.automation.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.Optional;
import java.util.UUID;

public record VaultPattern(
        UUID patternId,
        RecipeView recipe,
        long min,
        long max,
        int batch,
        int priority,
        TriggerMode triggerMode,
        boolean paused,
        PushMode pushMode,
        Modifiers modifiers
) {

    public enum TriggerMode {
        CONTINUOUS,
        BATCH_HYSTERESIS,
        MANUAL;

        public static final Codec<TriggerMode> CODEC = Codec.STRING.xmap(
                s -> TriggerMode.valueOf(s.toUpperCase(java.util.Locale.ROOT)),
                TriggerMode::name
        );
    }

    public enum PushMode {
        PUSH_ALL,
        WAIT_RESULT;

        public static final Codec<PushMode> CODEC = Codec.STRING.xmap(
                s -> PushMode.valueOf(s.toUpperCase(java.util.Locale.ROOT)),
                PushMode::name
        );
    }

    public record Modifiers(
            boolean adaptiveVelocityScaling,
            boolean ignoreNbt,
            Optional<Double> minDurabilityPct,
            boolean atomic
    ) {
        public static final Modifiers DEFAULT =
                new Modifiers(false, false, Optional.empty(), false);

        public static final Codec<Modifiers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("adaptive_velocity", false)
                        .forGetter(Modifiers::adaptiveVelocityScaling),
                Codec.BOOL.optionalFieldOf("ignore_nbt", false)
                        .forGetter(Modifiers::ignoreNbt),
                Codec.DOUBLE.optionalFieldOf("min_durability_pct")
                        .forGetter(Modifiers::minDurabilityPct),
                Codec.BOOL.optionalFieldOf("atomic", false)
                        .forGetter(Modifiers::atomic)
        ).apply(inst, Modifiers::new));

        public Modifiers withMinDurabilityPct(Optional<Double> v) {
            return new Modifiers(adaptiveVelocityScaling, ignoreNbt, v, atomic);
        }
    }

    public static final Codec<VaultPattern> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("pattern_id").forGetter(VaultPattern::patternId),
            RecipeView.CODEC.fieldOf("recipe").forGetter(VaultPattern::recipe),
            Codec.LONG.optionalFieldOf("min", 0L).forGetter(VaultPattern::min),
            Codec.LONG.optionalFieldOf("max", 0L).forGetter(VaultPattern::max),
            Codec.INT.optionalFieldOf("batch", 0).forGetter(VaultPattern::batch),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(VaultPattern::priority),
            TriggerMode.CODEC.optionalFieldOf("trigger", TriggerMode.BATCH_HYSTERESIS)
                    .forGetter(VaultPattern::triggerMode),
            Codec.BOOL.optionalFieldOf("paused", true).forGetter(VaultPattern::paused),
            PushMode.CODEC.optionalFieldOf("push_mode", PushMode.PUSH_ALL)
                    .forGetter(VaultPattern::pushMode),
            Modifiers.CODEC.optionalFieldOf("modifiers", Modifiers.DEFAULT)
                    .forGetter(VaultPattern::modifiers)
    ).apply(inst, VaultPattern::new));

    public VaultPattern {
        if (max < min) max = min;
        if (batch < 0) batch = 0;

        Optional<Double> pct = modifiers.minDurabilityPct();
        if (pct.isPresent()) {
            double v = pct.get();
            double clamped = Math.max(0.0, Math.min(100.0, v));
            if (clamped != v) {
                modifiers = modifiers.withMinDurabilityPct(Optional.of(clamped));
            }
        }
    }

    public ItemKey output() {
        return recipe.primaryOutput();
    }

    public int outputCount() {
        return recipe.primaryOutputCount();
    }

    public VaultPattern toggledPause() {
        return new VaultPattern(patternId, recipe, min, max, batch, priority,
                triggerMode, !paused, pushMode, modifiers);
    }
}