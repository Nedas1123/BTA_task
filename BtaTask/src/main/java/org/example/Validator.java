package org.example;

import org.example.errors.InvalidFolderPathException;
import org.example.errors.InvalidODTFileNameException;

import java.io.File;

public class Validator {
    /**
     * Method checks if there are no problems with the path
     * @param folderPath path to folder which contains odt files
     * @throws InvalidFolderPathException error with message
     */
    public void validateFolderPath(String folderPath) throws InvalidFolderPathException {
        if (folderPath == null || folderPath.isEmpty()) {
            throw new InvalidFolderPathException("Folder path is null or empty.");
        }

        File folder = new File(folderPath);

        if (!folder.exists()) {
            throw new InvalidFolderPathException("Folder does not exist in path: " + folderPath);
        }
        if(!folder.isDirectory()){
            throw new InvalidFolderPathException("Bad directory " + folderPath);
        }
    }

    /**
     * Checks if file name is valid
     * @param ODTFile ODT file
     * @throws IllegalArgumentException error on bad naming
     */
    public void validateODTFile(File ODTFile) throws InvalidODTFileNameException {
            if (ODTFile == null) {
                throw new IllegalArgumentException("File cannot be null.");
            }
            if (!ODTFile.getName().toLowerCase().endsWith(".odt")) {
                throw new IllegalArgumentException("Invalid file type. Expected an ODT file: " + ODTFile.getName());
            }
        }
}
