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
