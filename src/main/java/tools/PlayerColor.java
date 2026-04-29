package tools;

import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.paint.Color;

public class PlayerColor {
	private PlayerColor() { /* Prevent instantiation */ }

	private static double currentHue = ThreadLocalRandom.current().nextDouble() * 360.0;
	private static final double GOLDEN_RATIO_DEG = 222.4922359499622;

	public static Color randomColor() {
		currentHue = (currentHue + GOLDEN_RATIO_DEG) % 360.0;
		return Color.hsb(currentHue, 0.62, 0.92);
	}
}
