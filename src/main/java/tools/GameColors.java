/**
 * Game colors constants
 * 
 * @author: xpruzir00
 * @author: xblizna00
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
