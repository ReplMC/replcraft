package com.replmc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Cleanup");

            websocketServer.shutdown();
        }));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            LOGGER.info("Block interacted by" + player.getName());

            return ActionResult.PASS;
        });
    }


}
