package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ODTEditor {
    /**
     * Method to manage editing
     * @param odtFile odt file which will be modified
     * @param oldImport import that we want to change
     * @param newImport new import to swap with old
     * @param fileDir path to odt file
     */
    public static void modifyOdtImports(File odtFile, String oldImport, String newImport, String fileDir) {
        try {
            // Creates temporary directory to save copied files xml files from odt file
            File tempDir = new File(fileDir + File.separator + "temp_odt");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            File file = findODTFile( odtFile, fileDir);

            // Unzips odt file
            unzip(file, tempDir);

            // Modify import links in all XML files
            File[] xmlFiles = tempDir.listFiles((dir, name) -> name.endsWith(".xml"));
            if (xmlFiles != null) {
                for (File xmlFile : xmlFiles) {
                    replaceImportInXML(xmlFile, oldImport, newImport);
                }
            }

            // Repackage the ODT File and overwrite the original
            File modifiedOdt = new File(fileDir + File.separator + odtFile.getName());
            zipFolder(tempDir, modifiedOdt);

            // Clean up temp directory after use
            deleteDirectory(tempDir);

            System.out.println("ODT file modified successfully: " + modifiedOdt.getAbsolutePath());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static File findODTFile(File odtFile, String directoryPath) {
        File folder = new File(directoryPath);

        if (!folder.exists() || !folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File foundFile = findODTFile(odtFile, file.getAbsolutePath());
                if (foundFile != null) {
                    return foundFile; // Return immediately if found
                }
            } else if (file.getName().equalsIgnoreCase(odtFile.getName())) {
                return file; // Return the matching ODT file
            }
        }
        return null; // Return null if the file is not found
    }

    /**
     * Replaces given import to another import in XML file
     * @param xmlFile XML file which should be modified
     * @param oldImport import to change
     * @param newImport new import
     * @throws Exception
     */
    public static void replaceImportInXML(File xmlFile, String oldImport, String newImport) throws Exception {
        if (xmlFile.length() == 0) {
            System.out.println("Skipping empty XML file: " + xmlFile.getName());
            return;
        }

        // Parse the XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Find and modify text nodes containing import references
        NodeList textNodes = doc.getElementsByTagName("text:text-input");
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node node = textNodes.item(i);
            if (node.getTextContent().contains(oldImport)) {
                System.out.println("Replacing import in " + xmlFile.getName());
                node.setTextContent(node.getTextContent().replace(oldImport, newImport));
            }
        }

        // Save the modified XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(xmlFile));
    }

    /**
     * Unzips odt file
     * @param zipFile odt file
     * @param targetDir temporary created directory
     * @throws IOException throws exception on error
     */
    private static void unzip(File zipFile, File targetDir) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            targetDir.mkdirs();
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    // Skip empty XML files
                    if (entry.getSize() == 0 && entry.getName().endsWith(".xml")) {
                        System.out.println("Skipping empty XML file: " + entry.getName());
                        continue;
                    }

                    entryFile.getParentFile().mkdirs(); // Ensure parent directories exist
                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    /**
     * Zips modified xml files in folder to odt file
     * @param folder temporary created folder
     * @param zipFile modified odt file
     * @throws IOException throws exception on error
     */
    private static void zipFolder(File folder, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            File mimetypeFile = new File(folder, "mimetype");
            if (mimetypeFile.exists()) {
                // Ensure "mimetype" is stored uncompressed as required by ODT format
                ZipEntry mimetypeEntry = new ZipEntry("mimetype");
                zos.putNextEntry(mimetypeEntry);
                Files.copy(mimetypeFile.toPath(), zos);
                zos.closeEntry();
            }

            // Add all other files
            zipDirectory(folder, folder, zos);
        }
    }

    /**
     * Adds all files to directory and zips
     * @param folder
     * @param baseDir temporary created directory
     * @param zos Output stream
     * @throws IOException throws exception on error
     */
    private static void zipDirectory(File folder, File baseDir, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.getName().equals("mimetype")) continue; // Skip, as it's already added

            if (file.isDirectory()) {
                zipDirectory(file, baseDir, zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String entryName = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1)
                            .replace("\\", "/"); // Ensure correct path format
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * deletes temporary created directory
     * @param dir directory
     * @throws IOException
     */
    private static void deleteDirectory(File dir) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

}




