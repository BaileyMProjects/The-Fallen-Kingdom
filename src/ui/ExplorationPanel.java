package ui;

import javax.swing.*;
import java.awt.*;
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

    private final JTextArea outputArea;
    private final JTextArea sidebarStats;
    private final JTextArea sidebarQuests;
    private final JTextField inputField;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ExplorationPanel(Consumer<String> onSubmit) {
        super(new BorderLayout(4, 4));
        setBackground(BG_MAIN);

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
        JButton sendBtn = buildSendButton("Enter");

        Runnable doSubmit = () -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                inputField.setText("");
                onSubmit.accept(text);
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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
