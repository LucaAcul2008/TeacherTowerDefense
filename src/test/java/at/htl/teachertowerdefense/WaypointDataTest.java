package at.htl.teachertowerdefense;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prüft die Weg-Berechnung, die beim Zerfallen von Schülern verwendet wird.
 * Früher wurde der nächste Eckpunkt gesucht und dann der übernächste genommen –
 * dadurch sind Kinder-Schüler über Ecken abgekürzt und quer über die Karte gelaufen.
 */
class WaypointDataTest {

    private static final double[] ANTEILE = {0.02, 0.25, 0.5, 0.75, 0.98};

    @AfterEach
    void zuruecksetzen() {
        GameConfig.selectedMap = 0;
    }

    @ParameterizedTest(name = "Map {0}")
    @ValueSource(ints = {0, 1, 2})
    void jederPunktGehoertZuSeinemAbschnitt(int map) {
        GameConfig.selectedMap = map;
        List<Point2D> route = WaypointData.getROUTE();

        for (int i = 0; i < route.size() - 1; i++) {
            for (double t : ANTEILE) {
                Point2D p = punktAuf(route, i, t);
                assertEquals(i, WaypointData.naechsterAbschnitt(p, 0),
                        "Map " + map + ", Abschnitt " + i + ", Anteil " + t);
                // Auch Kinder von Kindern (Start mitten in der Route) finden den richtigen Abschnitt
                assertEquals(i, WaypointData.naechsterAbschnitt(p, i),
                        "Map " + map + ", Abschnitt " + i + " (Start ab " + i + "), Anteil " + t);
            }
        }
    }

    @ParameterizedTest(name = "Map {0}")
    @ValueSource(ints = {0, 1, 2})
    void kindLaeuftZurNaechstenEckeUndUeberspringtSieNicht(int map) {
        GameConfig.selectedMap = map;
        List<Point2D> route = WaypointData.getROUTE();

        for (int i = 0; i < route.size() - 1; i++) {
            // Kurz vor der Ecke – genau hier hat die alte Berechnung die Ecke übersprungen
            Point2D p = punktAuf(route, i, 0.9);
            int ziel = WaypointData.naechsterAbschnitt(p, 0) + 1;
            assertEquals(i + 1, ziel, "Map " + map + ": Kind muss zur Ecke " + (i + 1) + " laufen");
            // Erster Schritt des Kindes darf nicht weiter sein als der Rest des Abschnitts
            assertTrue(p.distance(route.get(ziel)) <= route.get(i).distance(route.get(i + 1)) + 1e-6);
        }
    }

    @ParameterizedTest(name = "Map {0}")
    @ValueSource(ints = {0, 1, 2})
    void fortschrittWaechstEntlangDerRoute(int map) {
        GameConfig.selectedMap = map;
        List<Point2D> route = WaypointData.getROUTE();

        double vorher = -1;
        for (int i = 0; i < route.size() - 1; i++) {
            for (double t : ANTEILE) {
                double f = WaypointData.fortschritt(punktAuf(route, i, t), 0);
                assertTrue(f > vorher, "Map " + map + ": Fortschritt muss wachsen (Abschnitt " + i + ")");
                vorher = f;
            }
        }
    }

    private static Point2D punktAuf(List<Point2D> route, int abschnitt, double t) {
        Point2D a = route.get(abschnitt), b = route.get(abschnitt + 1);
        return a.add(b.subtract(a).multiply(t));
    }
}
