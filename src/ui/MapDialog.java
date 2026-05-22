package ui;

import world.LocationId;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MapDialog — a non-modal popup showing the full world map.
 *
 * Fog-of-war: unvisited locations appear as "???" until the player enters them.
 * Each visited node shows its level requirement so the player knows when they
 * can return to unexplored branches.
 *
 * Two regions: DIVINE REALM (top) and MAIN WORLD (bottom), separated by a
 * dashed divider.  Line styles: solid = normal, red dashed = locked passage,
 * cyan dashed = portal / divine crossing.
 */
public class MapDialog extends JDialog {

    public MapDialog(JFrame owner, LocationId currentLocation,
                     Set<LocationId> visited, Map<LocationId, Integer> levelReqs) {
        super(owner, "World Map — The Fallen Kingdom", false);
        setContentPane(new MapCanvas(currentLocation, visited, levelReqs));
        setSize(840, 730);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    // =========================================================================
    // Data types
    // =========================================================================

    private static class Node {
        final String     label;
        final LocationId id;
        final int        cx, cy;

        Node(String label, LocationId id, int cx, int cy) {
            this.label = label; this.id = id; this.cx = cx; this.cy = cy;
        }
    }

    private enum EdgeType { NORMAL, LOCKED, PORTAL }

    private static class Edge {
        final Node a, b; final EdgeType type;
        Edge(Node a, Node b, EdgeType type) { this.a = a; this.b = b; this.type = type; }
    }

    // =========================================================================
    // Canvas
    // =========================================================================

    private class MapCanvas extends JPanel {

        private final Color BG          = new Color(10,  8, 20);
        private final Color BG_NODE     = new Color(22, 18, 42);
        private final Color BG_UNKNOWN  = new Color(14, 12, 26);
        private final Color BG_CURRENT  = new Color(55, 42,  8);
        private final Color BDR_NODE    = new Color(70, 60,105);
        private final Color BDR_CURR    = new Color(220,175, 50);
        private final Color BDR_UNKNOWN = new Color(40, 38, 60);
        private final Color FG_NODE     = new Color(195,195,215);
        private final Color FG_CURR     = new Color(240,210, 90);
        private final Color FG_UNKNOWN  = new Color(65, 62, 90);
        private final Color FG_LEVEL    = new Color(120,140,180);
        private final Color FG_TITLE    = new Color(200,170, 80);
        private final Color FG_REGION   = new Color(120,170,255);
        private final Color FG_DIM      = new Color(120,120,145);
        private final Color LINE_NORMAL = new Color(80, 72,115);
        private final Color LINE_LOCKED = new Color(155, 45, 45);
        private final Color LINE_PORTAL = new Color( 75,190,255);
        private final Color DIVIDER     = new Color(48, 38, 76);

        private static final int NODE_W = 116;
        private static final int NODE_H = 36;

        private final LocationId          current;
        private final Set<LocationId>     visited;
        private final Map<LocationId,Integer> levels;
        private final List<Node>          nodes = new ArrayList<>();
        private final List<Edge>          edges = new ArrayList<>();

        MapCanvas(LocationId current, Set<LocationId> visited, Map<LocationId,Integer> levels) {
            this.current = current;
            this.visited = visited;
            this.levels  = levels;
            setBackground(BG);
            buildGraph();
        }

        // ── Build node + edge lists ──────────────────────────────────────────

        private void buildGraph() {

            // ── Divine Realm ─────────────────────────────────────────────────
            //
            //  [Vault of Fallen] ── [Radiant Cathedral] ── [Celestial Barracks]
            //                               │                      │
            //                       [Sunken Shrine] ─ [Forge]   [Sanctum]
            //                               │
            //                       [Celestial Gate]  ← portal from Forgotten Bfield

            Node vault     = node("Vault of Fallen",   LocationId.VAULT_OF_THE_FALLEN,     200, 118);
            Node cathedral = node("Radiant Cathedral", LocationId.RADIANT_CATHEDRAL,        370, 118);
            Node barracks2 = node("Celestial Barracks",LocationId.CELESTIAL_BARRACKS,       635, 118);
            Node shrine    = node("Sunken Shrine",     LocationId.SUNKEN_SHRINE,            370, 191);
            Node forge     = node("Divine Forge",      LocationId.DIVINE_FORGE,             505, 191);
            Node sanctum   = node("Sanctum (Arbiter)", LocationId.SANCTUM_OF_THE_ARBITER,   635, 191);
            Node gate      = node("Celestial Gate",    LocationId.CELESTIAL_GATE,           370, 261);

            edge(vault,  cathedral,  EdgeType.NORMAL);
            edge(cathedral, barracks2, EdgeType.NORMAL);
            edge(cathedral, shrine,  EdgeType.NORMAL);
            edge(shrine,  forge,     EdgeType.NORMAL);
            edge(barracks2, sanctum, EdgeType.NORMAL);
            edge(gate,  shrine,      EdgeType.NORMAL);

            // ── Main World ────────────────────────────────────────────────────
            //
            //                        [Shadow Throne]
            //                              │ locked
            //  [Cursed Archives] ─ [Corrupted Castle] ─ [Shadow Barracks]
            //                              │ locked
            //  [Ancient Ruins] ─ [Forgotten Bfield]   [Underground Dungeon]
            //          │               ↑ portal               │
            //       [Dark Forest] ─────────────────────────────
            //        │        │
            //    [Village]  [Mystic Glade]
            //        │
            //  [Merchant's Village]
            //        │
            //  [Proving Grounds]

            Node throne    = node("Shadow Throne",      LocationId.SHADOW_THRONE,           570, 336);
            Node archives  = node("Cursed Archives",    LocationId.CURSED_ARCHIVES,         415, 401);
            Node castle    = node("Corrupted Castle",   LocationId.CORRUPTED_CASTLE,        570, 401);
            Node sbarracks = node("Shadow Barracks",    LocationId.SHADOW_BARRACKS,         725, 401);
            Node dungeon   = node("Underground Dungeon",LocationId.UNDERGROUND_DUNGEON,     570, 534);
            Node ruins     = node("Ancient Ruins",      LocationId.ANCIENT_RUINS,           308, 468);
            Node bfield    = node("Forgotten Bfield",   LocationId.FORGOTTEN_BATTLEFIELD,   440, 468);
            Node forest    = node("Dark Forest",        LocationId.DARK_FOREST,             308, 534);
            Node village   = node("Village",            LocationId.VILLAGE,                 165, 534);
            Node glade     = node("Mystic Glade",       LocationId.MYSTIC_GLADE,            308, 604);
            Node mvillage  = node("Merchant's Village", LocationId.MERCHANT_VILLAGE,        165, 604);
            Node proving   = node("Proving Grounds",    LocationId.PROVING_GROUNDS,         165, 664);

            edge(throne,   castle,    EdgeType.LOCKED);
            edge(archives, castle,    EdgeType.NORMAL);
            edge(castle,   sbarracks, EdgeType.NORMAL);
            edge(castle,   dungeon,   EdgeType.LOCKED);
            edge(dungeon,  forest,    EdgeType.NORMAL);
            edge(ruins,    bfield,    EdgeType.NORMAL);
            edge(ruins,    forest,    EdgeType.NORMAL);
            edge(forest,   village,   EdgeType.NORMAL);
            edge(forest,   glade,     EdgeType.NORMAL);
            edge(village,  mvillage,  EdgeType.NORMAL);
            edge(mvillage, proving,   EdgeType.NORMAL);
            edge(bfield,   gate,      EdgeType.PORTAL);
        }

        private Node node(String label, LocationId id, int cx, int cy) {
            Node n = new Node(label, id, cx, cy);
            nodes.add(n);
            return n;
        }

        private void edge(Node a, Node b, EdgeType type) {
            edges.add(new Edge(a, b, type));
        }

        // ── Paint ────────────────────────────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawTitle(g2);
            drawRegionLabel(g2, "— DIVINE REALM —", 20,  56);
            drawRegionLabel(g2, "— MAIN WORLD —",   20, 312);
            drawDivider(g2, 300);
            drawEdges(g2);
            drawNodes(g2);
            drawLegend(g2);
        }

        private void drawTitle(Graphics2D g2) {
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            g2.setColor(FG_TITLE);
            String t = "T H E   F A L L E N   K I N G D O M  —  W O R L D   M A P";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, 32);
        }

