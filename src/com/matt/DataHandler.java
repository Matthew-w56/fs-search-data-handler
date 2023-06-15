package com.matt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataHandler {
   private static final String STAR = "*";
   private static final String PATH_TO_OUTPUT_FOLDER = "output/";
   
   private static final String DATA_SOURCE_HR_A = "resource/allSearchYearsHR.csv";
   private static final String DATA_SOURCE_TREE_A = "resource/allSearchYearsTree.csv";
   private static final String DATA_SOURCE_LLS_A = "resource/allSearchYearsLLS.csv";
   
   //These sources are the ones that include queries that had a death date included
   private static final String DATA_SOURCE_HR_B = "resource/allSearchYearsHRWithDeaths.csv";
   private static final String DATA_SOURCE_TREE_B = "resource/allSearchYearsTreeWithDeaths.csv";
   private static final String DATA_SOURCE_LLS_B = "resource/allSearchYearsLLSWithDeaths.csv";
   
   private static final String[] DATA_SOURCES_A = { DATA_SOURCE_HR_A, DATA_SOURCE_TREE_A, DATA_SOURCE_LLS_A};
   private static final String[] DATA_SOURCES_B = {DATA_SOURCE_HR_B, DATA_SOURCE_TREE_B, DATA_SOURCE_LLS_B};
   private static final String[] ALL_DATA_SOURCES = { DATA_SOURCE_HR_A, DATA_SOURCE_TREE_A, DATA_SOURCE_LLS_A,
                                                      DATA_SOURCE_HR_B, DATA_SOURCE_TREE_B, DATA_SOURCE_LLS_B};
   
   /**
    * Creates a dataset that reflects the length of date ranges people are searching for.  This is
    * bucketed into groups of the size given.  Recommended chart type: Bar Graph.
    * <br><br>
    * Output is in the format: "length,count"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename.
    * @param bucketSize Size of bucket to group range lengths into.  Use 1 to avoid any bucketing.
    * @param stopLength The length beyond which all results are grouped together (Helps deal with outliers)
    */
   public static void dateRangeLengths(String outputFile, int bucketSize, int stopLength) {
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int bucketCount = (stopLength / bucketSize) + 1;
      for (int i = 0; i < bucketCount; i++) {
         table.add(Arrays.asList(i * bucketSize, 0));
      }
      table.add(Arrays.asList(-1, 0));
      int starIndex = table.size() - 1;
      
      for (String dataSource: ALL_DATA_SOURCES) {
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               int rowCount = Integer.parseInt(parts[1]);
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               String dateType = dateParts[0];
               if (dateParts.length != 3)
                  continue;
               
               int rowIndex;
               if (dateParts[1].equals(STAR) || dateParts[2].equals(STAR)) {
                  rowIndex = starIndex;
               }
               else {
                  int rangeLength = Integer.parseInt(dateParts[2]) - Integer.parseInt(dateParts[1]);
                  rowIndex = Math.min(rangeLength, stopLength) / bucketSize;
               }
               if (rowIndex < 0)
                  continue;
               
               //Actually add the data to the table
               List<Integer> entry = table.get(rowIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(1, entry.get(1) + rowCount);
               
            }
         }
         catch (Exception e) {
            System.out.println("Exception occured during \"dateRangeLengths\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      writeTableToFile(table, outputFile, "length,count");
      
   }
   
   
   /**
    * Creates a dataset that reflects the number of times that each
    * year is searched for in four categories (birth, death, any, and other) across any
    * of the systems (HR, Tree, and LLS). Recommended chart type: Multiple Line Graph.
    * <br><br>
    * Output is in the format: "year,birth,death,any,other"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename.
    * @param bucketSize Size of bucket to group years into.  Use 1 to avoid any bucketing.
    * @param stopYear The year after which all results are grouped together (Helps deal with outliers)
    */
   public static void searchedYearsByType(String outputFile, int bucketSize, int stopYear) {
      verifyDataSources();
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int bucketCount = (stopYear / bucketSize) + 1;
      for (int i = 0; i < bucketCount; i++) {
         table.add(Arrays.asList(i * bucketSize, 0, 0, 0, 0));
      }
      table.add(Arrays.asList(-1, 0, 0, 0, 0));
      int starIndex = table.size() - 1;
      
      //Part 1: Read in data from the sources
      for (String dataSource: ALL_DATA_SOURCES) {
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               int rowCount = Integer.parseInt(parts[1]);
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               String dateType = dateParts[0];
               
               int yearBucketIndex = (dateParts[1].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(dateParts[1]), stopYear) / bucketSize));
               int yearBucketIndex2 = -1;
               if (dateParts.length > 2) {
                  yearBucketIndex2 = (dateParts[2].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(dateParts[2]), stopYear) / bucketSize));
               }
               
               //Handle the second entry: Turn year type into year type index
               int columnIndex;
               switch (dateType) {
                  case "birth":
                     columnIndex = 1;
                     break;
                  case "death":
                     columnIndex = 2;
                     break;
                  case "any":
                     columnIndex = 3;
                     break;
                  case "residence":
                  case "other":
                  case "marriage":
                     columnIndex = 4;
                     break;
                  default:
                     System.out.println("Skipping line: Could not find index for type "
                                        + dateType + " within " + dataSource + " at year index "
                                        + yearBucketIndex + "!");
                     continue;
               }
               
               //Actually add the data to the table
               List<Integer> entry = table.get(yearBucketIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + rowCount);
               
               if (yearBucketIndex2 != -1) {
                  //Actually add the data to the table
                  List<Integer> entry2 = table.get(yearBucketIndex2);
                  //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
                  entry2.set(columnIndex, entry2.get(columnIndex) + rowCount);
               }
               
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      writeTableToFile(table, outputFile, "year,birth,death,any,other");
      
   }
   
   /**
    * Creates a dataset that shows what years are searched on as birth years, with one
    * column for each system (HR, Tree, and LLS). Recommended chart type: Multiple Line Graph.
    * <br><br>
    * Output is in the format: "year,hr,tree,lls"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename.
    * @param bucketSize Size of bucket to group years into.  Use 1 to avoid any bucketing.
    * @param stopYear The year after which all results are grouped together (Helps deal with outliers)
    */
   public static void birthYearSearchesBySystem(String outputFile, int bucketSize, int stopYear) {
      verifyDataSources();
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int bucketCount = (stopYear / bucketSize) + 1;
      for (int i = 0; i < bucketCount; i++) {
         table.add(Arrays.asList(i * bucketSize, 0, 0, 0));
      }
      table.add(Arrays.asList(-1, 0, 0, 0));
      int starIndex = table.size() - 1;
      
      //Part 1: Read in data from the sources
      for (String dataSource: ALL_DATA_SOURCES) {
         
         int columnIndex = -1;
         switch(dataSource) {
            case DATA_SOURCE_HR_A:
            case DATA_SOURCE_HR_B:
               columnIndex = 1;
               break;
            case DATA_SOURCE_TREE_A:
            case DATA_SOURCE_TREE_B:
               columnIndex = 2;
               break;
            case DATA_SOURCE_LLS_A:
            case DATA_SOURCE_LLS_B:
               columnIndex = 3;
               break;
         }
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               int rowCount = Integer.parseInt(parts[1]);
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               String dateType = dateParts[0];
               
               //Skip any records that are not birth years
               if (!dateType.equals("birth")) continue;
               
               int yearBucketIndex = (dateParts[1].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(dateParts[1]), stopYear) / bucketSize));
               int yearBucketIndex2 = -1;
               if (dateParts.length > 2) {
                  yearBucketIndex2 = (dateParts[2].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(dateParts[2]), stopYear) / bucketSize));
               }
               
               //Actually add the data to the table
               List<Integer> entry = table.get(yearBucketIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + rowCount);
               
               if (yearBucketIndex2 != -1) {
                  //Actually add the data to the table
                  List<Integer> entry2 = table.get(yearBucketIndex2);
                  //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
                  entry2.set(columnIndex, entry2.get(columnIndex) + rowCount);
               }
               
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      writeTableToFile(table, outputFile, "year,hr,tree,lls");
      
   }
   
   /**
    * Creates a dataset that shows how often each year type (birth, death, etc) is searched
    * for in the different systems.  Recommended chart type: Multiple Bar Graph.
    * <br><br>
    * Output is in the format: "yearType,hr,tree,lls"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename.
    */
   public static void yearTypesSearchedBySystem(String outputFile) {
      verifyDataSources();
      
      ArrayList<String> yearTypes = new ArrayList<String>(Arrays.asList("birth", "death", "residence", "any", "marriage", "other"));
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int numOfRows = yearTypes.size();
      for (int i = 0; i < numOfRows; i++) {
         table.add(Arrays.asList(i, 0, 0, 0));
      }
      
      //Part 1: Read in data from the sources
      for (String dataSource: ALL_DATA_SOURCES) {
         
         int columnIndex = -1;
         switch(dataSource) {
            case DATA_SOURCE_HR_A:
            case DATA_SOURCE_HR_B:
               columnIndex = 1;
               break;
            case DATA_SOURCE_TREE_A:
            case DATA_SOURCE_TREE_B:
               columnIndex = 2;
               break;
            case DATA_SOURCE_LLS_A:
            case DATA_SOURCE_LLS_B:
               columnIndex = 3;
               break;
         }
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               int rowCount = Integer.parseInt(parts[1]);
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               String dateType = dateParts[0];
               
               int rowIndex = yearTypes.indexOf(dateType);
               if (rowIndex == -1) {
                  System.out.println("Cannot find row index for type " + parts[1] + ".  Skipping..");
                  continue;
               }
               
               List<Integer> entry = table.get(rowIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + rowCount);
               
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      writeTableToFileWithReplacements(table, outputFile, "yearType,hr,tree,lls", yearTypes);
      
   }
   
   /**
    * Creates a dataset that shows how many of the (probable) living person queries
    * come from each system (hr, tree, lls).  Recommended chart type: Pie Chart.
    * <br><br>
    * Output is in the format: "system,total,living"
    * Final row is for all systems combined.
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename
    * @param birthCutoff The first year that will be considered the birth of a likely living person
    * @param otherCutoff The first year that will be considered an event of a likely living person (doesn't include death dates)
    * @param stopYear Years after this year will not be counted.  Generally, this is the current year.
    */
   public static void livingPersonSearchesBySystem(String outputFile, int birthCutoff, int otherCutoff, int stopYear) {
      verifyDataSources();
      
      ArrayList<String> systems = new ArrayList<String>(Arrays.asList("hr", "tree", "lls", "total"));
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int numOfRows = systems.size();
      for (int i = 0; i < numOfRows; i++) {
         table.add(Arrays.asList(i, 0, 0));
      }
      int totalSearches = 0;
      int totalLivingSearches = 0;
      
      for (String dataSource: DATA_SOURCES_A) {
         
         int rowIndex = -1;
         switch (dataSource) {
            case DATA_SOURCE_HR_A:
               rowIndex = 0;
               break;
            case DATA_SOURCE_TREE_A:
               rowIndex = 1;
               break;
            case DATA_SOURCE_LLS_A:
               rowIndex = 2;
               break;
         }
         List<Integer> tableRow = table.get(rowIndex);
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               int rowCount = Integer.parseInt(parts[1]);
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               String dateType = dateParts[0];
               
               //This part is decently complicated.  This is because we care about the most recent date being searched.
               //The most recent date is the date if it's not a range, and the end date if it's not a range.
               //But if the range ends in a star, the real end date being searched is 10 years after the start.
               //But if the range starts in a star, the end date is the given.
               int rowYear = -11;
               if (!dateParts[1].equals(STAR)) {
                  rowYear = Integer.parseInt(dateParts[1]);
               }
               if (dateParts.length > 2) {
                  if (dateParts[2].equals(STAR)) {
                     rowYear += 10;
                  } else {
                     rowYear = Math.max(Integer.parseInt(dateParts[2]), rowYear);
                  }
               }
               if (rowYear < 0) {
                  //This is usually because both ends of the range are stars.
                  System.out.println("Somehow, Row Date ended up less than zero.\nRow Date: " + rowYear + "\nLine: " + line);
                  continue;
               }
               
               int currentSystemTotalSearches = tableRow.get(1);
               int currentSystemTotalLivingSearches = tableRow.get(2);
               
               currentSystemTotalSearches += rowCount;
               totalSearches += rowCount;
               
               //Add them in to the living persons total depending on the date type
               switch (dateType) {
                  case "birth":
                     if (rowYear >= birthCutoff && rowYear <= stopYear) {
                        currentSystemTotalLivingSearches += rowCount;
                        totalLivingSearches += rowCount;
                     }
                     break;
                  case "residence":
                  case "any":
                  case "other":
                  case "marriage":
                     if (rowYear >= otherCutoff && rowYear <= stopYear) {
                        currentSystemTotalLivingSearches += rowCount;
                        totalLivingSearches += rowCount;
                     }
                     break;
                  default:
                     System.out.println("Skipping line: Could not find index for type "
                                        + dateType + " within " + dataSource + " at year "
                                        + rowYear + "!");
                     continue;
               }
               
               //Save the new count
               tableRow.set(1, currentSystemTotalSearches);
               tableRow.set(2, currentSystemTotalLivingSearches);
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      
      //Add in all the searches that originally included a death year into the total
      // (but not into the number of living person searches)
      for (String dataSource: DATA_SOURCES_B) {
         
         int rowIndex = -1;
         switch (dataSource) {
            case DATA_SOURCE_HR_B:
               rowIndex = 0;
               break;
            case DATA_SOURCE_TREE_B:
               rowIndex = 1;
               break;
            case DATA_SOURCE_LLS_B:
               rowIndex = 2;
               break;
         }
         List<Integer> tableRow = table.get(rowIndex);
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               String[] dateParts = parts[0].replaceAll("\"", "").split(";");
               
               int rowCount = Integer.parseInt(parts[1]);
               String dateType = dateParts[0];
               
               //Add this row to the totals, without checking for living searches
               totalSearches += rowCount;
               tableRow.set(1, tableRow.get(1) + rowCount);
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      
      List<Integer> lastRow = table.get(systems.size()-1);
      lastRow.set(1, totalSearches);
      lastRow.set(2, totalLivingSearches);
      
      writeTableToFileWithReplacements(table, outputFile, "system,total,living", systems);
   }
   
   
   //----------------[ Helper Methods ]-------------------------
   
   /**
    * Verifies that all the data source files exist.  If not, the ones that are
    * missing will be printed to the console and the system will exit with
    * code 7.  If all exist, then no action is taken.
    */
   private static void verifyDataSources() {
      boolean allFound = true;
      //Without Death Sources
      if (!(new File(DATA_SOURCE_HR_A).exists())) {
         System.out.println("Cannot find HR file at " + DATA_SOURCE_HR_A);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_TREE_A).exists())) {
         System.out.println("Cannot find Tree file at " + DATA_SOURCE_TREE_A);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_LLS_A).exists())) {
         System.out.println("Cannot find LLS file at " + DATA_SOURCE_LLS_A);
         allFound = false;
      }
      //With Death Sources
      if (!(new File(DATA_SOURCE_HR_B).exists())) {
         System.out.println("Cannot find HR file B at " + DATA_SOURCE_HR_B);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_TREE_B).exists())) {
         System.out.println("Cannot find Tree file B at " + DATA_SOURCE_TREE_B);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_LLS_B).exists())) {
         System.out.println("Cannot find LLS file B at " + DATA_SOURCE_LLS_B);
         allFound = false;
      }
      
      if (!allFound) System.exit(7);
   }
   
   /**
    * Writes the given table to the output file in csv format.
    * Prepends the default output file folder path to filename.
    *
    * @param table 2D array of integers to write
    * @param outputFile Filename for output file
    * @param header Header string to be written onto the first line
    */
   private static void writeTableToFile(List<List<Integer>> table, String outputFile, String header) {
      try (FileWriter writer = new FileWriter(PATH_TO_OUTPUT_FOLDER + outputFile)) {
         
         writer.write(header + "\n");
         for (List<Integer> line: table) {
            writer.write(line.get(0) == -1 ? STAR : line.get(0) + "");
            for (int i = 1; i < line.size(); i++) {
               writer.write("," + line.get(i));
            }
            writer.write("\n");
         }
      } catch (Exception e) {
         System.out.println("Exception occured during writing table to file " + outputFile
                            + "!\n" + e.getMessage());
      }
   }
   
   /**
    * Acts just like the writeTableToFile method, but also treats the first
    * item of each line as an index.  Instead of writing that item to the file,
    * the string being written to the file will be the string at the index of the
    * value of the first item in the replacements array.
    * <br><br>
    * Example:<br>
    * 2,155,2000 -> replacements[2],155,2000
    *
    * @param table 2D array of integers to write
    * @param outputFile Filename for output file
    * @param header Header string to be written onto the first line
    * @param replacements List of Strings that are represented by the first item of each line
    */
   private static void writeTableToFileWithReplacements(List<List<Integer>> table, String outputFile, String header, ArrayList<String> replacements) {
      try (FileWriter writer = new FileWriter(PATH_TO_OUTPUT_FOLDER + outputFile)) {
         
         writer.write(header + "\n");
         for (List<Integer> line: table) {
            writer.write(replacements.get(line.get(0)));
            for (int i = 1; i < line.size(); i++) {
               writer.write("," + line.get(i));
            }
            writer.write("\n");
         }
      } catch (Exception e) {
         System.out.println("Exception occured during writing table to file " + outputFile
                            + "!\n" + e.getMessage());
      }
   }
   
}
