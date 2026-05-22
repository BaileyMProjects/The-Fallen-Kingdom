package ui;

import util.AsciiArtLoader;
import util.EnemyImageLoader;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private static final String SPRITE_IMAGE = "IMAGE";
    private static final String SPRITE_TEXT  = "TEXT";

    private final CardLayout spriteLayout;
    private final JPanel     spritePanel;
    private final JLabel     spriteImage;
    private final JTextArea  spriteText;
    private final JLabel     enemyNameLabel;
    private final JProgressBar playerBar;
    private final JProgressBar enemyBar;
    private final JLabel       playerHpLabel;
    private final JLabel       enemyHpLabel;
    private final JTextArea    outputArea;
    private final JTextField   inputField;

    private static final int CHAR_DELAY_MS = 10;

    private final ExecutorService typewriterPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "typewriter-combat");
        t.setDaemon(true);
        return t;
    });

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CombatPanel(Consumer<String> onSubmit) {
        super(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        // ── Sprite display (image if available, text art fallback) ────────────
        spriteLayout = new CardLayout();
        spritePanel  = new JPanel(spriteLayout);
        spritePanel.setBackground(BG_DARK);
        spritePanel.setPreferredSize(new Dimension(0, 230));

        spriteImage = new JLabel("", SwingConstants.CENTER);
        spriteImage.setBackground(BG_DARK);
        spriteImage.setOpaque(true);

        spriteText = new JTextArea("  (no enemy)");
        spriteText.setEditable(false);
        spriteText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        spriteText.setBackground(BG_DARK);
        spriteText.setForeground(FG_ART);
        spriteText.setMargin(new Insets(10, 20, 6, 20));

        spritePanel.add(spriteImage, SPRITE_IMAGE);
        spritePanel.add(spriteText,  SPRITE_TEXT);

        enemyNameLabel = new JLabel("", SwingConstants.CENTER);
        enemyNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        enemyNameLabel.setForeground(FG_NAME);
        enemyNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Combine sprite + name into a top block
        JPanel artBlock = new JPanel(new BorderLayout(0, 4));
        artBlock.setBackground(BG_DARK);
        artBlock.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 30, 80)));
        artBlock.add(spritePanel,    BorderLayout.CENTER);
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
            inputField.setText("");
            // Empty Enter unblocks any post-combat "Press Enter to continue" gate.
            onSubmit.accept(text);
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

    /**
     * Submits text to the typewriter pool. Decorative lines appear instantly;
     * real combat messages type out character by character at CHAR_DELAY_MS.
     */
    public void appendText(String text) {
        typewriterPool.submit(() -> typewriteText(text));
    }

    private void typewriteText(String text) {
        StringBuilder line = new StringBuilder();
        for (char c : text.toCharArray()) {
            line.append(c);
            if (c == '\n') {
                dispatchLine(line.toString());
                line.setLength(0);
            }
        }
        if (line.length() > 0) dispatchLine(line.toString());
    }

    private void dispatchLine(String line) {
        if (isDecorativeLine(line)) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append(line);
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
        } else {
            for (char c : line.toCharArray()) {
                final char fc = c;
                SwingUtilities.invokeLater(() -> {
                    outputArea.append(String.valueOf(fc));
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
                try {
                    Thread.sleep(CHAR_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private static boolean isDecorativeLine(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return true;
        int run = 0;
        for (char c : trimmed.toCharArray()) {
            if (Character.isLetter(c)) { if (++run > 1) return false; }
            else                       { run = 0; }
        }
        return true;
    }

    public void requestInputFocus() {
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    /**
     * Shows a PNG sprite for the enemy if one exists in resources/images/,
     * otherwise falls back to the text ASCII art from resources/ascii/.
     */
    public void setEnemySprite(String enemyName) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon icon = EnemyImageLoader.load(enemyName, 380, 220);
            if (icon != null) {
                spriteImage.setIcon(icon);
                spriteLayout.show(spritePanel, SPRITE_IMAGE);
            } else {
                spriteText.setText(AsciiArtLoader.load(enemyName));
                spriteLayout.show(spritePanel, SPRITE_TEXT);
            }
        });
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
