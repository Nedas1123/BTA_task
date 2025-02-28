package org.example;

import org.example.errors.PropertiesLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesLoader {
    /**
     * Loads properties file
     * @param fileName properties file name
     * @return loaded properties file
     * @throws PropertiesLoadException error with message
     */
    public static Properties loadProperties(String fileName) throws PropertiesLoadException {
        Properties properties = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new PropertiesLoadException("File " + fileName + " not found in the classpath.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new PropertiesLoadException("Problems with the file: " + fileName, e);
        }
        return properties;
    }
}
