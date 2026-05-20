package events;

/**
 * QuestObserver — displays quest notifications to the player.
 *
 * Listens for QUEST_STARTED and QUEST_COMPLETED events fired by
 * QuestManager and prints appropriately formatted messages.
 *
 * Separating display from quest logic (QuestManager) keeps each class
 * focused on a single responsibility: QuestManager tracks state,
 * QuestObserver handles presentation.
 */
public class QuestObserver implements GameObserver {

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {

            case QUEST_STARTED:
                System.out.println("\n[New Quest] " + event.getSubject());
                break;

            case QUEST_COMPLETED:
                System.out.println("\n╔══════════════════════════════╗");
                System.out.println("║  QUEST COMPLETE: " + padRight(event.getSubject(), 12) + "  ║");
                System.out.println("╚══════════════════════════════╝");
                break;

            case PUZZLE_SOLVED:
                System.out.println("\n[Puzzle Solved] " + event.getSubject());
                break;

            default:
                break;
        }
    }

    private String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
