package org.example;


import org.example.errors.InvalidFolderPathException;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;

public class ValidatorTest {
    @Test
    void testValidateFolderPathNull() {
        Validator validator = new Validator();

        InvalidFolderPathException thrown = assertThrows(InvalidFolderPathException.class, () -> {
            validator.validateFolderPath(null);
        });

        assertEquals("Folder path is null or empty.", thrown.getMessage());
    }

    // Test when the folder path is empty
    @Test
    void testValidateFolderPathEmpty() {
        Validator validator = new Validator();

        InvalidFolderPathException thrown = assertThrows(InvalidFolderPathException.class, () -> {
            validator.validateFolderPath("");
        });

        assertEquals("Folder path is null or empty.", thrown.getMessage());
    }

    // Test when the folder does not exist
    @Test
    void testValidateFolderPathNonExistent() {
        Validator validator = new Validator();

        // Use a non-existent folder path
        String nonExistentFolderPath = "C:/some/non/existent/folder";

        InvalidFolderPathException thrown = assertThrows(InvalidFolderPathException.class, () -> {
            validator.validateFolderPath(nonExistentFolderPath);
        });

        assertEquals("Folder does not exist in path: " + nonExistentFolderPath, thrown.getMessage());
    }

    // Test when the path is not a directory
    @Test
    void testValidateFolderPathNotADirectory() throws IOException {
        Validator validator = new Validator();

        // Create a temporary file and test with its path
        File tempFile = File.createTempFile("test", ".txt");
        String filePath = tempFile.getAbsolutePath();

        InvalidFolderPathException thrown = assertThrows(InvalidFolderPathException.class, () -> {
            validator.validateFolderPath(filePath);
        });

        assertEquals("Bad directory " + filePath, thrown.getMessage());

        // Clean up after test
        tempFile.delete();
    }

    // Test when the folder path is valid
    @Test
    void testValidateFolderPathValid() {
        Validator validator = new Validator();

        // Create a temporary directory
        File tempDir = new File("tempDir");
        tempDir.mkdir();
        String validFolderPath = tempDir.getAbsolutePath();

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            validator.validateFolderPath(validFolderPath);
        });

        // Clean up after test
        tempDir.delete();
    }
        // Test case: when file is null
        @Test
        void testValidateODTFileNullFile() {
            Validator validator = new Validator();  // Assuming your method is in FileValidator class

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validateODTFile(null);  // Pass a null file
            });

            assertEquals("File cannot be null.", exception.getMessage());
        }

        // Test case: when file is not an ODT file
        @Test
        void testValidateODTFileInvalidFileType() {
            Validator validator = new Validator();
            File invalidFile = new File("invalid_file.txt");

            // Get the absolute path of the file for comparison
            String expectedMessage = "File does not exist: " + invalidFile.getAbsolutePath();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validateODTFile(invalidFile);  // Pass a file that is not an ODT file
            });

            assertEquals(expectedMessage, exception.getMessage());
        }

        // Test case: when file is a valid ODT file
        @Test
        void testValidateODTFileValidFile() {
            Validator validator = new Validator();
            // Create a temporary file with a .odt extension (you can create a dummy file for testing)
            File validFile = new File("valid_file.odt");

            // Ensure the file does not exist, so we don't actually create a file on disk
            try {
                assertTrue(validFile.createNewFile());  // Try creating a dummy file
                validator.validateODTFile(validFile);  // Validate the valid file
            } catch (Exception e) {
                fail("Exception should not be thrown for a valid file.");
            } finally {
                // Clean up the temporary file created
                validFile.delete();
            }
        }
}
