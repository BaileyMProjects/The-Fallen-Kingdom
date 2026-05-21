package ui;

import core.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * MenuPanel — the main-menu screen shown when the game launches.
 *
 * Displays the game title, a short lore blurb, and three styled difficulty
 * cards (EASY / MEDIUM / HARD). Clicking a card's SELECT button invokes the
 * provided callback with the chosen Difficulty and hands control back to
 * GameWindow, which then starts the game thread.
 */
public class MenuPanel extends JPanel {

    // Colours matching the dark RPG theme used by ExplorationPanel
    private static final Color BG       = new Color(18,  18,  28);
    private static final Color BG_CARD  = new Color(28,  28,  48);
    private static final Color FG_TITLE = new Color(200, 170,  80);  // amber/gold
    private static final Color FG_TEXT  = new Color(200, 200, 220);
    private static final Color FG_DIM   = new Color(150, 150, 170);
    private static final Color FG_HEAD  = new Color(130, 180, 255);

    // Accent colour per difficulty
    private static final Color COL_EASY = new Color( 60, 140,  80);
    private static final Color COL_MED  = new Color( 45,  90, 160);
    private static final Color COL_HARD = new Color(160,  50,  50);

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public MenuPanel(Consumer<Difficulty> onSelect) {
        setLayout(new GridBagLayout());
        setBackground(BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx    = 0;
        gbc.fill     = GridBagConstraints.HORIZONTAL;
        gbc.anchor   = GridBagConstraints.CENTER;

        // ── Title ─────────────────────────────────────────────────────────────
        gbc.gridy  = 0;
        gbc.insets = new Insets(40, 60, 20, 60);
        add(buildTitleBlock(), gbc);

        // ── Lore text ─────────────────────────────────────────────────────────
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 60, 30, 60);
        add(buildLore(), gbc);

        // ── "SELECT DIFFICULTY" heading ────────────────────────────────────────
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 60, 20, 60);
        add(buildSelectHeading(), gbc);

        // ── Difficulty cards ───────────────────────────────────────────────────
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 40, 60, 40);
        add(buildCardsPanel(onSelect), gbc);
    }

    // -------------------------------------------------------------------------
    // Section builders
    // -------------------------------------------------------------------------

    private JPanel buildTitleBlock() {
        JLabel line1 = new JLabel("T H E   F A L L E N   K I N G D O M", SwingConstants.CENTER);
        line1.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        line1.setForeground(FG_TITLE);

        JLabel line2 = new JLabel("────────────────────────────────────────", SwingConstants.CENTER);
        line2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        line2.setForeground(new Color(100, 80, 30));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.add(line1);
        panel.add(Box.createVerticalStrut(6));
        panel.add(line2);
        return panel;
    }

    private JLabel buildLore() {
        JLabel lore = new JLabel(
            "<html><center>"
            + "A darkness has consumed the kingdom. The Shadow Lord has shattered<br>"
            + "the Ancient Relic and cursed the land. You are the only adventurer<br>"
            + "brave enough to stop him."
            + "</center></html>",
            SwingConstants.CENTER);
        lore.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        lore.setForeground(FG_TEXT);
        return lore;
    }

    private JLabel buildSelectHeading() {
        JLabel label = new JLabel("─── SELECT DIFFICULTY ───", SwingConstants.CENTER);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        label.setForeground(FG_HEAD);
        return label;
    }

    private JPanel buildCardsPanel(Consumer<Difficulty> onSelect) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 24, 0));
        panel.setBackground(BG);
        panel.add(buildCard(Difficulty.EASY,   COL_EASY, onSelect));
        panel.add(buildCard(Difficulty.MEDIUM, COL_MED,  onSelect));
        panel.add(buildCard(Difficulty.HARD,   COL_HARD, onSelect));
        return panel;
    }

    // -------------------------------------------------------------------------
    // Card builder — one JPanel per difficulty
    // -------------------------------------------------------------------------

    private JPanel buildCard(Difficulty diff, Color accent, Consumer<Difficulty> onSelect) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        card.setPreferredSize(new Dimension(210, 240));

        // Name
        JLabel nameLabel = centeredLabel(diff.label.toUpperCase(), Font.BOLD, 18, accent);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel(
                "<html><center>" + diff.description + "</center></html>",
                SwingConstants.CENTER);
        descLabel.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 11));
        descLabel.setForeground(FG_DIM);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(descLabel);
        card.add(Box.createVerticalStrut(14));

        // Stats
        String statsHtml = String.format(
                "<html><center>"
                + "HP &nbsp;&nbsp;&nbsp;: %d<br>"
                + "ATK &nbsp;: %d<br>"
                + "DEF &nbsp;: %d<br>"
                + "Gold &nbsp;: %d<br>"
                + "Enemy dmg: %.0f%%"
                + "</center></html>",
                diff.startHp, diff.startAttack, diff.startDefense,
                diff.startGold, diff.enemyDamageMultiplier * 100);
        JLabel statsLabel = new JLabel(statsHtml, SwingConstants.CENTER);
        statsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsLabel.setForeground(FG_TEXT);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statsLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(14));

        // SELECT button
        JButton btn = new JButton("SELECT");
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> onSelect.accept(diff));
        card.add(btn);

        return card;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static JLabel centeredLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.MONOSPACED, style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
