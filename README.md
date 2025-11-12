# Chess Game

This project now ships with two separate JavaFX front-ends:

- `legacy.LegacyMain` — the original mutable board version (now located under the `legacy/` source folder).
- `Main.Main` — the new immutable engine + UI

To run either UI from the command line make sure to include JavaFX on the module path, for example:

```
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -classpath out/production/Chess \
     Main.Main
```

Swap `Main.Main` with `legacy.LegacyMain` to launch the legacy demo.

The immutable UI includes a **Load FEN** button so you can paste arbitrary positions (standard FEN strings) straight into the engine, and a **Load PGN** button to import a full game in PGN notation.
