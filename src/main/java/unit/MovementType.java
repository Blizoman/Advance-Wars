/**
 * Categorizes the different methods of locomotion used by units.
 * This classification is used by the pathfinding system to determine movement costs 
 * and accessibility across various terrain types. For example, units with FOOT movement 
 * might traverse mountains that are impassable for those with VEHICLE movement.
 *
 * @author xpruzir00
 */

package unit;

public enum MovementType {
	FOOT,
	VEHICLE;
}
