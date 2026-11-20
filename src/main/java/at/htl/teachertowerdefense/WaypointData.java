package at.htl.teachertowerdefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointData {

    // ── MAP 1 ────────────────────────────────────────────────────
    private static final List<Point2D> ROUTE_MAP1;
    static {
        List<Point2D> raw = List.of(
                new Point2D(2.67, 519.33), new Point2D(60.00, 518.67), new Point2D(88.00, 501.33),
                new Point2D(200.67, 501.33), new Point2D(228.00, 488.00), new Point2D(350.00, 485.33),
                new Point2D(376.00, 474.67), new Point2D(412.00, 472.00), new Point2D(437.33, 472.00),
                new Point2D(450.00, 455.33), new Point2D(454.67, 438.67), new Point2D(455.33, 418.00),
                new Point2D(456.00, 334.67), new Point2D(461.33, 327.33), new Point2D(474.00, 320.67),
                new Point2D(488.00, 312.67), new Point2D(550.00, 310.67), new Point2D(622.67, 310.00),
                new Point2D(631.33, 300.67), new Point2D(641.33, 293.33), new Point2D(661.33, 291.33),
                new Point2D(687.33, 289.33), new Point2D(715.33, 289.33), new Point2D(733.33, 291.33),
                new Point2D(748.00, 291.33), new Point2D(768.00, 294.67), new Point2D(796.67, 310.67),
                new Point2D(880.67, 310.00), new Point2D(902.00, 326.00), new Point2D(951.33, 325.33),
                new Point2D(960.00, 325.33)
        );
        List<Point2D> route = new ArrayList<>();
        for (Point2D p : raw) route.add(p.subtract(10, 10));
        ROUTE_MAP1 = Collections.unmodifiableList(route);
    }

    // ── MAP 2: City ──────────────────────────────────────────────
    // Mittellinien der Straßen und Parkplatz-Fahrspuren (aus City.tmx abgelesen):
    // unten rein, über den Parkplatz der Einbahn-Runde nach (rechts hoch, oben links,
    // Mitte runter, unten links raus), dann links hoch, oben um den Brunnen, rechts raus.
    private static final List<Point2D> ROUTE_MAP2 = verschiebe(List.of(
            new Point2D(-8,  592),
            new Point2D(520, 592),
            new Point2D(520, 480),
            new Point2D(440, 480),
            new Point2D(440, 520),
            new Point2D(352, 520),
            new Point2D(352, 222),
            new Point2D(608, 222),
            new Point2D(608, 448),
            new Point2D(968, 448)
    ));

    // ── HTL Saalfelden (Campus.tmx, erzeugt von tools/gen_campus.py) ──
    // Start am Haupteingang der HTL, durch das Tor im Schulzaun, Ziel ist die Bushaltestelle rechts.
    // Muss zu ROUTE in tools/gen_campus.py passen (das Skript gibt diese Werte aus).
    private static final List<Point2D> ROUTE_CAMPUS = verschiebe(List.of(
            new Point2D(520, 136),
            new Point2D(520, 248),
            new Point2D(104, 248),
            new Point2D(104, 376),
            new Point2D(712, 376),
            new Point2D(712, 520),
            new Point2D(984, 520)
    ));

    /** Entity-Position = linke obere Ecke der Hitbox → so verschieben, dass die Schüler mittig laufen. */
    private static List<Point2D> verschiebe(List<Point2D> mittellinie) {
        List<Point2D> route = new ArrayList<>();
        for (Point2D p : mittellinie) route.add(p.subtract(14, 16));
        return Collections.unmodifiableList(route);
    }

    // ── MAP 3: Schulausflug ──────────────────────────────────────
    // Mittellinie des 3 Kacheln breiten Wegs aus Map3.tmx (wird von tools/gen_map3.py erzeugt)
    private static final List<Point2D> ROUTE_MAP3;
    static {
        List<Point2D> raw = List.of(
                new Point2D(-8,  216),
                new Point2D(264, 216),
                new Point2D(264, 504),
                new Point2D(520, 504),
                new Point2D(520, 152),
                new Point2D(744, 152),
                new Point2D(744, 392),
                new Point2D(968, 392)
        );
        List<Point2D> route = new ArrayList<>();
        // Entity-Position = linke obere Ecke der Hitbox → so verschieben, dass die Schüler mittig laufen
        for (Point2D p : raw) route.add(p.subtract(14, 16));
        ROUTE_MAP3 = Collections.unmodifiableList(route);
    }

    public static List<Point2D> getROUTE() {
        return switch (GameConfig.selectedMap) {
            case 1  -> ROUTE_MAP2;
            case 2  -> ROUTE_MAP3;
            case 3  -> ROUTE_MAP1;      // Teichwiese (ursprüngliche erste Map)
            default -> ROUTE_CAMPUS;    // HTL Saalfelden
        };
    }

    public static final List<Point2D> ROUTE = ROUTE_MAP1;

    /**
     * Index i des Wegabschnitts (route[i] → route[i+1]), dem pos am nächsten liegt.
     * Gesucht wird erst ab dem Abschnitt, auf dem der Schüler gestartet ist (nie rückwärts).
     *
     * Wichtig: Abschnitte statt Eckpunkte! Die alte Suche nach dem nächsten Eckpunkt
     * hat Ecken übersprungen – Kinder-Schüler sind dann quer über die Karte gelaufen.
     */
    public static int naechsterAbschnitt(Point2D pos, int abWaypoint) {
        List<Point2D> route = getROUTE();
        int bester = Math.max(0, Math.min(abWaypoint - 1, route.size() - 2));
        double besteDist = Double.MAX_VALUE;
        for (int i = Math.max(0, abWaypoint - 1); i < route.size() - 1; i++) {
            double d = abstandZuAbschnitt(pos, route.get(i), route.get(i + 1));
            if (d < besteDist) { besteDist = d; bester = i; }
        }
        return bester;
    }

    private static double abstandZuAbschnitt(Point2D p, Point2D a, Point2D b) {
        Point2D ab = b.subtract(a);
        double len2 = ab.dotProduct(ab);
        if (len2 == 0) return p.distance(a);
        double t = Math.max(0, Math.min(1, p.subtract(a).dotProduct(ab) / len2));
        return a.add(ab.multiply(t)).distance(p);
    }

    /**
     * Wie weit ein Schüler (Entity-Position) schon entlang der Route gelaufen ist, in Pixeln.
     * Wird zum Zielen verwendet: der Schüler mit dem größten Fortschritt ist "der Erste".
     * Luftlinie zum Ausgang ist bei kurvigen Wegen (City-Map) falsch.
     */
    public static double fortschritt(Point2D pos, int abWaypoint) {
        List<Point2D> route = getROUTE();
        double kumuliert = 0, bester = 0, besteDist = Double.MAX_VALUE;
        for (int i = 0; i < route.size() - 1; i++) {
            Point2D a = route.get(i), b = route.get(i + 1);
            Point2D ab = b.subtract(a);
            double len = ab.magnitude();
            if (i >= abWaypoint - 1 && len > 0) {
                double t = Math.max(0, Math.min(1, pos.subtract(a).dotProduct(ab) / (len * len)));
                double d = a.add(ab.multiply(t)).distance(pos);
                if (d < besteDist) { besteDist = d; bester = kumuliert + t * len; }
            }
            kumuliert += len;
        }
        return bester;
    }

    public static List<Point2D> routeAbIndex(int startIndex) {
        List<Point2D> route = getROUTE();
        return new ArrayList<>(route.subList(startIndex, route.size()));
    }
}