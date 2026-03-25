package at.htl.teachertowerdefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zentrale Wegpunkt-Listen für alle Maps.
 * Auswahl über GameConfig.selectedMap.
 */
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
    // Pfad aus City.tmx: Polyline bei x=1, y=608.667
    private static final List<Point2D> ROUTE_MAP2;
    static {
        List<Point2D> raw = List.of(
                new Point2D(1,   609),   // Spawn links
                new Point2D(520, 609),   // nach rechts
                new Point2D(520, 480),   // nach oben
                new Point2D(440, 480),   // nach links
                new Point2D(440, 520),   // nach unten
                new Point2D(368, 520),   // nach links
                new Point2D(368, 239),   // nach oben
                new Point2D(592, 240),   // nach rechts
                new Point2D(592, 463),   // nach unten
                new Point2D(960, 464)    // nach rechts – Ziel
        );
        List<Point2D> route = new ArrayList<>();
        for (Point2D p : raw) route.add(p.subtract(8, 8));
        ROUTE_MAP2 = Collections.unmodifiableList(route);
    }

    // ── Aktive Route je nach Map ─────────────────────────────────

    public static List<Point2D> getROUTE() {
        return GameConfig.selectedMap == 1 ? ROUTE_MAP2 : ROUTE_MAP1;
    }

    // Rückwärtskompatibel
    public static final List<Point2D> ROUTE = ROUTE_MAP1;

    public static int naechsterWaypointIndex(double x, double y) {
        List<Point2D> route = getROUTE();
        double minDist = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < route.size(); i++) {
            double dist = route.get(i).distance(x, y);
            if (dist < minDist) { minDist = dist; index = i; }
        }
        return Math.min(index + 1, route.size() - 1);
    }

    public static List<Point2D> routeAbIndex(int startIndex) {
        List<Point2D> route = getROUTE();
        return new ArrayList<>(route.subList(startIndex, route.size()));
    }
}