1. Klasyczne znaki ASCII (czytelne wszędzie, kod < 128):

```log

| Znak | Kod ASCII | Zastosowanie  |               |
| ---- | --------- | ------------- | ------------- |
| `+`  | 43        | narożniki     |               |
| `-`  | 45        | poziome linie |               |
| \`   | \`        | 124           | pionowe linie |

+-------+--------+
| Name  | Score  |
+-------+--------+
| Alice |  100   |
| Bob   |   95   |
+-------+--------+
```

2. Rozszerzone znaki pseudo-graficzne (kodowanie CP437, UTF-8 – zależy od terminala):
```log
   | Znak | Kod Unicode | Opis            |
   | ---- | ----------- | --------------- |
   | `┌`  | U+250C      | lewy górny róg  |
   | `┐`  | U+2510      | prawy górny róg |
   | `└`  | U+2514      | lewy dolny róg  |
   | `┘`  | U+2518      | prawy dolny róg |
   | `─`  | U+2500      | linia pozioma   |
   | `│`  | U+2502      | linia pionowa   |
   | `├`  | U+251C      | lewy węzeł      |
   | `┤`  | U+2524      | prawy węzeł     |
   | `┬`  | U+252C      | górny węzeł     |
   | `┴`  | U+2534      | dolny węzeł     |
   | `┼`  | U+253C      | środek (krzyż)  |

┌───────┬────────┐
│ Name  │ Score  │
├───────┼────────┤
│ Alice │  100   │
│ Bob   │   95   │
└───────┴────────┘

```
