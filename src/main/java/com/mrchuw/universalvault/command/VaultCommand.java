package com.mrchuw.universalvault.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.block.VaultIOBlock;
import com.mrchuw.universalvault.gui.VaultMenuHelper;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.util.DevEnvironment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions;
//?}

import java.util.ArrayList;
import java.util.List;

public class VaultCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var vaultNode = Commands.literal("vault")
                .executes(VaultCommand::openGlobalVault)
                .then(Commands.literal("global").executes(VaultCommand::openGlobalVault))
                .then(Commands.literal("personal").executes(VaultCommand::openPersonalVault))
                .then(
                        Commands.literal("player")
                                //? if >=1.21.11 {
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                                //?} else {
                                /*.requires(s -> s.hasPermission(4))
                                 *///?}
                                .then(
                                        Commands.argument("target", EntityArgument.player())
                                                .executes(VaultCommand::openPlayerVault)
                                )
                );

        // Registra apenas em ambiente de desenvolvimento (runClient, dev-env, etc.)
        if (DevEnvironment.isDev()) {
            vaultNode.then(
                    Commands.literal("debug_chests")
                            //? if >=1.21.11 {
                            .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                            //?} else {
                            /*.requires(s -> s.hasPermission(4))
                             *///?}
                            .executes(ctx -> spawnDebugChests(ctx, "default"))
                            .then(Commands.literal("full").executes(ctx -> spawnDebugChests(ctx, "full")))
                            .then(Commands.literal("random").executes(ctx -> spawnDebugChests(ctx, "random")))
                            .then(Commands.literal("almost_broken").executes(ctx -> spawnDebugChests(ctx, "almost_broken")))
            );
        }

        dispatcher.register(vaultNode);
    }

    private static int spawnDebugChests(CommandContext<CommandSourceStack> ctx, String damageMode) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();

        Direction forward = player.getDirection();
        Direction side = forward.getClockWise();
        Direction opposite = forward.getOpposite();
        BlockPos basePos = player.blockPosition().relative(forward, 5);

        List<List<ItemStack>> chestContents = generatePresetItems(damageMode);

        for (int i = 0; i < 9; i++) {
            BlockPos chestPos = basePos.relative(side, i - 4).above();
            BlockPos hopperPos = chestPos.below();
            BlockPos targetPos = hopperPos.relative(opposite);

            BlockState customBlockState = ModRegistry.VAULT_IO.get().defaultBlockState();
            level.setBlock(targetPos, customBlockState, Block.UPDATE_ALL);

            BlockState hopperState = Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, opposite)
                    .setValue(HopperBlock.ENABLED, true);
            level.setBlock(hopperPos, hopperState, Block.UPDATE_ALL);

            BlockState chestState = Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, opposite);
            level.setBlock(chestPos, chestState, Block.UPDATE_ALL);

            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                List<ItemStack> items = chestContents.get(i);
                for (int slot = 0; slot < items.size() && slot < chest.getContainerSize(); slot++) {
                    chest.setItem(slot, items.get(slot));
                }
                chest.setChanged();
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§a9 Chests with Hoppers aligned with your view have been generated!"), true);
        return 1;
    }

    private static List<List<ItemStack>> generatePresetItems(String damageMode) {
        List<List<ItemStack>> chests = new ArrayList<>();

        chests.add(createFullChest(
                Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT, Items.IRON_INGOT,
                Items.COAL, Items.REDSTONE, Items.LAPIS_LAZULI, Items.NETHERITE_INGOT,
                Items.QUARTZ, Items.OBSIDIAN, Items.GLOWSTONE_DUST, Items.BLAZE_POWDER,
                Items.SLIME_BALL, Items.MAGMA_CREAM, Items.ENDER_PEARL, Items.GUNPOWDER,
                Items.STRING, Items.FEATHER, Items.LEATHER, Items.BONE,
                Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.SHULKER_SHELL, Items.CLAY_BALL,
                Items.BRICK, Items.FLINT, Items.ARROW
        ));

        chests.add(createFullChest(
                Items.BREAD, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON,
                Items.COOKED_CHICKEN, Items.COOKED_SALMON, Items.COOKED_COD, Items.BAKED_POTATO,
                Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.APPLE,
                Items.MELON_SLICE, Items.SWEET_BERRIES, Items.CARROT, Items.POTATO,
                Items.BEETROOT, Items.DRIED_KELP, Items.COOKIE, Items.PUMPKIN_PIE,
                Items.MUSHROOM_STEW, Items.HONEY_BOTTLE, Items.BEEF, Items.PORKCHOP,
                Items.MUTTON, Items.CHICKEN, Items.ROTTEN_FLESH
        ));

        chests.add(createFullChest(
                Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG,
                Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.CRIMSON_STEM, Items.WARPED_STEM,
                Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
                Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.CRIMSON_PLANKS, Items.WARPED_PLANKS,
                Items.STICK, Items.BOWL, Items.LADDER, Items.CRAFTING_TABLE,
                Items.CHEST, Items.BARREL, Items.GLASS, Items.GLASS_PANE,
                Items.SCAFFOLDING, Items.TORCH, Items.LANTERN
        ));

        chests.add(createFullChest(
                Items.STONE, Items.COBBLESTONE, Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS,
                Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.GRANITE, Items.POLISHED_GRANITE,
                Items.DIORITE, Items.POLISHED_DIORITE, Items.ANDESITE, Items.POLISHED_ANDESITE,
                Items.SANDSTONE, Items.RED_SANDSTONE, Items.NETHER_BRICKS, Items.RED_NETHER_BRICKS,
                Items.BASALT, Items.BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.END_STONE,
                Items.END_STONE_BRICKS, Items.PURPUR_BLOCK, Items.PRISMARINE, Items.PRISMARINE_BRICKS,
                Items.DARK_PRISMARINE, Items.SEA_LANTERN, Items.GLOWSTONE
        ));

        chests.add(createFullChest(
                Items.DIRT, Items.GRASS_BLOCK, Items.COARSE_DIRT, Items.PODZOL,
                Items.MYCELIUM, Items.GRAVEL, Items.SAND, Items.RED_SAND,
                Items.SOUL_SAND, Items.SOUL_SOIL, Items.NETHERRACK, Items.MAGMA_BLOCK,
                Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE, Items.SNOW_BLOCK,
                Items.CLAY, Items.HAY_BLOCK, Items.BONE_BLOCK, Items.SPONGE,
                Items.WET_SPONGE, Items.HONEYCOMB_BLOCK, Items.SLIME_BLOCK, Items.HONEY_BLOCK,
                Items.TERRACOTTA, Items.SPAWNER, Items.SLIME_SPAWN_EGG
        ));

        chests.add(createFullChest(
                Items.REDSTONE_TORCH, Items.REPEATER, Items.COMPARATOR, Items.LEVER,
                Items.STONE_BUTTON, Items.OAK_BUTTON, Items.PISTON, Items.STICKY_PISTON,
                Items.DISPENSER, Items.DROPPER, Items.HOPPER, Items.OBSERVER,
                Items.DAYLIGHT_DETECTOR, Items.TRIPWIRE_HOOK, Items.TRAPPED_CHEST, Items.TNT,
                Items.RAIL, Items.POWERED_RAIL, Items.DETECTOR_RAIL, Items.ACTIVATOR_RAIL,
                Items.IRON_DOOR, Items.OAK_DOOR, Items.IRON_TRAPDOOR, Items.OAK_TRAPDOOR,
                Items.REDSTONE_LAMP, Items.NOTE_BLOCK, Items.TARGET
        ));

        chests.add(createFullChest(
                Items.GLASS, Items.GLASS_PANE, Items.IRON_BARS, Items.IRON_CHAIN,
                Items.LANTERN, Items.SOUL_LANTERN, Items.CAMPFIRE, Items.SOUL_CAMPFIRE,
                Items.SEA_LANTERN, Items.GLOWSTONE, Items.SHROOMLIGHT, Items.END_ROD,
                Items.GOLD_BLOCK, Items.IRON_BLOCK, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK,
                Items.NETHERITE_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.COAL_BLOCK,
                Items.QUARTZ_BLOCK, Items.SMOOTH_QUARTZ, Items.PURPUR_BLOCK, Items.PRISMARINE,
                Items.PRISMARINE_BRICKS, Items.DARK_PRISMARINE, Items.BRICKS
        ));

        chests.add(createFullChest(
                Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS,
                Items.SUGAR_CANE, Items.CACTUS, Items.BAMBOO, Items.KELP,
                Items.NETHER_WART, Items.COCOA_BEANS, Items.OAK_SAPLING, Items.SPRUCE_SAPLING,
                Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING,
                Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM,
                Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM,
                Items.LILY_PAD, Items.VINE, Items.BONE_MEAL
        ));

        List<ItemStack> gear = new ArrayList<>();
        Item[] gearItems = {
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                Items.ELYTRA, Items.TURTLE_HELMET, Items.SHIELD,
                Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE,
                Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.BOW, Items.CROSSBOW,
                Items.TRIDENT, Items.FISHING_ROD, Items.SHEARS
        };

        java.util.Random rng = new java.util.Random();
        for (Item item : gearItems) {
            gear.add(createGear(item, damageMode, rng));
        }
        chests.add(gear);

        return chests;
    }

    private static List<ItemStack> createFullChest(Item... items) {
        List<ItemStack> list = new ArrayList<>();
        for (Item item : items) {
            list.add(new ItemStack(item, 64));
        }
        return list;
    }

    private static ItemStack createGear(Item item, String mode, java.util.Random rng) {
        ItemStack stack = new ItemStack(item, 1);
        int maxDurability = stack.getMaxDamage();

        int damage = switch (mode) {
            case "full" -> 0;
            case "almost_broken" -> Math.max(1, maxDurability - 1 - rng.nextInt(Math.min(5, maxDurability)));
            case "random" -> maxDurability > 1 ? 1 + rng.nextInt(maxDurability - 1) : 0;
            default -> Math.min(maxDurability / 3, 150); // Valor fixo moderado
        };

        if (damage > 0) {
            //? if >=1.20.5 {
            stack.set(DataComponents.DAMAGE, damage);
            //?} else {
            /*stack.setDamageValue(damage);
             *///?}
        }
        return stack;
    }

    private static int openGlobalVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VaultMenuHelper.openVault(
                player,
                UniversalVault.GLOBAL_VAULT_UUID,
                Component.translatable("gui.universal_vault.global_title")
        );
        return 1;
    }

    private static int openPersonalVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VaultMenuHelper.openVault(
                player,
                player.getUUID(),
                Component.translatable("gui.universal_vault.personal_title")
        );
        return 1;
    }

    private static int openPlayerVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer caller = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        VaultMenuHelper.openVault(
                caller,
                target.getUUID(),
                Component.translatable("gui.universal_vault.player_vault_title", target.getScoreboardName())
        );
        return 1;
    }
}