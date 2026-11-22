# Grizzly Bear Mod (Forge 1.20.1)

- Grizzlybär spawnt in allen Taiga-Biomen (`#minecraft:is_taiga`), einzeln oder zu zweit
- Greift Spieler automatisch an (Reichweite 24 Blöcke), Schaden 8, 40 HP
- Wird er von einem Projektil getroffen (Pfeil, Dreizack, Schneeball ...), flieht er 15 Sekunden lang
- Spawn-Ei im Kreativ-Tab "Spawn-Eier", Drops: Rindfleisch + Leder

## Bauen
Mit GitHub Actions: Ordner in ein Repo pushen, unter Actions das Artifact `grizzlymod` (JAR) laden.
Lokal: Gradle 8.x + JDK 17, dann `gradle build` -> `build/libs/grizzlymod-1.0.0.jar`
(oder Gradle-Wrapper aus dem offiziellen Forge-1.20.1-MDK hineinkopieren).

Einstellungen (Spawn-Gewicht, Flucht-Dauer, Werte): `add_grizzly_spawns.json` bzw. `GrizzlyBear.java`.
