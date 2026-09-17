package net.toolscreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

/**
 * Reimplements the parts of the desktop "Toolscreen" speedrunning utility that are possible
 * inside the game itself: a hold-to-zoom magnifier (for eye-spy pixel peeping) and a toggleable
 * on-screen alignment guide (nether "mapless" and end "preemptive" presets). The original tool's
 * DLL-injected window resizing, global key rebinding and virtual camera have no iOS/Amethyst
 * equivalent and are intentionally out of scope (see README).
 */
public class ToolscreenClient implements ClientModInitializer {
	private static ToolscreenConfig config;

	private KeyBinding zoomKey;
	private KeyBinding overlayToggleKey;
	private KeyBinding cyclePresetKey;

	private boolean zooming = false;
	private int savedFov = -1;

	@Override
	public void onInitializeClient() {
		config = ToolscreenConfig.load();

		zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.toolscreen.zoom",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_C,
				"category.toolscreen"
		));
		overlayToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.toolscreen.toggle_overlay",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"category.toolscreen"
		));
		cyclePresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.toolscreen.cycle_preset",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.toolscreen"
		));

		// Touch-only players (Amethyst-iOS) should map these three keybinds to on-screen
		// buttons via the launcher's own custom-control editor rather than us drawing our
		// own touch buttons, so behaviour matches every other keybind in the game.
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		HudRenderCallback.EVENT.register(this::onHudRender);
	}

	private void onEndTick(MinecraftClient client) {
		if (client.options == null) {
			return;
		}

		boolean held = zoomKey.isPressed() && client.currentScreen == null;
		if (held && !zooming) {
			zooming = true;
			savedFov = client.options.fov;
			client.options.fov = Math.max(config.zoomFov, 1);
		} else if (!held && zooming) {
			zooming = false;
			if (savedFov >= 0) {
				client.options.fov = savedFov;
				savedFov = -1;
			}
		}

		while (overlayToggleKey.wasPressed()) {
			config.overlayEnabled = !config.overlayEnabled;
			config.save();
		}

		while (cyclePresetKey.wasPressed()) {
			config.overlayPreset = "mapless".equals(config.overlayPreset) ? "preemptive" : "mapless";
			config.save();
		}
	}

	private void onHudRender(MatrixStack matrices, float tickDelta) {
		if (config == null || !config.overlayEnabled) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen != null || client.player == null) {
			return;
		}

		Window window = client.getWindow();
		int centerX = window.getScaledWidth() / 2;
		int centerY = window.getScaledHeight() / 2;
		int color = config.overlayColor;

		if ("preemptive".equals(config.overlayPreset)) {
			drawAlignmentBox(matrices, centerX, centerY, 40, color);
		} else {
			drawAlignmentCross(matrices, centerX, centerY, 60, color);
		}
	}

	/** "Mapless" preset: a wide horizontal/vertical cross for eyeballing nether portal alignment. */
	private void drawAlignmentCross(MatrixStack matrices, int centerX, int centerY, int radius, int color) {
		DrawableHelper.fill(matrices, centerX - radius, centerY, centerX + radius, centerY + 1, color);
		DrawableHelper.fill(matrices, centerX, centerY - radius, centerX + 1, centerY + radius, color);
	}

	/** "Preemptive" preset: a square guide for lining up the predicted end portal room. */
	private void drawAlignmentBox(MatrixStack matrices, int centerX, int centerY, int size, int color) {
		DrawableHelper.fill(matrices, centerX - size, centerY - size, centerX - size + 1, centerY + size, color);
		DrawableHelper.fill(matrices, centerX + size, centerY - size, centerX + size + 1, centerY + size, color);
		DrawableHelper.fill(matrices, centerX - size, centerY - size, centerX + size, centerY - size + 1, color);
		DrawableHelper.fill(matrices, centerX - size, centerY + size, centerX + size, centerY + size + 1, color);
	}
}
