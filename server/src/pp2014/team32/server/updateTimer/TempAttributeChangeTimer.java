package pp2014.team32.server.updateTimer;

import java.util.Timer;
import java.util.TimerTask;

import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Runnable welches nach Wirkungsdauer eines benutzten Items die Attribute eines
 * GameCharacters zuruecksetzt.
 * 
 * @author Moritz
 * 
 */
public class TempAttributeChangeTimer implements Runnable {

	private GameCharacter	gC;
	private Attributes		itemAttributes;
	private static int		itemEffectTime	= Integer.parseInt(PropertyManager.getProperty("itemEffectTime"));

	/**
	 * 
	 * @param gC GameCharacter welcher temporaer wirkendes Item gerade benutzt hat
	 * @param itemAttributeChanges Attributaenderungen des temporaer wirkenden Items
	 */
	public TempAttributeChangeTimer(GameCharacter gC, Attributes itemAttributeChanges) {
		this.gC = gC;
		this.itemAttributes = itemAttributeChanges;
	}

	@Override
	public void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				// nach itemEffectTime macht sich die Wirkung des Items
				// rueckgaengig
				gC.decreaseAttributesBy(itemAttributes);
				OutputMessageHandler.updateAttributesToClient(gC);
			}
		}, itemEffectTime);
	}
}
