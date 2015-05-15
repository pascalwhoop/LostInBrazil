package pp2014.team32.shared.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Diese Klasse verwaltet die Properties-Datei und Initialisiert das Log
 * 
 * Die Methoden sind fuer den einfachen Zugriff statisch gehalten
 * 
 * @author Christian Hovestadt
 * @version 29.6.2014
 */
public class PropertyManager {

	private static Properties	properties	= new Properties();
	private static Logger		log;

	/**
	 * Initialisiert das Log und laedt mehrere Properties-Dateien ein
	 * 
	 * @param propertyFilePaths Pfade der Properties-Dateien
	 * @param loggingPath Pfad der Logging-Konfigurationsdatei
	 * @author Christian Hovestadt
	 */
	public PropertyManager(List<String> propertyFilePaths, String loggingPath) {
		// Logging-Konfigurationen laden
		System.setProperty("java.util.logging.config.file", loggingPath);

		try {
			LogManager.getLogManager().readConfiguration();
		} catch (Exception e) {
			System.err.println("Logging properties file was not found.");
		}

		// Properties laden
		for (String path : propertyFilePaths)
			try {
				BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path));
				properties.load(stream);
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * Gibt den Value fuer den uebergebenen Key zurueck. Der Rueckgabewert ist
	 * null, wenn es keine Property zum uebergebenen Key gibt.
	 * 
	 * @param key
	 *            Key der Property
	 * @return value Wert der Property
	 * @author Christian Hovestadt
	 */
	public static String getProperty(String key) {
		String prop = properties.getProperty(key);
		if (prop == null)
			log.log(Level.SEVERE, "Key '" + key + "' was not found in the properties-file.");
		return properties.getProperty(key);
	}
}