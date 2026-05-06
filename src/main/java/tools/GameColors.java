/**
 * Utility class that defines the visual palette and color constants for the game.
 * It provides standardized Hex-based colors for different players and maps these colors 
 * to human-readable labels through the ColorOption record. This ensures visual 
 * consistency across the UI, from unit sprites to territory highlighting.
 *
 * @author xpruzir00
 * @author xblizna00
 */

package tools;

import javafx.scene.paint.Color;
import java.util.List;

public class GameColors {
	private GameColors() { /* Prevent instantiation */ }

	public static final Color RED = Color.web("#e74c3c");
	public static final Color BLUE = Color.web("#3498db");
	public static final Color GREEN = Color.web("#2ecc71");
	public static final Color YELLOW = Color.web("#f1c40f");

	public static final Color[] AVAILABLE_COLORS = { RED, BLUE, GREEN, YELLOW };

	public record ColorOption(String label, Color color) {}

	public static final List<ColorOption> COLOR_OPTIONS = List.of(
			new ColorOption("Red", RED),
			new ColorOption("Blue", BLUE),
			new ColorOption("Green", GREEN),
			new ColorOption("Yellow", YELLOW));
}
