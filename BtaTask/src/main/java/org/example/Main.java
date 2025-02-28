package org.example;

import org.example.errors.InvalidFolderPathException;
import org.example.errors.InvalidODTFileNameException;
import org.example.errors.PropertiesLoadException;

import java.io.File;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            // loads the properties
            Validator validator = new Validator();
            Properties properties = PropertiesLoader.loadProperties("config.properties");

            String folderPath = properties.getProperty("folderPath");
            File odtFile = new File(properties.getProperty("odtFile"));
            String oldImport = properties.getProperty("oldImport");
            String newImport = properties.getProperty("newImport");

            // Checks if path is valid
            validator.validateFolderPath(folderPath);

            // Checks if file is valid
            validator.validateODTFile(odtFile);

            // Reads odt files
            ODTReader reader = new ODTReader();
            reader.ReadODTFiles(folderPath);

            // Edits odt file
            ODTEditor editor = new ODTEditor();
            editor.modifyOdtImports(odtFile, oldImport, newImport, folderPath);

        } catch (PropertiesLoadException | InvalidFolderPathException | InvalidODTFileNameException e) {
            // Catch and log errors
            System.err.println(e.getMessage());
        }
    }

}