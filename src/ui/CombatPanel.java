package ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * CombatPanel — the combat screen, shown while an encounter is active.
 *
 * Layout:
 *   NORTH  — enemy ASCII art (loaded from resources/ascii/) + enemy name
 *   CENTER — split between HP bars (top) and scrollable combat log (bottom)
 *   SOUTH  — input row with combat hint labels
 *
 * GuiObserver calls setAsciiArt(), setEnemyName(), and updateHpBars()
 * whenever it receives COMBAT_STARTED or polls the player/enemy HP.
 * All Swing mutations happen on the EDT via SwingUtilities.invokeLater().
 */
public class CombatPanel extends JPanel {

    // Dark, ominous palette for the combat screen
    private static final Color BG_DARK    = new Color(12,  8,  18);
    private static final Color BG_LOG     = new Color(18, 12, 28);
    private static final Color FG_ART     = new Color(200, 60, 60);
    private static final Color FG_NAME    = new Color(230, 80, 80);
    private static final Color FG_LOG     = new Color(200, 200, 220);
    private static final Color FG_HINT    = new Color(150, 110, 200);
    private static final Color HP_PLAYER  = new Color(70, 190, 70);
    private static final Color HP_ENEMY   = new Color(200, 60, 60);

    private final JTextArea    asciiArt;
    private final JLabel       enemyNameLabel;
    private final JProgressBar playerBar;
    private final JProgressBar enemyBar;
    private final JLabel       playerHpLabel;
    private final JLabel       enemyHpLabel;
    private final JTextArea    outputArea;
    private final JTextField   inputField;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CombatPanel(Consumer<String> onSubmit) {
        super(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        // ── ASCII art display ─────────────────────────────────────────────────
        asciiArt = new JTextArea("  (no enemy)");
        asciiArt.setEditable(false);
        asciiArt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        asciiArt.setBackground(BG_DARK);
        asciiArt.setForeground(FG_ART);
        asciiArt.setMargin(new Insets(10, 20, 6, 20));
        asciiArt.setAlignmentX(Component.CENTER_ALIGNMENT);

        enemyNameLabel = new JLabel("", SwingConstants.CENTER);
        enemyNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        enemyNameLabel.setForeground(FG_NAME);
        enemyNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Combine art + name into a top block
        JPanel artBlock = new JPanel(new BorderLayout(0, 4));
        artBlock.setBackground(BG_DARK);
        artBlock.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 30, 80)));
        artBlock.add(asciiArt,      BorderLayout.CENTER);
        artBlock.add(enemyNameLabel, BorderLayout.SOUTH);

        // ── HP bars ──────────────────────────────────────────────────────────
        playerBar    = buildBar(HP_PLAYER);
        enemyBar     = buildBar(HP_ENEMY);
        playerHpLabel = buildHpLabel("Player");
        enemyHpLabel  = buildHpLabel("Enemy");

        JPanel hpGrid = new JPanel(new GridLayout(2, 2, 8, 6));
        hpGrid.setBackground(new Color(20, 14, 32));
        hpGrid.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        hpGrid.add(playerHpLabel); hpGrid.add(playerBar);
        hpGrid.add(enemyHpLabel);  hpGrid.add(enemyBar);

        // ── Combat log ───────────────────────────────────────────────────────
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBackground(BG_LOG);
        outputArea.setForeground(FG_LOG);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setMargin(new Insets(6, 10, 6, 10));
        JScrollPane logScroll = new JScrollPane(outputArea);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(55, 35, 75)));
        logScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Centre pane: HP bars on top, log below
        JPanel centrePane = new JPanel(new BorderLayout(0, 0));
        centrePane.setBackground(BG_DARK);
        centrePane.add(hpGrid,     BorderLayout.NORTH);
        centrePane.add(logScroll,  BorderLayout.CENTER);

        // ── Input row ────────────────────────────────────────────────────────
        inputField = new JTextField();
        inputField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inputField.setBackground(new Color(28, 18, 38));
        inputField.setForeground(new Color(220, 200, 240));
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 45, 100)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        JButton sendBtn = new JButton("Enter");
        sendBtn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        sendBtn.setBackground(new Color(90, 35, 100));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        Runnable doSubmit = () -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                inputField.setText("");
                onSubmit.accept(text);
            }
        };
        inputField.addActionListener(e -> doSubmit.run());
        sendBtn.addActionListener(e -> doSubmit.run());

        JLabel hint = new JLabel("  [A] Attack   [U] Use item   [R] Run  —  ");
        hint.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        hint.setForeground(FG_HINT);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBackground(new Color(10, 6, 16));
        inputRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
        inputRow.add(hint,       BorderLayout.WEST);
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendBtn,    BorderLayout.EAST);

        // ── Assemble ─────────────────────────────────────────────────────────
        add(artBlock,   BorderLayout.NORTH);
        add(centrePane, BorderLayout.CENTER);
        add(inputRow,   BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Public API — called by GuiObserver
    // -------------------------------------------------------------------------

    /** The JTextArea that System.out is redirected into during combat. */
    public JTextArea getOutputArea() { return outputArea; }

    public void requestInputFocus() {
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    /** Display the ASCII art for the current enemy. */
    public void setAsciiArt(String art) {
        SwingUtilities.invokeLater(() -> asciiArt.setText(art));
    }

    /** Display the enemy name in large text above the HP bars. */
    public void setEnemyName(String name) {
        SwingUtilities.invokeLater(() ->
                enemyNameLabel.setText("☠  " + name.toUpperCase() + "  ☠"));
    }

    /** Refresh both HP progress bars and their text labels. */
    public void updateHpBars(int playerHp, int playerMax, int enemyHp, int enemyMax) {
        SwingUtilities.invokeLater(() -> {
            playerBar.setMaximum(Math.max(playerMax, 1));
            playerBar.setValue(Math.max(playerHp, 0));
            playerHpLabel.setText("Player  " + playerHp + "/" + playerMax);

            enemyBar.setMaximum(Math.max(enemyMax, 1));
            enemyBar.setValue(Math.max(enemyHp, 0));
            enemyHpLabel.setText("Enemy   " + enemyHp + "/" + enemyMax);
        });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private JProgressBar buildBar(Color fill) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(100);
        bar.setForeground(fill);
        bar.setBackground(new Color(38, 24, 48));
        bar.setStringPainted(false);
        bar.setBorder(BorderFactory.createLineBorder(new Color(60, 40, 70)));
        bar.setPreferredSize(new Dimension(0, 18));
        return bar;
    }

    private JLabel buildHpLabel(String name) {
        JLabel lbl = new JLabel(name, SwingConstants.RIGHT);
        lbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        lbl.setForeground(new Color(190, 190, 210));
        lbl.setBackground(new Color(20, 14, 32));
        lbl.setOpaque(true);
        return lbl;
    }
}
