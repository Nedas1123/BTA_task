## BTA_task
application to edit and read imports in odt files

# Requirements:
Java 17 installed, Maven

# How to run:
Requirements:
Java 17 installed

run mvn clean install

in resouces folder - config.properties file edit fields:
oldImport - Value which will be swapped
newImport - Value which suppose to swap oldImport
odtFile - Name of file which will be edited
folderPath - Path to the folder which contains odt files

# How it works
1. Checks if path and folder are correct
2. Finds all ODT files in given directory
3. Extracts all XML files from ODT files
4. Checks for imports in those extracted XML files
5. Creates Map with ODT file and import mentions in XML files
6. Finds given ODT file in specified directory
7. Creates new temporary directory to copy and store XML files from ODT file which we want to edit
8. Stores copied files
9. Edits stored XML files
10. Zips everything to ODT file
11. Deletes old ODT file directory
12. Prints out success message
   

# Tests
for run tests locally:
click on test or execute mvn clean test command
