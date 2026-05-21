package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {

    private static final List<List<SpawnGruppe>> RUNDEN = new ArrayList<>();

    static {
        // Runde 1 – nur Erstklässler
        RUNDEN.add(List.of(new SpawnGruppe(SchuelerTyp.TYP1, 8, 0.8)));

        // Runde 2
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP1, 5, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP2, 5, 0.8)
        ));

        // Runde 3
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP2, 5, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP3, 5, 0.8)
        ));

        // Runde 4
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP2, 2, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP3, 2, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP4, 3, 0.8)
        ));

        // Runde 5
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP3, 5, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP5, 3, 2.0)
        ));

        // Runde 6
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP2, 5, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP5, 3, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP6, 1, 1.5)
        ));

        // Runde 7
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 2, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP6, 3, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 1, 3.0)
        ));

        // Runde 8
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 2, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 3.0)
        ));

        // Runde 9
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 3, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 3.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 1, 2.0)
        ));

        // Runde 10 – Boss
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 2, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP8, 3, 2.0)
        ));

        // Runde 11
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 4, 0.7),
                new SpawnGruppe(SchuelerTyp.TYP6, 3, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP7, 2, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 1, 2.5)
        ));

        // Runde 12
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 4, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 2, 3.0)
        ));

        // Runde 13
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP3, 8, 0.5),
                new SpawnGruppe(SchuelerTyp.TYP5, 4, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP7, 2, 2.5)
        ));

        // Runde 14
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 4, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 2.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 2, 2.5)
        ));

        // Runde 15 – EASY BOSS
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 5, 0.7),
                new SpawnGruppe(SchuelerTyp.TYP7, 4, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP8, 3, 2.0)
        ));

        // Runde 16
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 6, 0.6),
                new SpawnGruppe(SchuelerTyp.TYP7, 3, 1.5),
                new SpawnGruppe(SchuelerTyp.TYP8, 3, 2.0)
        ));

        // Runde 17
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 5, 1.2),
                new SpawnGruppe(SchuelerTyp.TYP8, 4, 1.8)
        ));

        // Runde 18
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP4, 8, 0.5),
                new SpawnGruppe(SchuelerTyp.TYP6, 4, 0.7),
                new SpawnGruppe(SchuelerTyp.TYP8, 3, 2.0)
        ));

        // Runde 19
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 4, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 5, 1.5)
        ));

        // Runde 20 – MEDIUM BOSS
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 6, 0.6),
                new SpawnGruppe(SchuelerTyp.TYP7, 5, 1.2),
                new SpawnGruppe(SchuelerTyp.TYP8, 4, 1.5)
        ));

        // Runde 21
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 8, 0.5),
                new SpawnGruppe(SchuelerTyp.TYP7, 5, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 4, 1.5)
        ));

        // Runde 22
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 6, 0.6),
                new SpawnGruppe(SchuelerTyp.TYP7, 6, 1.0),
                new SpawnGruppe(SchuelerTyp.TYP8, 4, 1.5)
        ));

        // Runde 23
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP7, 6, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP8, 6, 1.2)
        ));

        // Runde 24
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP5, 6, 0.5),
                new SpawnGruppe(SchuelerTyp.TYP6, 5, 0.6),
                new SpawnGruppe(SchuelerTyp.TYP7, 5, 0.9),
                new SpawnGruppe(SchuelerTyp.TYP8, 5, 1.2)
        ));

        // Runde 25 – HARD BOSS
        RUNDEN.add(List.of(
                new SpawnGruppe(SchuelerTyp.TYP6, 8, 0.5),
                new SpawnGruppe(SchuelerTyp.TYP7, 7, 0.8),
                new SpawnGruppe(SchuelerTyp.TYP8, 7, 1.0)
        ));
    }

    private int  aktuelleRunde = 0;
    private boolean rundeAktiv  = false;
    private Runnable onRundeEnde;

    public void setOnRundeEnde(Runnable r) { this.onRundeEnde = r; }

    public int getAktuelleRundeAnzeige() { return aktuelleRunde + 1; }

    public int getMaxRunden() {
        return switch (GameConfig.selectedDiff) {
            case 1  -> 20; // Medium
            case 2  -> 25; // Hard
            default -> 15; // Easy
        };
    }

    public boolean isRundeAktiv()  { return rundeAktiv; }
    public boolean isSpielEnde()   { return aktuelleRunde >= getMaxRunden(); }

    public void starteNaechsteRunde() {
        if (rundeAktiv || isSpielEnde()) return;

        rundeAktiv = true;
        notifiziereGassnerRundeStart();

        List<SpawnGruppe> gruppen = RUNDEN.get(aktuelleRunde);
        double zeitOffset = 0;

        for (SpawnGruppe gruppe : gruppen) {
            for (int i = 0; i < gruppe.anzahl(); i++) {
                final SchuelerTyp typ = gruppe.typ();
                final double delay = zeitOffset;
                FXGL.getGameTimer().runOnceAfter(() -> {
                    FXGL.spawn("Schueler",
                            new SpawnData(WaypointData.getROUTE().get(0).getX(),
                                    WaypointData.getROUTE().get(0).getY())
                                    .put("typ", typ)
                                    .put("startWaypoint", 0)
                    );
                }, Duration.seconds(delay));
                zeitOffset += gruppe.delay();
            }
        }

        FXGL.getGameTimer().runOnceAfter(() -> notifiziereGassnerPayout(),
                Duration.seconds(0.5));

        double mitte = zeitOffset / 2.0;
        FXGL.getGameTimer().runOnceAfter(() -> notifiziereGassnerPayout(),
                Duration.seconds(Math.max(mitte, 2.0)));

        final double endeDelay = zeitOffset + 1.0;
        FXGL.getGameTimer().runOnceAfter(() -> pruefeRundeEnde(), Duration.seconds(endeDelay));
    }

    private void pruefeRundeEnde() {
        int schuelerAnzahl = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.SCHUELER).size();
        if (schuelerAnzahl == 0) {
            rundeAktiv = false;
            aktuelleRunde++;
            if (onRundeEnde != null) onRundeEnde.run();
        } else {
            FXGL.getGameTimer().runOnceAfter(() -> pruefeRundeEnde(), Duration.seconds(1));
        }
    }

    private void notifiziereGassnerRundeStart() {
        FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER).forEach(e -> {
            if (e.hasComponent(GassnerComponent.class))
                e.getComponent(GassnerComponent.class).onRundeStart();
        });
    }

    private void notifiziereGassnerPayout() {
        FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER).forEach(e -> {
            if (e.hasComponent(GassnerComponent.class))
                e.getComponent(GassnerComponent.class).notifyRoundPayout();
        });
    }

    public record SpawnGruppe(SchuelerTyp typ, int anzahl, double delay) {}
}