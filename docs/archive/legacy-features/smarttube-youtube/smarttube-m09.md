# M09 – SmartTube und öffentliche YouTube-Kanäle

Einstellungen → SmartTube / YouTube → aktivieren (standardmäßig aus). Plume liest dann vorhandene SmartTube-Android-TV-Preview-Programme des **Subscriptions**-Kanals (nicht History/Recommended) und optional öffentliche RSS-Feeds aus bis zu 12 manuell eingetragenen **UC**-Kanal-IDs. Es findet **kein Zugriff auf die vollständige SmartTube-Abo-Liste** statt, kein Google-Login und kein Download von Medien.

RSS akzeptiert ausschließlich echte Video-IDs, Namen und Zeitstempel innerhalb 30 Tagen; kein Datum bedeutet keine RSS-Neuheit. Das Feature verändert die klassische Startseite nicht. Aktualisiert wird nur beim Öffnen/Rückkehren oder Ändern, mit 30 Minuten regulärer und 15 Minuten Fehler-Sperrfrist; pro Kanal maximal 256 KiB und je 2,5 s Connect/Read-Timeout. Cache maximal 80 Videos, höchstens 72 Stunden; bei alter Anzeige sichtbarer Feed-Altershinweis. TV-Subscriptions-Programme liefern möglicherweise keinen Veröffentlichungszeitpunkt und sind nicht zwingend ungesehen.

**Manuell auf Shield prüfen:** SmartTube öffnen/anmelden und dessen veröffentlichte Android-TV-Subscriptions-Reihe unter den TV-Kanälen suchen; Plume neu starten und M09 aktivieren. Eine öffentliche UC-Kanal-ID eintragen und bestätigen; neue Videos in „Neu für dich“ finden, zwischen Nuvio- und YouTube-Karten wechseln, D-Pad-Navigation und tatsächliche Ziel-App beim OK testen. Ohne TV-Berechtigung nur RSS, ohne Netzwerk gespeicherten Cache, ohne installierte SmartTube-App URL-Fallback prüfen. Gültige/ungültige/leere 12+/Video-Duplikate und fehlende Veröffentlichungsdaten prüfen.

**Nicht ohne Gerät nachweisbar:** Ob die konkret installierte SmartTube-Version Kanäle publiziert sowie welcher Video-Intent auf der NVIDIA Shield tatsächlich aufgelöst wird.
