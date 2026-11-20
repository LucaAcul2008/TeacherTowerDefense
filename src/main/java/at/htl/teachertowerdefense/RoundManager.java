package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;

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
    private boolean gestoppt    = false;
    private Runnable onRundeEnde;

    // Zeitplan der laufenden Runde – läuft über Spielzeit (× speedMulti),
    // damit 2x-Tempo auch die Spawn-Abstände halbiert und Pausen respektiert werden.
    private final List<GeplanterSpawn> spawnQueue  = new ArrayList<>();
    private final List<Double>         payoutQueue = new ArrayList<>();
    private double rundenZeit = 0;
    private double endeZeit   = 0;

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

    /** Stoppt alle weiteren Spawns (z.B. bei Game Over). */
    public void stoppen() {
        gestoppt = true;
        rundeAktiv = false;
        spawnQueue.clear();
        payoutQueue.clear();
    }

    /** Kurzbeschreibung der nächsten Runde, z.B. "8× Erstklässler · 2× Maturant". */
    public String getNaechsteRundeBeschreibung() {
        if (isSpielEnde()) return "";
        StringBuilder sb = new StringBuilder();
        for (SpawnGruppe g : RUNDEN.get(aktuelleRunde)) {
            if (!sb.isEmpty()) sb.append("  ·  ");
            sb.append(g.anzahl()).append("× ").append(g.typ().anzeigeName);
        }
        return sb.toString();
    }

    public void starteNaechsteRunde() {
        if (rundeAktiv || isSpielEnde() || gestoppt) return;

        rundeAktiv = true;
        rundenZeit = 0;
        spawnQueue.clear();
        payoutQueue.clear();
        notifiziereGassnerRundeStart();

        double zeitOffset = 0;
        for (SpawnGruppe gruppe : RUNDEN.get(aktuelleRunde)) {
            for (int i = 0; i < gruppe.anzahl(); i++) {
                spawnQueue.add(new GeplanterSpawn(zeitOffset, gruppe.typ()));
                zeitOffset += gruppe.delay();
            }
        }

        payoutQueue.add(0.5);
        payoutQueue.add(Math.max(zeitOffset / 2.0, 2.0));
        endeZeit = zeitOffset + 1.0;
    }

    /** Wird jeden Frame aus der App aufgerufen. */
    public void update(double tpf) {
        if (!rundeAktiv || gestoppt) return;
        rundenZeit += tpf * GameConfig.speedMulti;

        while (!spawnQueue.isEmpty() && spawnQueue.get(0).zeit() <= rundenZeit) {
            spawne(spawnQueue.remove(0).typ());
        }
        while (!payoutQueue.isEmpty() && payoutQueue.get(0) <= rundenZeit) {
            payoutQueue.remove(0);
            notifiziereGassnerPayout();
        }

        if (rundenZeit >= endeZeit && spawnQueue.isEmpty()
                && FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER).isEmpty()) {
            rundeAktiv = false;
            aktuelleRunde++;
            if (onRundeEnde != null) onRundeEnde.run();
        }
    }

    private void spawne(SchuelerTyp typ) {
        FXGL.spawn("Schueler",
                new SpawnData(WaypointData.getROUTE().get(0).getX(),
                        WaypointData.getROUTE().get(0).getY())
                        .put("typ", typ)
                        .put("startWaypoint", 0)
        );
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
    private record GeplanterSpawn(double zeit, SchuelerTyp typ) {}
}