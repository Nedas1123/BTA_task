package org.example;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ODTEditorTest {
    private File tempXMLFile;
    private File emptyXMLFile;

    @BeforeEach
    void setUp() throws Exception {
        // Create a temporary XML file with an import reference
        tempXMLFile = Files.createTempFile("test", ".xml").toFile();
        try (FileWriter writer = new FileWriter(tempXMLFile)) {
            writer.write("<root><text:text-input>[import aaa]</text:text-input></root>");
        }

        // Create an empty XML file
        emptyXMLFile = Files.createTempFile("empty", ".xml").toFile();
    }

    @AfterEach
    void tearDown() {
        // Cleanup temporary files
        tempXMLFile.delete();
        emptyXMLFile.delete();
    }

    @Test
    void testReplaceImportInXML_Success() throws Exception {
        String oldImport = "[import aaa]";
        String newImport = "[import bbb]";

        // Call method to modify the XML
        ODTEditor.replaceImportInXML(tempXMLFile, oldImport, newImport);

        // Read the modified XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(tempXMLFile);
        doc.getDocumentElement().normalize();

        // Extract modified text
        NodeList textNodes = doc.getElementsByTagName("text:text-input");
        assertEquals(1, textNodes.getLength(), "There should be one text:text-input node.");
        assertEquals("[import bbb]", textNodes.item(0).getTextContent(), "The import should be replaced.");
    }

    @Test
    void testReplaceImportInXML_EmptyFile() throws Exception {
        // Call method on empty file (should skip without exception)
        ODTEditor.replaceImportInXML(emptyXMLFile, "[import aaa]", "[import bbb]");

        // Ensure the empty file is still empty
        assertEquals(0, emptyXMLFile.length(), "Empty XML file should remain empty.");
    }
}
