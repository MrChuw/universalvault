package com.mrchuw.universalvault.automation.tracking;

import com.mrchuw.universalvault.config.VaultConfig;
import net.minecraft.server.level.ServerLevel;

public final class AdaptiveVelocityTracker {

    public static final int BUCKET_COUNT = 30;
    private static final double DECAY_PER_SECOND = 0.85;

    private AdaptiveVelocityTracker() {}

    public static void recordPlayerExtract(ServerLevel level, long amount) {
        if (amount <= 0) return;

        TrackingData tracking = TrackingData.get(level);
        TrackingData.VelocityState s = tracking.getVelocity();

        long[] buckets = s.buckets().clone();
        int head = s.head();

        int second = (int) (level.getGameTime() % BUCKET_COUNT);
        if (second != head) {
            buckets[second] = 0L;
            head = second;
        }
        buckets[head] += amount;

        tracking.setVelocity(new TrackingData.VelocityState(
                s.currentMultiplier(), buckets, head, s.lastDecayTick()));
    }

    public static double update(ServerLevel level, long gameTime) {
        TrackingData tracking = TrackingData.get(level);
        TrackingData.VelocityState s = tracking.getVelocity();

        long sum = 0;
        for (long b : s.buckets()) sum += b;

        double rate = sum / (double) BUCKET_COUNT;
        double baseline = VaultConfig.get().baselineExtractionRate();
        if (baseline <= 0) baseline = 1.0;

        double target = Math.max(1.0, rate / baseline);
        double max = VaultConfig.get().maxVelocityMultiplier();
        target = Math.min(target, max);

        double current = s.currentMultiplier();
        long lastDecay = s.lastDecayTick();

        if (target > current) {
            current = target;
            lastDecay = gameTime;
        } else {
            long elapsed = gameTime - lastDecay;
            if (elapsed > 0) {
                current = Math.max(1.0,
                        current * Math.pow(DECAY_PER_SECOND, elapsed));
                lastDecay = gameTime;
            } else if (lastDecay == 0L) {
                lastDecay = gameTime;
            }
        }

        tracking.setVelocity(new TrackingData.VelocityState(
                current, s.buckets(), s.head(), lastDecay));
        return current;
    }

    public static double current(ServerLevel level) {
        return TrackingData.get(level).getVelocity().currentMultiplier();
    }
}
