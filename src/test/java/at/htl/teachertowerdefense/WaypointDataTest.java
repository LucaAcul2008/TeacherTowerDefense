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
    @ValueSource(ints = {0, 1, 2, 3})
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
    @ValueSource(ints = {0, 1, 2, 3})
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
    @ValueSource(ints = {0, 1, 2, 3})
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

    /**
     * Die generierten Maps (Campus = 0, Schulausflug = 2) enthalten den Weg als "Pfad"-Linie.
     * Die Route im Code muss genau darauf liegen – sonst laufen die Schüler neben dem Weg.
     */
    @ParameterizedTest(name = "Map {0}")
    @ValueSource(ints = {0, 2})
    void routeLiegtAufDemWegDerMapDatei(int map) throws Exception {
        GameConfig.selectedMap = map;
        String datei = "/assets/levels/" + GameConfig.getMapDatei();
        String xml;
        try (var in = WaypointDataTest.class.getResourceAsStream(datei)) {
            assertNotNull(in, datei + " fehlt");
            xml = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        var m = java.util.regex.Pattern.compile(
                "type=\"Pfad\" x=\"([-\\d.]+)\" y=\"([-\\d.]+)\">\\s*<polyline points=\"([^\"]+)\"").matcher(xml);
        assertTrue(m.find(), "Pfad-Linie nicht gefunden");
        double x0 = Double.parseDouble(m.group(1)), y0 = Double.parseDouble(m.group(2));
        String[] punkte = m.group(3).split(" ");

        List<Point2D> route = WaypointData.getROUTE();
        assertEquals(punkte.length, route.size(), "Anzahl Wegpunkte");
        for (int i = 0; i < punkte.length; i++) {
            String[] xy = punkte[i].split(",");
            Point2D mitte = new Point2D(x0 + Double.parseDouble(xy[0]), y0 + Double.parseDouble(xy[1]));
            Point2D erwartet = mitte.subtract(14, 16);   // Entity-Position = Mitte minus halbe Hitbox
            assertEquals(erwartet.getX(), route.get(i).getX(), 0.01, "Wegpunkt " + i + " x");
            assertEquals(erwartet.getY(), route.get(i).getY(), 0.01, "Wegpunkt " + i + " y");
        }
    }

    private static Point2D punktAuf(List<Point2D> route, int abschnitt, double t) {
        Point2D a = route.get(abschnitt), b = route.get(abschnitt + 1);
        return a.add(b.subtract(a).multiply(t));
    }
}
