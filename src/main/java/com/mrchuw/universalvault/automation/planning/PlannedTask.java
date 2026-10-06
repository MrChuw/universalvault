package com.mrchuw.universalvault.automation.planning;

import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlannedTask(
        UUID taskId,
        UUID patternId,
        RecipeView recipe,
        ItemKey output,
        int units,
        Map<ItemKey, Long> reservedIngredients,
        int priority,
        int batch,
        RecipeKind kind
) {

    public PlannedTask {
        reservedIngredients = Map.copyOf(reservedIngredients);
    }

}
