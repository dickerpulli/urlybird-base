package suncertify.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration class to store last typed parameters between two sessions.
 */
public class Configuration {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(Configuration.class
	    .getName());

    /** The key for the properties. */
    public enum ConfigurationKey {
	/** The database location used by the client. */
	CLIENT_DATABASE_LOCATION("urlybird.client.databaseLocation"),
	/** The server location used by the client. */
	CLIENT_SERVER_LOCATION("urlybird.client.serverLocation"),
	/** The server port used by the client. */
	CLIENT_SERVER_PORT("urlybird.client.serverPort"),
	/** The database location used by the server. */
	SERVER_DATABASE_LOCATION("urlybird.server.databaseLocation"),
	/** The port used by the server. */
	SERVER_PORT("urlybird.server.port");

	private String propertyKey;

	private ConfigurationKey(String propertyKey) {
	    this.propertyKey = propertyKey;
	}

	/**
	 * Returns the property name of this configuration key.
	 * 
	 * @return The property
	 */
	public String getPropertyKey() {
	    return propertyKey;
	}
    }

    /** The singleton instance. */
    private static final Configuration INSTANCE = new Configuration();

    /** The filename of the properties file. */
    private static final String FILENAME = "urlybird.properties";

    /** Local properties. */
    private Properties properties;

    /**
     * Private default constructor because it's a singleton.
     */
    private Configuration() {
	load();
    }

    /**
     * Loads configuration properties from file.
     */
    private void load() {
	properties = new Properties();
	try {
	    File file = new File(FILENAME);
	    if (!file.exists()) {
		file.createNewFile();
	    }
	    properties.load(new FileInputStream(file));
	} catch (FileNotFoundException e) {
	    LOGGER.severe("Should not happen, because creation of file is done");
	} catch (IOException e) {
	    LOGGER.severe("Some problem happens when accessing config file");
	}
    }

    /**
     * Saves the configuration to file.
     */
    public void save() {
	try {
	    File file = new File(FILENAME);
	    properties.store(new FileOutputStream(file), null);
	} catch (FileNotFoundException e) {
	    LOGGER.severe("Should not happen, because creation of file is done"
		    + " in constructor");
	} catch (IOException e) {
	    LOGGER.severe("Some problem happens when accessing config file");
	}
    }

    /**
     * Gives the instance of this singleton configuration class.
     * 
     * @return The instance
     */
    public static Configuration getInstance() {
	return INSTANCE;
    }

    /**
     * Returns the configuration value.
     * 
     * @param key
     *            The configuration key
     * @return The value
     */
    public String getValue(ConfigurationKey key) {
	return properties.getProperty(key.getPropertyKey());
    }

    /**
     * Sets the configuration value.
     * 
     * @param key
     *            The configuration key
     * @param value
     *            The value
     */
    public void setValue(ConfigurationKey key, String value) {
	properties.setProperty(key.getPropertyKey(), value);
    }

}
