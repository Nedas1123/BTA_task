package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.example.errors.ODTFileReadingException;
import org.example.errors.XMLFileReadingException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ODTReaderTest {
        // Test with an empty ODT file (no XML files inside)
        @Test
        void testExtractImportsFromAllXMLNoXMLFiles() throws Exception {
            // Create an empty ODT file (no XML files)
            File tempODTFile = createEmptyODTFile("empty.odt");

            List<String> imports = ODTReader.extractImportsFromAllXML(tempODTFile);

            // Assert that the result is an empty list (no XML files found)
            assertTrue(imports.isEmpty());

            // Clean up the temporary file
            tempODTFile.delete();
        }

        // Test for ODT file with non-XML files (should ignore non-XML)
        @Test
        void testExtractImportsFromAllXMLNonXMLFiles() throws Exception {
            // Create an ODT file with a non-XML file inside
            File tempODTFile = createODTWithNonXML("odtWithNonXMLFiles.odt", "image.png");

            List<String> imports = ODTReader.extractImportsFromAllXML(tempODTFile);

            // Assert that no imports are found
            assertTrue(imports.isEmpty());

            // Clean up the temporary file
            tempODTFile.delete();
        }

        // Test for invalid ODT file (malformed ZIP)
        @Test
        void testExtractImportsFromAllXMLInvalidODTFile() {
            // Create a malformed (invalid) ODT file (not a valid ZIP)
            File invalidODTFile = new File("invalid.odt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(invalidODTFile))) {
                writer.write("This is not a valid ZIP file content.");
            } catch (IOException e) {
                fail("Error creating invalid ODT file.");
            }

            // Verify that the method throws an ODTFileReadingException
            ODTFileReadingException thrown = assertThrows(ODTFileReadingException.class, () -> {
                ODTReader.extractImportsFromAllXML(invalidODTFile);
            });

            assertTrue(thrown.getMessage().contains("Error reading invalid.odt"));
        }

        // Test for exception during reading XML files
        @Test
        void testExtractImportsFromAllXMLReadingXMLException() {
            // Create a malformed (invalid) ODT file that is not a valid ZIP file
            File invalidODTFile = new File("invalid.odt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(invalidODTFile))) {
                writer.write("This is not a valid ZIP file content.");
            } catch (IOException e) {
                fail("Error creating invalid ODT file.");
            }

            // Verify that the method throws an ODTFileReadingException due to the malformed file
            ODTFileReadingException thrown = assertThrows(ODTFileReadingException.class, () -> {
                ODTReader.extractImportsFromAllXML(invalidODTFile);
            });

            // Assert that the exception message contains the expected error
            assertTrue(thrown.getMessage().contains("Error reading invalid.odt"));

            // Clean up the temporary file
            invalidODTFile.delete();
        }

        // Helper method to create an empty ODT file (with no XML files)
        private File createEmptyODTFile(String odtFileName) throws IOException {
            File odtFile = new File(odtFileName);

            try (FileOutputStream fos = new FileOutputStream(odtFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                // No XML files, just an empty ODT (ZIP archive)
            }

            return odtFile;
        }

        // Helper method to create an ODT file with a non-XML entry (e.g., an image)
        private File createODTWithNonXML(String odtFileName, String nonXmlFileName) throws IOException {
            File odtFile = new File(odtFileName);

            try (FileOutputStream fos = new FileOutputStream(odtFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Add a non-XML file (image)
                ZipEntry imageEntry = new ZipEntry(nonXmlFileName);
                zos.putNextEntry(imageEntry);
                zos.write(new byte[0]);  // Empty content for the image file
                zos.closeEntry();
            }

            return odtFile;
        }
    // Test case empty xml file
    @Test
    void testExtractImportsEmptyXML() throws Exception {
        // Create a temporary XML file with no import-like text (empty XML file)
        File tempXMLFile = createTempXMLFile("empty.xml", "");

        // Create a ZIP file (ODT file) containing the empty XML file
        File tempODTFile = createZipFileWithXML(tempXMLFile);

        // Open the created ZIP file (ODT file) for testing
        try (ZipFile zipFile = new ZipFile(tempODTFile)) {
            // List all entries in the ZIP file to debug the issue
            zipFile.stream().forEach(entry -> System.out.println("Entry: " + entry.getName()));

            // Ensure the XML entry is correctly retrieved from the ZIP file
            ZipEntry xmlEntry = zipFile.getEntry("empty.xml");

            // Check if the entry exists (it should)
            assertNotNull(xmlEntry, "XML entry should not be null");

            // Extract imports from the XML file (should be empty)
            List<String> imports = ODTReader.extractImports(zipFile, xmlEntry);

            // Assert that no imports are extracted from an empty XML file
            assertTrue(imports.isEmpty());
        }

        // Clean up the temporary files
        tempXMLFile.delete();
        tempODTFile.delete();
    }

    // Valid XML file with imports
    @Test
    void testExtractImportsValidXML() throws Exception {
        // Create a temporary XML file with valid import-like text
        File tempXMLFile = createTempXMLFile("empty.xml", "<root><tag>[import]import1</tag><other>[import]import2</other></root>");

        // Create a ZIP file (ODT file) containing the XML file
        File tempODTFile = createZipFileWithXML(tempXMLFile);

        // Open the created ZIP file (ODT file) for testing
        try (ZipFile zipFile = new ZipFile(tempODTFile)) {
            // Look for the XML entry in the ZIP file
            ZipEntry xmlEntry = zipFile.getEntry("empty.xml");
            assertNotNull(xmlEntry, "XML entry should not be null");

            // Extract imports from the XML file
            List<String> imports = ODTReader.extractImports(zipFile, xmlEntry);

            // Assert that the imports were extracted correctly, including the '[import]' text
            assertEquals(List.of("[import]import1 (from empty.xml)", "[import]import2 (from empty.xml)"), imports);
        }

        // Clean up the temporary files
        tempXMLFile.delete();
        tempODTFile.delete();
    }

    // XML file with no imports
    @Test
    void testExtractImportsNoImports() throws Exception {
        // Create a temporary XML file that doesn't contain any imports
        File tempXMLFile = createTempXMLFile("empty.xml", "<root><tag>no import here</tag></root>");

        // Create a ZIP file (ODT file) containing this XML file
        File tempODTFile = createZipFileWithXML(tempXMLFile);

        // Open the ODT (ZIP file) for testing
        try (ZipFile zipFile = new ZipFile(tempODTFile)) {
            ZipEntry xmlEntry = zipFile.getEntry("empty.xml");
            assertNotNull(xmlEntry, "XML entry should not be null");

            List<String> imports = ODTReader.extractImports(zipFile, xmlEntry);

            // Assert that no imports are extracted
            assertTrue(imports.isEmpty(), "Expected no imports, but found some.");
        }

        // Clean up the temporary files
        tempXMLFile.delete();
        tempODTFile.delete();
    }

    // Invalid XML file
    @Test
    void testExtractImportsInvalidXML() throws Exception {
        // Create a temporary invalid XML file
        File tempXMLFile = createTempXMLFile("empty.xml", "<root><tag>no import here<root>");

        // Create a ZIP file (ODT file) containing this invalid XML file
        File tempODTFile = createZipFileWithXML(tempXMLFile);

        // Open the ODT (ZIP file) for testing
        try (ZipFile zipFile = new ZipFile(tempODTFile)) {
            ZipEntry xmlEntry = zipFile.getEntry("empty.xml");
            assertNotNull(xmlEntry, "XML entry should not be null");

            // Verify that parsing invalid XML throws an exception
            assertThrows(XMLFileReadingException.class, () -> {
                ODTReader.extractImports(zipFile, xmlEntry);
            });
        }

        // Clean up the temporary files
        tempXMLFile.delete();
        tempODTFile.delete();
    }

    // Helper method to create a temporary XML file with content
    private File createTempXMLFile(String fileName, String content) throws IOException {
        File tempXMLFile = File.createTempFile("empty", ".xml");
        tempXMLFile.deleteOnExit(); // Ensure cleanup after test

        try (FileOutputStream out = new FileOutputStream(tempXMLFile)) {
            out.write(content.getBytes());
        }

        return tempXMLFile;
    }

    // Helper method to create a temporary Zip file with XML
    private File createZipFileWithXML(File xmlFile) throws IOException {
        // Create a temporary file to simulate the ODT file (a ZIP file)
        File zipFile = File.createTempFile("test_odt", ".odt");
        zipFile.deleteOnExit();

        // Create a ZipOutputStream to write the XML content into the ZIP file
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Add the XML file entry into the ZIP file
            ZipEntry entry = new ZipEntry("empty.xml");  // Ensure the entry is named "empty.xml"
            zipOut.putNextEntry(entry);

            // Write the content of the XML file into the ZIP
            try (FileInputStream fis = new FileInputStream(xmlFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zipOut.write(buffer, 0, length);
                }
            }

            zipOut.closeEntry(); // Close the current entry
        }

        // Return the created ZIP file
        return zipFile;
    }

}
