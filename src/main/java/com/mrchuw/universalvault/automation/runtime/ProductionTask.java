package com.mrchuw.universalvault.automation.runtime;

import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public record ProductionTask(
        UUID taskId,
        UUID patternId,
        RecipeView recipe,
        ItemKey output,
        int remaining,
        Map<ItemKey, Long> reservedIngredients,
        RecipeKind kind
) {

    public ProductionTask {
        reservedIngredients = Map.copyOf(reservedIngredients);
    }

    public ProductionTask consumeOne() {
        int r = remaining - 1;
        return r <= 0 ? null
                : new ProductionTask(taskId, patternId, recipe, output, r,
                reservedIngredients, kind);
    }

    public ProductionTask consumeN(int n) {
        if (n <= 0) return this;
        int r = remaining - n;
        return r <= 0 ? null
                : new ProductionTask(taskId, patternId, recipe, output, r,
                reservedIngredients, kind);
    }

    @Nullable
    public static ProductionTask fromPlanned(
            com.mrchuw.universalvault.automation.planning.PlannedTask p) {
        if (p == null) return null;
        return new ProductionTask(
                p.taskId(), p.patternId(), p.recipe(), p.output(), p.units(),
                p.reservedIngredients(), p.kind());
    }
}