        private void drawRegionLabel(Graphics2D g2, String text, int x, int y) {
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            g2.setColor(FG_REGION);
            g2.drawString(text, x, y);
        }

        private void drawDivider(Graphics2D g2, int y) {
            g2.setColor(DIVIDER);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{4f, 4f}, 0f));
            g2.drawLine(20, y, getWidth() - 20, y);
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawEdges(Graphics2D g2) {
            for (Edge e : edges) {
                switch (e.type) {
                    case LOCKED:
                        g2.setColor(LINE_LOCKED);
                        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
                        break;
                    case PORTAL:
                        g2.setColor(LINE_PORTAL);
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER, 10f, new float[]{6f, 3f}, 0f));
                        break;
                    default:
                        g2.setColor(LINE_NORMAL);
                        g2.setStroke(new BasicStroke(1.5f));
                        break;
                }
                g2.drawLine(e.a.cx, e.a.cy, e.b.cx, e.b.cy);

                if (e.type == EdgeType.PORTAL) {
                    int mx = (e.a.cx + e.b.cx) / 2 + 6;
                    int my = (e.a.cy + e.b.cy) / 2 - 3;
                    g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
                    g2.setColor(LINE_PORTAL);
                    g2.drawString("PORTAL", mx, my);
                }
            }
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawNodes(Graphics2D g2) {
            Font nameFont  = new Font(Font.MONOSPACED, Font.PLAIN,  10);
            Font levelFont = new Font(Font.MONOSPACED, Font.BOLD,    9);

            for (Node n : nodes) {
                boolean isCurrent  = n.id == current;
                boolean isVisited  = visited.contains(n.id);
                boolean isKnown    = isCurrent || isVisited;

                int x = n.cx - NODE_W / 2;
                int y = n.cy - NODE_H / 2;

                // ── Box fill + border ────────────────────────────────────────
                g2.setColor(isCurrent ? BG_CURRENT : isKnown ? BG_NODE : BG_UNKNOWN);
                g2.fillRoundRect(x, y, NODE_W, NODE_H, 8, 8);

                g2.setStroke(new BasicStroke(isCurrent ? 2f : 1f));
                g2.setColor(isCurrent ? BDR_CURR : isKnown ? BDR_NODE : BDR_UNKNOWN);
                g2.drawRoundRect(x, y, NODE_W, NODE_H, 8, 8);
                g2.setStroke(new BasicStroke(1f));

                if (!isKnown) {
                    // ── Unknown: just show ??? centred ───────────────────────
                    g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                    g2.setColor(FG_UNKNOWN);
                    FontMetrics fm = g2.getFontMetrics();
                    String q = "???";
                    g2.drawString(q, x + (NODE_W - fm.stringWidth(q)) / 2,
                            y + (NODE_H + fm.getAscent() - fm.getDescent()) / 2 - 1);
                } else {
                    // ── Known: name on top line, Lv. on bottom line ──────────
                    g2.setFont(nameFont);
                    FontMetrics fmN = g2.getFontMetrics();

                    String label = isCurrent ? "★ " + n.label : n.label;
                    int maxW = NODE_W - 8;
                    while (fmN.stringWidth(label) > maxW && label.length() > 4)
                        label = label.substring(0, label.length() - 1);
                    if (fmN.stringWidth(n.label) > maxW)
                        label = label.substring(0, label.length() - 1) + "…";

                    g2.setColor(isCurrent ? FG_CURR : FG_NODE);
                    int tx = x + (NODE_W - fmN.stringWidth(label)) / 2;
                    g2.drawString(label, tx, y + 13);

                    // Level requirement on second line
                    Integer req = levels.get(n.id);
                    if (req != null) {
                        g2.setFont(levelFont);
                        FontMetrics fmL = g2.getFontMetrics();
                        String lvl = "Lv. " + req + "+";
                        g2.setColor(FG_LEVEL);
                        g2.drawString(lvl, x + (NODE_W - fmL.stringWidth(lvl)) / 2, y + 27);
                    }
                }
            }
        }

        private void drawLegend(Graphics2D g2) {
            int by = getHeight() - 14;
            int x  = 24;
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

            // Current location
            g2.setColor(BDR_CURR);
            g2.fillRect(x, by - 9, 11, 11);
            g2.setColor(FG_DIM);
            g2.drawString("Current location", x + 15, by);
            x += 152;

            // Unvisited
            g2.setColor(BDR_UNKNOWN);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, by - 9, 11, 11);
            g2.setColor(FG_UNKNOWN);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
            g2.drawString("?", x + 3, by - 1);
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            g2.setColor(FG_DIM);
            g2.drawString("Unvisited area", x + 15, by);
            x += 145;

            // Locked passage
            g2.setColor(LINE_LOCKED);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{4f, 4f}, 0f));
            g2.drawLine(x, by - 4, x + 22, by - 4);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(FG_DIM);
            g2.drawString("Locked passage", x + 26, by);
            x += 158;

            // Portal
            g2.setColor(LINE_PORTAL);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 3f}, 0f));
            g2.drawLine(x, by - 4, x + 22, by - 4);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(FG_DIM);
            g2.drawString("Portal / divine passage", x + 26, by);
        }
    }
}
