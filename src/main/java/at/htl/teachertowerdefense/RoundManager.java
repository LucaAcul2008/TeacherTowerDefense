package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet das Runden-System.
 * Jede Runde besteht aus einer Liste von SpawnGruppen.
 * Eine SpawnGruppe definiert: welcher Typ, wie viele, mit welchem Abstand.
 */
public class RoundManager {

    // --- RUNDEN-DEFINITION ---
    private static final List<List<SpawnGruppe>> RUNDEN = new ArrayList<>();

    static {
        // Runde 1 – nur Erstklässler
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP1, 8, 1.5)
        ));

        // Runde 2 – Erst- und Zweitklässler
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP1, 5, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP2, 5, 1.5)
        ));

        // Runde 3 – bis Drittklässler
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP2, 5, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP3, 5, 1.5)
        ));

        // Runde 4 – Viertklässler tauchen auf
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP2, 4, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP3, 4, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP4, 3, 1.5)
        ));

        // Runde 5 – erste Fünftklässler (mittelgroß)
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP3, 5, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP5, 3, 2.0)
        ));

        // Runde 6 – Sechstklässler
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP4, 5, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP5, 3, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP6, 2, 2.5)
        ));

        // Runde 7 – erster Maturant (groß!)
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 4, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP6, 3, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 1, 3.0)
        ));

        // Runde 8 – mehrere Maturanten
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 4, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 3.0)
        ));

        // Runde 9 – erster Schulleiter
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 3, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 3.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 1, 4.0)
        ));

        // Runde 10 – Boss-Runde
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 4, 2.5),
                new SpawnGruppe(SchuelerTyp.TYP8, 3, 4.0)
        ));
    }

    // --- ZUSTAND ---
    private int  aktuelleRunde = 0; // 0-basiert intern
    private boolean rundeAktiv  = false;
    private Runnable onRundeEnde;

    public void setOnRundeEnde(Runnable r) { this.onRundeEnde = r; }

    public int getAktuelleRundeAnzeige() { return aktuelleRunde + 1; }
    public int getMaxRunden()            { return RUNDEN.size(); }
    public boolean isRundeAktiv()        { return rundeAktiv; }
    public boolean isSpielEnde()         { return aktuelleRunde >= RUNDEN.size(); }

    /**
     * Startet die nächste Runde – spawnt alle Schüler mit korrekten Delays.
     */
    public void starteNaechsteRunde() {
        if (rundeAktiv || isSpielEnde()) return;

        rundeAktiv = true;
        List<SpawnGruppe> gruppen = RUNDEN.get(aktuelleRunde);

        // Berechne Gesamtdelay pro Schüler (gestapelt über alle Gruppen)
        double zeitOffset = 0;

        for (SpawnGruppe gruppe : gruppen) {
            for (int i = 0; i < gruppe.anzahl(); i++) {
                final SchuelerTyp typ = gruppe.typ();
                final double delay = zeitOffset;

                FXGL.getGameTimer().runOnceAfter(() -> {
                    FXGL.spawn("Schueler",
                            new SpawnData(WaypointData.ROUTE.get(0).getX(),
                                          WaypointData.ROUTE.get(0).getY())
                                    .put("typ", typ)
                                    .put("startWaypoint", 0)
                    );
                }, Duration.seconds(delay));

                zeitOffset += gruppe.delay();
            }
        }

        // Nach dem letzten Spawn prüfen ob die Runde beendet ist
        final double endeDelay = zeitOffset + 1.0;
        FXGL.getGameTimer().runOnceAfter(() -> pruefeRundeEnde(), Duration.seconds(endeDelay));
    }

    /**
     * Prüft ob noch Schüler auf dem Feld sind.
     * Falls nicht → Runde beendet.
     */
    private void pruefeRundeEnde() {
        int schuelerAnzahl = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.SCHUELER).size();

        if (schuelerAnzahl == 0) {
            rundeAktiv = false;
            aktuelleRunde++;
            if (onRundeEnde != null) onRundeEnde.run();
        } else {
            // Noch Schüler da → nochmal in 1 Sekunde prüfen
            FXGL.getGameTimer().runOnceAfter(() -> pruefeRundeEnde(), Duration.seconds(1));
        }
    }

    // --- INNERE KLASSE ---
    public record SpawnGruppe(SchuelerTyp typ, int anzahl, double delay) {}
}
