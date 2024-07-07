package com.replmc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplCraft implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "replcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private WebsocketServer websocketServer = null;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("replcraft").executes(context -> {
            ServerCommandSource source = context.getSource();

            ServerPlayerEntity player = source.getPlayerOrThrow();

            player.getWorld().setBlockState(player.getBlockPos(), Blocks.IRON_BLOCK.getDefaultState());

            source.sendFeedback(() -> Text.literal("Hello from /replcraft, " + player.getName().getString() + "!"), false);
            return 1;
        }))));

        websocketServer = new WebsocketServer();

        // Gracefully shutdown WebsocketServer on server /stop
        ServerLifecycleEvents.SERVER_STOPPING.register((minecraftServer) -> {
            LOGGER.info("Server stopping event");

            websocketServer.shutdown();
        });

        // test
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {// ignore if player is not sneaking
            if (!player.isSneaking()) {
                return ActionResult.PASS;
            }


            BlockState signBlockState = world.getBlockState(hitResult.getBlockPos());
            Block sign = signBlockState.getBlock();
            BlockEntity signBlockEntity = world.getBlockEntity(hitResult.getBlockPos());

            // ignore if sign isn't a wall sign
            if (!(sign instanceof WallSignBlock) || !(signBlockEntity instanceof SignBlockEntity)) {
                return ActionResult.PASS;
            }

            // ensure sign first line is "REPL"
            SignText signText = ((SignBlockEntity) signBlockEntity).getFrontText();
            if (!signText.getMessage(0, false).equals(Text.literal("REPL"))) {
                return ActionResult.PASS;
            }


            BlockPos signPlacedBlockPos = hitResult.getBlockPos().add(signBlockState.get(WallSignBlock.FACING).getOpposite().getVector());
            Identifier structureMaterial = Registries.BLOCK.getId(world.getBlockState(signPlacedBlockPos).getBlock());

            // find top north-east corner
            BlockPos structureTopLeftNorthCorner = signPlacedBlockPos;
            while (true) {
                // look upwards
                BlockPos nextPos = structureTopLeftNorthCorner.up();
                Identifier blockType = Registries.BLOCK.getId(world.getBlockState(nextPos).getBlock());


                if (blockType.equals(structureMaterial)) {
                    structureTopLeftNorthCorner = nextPos;
                } else {
                    break;
                }
            }
            while (true) {
                // look north
                BlockPos nextPos = structureTopLeftNorthCorner.north();
                Identifier blockType = Registries.BLOCK.getId(world.getBlockState(nextPos).getBlock());


                if (blockType.equals(structureMaterial)) {
                    structureTopLeftNorthCorner = nextPos;
                } else {
                    break;
                }
            }
            while (true) {
                // look east
                BlockPos nextPos = structureTopLeftNorthCorner.east();
                Identifier blockType = Registries.BLOCK.getId(world.getBlockState(nextPos).getBlock());


                if (blockType.equals(structureMaterial)) {
                    structureTopLeftNorthCorner = nextPos;
                } else {
                    break;
                }
            }

            // replace top north-east corner with diamond for debugging
            world.setBlockState(structureTopLeftNorthCorner, Blocks.DIAMOND_BLOCK.getDefaultState());

            // todo: check for valid structure


            String token = "eyJ.dummyToken";
            player.sendMessage(Text.literal("Here's your token!\n" + token));

            // don't show the sign edit UI
            return ActionResult.FAIL;

        });


    }


}
