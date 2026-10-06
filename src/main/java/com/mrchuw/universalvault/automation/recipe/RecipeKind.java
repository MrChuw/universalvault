package com.mrchuw.universalvault.automation.recipe;

import com.mojang.serialization.Codec;

public enum RecipeKind {
    CRAFTING,
    CUSTOM;

    public static final Codec<RecipeKind> CODEC = Codec.STRING.xmap(
            s -> RecipeKind.valueOf(s.toUpperCase(java.util.Locale.ROOT)),
            RecipeKind::name
    );

    public static RecipeKind of(RecipeView view) {
        return (view instanceof RecipeView.Crafting) ? CRAFTING : CUSTOM;
    }
}