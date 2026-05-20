package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * ExplorationPanel — the main gameplay screen shown outside of combat.
 *
 * Layout (BorderLayout):
 *   CENTER — scrollable main text area where all game output is printed
 *   EAST   — sidebar with live character stats and active quest list
 *   SOUTH  — input field with prompt label and submit button
 *
 * The sidebar is updated by GuiObserver whenever the player's state changes
 * (level up, quest update, post-combat). Between those events it shows the
 * last-known values, which is always fresh enough for exploration pacing.
 */
public class ExplorationPanel extends JPanel {

    // Colours for the dark RPG theme
    private static final Color BG_MAIN    = new Color(18,  18,  28);
    private static final Color BG_SIDEBAR = new Color(28,  28,  48);
    private static final Color BG_INPUT   = new Color(12,  12,  22);
    private static final Color FG_TEXT    = new Color(200, 200, 220);
    private static final Color FG_HEADER  = new Color(130, 180, 255);
    private static final Color FG_PROMPT  = new Color(100, 210, 100);

    private final JTextArea     outputArea;
    private final JTextArea     sidebarStats;
    private final JTextArea     sidebarQuests;
    private final JTextField    inputField;

    private static final int CHAR_DELAY_MS = 22;

    private final StringBuilder  fullHistory    = new StringBuilder();
    private final StringBuilder  currentPage    = new StringBuilder();
    private boolean              historyMode    = false;
    private final AtomicLong     pageVersion    = new AtomicLong(0);
    private final ExecutorService typewriterPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "typewriter-explore");
        t.setDaemon(true);
        return t;
    });

    // Tab-autocomplete state
    private final BiFunction<String, String, List<String>> completer;
    private List<String> tabCompletions = Collections.emptyList();
    private int          tabIndex       = 0;
    private String       tabBase        = "";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ExplorationPanel(Consumer<String> onSubmit,
                            BiFunction<String, String, List<String>> completer) {
        super(new BorderLayout(4, 4));
        setBackground(BG_MAIN);
        this.completer = completer;

        // ── Main output area ─────────────────────────────────────────────────
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setBackground(BG_MAIN);
        outputArea.setForeground(FG_TEXT);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());
        outputScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ── Sidebar ──────────────────────────────────────────────────────────
        sidebarStats  = buildSidebarText();
        sidebarQuests = buildSidebarText();

        JPanel statsBlock  = buildSidebarBlock("CHARACTER", sidebarStats);
        JPanel questsBlock = buildSidebarBlock("QUESTS",    sidebarQuests);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        sidebar.add(statsBlock);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(questsBlock);

        // ── Input row ────────────────────────────────────────────────────────
        inputField = buildInputField();
        // Allow Tab to be handled as a key event instead of cycling focus
        inputField.setFocusTraversalKeysEnabled(false);
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume();
                    handleTab();
                } else {
                    tabCompletions = Collections.emptyList();
                }
            }
        });

        JButton sendBtn = buildSendButton("Enter");

        Runnable doSubmit = () -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                inputField.setText("");
                if (text.equalsIgnoreCase("history") || text.equalsIgnoreCase("log")) {
                    toggleHistory();
                } else {
                    onSubmit.accept(text);
                }
            }
        };
        inputField.addActionListener(e -> doSubmit.run());
        sendBtn.addActionListener(e -> doSubmit.run());

        JLabel promptLabel = new JLabel("  >  ");
        promptLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        promptLabel.setForeground(FG_PROMPT);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBackground(BG_INPUT);
        inputRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
        inputRow.add(promptLabel, BorderLayout.WEST);
        inputRow.add(inputField,  BorderLayout.CENTER);
        inputRow.add(sendBtn,     BorderLayout.EAST);

        // ── Assemble ─────────────────────────────────────────────────────────
        add(outputScroll, BorderLayout.CENTER);
        add(sidebar,      BorderLayout.EAST);
        add(inputRow,     BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Public API — called by GuiObserver on the EDT
    // -------------------------------------------------------------------------

    /** The JTextArea that System.out is redirected into. */
    public JTextArea getOutputArea() { return outputArea; }

    public void requestInputFocus() {
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    /** Replace the character-stats block with fresh text. */
    public void updateStats(String text) {
        SwingUtilities.invokeLater(() -> sidebarStats.setText(text));
    }

    /** Replace the quest-log block with fresh text. */
    public void updateQuests(String text) {
        SwingUtilities.invokeLater(() -> sidebarQuests.setText(text));
    }

    /**
     * Submits text to the single-threaded typewriter pool.
     * Decorative lines (borders, spaced-out titles) appear instantly;
     * narrative/dialogue text types out character by character.
     * Called on the EDT via TextAreaStream.
     */
    public void appendText(String text) {
        fullHistory.append(text);
        currentPage.append(text);
        if (!historyMode) {
            final long version = pageVersion.get();
            typewriterPool.submit(() -> typewriteText(text, version));
        }
    }

    /**
     * Clears the visible screen and starts a new page while preserving history.
     * Increments pageVersion so any in-flight typewriter task abandons its output.
     * Must be called on the EDT (GuiObserver uses invokeAndWait).
     */
    public void clearScreen() {
        pageVersion.incrementAndGet();
        fullHistory.append("\n--- [ Previous location ] ---\n\n");
        currentPage.setLength(0);
        outputArea.setText("");
    }

    /**
     * Toggles between the clean current-location view and the full scrollable history.
     * Called on the EDT when the player types "history" or "log".
     */
    public void toggleHistory() {
        pageVersion.incrementAndGet();
        historyMode = !historyMode;
        if (historyMode) {
            outputArea.setText(fullHistory.toString());
            outputArea.append("\n--- [ Showing full history — type 'history' to return ] ---\n");
        } else {
            outputArea.setText(currentPage.toString());
        }
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void handleTab() {
        String current = inputField.getText();
        int spaceIdx   = current.indexOf(' ');
        if (spaceIdx < 0) return; // nothing after the command yet

        // Start a fresh cycle if completions are empty or the text has diverged
        if (tabCompletions.isEmpty() || !current.startsWith(tabBase)) {
            tabBase          = current.substring(0, spaceIdx + 1);        // e.g. "talk "
            String cmd       = current.substring(0, spaceIdx).trim();
            String prefix    = current.substring(spaceIdx + 1).trim();
            List<String> raw = completer.apply(cmd, prefix);
            tabCompletions   = raw.isEmpty() ? Collections.emptyList() : new ArrayList<>(raw);
            if (tabCompletions.isEmpty()) return;
            tabIndex = 0;
        }

        inputField.setText(tabBase + tabCompletions.get(tabIndex));
        tabIndex = (tabIndex + 1) % tabCompletions.size();
    }

    /**
     * Runs on the typewriter pool thread. Splits text into lines and either
     * dispatches them instantly (decorative) or drips them char-by-char (real text).
     * Checks pageVersion before each dispatch so a clearScreen() mid-flight
     * prevents stale characters from appearing on the new page.
     */
    private void typewriteText(String text, long version) {
        StringBuilder line = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (pageVersion.get() != version) return;
            line.append(c);
            if (c == '\n') {
                dispatchLine(line.toString(), version);
                line.setLength(0);
            }
        }
        if (line.length() > 0) dispatchLine(line.toString(), version);
    }

    private void dispatchLine(String line, long version) {
        if (isDecorativeLine(line)) {
            SwingUtilities.invokeLater(() -> {
                if (pageVersion.get() == version) {
                    outputArea.append(line);
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                }
            });
        } else {
            for (char c : line.toCharArray()) {
                if (pageVersion.get() != version) return;
                final char fc = c;
                SwingUtilities.invokeLater(() -> {
                    if (pageVersion.get() == version) {
                        outputArea.append(String.valueOf(fc));
                        outputArea.setCaretPosition(outputArea.getDocument().getLength());
                    }
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

    /**
     * Returns true when a line is purely decorative and should appear instantly.
     * A line is decorative if no letter appears in a run of more than one — so
     * "========", "--------", and spaced-out titles like "T H E   K I N G D O M"
     * are instant, while real words like "darkness" are typewritten.
     */
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

    private JTextArea buildSidebarText() {
        JTextArea area = new JTextArea("—");
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        area.setBackground(BG_SIDEBAR);
        area.setForeground(new Color(175, 175, 195));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(4, 4, 4, 4));
        return area;
    }

    private JPanel buildSidebarBlock(String title, JTextArea content) {
        JLabel header = new JLabel(title);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        header.setForeground(FG_HEADER);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 80)));
        scroll.setPreferredSize(new Dimension(198, 140));

        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setBackground(BG_SIDEBAR);
        block.add(header, BorderLayout.NORTH);
        block.add(scroll,  BorderLayout.CENTER);
        return block;
    }

    private JTextField buildInputField() {
        JTextField field = new JTextField();
        field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        field.setBackground(new Color(30, 35, 50));
        field.setForeground(new Color(220, 220, 240));
        field.setCaretColor(new Color(200, 200, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 100)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return field;
    }

    private JButton buildSendButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        btn.setBackground(new Color(45, 75, 130));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        return btn;
    }
}
