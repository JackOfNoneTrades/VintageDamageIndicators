package org.fentanylsolutions.vintagedamageindicators.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPotionEffectsCache {

    private static final Map<Integer, List<EntityPotionEffectsMessage.PotionEntry>> EFFECTS_BY_ENTITY = new ConcurrentHashMap<>();

    private ClientPotionEffectsCache() {}

    public static void put(int entityId, List<EntityPotionEffectsMessage.PotionEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            EFFECTS_BY_ENTITY
                .put(Integer.valueOf(entityId), Collections.<EntityPotionEffectsMessage.PotionEntry>emptyList());
            return;
        }
        EFFECTS_BY_ENTITY.put(Integer.valueOf(entityId), Collections.unmodifiableList(new ArrayList<>(entries)));
    }

    public static List<EntityPotionEffectsMessage.PotionEntry> get(int entityId) {
        return EFFECTS_BY_ENTITY.get(Integer.valueOf(entityId));
    }

    public static void clear() {
        EFFECTS_BY_ENTITY.clear();
    }
}
