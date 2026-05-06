/**
 * Global configuration class that stores constant values used throughout the application.
 * It defines core gameplay balance parameters such as starting capital, unit health limits, 
 * healing rates, capture thresholds, and economic income. It also includes technical 
 * settings like the artificial intelligence action delay.
 *
 * @author xpruzir00
 */

package tools;

public class Consts {
	private Consts() { /* Prevent instantiation */ }

	public final static int STARTING_MONEY = 20000;
	public final static int MAX_HP = 100;
	public final static int MAX_HEAL = 20;
	public final static int CAPTURE_HP = 20;
	public final static int CITY_INCOME = 1000;

	public final static int BOT_ACTION_DELAY = 50;
}
