package com.matt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataHandler {
   private static final String STAR = "\"*\"";
   private static final String PATH_TO_OUTPUT = "output/";
   private static final String DATA_SOURCE_HR = "resource/allSearchYearsHR.csv";
   private static final String DATA_SOURCE_TREE = "resource/allSearchYearsTree.csv";
   private static final String DATA_SOURCE_LLS = "resource/allSearchYearsLLS.csv";
   private static final String[] DATA_SOURCES = {DATA_SOURCE_HR, DATA_SOURCE_TREE, DATA_SOURCE_LLS};
   
   
   /**
    * Creates a dataset that reflects the number of times that each
    * year is searched for in three categories (birth, death, and other) across any
    * of the systems (HR, Tree, and LLS). Recommended chart type: Multiple Line Graph.
    * <br><br>
    * Output is in the format: "year,birth,death,other"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename.
    * @param bucketSize Size of bucket to group years into.  Use 1 to avoid any bucketing.
    * @param stopYear The year after which all results are grouped together (Helps deal with outliers)
    */
   public static void searchedYearsAcrossAllSystems(String outputFile, int bucketSize, int stopYear) {
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
      for (String dataSource: DATA_SOURCES) {
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               //Handle the first entry: Turn year into year bucket index
               int yearBucketIndex = (parts[0].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(parts[0]), stopYear) / bucketSize));
               
               //Handle the second entry: Turn year type into year type index
               int columnIndex;
               switch (parts[1]) {
                  case "birth":
                     columnIndex = 1;
                     break;
                  case "death":
                     columnIndex = 2;
                     break;
                  case "residence":
                  case "any":
                  case "other":
                  case "marriage":
                     columnIndex = 3;
                     break;
                  default:
                     System.out.println("Skipping line: Could not find index for type "
                                        + parts[1] + " within " + dataSource + " at year index "
                                        + yearBucketIndex + "!");
                     continue;
               }
               
               //Handle the remainder of the entries: the counts sections
               int anyCount = parts.length < 3 || parts[2].isEmpty() ? 0 : Integer.parseInt(parts[2]);
               int fromCount = parts.length < 4 || parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
               int toCount = parts.length < 5 || parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
               
               //Actually add the data to the table
               List<Integer> entry = table.get(yearBucketIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + anyCount + fromCount + toCount);
               
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      writeTableToFile(table, outputFile, "year,birth,death,other");
      
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
      for (String dataSource: DATA_SOURCES) {
         
         int columnIndex = -1;
         switch(dataSource) {
            case DATA_SOURCE_HR:
               columnIndex = 1;
               break;
            case DATA_SOURCE_TREE:
               columnIndex = 2;
               break;
            case DATA_SOURCE_LLS:
               columnIndex = 3;
               break;
         }
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               //Skip any records that are not birth years
               if (!parts[1].equals("birth")) continue;
               
               int yearBucketIndex = (parts[0].equals(STAR)
                                      ? starIndex
                                      : (Math.min(Integer.parseInt(parts[0]), stopYear) / bucketSize));
               
               int anyCount = parts.length < 3 || parts[2].isEmpty() ? 0 : Integer.parseInt(parts[2]);
               int fromCount = parts.length < 4 || parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
               int toCount = parts.length < 5 || parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
               
               List<Integer> entry = table.get(yearBucketIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + anyCount + fromCount + toCount);
               
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
      
      String[] yearTypes = {"birth", "death", "residence", "any", "marriage", "other"};
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int rowCount = yearTypes.length;
      for (int i = 0; i < rowCount; i++) {
         table.add(Arrays.asList(i, 0, 0, 0));
      }
      
      //Part 1: Read in data from the sources
      for (String dataSource: DATA_SOURCES) {
         
         int columnIndex = -1;
         switch(dataSource) {
            case DATA_SOURCE_HR:
               columnIndex = 1;
               break;
            case DATA_SOURCE_TREE:
               columnIndex = 2;
               break;
            case DATA_SOURCE_LLS:
               columnIndex = 3;
               break;
         }
         
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               int rowIndex;
               boolean found = false;
               for (rowIndex = 0; rowIndex < yearTypes.length; rowIndex++) {
                  if (yearTypes[rowIndex].equals(parts[1])) {
                     found = true;
                     break;
                  }
               }
               if (!found) {
                  System.out.println("Cannot find row index for type " + parts[1] + ".  Skipping..");
                  continue;
               }
               
               int anyCount = parts.length < 3 || parts[2].isEmpty() ? 0 : Integer.parseInt(parts[2]);
               int fromCount = parts.length < 4 || parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
               int toCount = parts.length < 5 || parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
               
               List<Integer> entry = table.get(rowIndex);
               //Design choice: Add the Any, From, and To as all the same thing. (treat From just like Any)
               entry.set(columnIndex, entry.get(columnIndex) + anyCount + fromCount + toCount);
               
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
    * Creates a dataset with three values.
    * The first is the total number of dates searched.
    * The second is the total number of searched dates that fell after
    *    the given cutoffs
    * The third is the ratio of living to total dates searched, multiplied by 10,000
    * (because the data structure only supports Integers.)
    * Recommended chart type: Pie chart.
    * <br><br>
    * Output is in the format: "totalSum, livingSum, ratio10000"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename
    * @param birthCutoff The first year that will be considered the birth of a likely living person
    * @param otherCutoff The first year that will be considered an event of a likely living person (doesn't include death dates)
    * @param stopYear Years after this year will not be counted.  Generally, this is the current year.
    */
   public static void livingPersonsPercentageStats(String outputFile, int birthCutoff, int otherCutoff, int stopYear) {
      verifyDataSources();
      
      //Initialize the data structure to hold the sums
      int totalSum = 0;
      int livingSum = 0;
      
      //Part 1: Read in data from the sources
      for (String dataSource: DATA_SOURCES) {
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               //Get the year of this row
               int rowYear;
               if (parts[0].equals(STAR)) {
                  rowYear = 0;
               } else {
                  rowYear = Integer.parseInt(parts[0]);
               }
               
               //Get the counts from this row
               int anyCount = parts.length < 3 || parts[2].isEmpty() ? 0 : Integer.parseInt(parts[2]);
               int fromCount = parts.length < 4 || parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
               int toCount = parts.length < 5 || parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
               int rowCount = anyCount + fromCount + toCount;
               
               //All realistic years' counts get added to the total
               if (rowYear <= stopYear) { totalSum += rowCount; }
               
               //Add them in to the living persons total depending on the date type
               switch (parts[1]) {
                  case "birth":
                     if (rowYear >= birthCutoff && rowYear <= stopYear) { livingSum += rowCount; }
                     break;
                  case "death":
                     //Death dates do not get added to living person counts
                     break;
                  case "residence":
                  case "any":
                  case "other":
                  case "marriage":
                     if (rowYear >= otherCutoff && rowYear <= stopYear) { livingSum += rowCount; }
                     break;
                  default:
                     System.out.println("Skipping line: Could not find index for type "
                                        + parts[1] + " within " + dataSource + " at year "
                                        + rowYear + "!");
                     continue;
               }
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      //Create table from statistics
      ArrayList<List<Integer>> table = new ArrayList<>();
      table.add(Arrays.asList(totalSum, livingSum, (int)(( (double)livingSum / (double)totalSum ) * 10000) ));
      
      writeTableToFile(table, outputFile, "totalSum,livingSum,ratio10000");
   }
   
   /**
    * Creates a dataset that shows how many of the (probable) living person queries
    * come from each system (hr, tree, lls).  Recommended chart type: Pie Chart.
    * <br><br>
    * Output is in the format: "hr,tree,lls"
    *
    * @param outputFile Filename to have results saved to.  Make sure this is a .csv filename
    * @param birthCutoff The first year that will be considered the birth of a likely living person
    * @param otherCutoff The first year that will be considered an event of a likely living person (doesn't include death dates)
    * @param stopYear Years after this year will not be counted.  Generally, this is the current year.
    */
   public static void livingPersonSearchesBySystem(String outputFile, int birthCutoff, int otherCutoff, int stopYear) {
      verifyDataSources();
      
      //Initialize the data structure to hold the sums
      List<Integer> tableRow = Arrays.asList(0, 0, 0);
      
      //Part 1: Read in data from the sources
      for (String dataSource: DATA_SOURCES) {
         int sourceIndex = -1;
         switch (dataSource) {
            case DATA_SOURCE_HR:
               sourceIndex = 0;
               break;
            case DATA_SOURCE_TREE:
               sourceIndex = 1;
               break;
            case DATA_SOURCE_LLS:
               sourceIndex = 2;
               break;
         }
         try (BufferedReader reader = new BufferedReader(new FileReader(dataSource))) {
            
            String line;
            reader.readLine(); //Clear out headers
            while (( line = reader.readLine() ) != null) {
               
               String[] parts = line.split(",");
               
               //Get the year of this row
               int rowYear;
               if (parts[0].equals(STAR)) {
                  rowYear = 0;
               } else {
                  rowYear = Integer.parseInt(parts[0]);
               }
               
               //Get the counts from this row
               int anyCount = parts.length < 3 || parts[2].isEmpty() ? 0 : Integer.parseInt(parts[2]);
               int fromCount = parts.length < 4 || parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
               int toCount = parts.length < 5 || parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
               int rowCount = anyCount + fromCount + toCount;
               
               int currentCount = tableRow.get(sourceIndex);
               
               //Add them in to the living persons total depending on the date type
               switch (parts[1]) {
                  case "birth":
                     if (rowYear >= birthCutoff && rowYear <= stopYear) { currentCount += rowCount; }
                     break;
                  case "death":
                     //Death dates do not get added to living person counts
                     break;
                  case "residence":
                  case "any":
                  case "other":
                  case "marriage":
                     if (rowYear >= otherCutoff && rowYear <= stopYear) { currentCount += rowCount; }
                     break;
                  default:
                     System.out.println("Skipping line: Could not find index for type "
                                        + parts[1] + " within " + dataSource + " at year "
                                        + rowYear + "!");
                     continue;
               }
               
               //Save the new count
               tableRow.set(sourceIndex, currentCount);
            }
         } catch (Exception e) {
            System.out.println("Exception occured during \"searchedYearsAcrossAllSystems\" READ with file " + dataSource
                               + "!\n" + e.getMessage());
            e.printStackTrace();
         }
      }
      
      //Flesh out a 2D structure to pass to method
      ArrayList<List<Integer>> table = new ArrayList<>();
      table.add(tableRow);
      
      writeTableToFile(table, outputFile, "hr,tree,lls");
   }
   
   //----------------[ Helper Methods ]-------------------------
   
   /**
    * Verifies that all the data source files exist.  If not, the ones that are
    * missing will be printed to the console and the system will exit with
    * code 7.  If all exist, then no action is taken.
    */
   private static void verifyDataSources() {
      boolean allFound = true;
      if (!(new File(DATA_SOURCE_HR).exists())) {
         System.out.println("Cannot find HR file at " + DATA_SOURCE_HR);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_TREE).exists())) {
         System.out.println("Cannot find Tree file at " + DATA_SOURCE_TREE);
         allFound = false;
      }
      if (!(new File(DATA_SOURCE_LLS).exists())) {
         System.out.println("Cannot find LLS file at " + DATA_SOURCE_LLS);
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
      try (FileWriter writer = new FileWriter(PATH_TO_OUTPUT + outputFile)) {
         
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
   private static void writeTableToFileWithReplacements(List<List<Integer>> table, String outputFile, String header, String[] replacements) {
      try (FileWriter writer = new FileWriter(PATH_TO_OUTPUT + outputFile)) {
         
         writer.write(header + "\n");
         for (List<Integer> line: table) {
            writer.write(replacements[line.get(0)]);
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
