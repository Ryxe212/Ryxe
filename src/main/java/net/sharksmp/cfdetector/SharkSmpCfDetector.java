package net.sharksmp.cfdetector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SharkSmpCfDetector implements ClientModInitializer {
    private static final Pattern CF_TITLE = Pattern.compile("coin\\s*flip|\\bcf\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern OUTCOME_WORDS = Pattern.compile(
            "\\b(win|winner|won|lose|loss|lost|heads|tails|payout|multiplier|chance|result|outcome)\\b",
            Pattern.CASE_INSENSITIVE);

    private String lastSnapshot = "";
    private boolean warnedThisMenu = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
            lastSnapshot = "";
            warnedThisMenu = false;
            return;
        }

        String title = screen.getTitle().getString();
        if (!CF_TITLE.matcher(title).find()) {
            lastSnapshot = "";
            warnedThisMenu = false;
            return;
        }

        StringBuilder snapshot = new StringBuilder();
        snapshot.append("GUI title: ").append(title).append('\n');
        boolean suspicious = OUTCOME_WORDS.matcher(title).find();

        for (int i = 0; i < screen.getMenu().slots.size(); i++) {
            Slot slot = screen.getMenu().slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String itemName = stack.getHoverName().getString();
            String components = stack.getComponents().toString();
            snapshot.append("slot=").append(i)
                    .append(" item=").append(itemName)
                    .append(" count=").append(stack.getCount())
                    .append(" components=").append(components)
                    .append('\n');

            if (OUTCOME_WORDS.matcher(itemName + " " + components).find()) {
                suspicious = true;
            }
        }

        String current = snapshot.toString();
        if (!current.equals(lastSnapshot)) {
            lastSnapshot = current;
            appendLog("\n=== " + LocalDateTime.now() + " ===\n" + current);
        }

        if (suspicious && !warnedThisMenu) {
            warnedThisMenu = true;
            client.player.displayClientMessage(
                    Component.literal("[CF Detector] Suspicious outcome-related text found in the Coin Flip GUI. Check logs/sharksmp-cf-detector.log"),
                    false);
        }
    }

    private void appendLog(String text) {
        Path log = FabricLoader.getInstance().getGameDir()
                .resolve("logs")
                .resolve("sharksmp-cf-detector.log");
        try {
            Files.createDirectories(log.getParent());
            Files.writeString(log, text, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[SharkSMP CF Detector] Could not write audit log: " + e.getMessage());
        }
    }
}
