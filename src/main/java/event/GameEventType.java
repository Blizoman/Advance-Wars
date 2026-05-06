/**
 * Defines the various types of events that can occur and be tracked during a game session. These
 * event types correspond to specific player actions or core game state changes, such as unit
 * manipulation (buying, moving, attacking, dying), territory control (capturing), and turn
 * progression.
 *
 * @author xpruzir00
 */

package event;

public enum GameEventType {
    UNIT_BOUGHT,
    UNIT_MOVED,
    UNIT_ATTACKED,
    UNIT_DIED,
    CITY_CAPTURED,
    CAPTURE_PROGRESS,
    PLAYER_ELIMINATED,
    TURN_CHANGED,
}
