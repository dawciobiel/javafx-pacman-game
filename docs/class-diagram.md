# Class diagram

```log
+------------------+
|      Game        | <== Main loop, inicjalizacja
+------------------+
         |
         v
+------------------+      +------------------+
|  GameController  | ---> |   InputHandler   |
+------------------+      +------------------+
         |
         v
+------------------+
|     GameModel    | <== Pozycje gracza, duchów, mapy
+------------------+
         |
         v
+------------------+      +------------------+
|     Player       |      |      Ghost       |
+------------------+      +------------------+
         |
         v
+------------------+ <== Rysowanie mapy, gracza, duchów
|   GameRenderer   |
+------------------+

```
