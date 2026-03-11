package at.htl.teachertowerdefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zentrale Wegpunkt-Liste für alle Schüler.
 * Kinder spawnen an der Position ihres Elternteils und setzen
 * die Route ab dem nächsten Wegpunkt fort.
 */
public class WaypointData {

    public static final List<Point2D> ROUTE;

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
        ROUTE = Collections.unmodifiableList(route);
    }

    /**
     * Gibt den Index des nächsten Wegpunkts zurück, der am nächsten
     * zur gegebenen Position liegt. Kinder starten ab dort.
     */
    public static int naechsterWaypointIndex(double x, double y) {
        double minDist = Double.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < ROUTE.size(); i++) {
            double dist = ROUTE.get(i).distance(x, y);
            if (dist < minDist) {
                minDist = dist;
                index = i;
            }
        }

        // Einen Schritt weiter damit die Kinder nicht rückwärts laufen
        return Math.min(index + 1, ROUTE.size() - 1);
    }

    /**
     * Gibt die verbleibende Route ab einem bestimmten Index zurück.
     */
    public static List<Point2D> routeAbIndex(int startIndex) {
        return new ArrayList<>(ROUTE.subList(startIndex, ROUTE.size()));
    }
}
