# Grizzly Bear Mod (Forge 1.20.1)

## Der Bär
- Spawnt in allen Taiga-Biomen (`#minecraft:is_taiga`), einzeln oder zu zweit
- Modell aus `model_src/grizzly_bear.bbmodel`, realistisch bemalte Fell-Textur
- Greift Spieler automatisch an (bis 24 Blöcke), 40 HP, 8 Schaden
- Ist der Spieler näher als ca. 2,5 Blöcke (Mitte zu Mitte), stellt er sich auf die Hinterbeine und brüllt
- 37 % der Bären spawnen als **Kannibale**: deutlich dunkleres Fell, Blutflecken am Maul, dunkelrote Augen
- Normale Bären (die anderen 63 %) bringen mit 34 % Wahrscheinlichkeit 1-2 **Bärenjunge** mit. Die Jungen folgen einem Erwachsenen, greifen nicht an und fliehen bei Schaden; Bären in der Nähe verteidigen sie
- Wird er von einem Projektil getroffen (Pfeil, Dreizack, Schneeball ...), flieht er 15 Sekunden lang

## Items
| Item | Wie |
|---|---|
| Rohes Bärenfleisch | Drop (2-4, mit Looting mehr), im Ofen/Räucherofen/Lagerfeuer -> Gebratenes Bärenfleisch |
| Bärenfell | Drop (1-2) |
| Bärenkopf | Helm, craftbar aus 5 Bärenfellen (wie ein Lederhelm). Getragen: Bären bemerken dich erst ab 19 statt 24 Blöcken (-5) |
| Grizzlybär- und Bärenjunges-Spawn-Ei | Kreativ-Tab "Spawn-Eier" (auch Eier würfeln Kannibale/Junge) |

## Bauen
Mit GitHub Actions: Push -> Reiter Actions -> Artifact `grizzlymod` (JAR).
Lokal: Gradle 8.x + JDK 17, `gradle build` -> `build/libs/grizzlymod-1.0.0.jar`

## Modell oder Texturen ändern
`GrizzlyBearModel.java` und `grizzly_bear.png` werden von `tools/generate_model.py` erzeugt:
Neue `.bbmodel` nach `model_src/grizzly_bear.bbmodel` legen, `python3 tools/generate_model.py` ausführen (benötigt Pillow).
Item-Icons: `python3 tools/generate_items.py`.

Einstellungen: Spawn-Gewicht in `add_grizzly_spawns.json`, Größe `MODEL_SCALE` in `GrizzlyBearRenderer.java`,
Distanzen und Werte oben in `GrizzlyBear.java`, Position des Bärenkopf-Hutes in `BearHeadModel.java`.
