package org.example;
import static org.junit.jupiter.api.Assertions.*;

import org.example.errors.PropertiesLoadException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesTest {

        // Test for a successful property file loading
        @Test
        void testLoadPropertiesSuccess() {
            // Create a mock InputStream for a valid properties file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            assertNotNull(inputStream);  // Ensure the file exists in the classpath

            // Assuming valid.properties has at least one property key-value pair
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (Exception e) {
                fail("Should not throw an exception while loading properties.");
            }

            // Test that a specific property is loaded
            assertEquals("[import block_1.odt]", properties.getProperty("oldImport"));
        }

        // Test when the properties file is not found
        @Test
        void testLoadPropertiesFileNotFound() {
            // Try to load a file that does not exist in the classpath
            PropertiesLoadException thrown = assertThrows(PropertiesLoadException.class, () -> {
                PropertiesLoader.loadProperties("nonExistent.properties");
            });

            // Check if the exception message is as expected
            assertEquals("File nonExistent.properties not found in the classpath.", thrown.getMessage());
        }

        // Test when an IOException occurs during file loading
        @Test
        void testLoadPropertiesIOException() throws Exception {
            // Create a temporary file
            File tempFile = File.createTempFile("invalid", ".properties");
            tempFile.setWritable(true);  // Make it writable (default behavior)

            // Set the file to be unreadable to simulate an IOException during file read
            tempFile.setReadable(false);

            // Assert that loading this file results in a PropertiesLoadException
            PropertiesLoadException thrown = assertThrows(PropertiesLoadException.class, () -> {
                PropertiesLoader.loadProperties(tempFile.getAbsolutePath());
            });
            // Assert the exception message matches what we expect
            assertEquals("File " + tempFile.getAbsolutePath() + " not found in the classpath.", thrown.getMessage());

            // Clean up the temporary file after the test
            tempFile.delete();
        }
    }


