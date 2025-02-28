package org.example;

import org.example.errors.ODTFileReadingException;
import org.example.errors.XMLFileReadingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ODTReader {

    /**
     * Method to manage reading
     * @param folderPath folder path which contains odt files
     */
    public void ReadODTFiles(String folderPath){
        try {
            File folder = new File(folderPath);
            List<File> files = new ArrayList<>();
            Map<File, List<String>> fileImportsMap = new HashMap<>();

            // Finds all odt files
            findODTFiles(folder, files);

            // Populate the Map with each file and its imports
            for (File file : files) {
                List<String> imports = extractImportsFromAllXML(file);
                fileImportsMap.put(file, imports);
            }

            // Generates JSON object for output
            JSONArray jsonOutput = generateJsonOutput(fileImportsMap);
            System.out.println(jsonOutput.toString(2));

        } catch (ODTFileReadingException | XMLFileReadingException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Finds all ODT files in specified folder recursively
     * @param folder
     * @param odtFiles
     */
    private static void findODTFiles(File folder, List<File> odtFiles) {
        if (folder == null || !folder.exists()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findODTFiles(file, odtFiles);
            } else if (file.getName().endsWith(".odt")) {
                odtFiles.add(file);
            }
        }
    }

    /**
     * Extracts all imports that are in odt file
     * @param zipFile odt file
     * @param xmlEntry xml file
     * @return List of imports mentioned in xml files
     */
    public static List<String> extractImports(ZipFile zipFile, ZipEntry xmlEntry) throws XMLFileReadingException {
        List<String> imports = new ArrayList<>();

        try {
            // Skips empty file
            if (xmlEntry.getSize() == 0) {
                System.out.println("Skipping empty file: " + xmlEntry.getName());
                return imports;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(zipFile.getInputStream(xmlEntry));
            doc.getDocumentElement().normalize();

            // Use XPath to find import-like text anywhere
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            String expression = "//*[contains(text(), '[import')]";
            NodeList importNodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            for (int i = 0; i < importNodes.getLength(); i++) {
                imports.add(importNodes.item(i).getTextContent().trim() + " (from " + xmlEntry.getName() + ")");
            }
        } catch (Exception e) {
            throw new XMLFileReadingException("Error reading " + xmlEntry.getName() + ": ", e);
        }
        return imports;
    }

    /**
     * Finds xml files one by one and then extracts all mentioned imports
     * @param odtFile odt file
     * @return imports that are mentioned in odt file
     * @throws ODTFileReadingException when there are errors with reading odt file
     */
    public static List<String> extractImportsFromAllXML(File odtFile) throws ODTFileReadingException, XMLFileReadingException {
        List<String> imports = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(odtFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".xml")) { // Only process XML files
                    imports.addAll(extractImports(zipFile, entry));
                }
            }
        } catch (Exception e) {
            throw new ODTFileReadingException("Error reading " + odtFile.getName() + ": ", e);
        }
        return imports;
    }

    /**
     * Generates output in JSON format
     * @param fileImportsMap Map of odt files with their imports
     * @return jsonArray
     */
    private static JSONArray generateJsonOutput(Map<File,List<String>> fileImportsMap) {
        JSONArray jsonArray = new JSONArray();

        for (Map.Entry<File, List<String>> entry : fileImportsMap.entrySet()) {
            File file = entry.getKey();
            List<String> imports = entry.getValue();

            // Create a JSON object for the current file
            JSONObject fileData = new JSONObject();
            fileData.put("file", file.getName()); // Store the file name
            fileData.put("imports", new JSONArray(imports)); // Store the list of imports

            // Add the file data to the JSON array
            jsonArray.put(fileData);
        }

        return jsonArray;
    }


}
