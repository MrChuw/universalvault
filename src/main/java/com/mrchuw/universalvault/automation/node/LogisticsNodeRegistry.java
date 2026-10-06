package com.mrchuw.universalvault.automation.node;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LogisticsNodeRegistry {

    private static final Set<LogisticsNodeBlockEntity> NODES = ConcurrentHashMap.newKeySet();

    private LogisticsNodeRegistry() {}

    public static void register(LogisticsNodeBlockEntity node) {
        NODES.add(node);
    }

    public static void unregister(LogisticsNodeBlockEntity node) {
        NODES.remove(node);
    }

    public static Iterable<LogisticsNodeBlockEntity> all() {
        return NODES;
    }

    public static int count() {
        return NODES.size();
    }
}