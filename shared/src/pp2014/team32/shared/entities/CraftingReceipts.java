package pp2014.team32.shared.entities;

import java.util.Set;
import java.util.logging.Logger;

import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse enthaelt die enscheidenden Rezepte zum Craften.
 * 
 * @author Moritz Bittner
 * @author Christian Hovestadt
 * @author Can Dogan
 * 
 */
public class CraftingReceipts {
	private static final int	CRAFTING_SIZE	= Integer.parseInt(PropertyManager.getProperty("craftingSize"));
	private static final Logger	LOGGER			= Logger.getLogger(CraftingReceipts.class.getName());

	/**
	 * Definiert und erkennt alle Crafting Moeglichkeiten und gibt das Resultat
	 * der uebergebenen Crafting Sources als ItemType zurueck
	 * 
	 * @param input set von ItemTypes die als Sources zum Craften verwendet
	 *            werden sollen
	 * @return ItemType des Resultats, falls es sich um ein gueltiges Crafting
	 *         Rezept handelt, sonst null
	 * @author Christian Hovestadt
	 */
	public static ItemType craft(Set<ItemType> input) {
		if (input.size() > CRAFTING_SIZE) {
			LOGGER.warning("Given crafting-array does not fit to CRAFTING_SIZE");
			return null;
		}

		if (input.size() == 1) {
			if (input.contains(ItemType.CORN))
				return ItemType.BREAD;
			else if (input.contains(ItemType.WOOD))
				return ItemType.BASEBALLBAT;
			else if (input.contains(ItemType.WOOL))
				return ItemType.CAP;
		} else if (input.size() == 2) {
			if (input.contains(ItemType.BANANA) && input.contains(ItemType.BREAD))
				return ItemType.BANANABREAD;
			else if (input.contains(ItemType.MEAT) && input.contains(ItemType.COAL))
				return ItemType.ROASTEDMEAT;
			else if (input.contains(ItemType.BASEBALLBAT) && input.contains(ItemType.STONE))
				return ItemType.AXE;
			else if (input.contains(ItemType.SWORD) && input.contains(ItemType.BRONZE))
				return ItemType.BRONZESWORD;
			else if (input.contains(ItemType.BRONZESWORD) && input.contains(ItemType.GOLD))
				return ItemType.GOLDSWORD;
			else if (input.contains(ItemType.GOLDSWORD) && input.contains(ItemType.RUBY))
				return ItemType.RUBYSWORD;
			else if (input.contains(ItemType.RUBYSWORD) && input.contains(ItemType.DIAMOND))
				return ItemType.DIAMONDSWORD;
			else if (input.contains(ItemType.WOOD) && input.contains(ItemType.COAL))
				return ItemType.TORCH;
			else if (input.contains(ItemType.WOOD) && input.contains(ItemType.STONE))
				return ItemType.CATAPULT;
			else if (input.contains(ItemType.CATAPULT) && input.contains(ItemType.IRON))
				return ItemType.PISTOL;
			else if (input.contains(ItemType.CAP) && input.contains(ItemType.IRON))
				return ItemType.HELMET;
			else if (input.contains(ItemType.WOOL) && input.contains(ItemType.CAP))
				return ItemType.GLOVES;
			else if (input.contains(ItemType.WOOL) && input.contains(ItemType.IRON))
				return ItemType.SHOES;
			else if (input.contains(ItemType.CRYSTAL) && input.contains(ItemType.CHEESE))
				return ItemType.ATTACKINGSPEEDPOTION;
			else if (input.contains(ItemType.CRYSTAL) && input.contains(ItemType.COAL))
				return ItemType.SPEEDPOTION;
			else if (input.contains(ItemType.CRYSTAL) && input.contains(ItemType.BANANA))
				return ItemType.STRENGTHPOTION;
			else if (input.contains(ItemType.CRYSTAL) && input.contains(ItemType.MEAT))
				return ItemType.DEFENSEPOTION;
			else if (input.contains(ItemType.CRYSTAL) && input.contains(ItemType.PINEAPPLE))
				return ItemType.HEALTHREGENERATIONPOTION;
		} else {
			if (input.contains(ItemType.BREAD) && input.contains(ItemType.PINEAPPLE) && input.contains(ItemType.CHEESE))
				return ItemType.HAWAIITOAST;
			else if (input.contains(ItemType.BREAD) && input.contains(ItemType.ROASTEDMEAT) && input.contains(ItemType.CHEESE))
				return ItemType.BURGER;
			else if (input.contains(ItemType.AXE) && input.contains(ItemType.IRON) && input.contains(ItemType.COAL))
				return ItemType.SWORD;
			else if (input.contains(ItemType.BANANA) && input.contains(ItemType.TORCH) && input.contains(ItemType.RUBY))
				return ItemType.MACHETE;
			else if (input.contains(ItemType.PISTOL) && input.contains(ItemType.GOLD) && input.contains(ItemType.BRONZE))
				return ItemType.SEMIAUTOMATIC;
			else if (input.contains(ItemType.SEMIAUTOMATIC) && input.contains(ItemType.PISTOL) && input.contains(ItemType.DIAMOND))
				return ItemType.SHOTGUN;
			else if (input.contains(ItemType.HELMET) && input.contains(ItemType.IRON) && input.contains(ItemType.GOLD))
				return ItemType.CLOSEDHELMET;
			else if (input.contains(ItemType.WOOL) && input.contains(ItemType.IRON) && input.contains(ItemType.BRONZE))
				return ItemType.HAUBERK;
			else if (input.contains(ItemType.BRONZE) && input.contains(ItemType.DIAMOND) && input.contains(ItemType.RUBY))
				return ItemType.RING;
			else if (input.contains(ItemType.CLOSEDHELMET) && input.contains(ItemType.HAUBERK) && input.contains(ItemType.GLOVES))
				return ItemType.FULLARMOUR;
		}
		return null;
	}
}
