package net.sharksmp.cfdetector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SharkSmpCfDetector implements ClientModInitializer {
    private static final Pattern CF_TITLE = Pattern.compile("coin\\s*flip|\\bcf\\b", Pattern.CASE_INSENSITIVE);

    // Only clear result-style phrases count. Generic words like "chance", "payout",
    // or a button labelled "Win" are not proof of an outcome.
    private static final Pattern WIN_TEXT = Pattern.compile(
            "\\b(?:you\\s+(?:win|won)|your\\s+result\\s+(?:is\\s+)?(?:win|won)|(?:result|outcome|status)\\s*[:= -]\\s*(?:win|won)|winner\\s*[:=]\\s*you)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LOSE_TEXT = Pattern.compile(
            "\\b(?:you\\s+(?:lose|lost)|your\\s+result\\s+(?:is\\s+)?(?:lose|lost)|(?:result|outcome|status)\\s*[:= -]\\s*(?:lose|lost)|loser\\s*[:=]\\s*you)\\b",
            Pattern.CASE_INSENSITIVE);

    private String lastSnapshot = "";
    private String lastStatus = "";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
            lastSnapshot = "";
            lastStatus = "";
            return;
        }

        String title = screen.getTitle().getString();
        if (!CF_TITLE.matcher(title).find()) {
            lastSnapshot = "";
            lastStatus = "";
            return;
        }

        StringBuilder visibleText = new StringBuilder(title);
        for (Slot slot : screen.getMenu().slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            visibleText.append(' ').append(stack.getHoverName().getString());
            visibleText.append(' ').append(stack.getComponents());
        }

        String snapshot = visibleText.toString();
        String normalized = snapshot.toLowerCase(Locale.ROOT);
        boolean win = WIN_TEXT.matcher(normalized).find();
        boolean lose = LOSE_TEXT.matcher(normalized).find();

        // Conflicting or unclear text is never presented as a definite result.
        String status = win && !lose ? "WIN TEXT FOUND"
                : lose && !win ? "LOSE TEXT FOUND"
                : "UNKNOWN";

        if (!snapshot.equals(lastSnapshot) || !status.equals(lastStatus)) {
            lastSnapshot = snapshot;
            lastStatus = status;

            String message = switch (status) {
                case "WIN TEXT FOUND" -> "[CF Detector] WIN TEXT FOUND - verify it is your result.";
                case "LOSE TEXT FOUND" -> "[CF Detector] LOSE TEXT FOUND - verify it is your result.";
                default -> "[CF Detector] UNKNOWN - no clear result is exposed in the menu.";
            };
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal(message), true);
            }
        }
    }
}
