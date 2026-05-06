/**
 * The main entry point for the executable application. This class serves as a thin wrapper to
 * launch the JavaFX-based GUI. It is primarily used to ensure compatibility when bundling the
 * application into an executable JAR file, as it avoids direct inheritance from the JavaFX
 * Application class in the main manifest entry.
 *
 * @author xpruzir00
 */

package tools;

import gui.App;

public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
