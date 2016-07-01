# MirrorController
control the Mirror via MQTT to set the Sun to one Point to collect Heat.

Das Projekt realisiert die Steuerung von Spiegeln.
Den ganzen Tag über wird die Sonne auf einen Punkt gespiegelt.
Damit sammle ich Wärme, die in einem Speicher gehalten wird.

Zum Einstellen und Anlernen des Spiegels, zur Steuerung und Regelung der Speicherheizung und zur Darstellung der Heizkurven wird eine Webanwendung bereitgestellt. Dafür benutze ich VAADIN.

Die Services laufen auf jBoss, Tomcat bzw. als Windows-Services.

Der Spiegel-Controller ist ein Rasp-Pi mit Wlanstick, Kompass/Lagesensor und Camera-Modul.





