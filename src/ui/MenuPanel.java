package ui;

import core.Difficulty;
import save.GameSnapshot;
import save.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * MenuPanel — the main-menu screen shown when the game launches.
 *
 * Top half: title, lore blurb, and three difficulty cards (NEW GAME).
 * Bottom half: three save-slot cards (CONTINUE).  Each slot shows a summary
 * of the saved character and has LOAD / WIPE buttons.  Clicking LOAD calls
 * the onLoad callback with the slot number; WIPE shows a confirm dialog then
 * refreshes the slot display.
 */
public class MenuPanel extends JPanel {

    private static final Color BG       = new Color(18,  18,  28);
    private static final Color BG_CARD  = new Color(28,  28,  48);
    private static final Color FG_TITLE = new Color(200, 170,  80);
    private static final Color FG_TEXT  = new Color(200, 200, 220);
    private static final Color FG_DIM   = new Color(150, 150, 170);
    private static final Color FG_HEAD  = new Color(130, 180, 255);
    private static final Color COL_EASY = new Color( 60, 140,  80);
    private static final Color COL_MED  = new Color( 45,  90, 160);
    private static final Color COL_HARD = new Color(160,  50,  50);
    private static final Color COL_SAVE = new Color(160, 120,  40);  // amber for occupied slots
    private static final Color COL_WIPE = new Color(140,  40,  40);

    private final IntConsumer    onLoad;
    private final JPanel         slotsRow;  // rebuilt by refreshSlots()

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public MenuPanel(Consumer<Difficulty> onSelect, IntConsumer onLoad) {
        this.onLoad   = onLoad;
        this.slotsRow = new JPanel(new GridLayout(1, SaveManager.NUM_SLOTS, 20, 0));
        this.slotsRow.setBackground(BG);

        setLayout(new GridBagLayout());
        setBackground(BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Title
        gbc.gridy  = 0;
        gbc.insets = new Insets(30, 60, 16, 60);
        add(buildTitleBlock(), gbc);

        // Lore
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 60, 20, 60);
        add(buildLore(), gbc);

        // NEW GAME heading
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 60, 14, 60);
        add(buildHeading("─── NEW GAME ───"), gbc);

        // Difficulty cards
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 40, 26, 40);
        add(buildCardsPanel(onSelect), gbc);

        // CONTINUE heading
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 60, 14, 60);
        add(buildHeading("─── CONTINUE ───"), gbc);

        // Save-slot cards
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 40, 30, 40);
        refreshSlots();
        add(slotsRow, gbc);
    }

    // -------------------------------------------------------------------------
    // Slot refresh — call to rebuild after a wipe
    // -------------------------------------------------------------------------

    public void refreshSlots() {
        slotsRow.removeAll();
        for (int slot = 1; slot <= SaveManager.NUM_SLOTS; slot++) {
            slotsRow.add(buildSlotCard(slot));
        }
        slotsRow.revalidate();
        slotsRow.repaint();
    }

    // -------------------------------------------------------------------------
    // Section builders
    // -------------------------------------------------------------------------

    private JPanel buildTitleBlock() {
        JLabel line1 = new JLabel("T H E   F A L L E N   K I N G D O M", SwingConstants.CENTER);
        line1.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        line1.setForeground(FG_TITLE);

        JLabel line2 = new JLabel("────────────────────────────────────────", SwingConstants.CENTER);
        line2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        line2.setForeground(new Color(100, 80, 30));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.add(line1);
        panel.add(Box.createVerticalStrut(4));
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
        lore.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lore.setForeground(FG_TEXT);
        return lore;
    }

    private JLabel buildHeading(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        label.setForeground(FG_HEAD);
        return label;
    }

    // -------------------------------------------------------------------------
    // Difficulty card builder
    // -------------------------------------------------------------------------

    private JPanel buildCardsPanel(Consumer<Difficulty> onSelect) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(BG);
        panel.add(buildDiffCard(Difficulty.EASY,   COL_EASY, onSelect));
        panel.add(buildDiffCard(Difficulty.MEDIUM, COL_MED,  onSelect));
        panel.add(buildDiffCard(Difficulty.HARD,   COL_HARD, onSelect));
        return panel;
    }

    private JPanel buildDiffCard(Difficulty diff, Color accent, Consumer<Difficulty> onSelect) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        card.setPreferredSize(new Dimension(210, 220));

        card.add(centeredLabel(diff.label.toUpperCase(), Font.BOLD, 16, accent));
        card.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("<html><center>" + diff.description + "</center></html>",
                SwingConstants.CENTER);
        desc.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 10));
        desc.setForeground(FG_DIM);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(10));

        String statsHtml = String.format(
                "<html><center>HP: %d  ATK: %d  DEF: %d<br>Gold: %dg  Enemy dmg: %.0f%%</center></html>",
                diff.startHp, diff.startAttack, diff.startDefense,
                diff.startGold, diff.enemyDamageMultiplier * 100);
        JLabel stats = new JLabel(statsHtml, SwingConstants.CENTER);
        stats.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        stats.setForeground(FG_TEXT);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(stats);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(10));

        JButton btn = styledButton("SELECT", accent);
        btn.addActionListener(e -> onSelect.accept(diff));
        card.add(btn);
        return card;
    }

    // -------------------------------------------------------------------------
    // Save-slot card builder
    // -------------------------------------------------------------------------

    private JPanel buildSlotCard(int slot) {
        GameSnapshot snap     = SaveManager.load(slot);
        boolean      occupied = snap != null;
        Color        accent   = occupied ? COL_SAVE : new Color(60, 60, 80);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setPreferredSize(new Dimension(210, 130));

        card.add(centeredLabel("SLOT " + slot, Font.BOLD, 13, accent));
        card.add(Box.createVerticalStrut(8));

        String summary = occupied ? snap.getSummary() : "— Empty —";
        JLabel info = new JLabel("<html><center>" + summary + "</center></html>",
                SwingConstants.CENTER);
        info.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        info.setForeground(occupied ? FG_TEXT : FG_DIM);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(info);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton loadBtn = styledButton("LOAD", accent);
        loadBtn.setEnabled(occupied);
        loadBtn.addActionListener(e -> onLoad.accept(slot));
        btnRow.add(loadBtn);

        JButton wipeBtn = styledButton("WIPE", COL_WIPE);
        wipeBtn.setEnabled(occupied);
        wipeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    MenuPanel.this,
                    "Delete save in slot " + slot + "? This cannot be undone.",
                    "Wipe save",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                SaveManager.delete(slot);
                refreshSlots();
            }
        });
        btnRow.add(wipeBtn);

        card.add(btnRow);
        return card;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private static JLabel centeredLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.MONOSPACED, style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
