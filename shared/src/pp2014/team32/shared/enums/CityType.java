package pp2014.team32.shared.enums;

import java.util.HashMap;

import pp2014.team32.shared.utils.PropertyManager;

public enum CityType {
	BELO_HORIZONTE, BRASILIA, CUIABA, CURITIBA, FORTALEZA, MANAUS, NATAL, PORTO_ALEGRE, RECIFE, RIO_DE_JANEIRO, SALVADOR, SAO_PAULO, UNDEF;

	private static HashMap<CityType, String> names;
	
	static {
		names = new HashMap<CityType, String>();
		for (CityType type: CityType.values())
			names.put(type, PropertyManager.getProperty("cityType." + type));
	}
	
	public static String getName(CityType city) {
		return names.get(city);
	}
}
