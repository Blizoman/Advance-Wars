package tools;

public class P {

	/** Prints text */
	public static void print(String text) {
		System.out.print(text == null ? "" : text);
	}

	/** Prints text with \n */
	public static void println(String text) {
		print(text);
		print("\n");
	}

	/** Prints empty newline */
	public static void eprintln() {
		print("\n");
	}
}
