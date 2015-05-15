package pp2014.team32.server.updateTimer;

import java.util.TimerTask;

import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.player.ServerPlayer;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.enums.AttributeType;

/**
 * Diese Klasse erbt von TimerTask. Sie wird mit Starten des Servers gestartet.
 * Hier ist die zeitbedingte Steuerung der Gesundheitsregenerierung und der
 * Angriffsschlagpausierung aller GameCharacter umgesetzt.
 * 
 * @author Moritz Bittner
 * 
 */
public class CharacterUpdateTimer extends TimerTask {

	@Override
	public void run() {

		// wir gehen alle aktiven Player durch
		for (ServerPlayer p : PlayerConnectionHandler.getActivePlayers()) {
			// und schauen uns fuer den GameCharacter des Players ...
			GameCharacter gC = p.getMyCharacter();

			/*
			 * Steuerung der Gesundheitsregenerierung
			 */
			// ... an ob der Countdown fuer dessen Gesundheitsregeneriung
			// groesser 0 ist
			if (gC.getHealthRegenerationTime() > 0)
				// wenn ja, so verringern wir den Wert
				gC.decreaseHealthRegenerationTime();
			// wenn sie dies nicht ist und der Gesundheitswert kleiner als 100
			// ist
			else if (gC.getAttributeValue(AttributeType.HEALTH) < 100) {
				// so erhoehen wir dessen Gesundheit um eine fixe Punktzahl
				gC.increaseAttribute(AttributeType.HEALTH, 1);
				// und setzen die Zeit fuer die folgende Pause bis zur naechsten
				// Gesundheitsregenierung in Abhaengigkeit des GameCharacter
				// Attributs Gesundheitsaufladung
				gC.setHealthRegenerationTime(75 - gC.getAttributeValue(AttributeType.HEALTH_REGENERATION) / 2);
				// und informieren alle Clients auf der LevelMap ueber die
				// Erhoehung der Gesundheit
				OutputMessageHandler.updateHealthValueToClients(gC, LevelMapsHandler.getLevelMapForGameCharacter(gC));
			}

			/*
			 * Steuerung der Angriffsschlagpausierung
			 */
			// wir ueberpruefen, ob der aktuelle Wert fuer die
			// Angriffschlagpausierung groesser als 0 ist
			if (gC.getAttackSleepTime() > 0) {
				// wenn ja, so verringern wir den Wert
				gC.decreaseAttackSleepTime();
			}
			// ansonsten
			else {
				// setzen wir dessen boolean attackenabled auf true
				gC.attackEnabled = true;
			}
		}
	}

}
