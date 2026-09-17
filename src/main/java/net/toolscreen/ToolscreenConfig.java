package net.toolscreen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config, hand-editable in .minecraft/config/toolscreen.json.
 * No in-game screen yet — see README roadmap.
 */
public class ToolscreenConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("toolscreen.json");

	/** FOV (degrees) applied while the zoom key is held. Lower = more zoomed in. */
	public int zoomFov = 5;

	/** Master toggle for the alignment overlay, flipped in-game via the toggle keybind. */
	public boolean overlayEnabled = true;

	/** "mapless" (nether portal alignment cross) or "preemptive" (end room alignment box). */
	public String overlayPreset = "mapless";

	/** ARGB overlay line color. */
	public int overlayColor = 0x80FFFFFF;

	public static ToolscreenConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				ToolscreenConfig cfg = GSON.fromJson(reader, ToolscreenConfig.class);
				if (cfg != null) {
					return cfg;
				}
			} catch (IOException e) {
				Toolscreen.LOGGER.warn("Failed to read toolscreen.json, using defaults", e);
			}
		}
		ToolscreenConfig cfg = new ToolscreenConfig();
		cfg.save();
		return cfg;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			Toolscreen.LOGGER.warn("Failed to write toolscreen.json", e);
		}
	}
}
