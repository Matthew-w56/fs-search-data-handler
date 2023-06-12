package com.matt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Application {
   
   private static final int DEFAULT_BUCKET_SIZE = 20;
   private static final int DEFAULT_STOP_YEAR = 2050;
   
   public static void main(String[] args) {
      
      DataHandler.searchedYearsAcrossAllSystems("searchYearsAcrossAllSystems.csv", DEFAULT_BUCKET_SIZE, DEFAULT_STOP_YEAR);
      DataHandler.birthYearSearchesBySystem("birthYearsBySystem.csv", DEFAULT_BUCKET_SIZE, DEFAULT_STOP_YEAR);
      DataHandler.yearTypesSearchedBySystem("yearTypesBySystem.csv");
      
   }
   

   /**
    * Override method that applies a default bucket size and stop year to
    * the <code>aggregateYearsIntoBuckets</code> method.  Just specify an input and output file path.
    *
    * @param inputFile
    * @param outputFile
    * 
    * @see Application#aggregateYearsIntoBuckets(String, String, int, int) 
    */
   private static void aggregateYearsIntoBuckets(String inputFile, String outputFile) {
      aggregateYearsIntoBuckets(inputFile, outputFile, DEFAULT_BUCKET_SIZE, DEFAULT_STOP_YEAR);
   }

   /**
    * Takes in a csv file in the form of "year,count" for individual years, and aggregates their entries and
    * counts into an output csv file in the form of "year,count" for year buckets.  Zeros are written for
    * year buckets without any entries in the input file.
    *
    * @param inputFile csv file with the data to be aggregated
    * @param outputFile csv file (doesn't need to already exist) to write aggregated data to
    * @param bucketSize Span of the buckets that the data is aggregated into
    * @param stopYear Last year considered for new buckets (anything after is aggregated together)
    */
   private static void aggregateYearsIntoBuckets(String inputFile, String outputFile, int bucketSize, int stopYear) {

      //Anything after the stop year is aggregated into one bucket.
      //This is because there were some searches for years like 2056, 3012, 7096, etc.

      int currentYearBucket = 0;
      int nextYearBucket = bucketSize;

      try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
         FileWriter writer = new FileWriter(outputFile);

         String line;
         int sum = 0;
         reader.readLine(); //Clear out headers
         while ((line = reader.readLine()) != null) {

            //Steps for this section (each year entry)
            // 1) Extract it's year and count as integers
            // 2) If this year is outside of the current bucket, increment the bucket
            //    - This involves updating the bucket markers, and resetting the sum.
            //    - This also involves writing the old bucket and it's sum to the file.
            // 3) At this point in the method, we can assume that the current year and count
            //    correspond to this bucket.  So we add it's count to the sum.  Then we're done
            //    with this year.

            // 1)
            String[] parts = line.split(",");
            int currentYear = Integer.parseInt(parts[0]);
            int currentCount = Integer.parseInt(parts[1]);

            // 2)
            while (currentYear > nextYearBucket && currentYear < stopYear) {
               writer.write(currentYearBucket + "," + sum + "\n");
               currentYearBucket += bucketSize;
               nextYearBucket += bucketSize;
               sum = 0;
            }

            // 3)
            sum += currentCount;

         }

         //Flush out final line's data
         if (sum > 0) writer.write(currentYearBucket + "," + sum + "\n");

         writer.flush();
         writer.close();
      } catch (FileNotFoundException e) {
         System.out.println("Cannot find that file! (" + inputFile + ")");
         e.printStackTrace();
      } catch (IOException e1) {
         System.out.println("IO Exception!  Message: " + e1.getMessage());
      }
   }
   
   /**
    * Override method that applies a default bucket size and stop year to
    * the <code>countOccurancesOfYearsOnBothSides</code> method.  Just specify an
    * input and output file path.
    *
    * @param inputFile
    * @param outputFile
    *
    * @see Application#countOccurencesOfYearsOnBothSides(String, String, int, int)
    */
   private static void countOccurencesOfYearsOnBothSides(String inputFile, String outputFile) {
      countOccurencesOfYearsOnBothSides(inputFile, outputFile, DEFAULT_BUCKET_SIZE, DEFAULT_STOP_YEAR);
   }
   
   /**
    * Takes in a csv file that holds year ranges in the form of "startYear,endYear,count" and
    * aggregates them into two lists that hold the number of total occurances of each start year
    * and end year respectively, summed into buckets of years.  This is then written to the output
    * file in the form "year, startYearCount, endYearCount".
    *
    * @param inputFile csv file with the data to be aggregated
    * @param outputFile csv file (doesn't need to already exist) to write aggregated data to
    * @param bucketSize Span of the buckets that the data is aggregated into
    * @param stopYear Last year considered for new buckets (anything after is aggregated together)
    */
   private static void countOccurencesOfYearsOnBothSides(String inputFile, String outputFile, int bucketSize, int stopYear) {
      final String STAR = "\"*\"";
      
      int totalQueries = 0;
      
      //Initialize the data structure to hold the sums
      ArrayList<List<Integer>> table = new ArrayList<>();
      int num_of_buckets = (stopYear / bucketSize) + 1; //extra one is for rounding up
      for (int i = 0; i < num_of_buckets; i++) {
         table.add(Arrays.asList(i * bucketSize, 0, 0));
      }
      table.add(Arrays.asList(-1, 0, 0));
      int tableHeight = table.size()-1;
      
      
      try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
         
         String line;
         reader.readLine(); //Clear out headers
         while ((line = reader.readLine()) != null) {
            
            String[] parts = line.split(",");
            int startYear;
            int endYear;
            int yearCount;
            try {
               if (parts[0].equals(STAR)) startYear = -1;
               else startYear = Integer.parseInt(parts[0]);
               if (parts[1].equals(STAR)) endYear = -1;
               else endYear = Integer.parseInt(parts[1]);
               yearCount = Integer.parseInt(parts[2]);
            } catch (Exception e) {
               System.out.println("INFO - Skipping line (can't be parsed): " + line);
               continue;
            }
            
            int startIndex = (startYear == -1 ?
                              tableHeight :
                              Math.min( startYear / bucketSize , tableHeight));
            int temp = table.get(startIndex).get(1) + yearCount;
            table.get(startIndex).set(1, temp);
            
            int endIndex = (endYear == -1 ?
                              tableHeight :
                              Math.min( endYear / bucketSize , tableHeight));
            temp = table.get(endIndex).get(2) + yearCount;
            table.get(endIndex).set(2, temp);
            
            totalQueries += yearCount;
            
         }
         
         FileWriter writer = new FileWriter(outputFile);

         //File header
         writer.write("year,startCount,endCount\n");
         
         for (List<Integer> row: table) {
            //This makes the very bold assumption that there are exactly 3 entries per row that we care about
            writer.write((row.get(0) == -1 ? "*" : row.get(0)) + "," + row.get(1) + "," + row.get(2) + "\n");
         }
         
         writer.write("totalQueries," + totalQueries + "," + totalQueries);
         
         writer.flush();
         writer.close();
         
      } catch (FileNotFoundException e) {
         System.out.println("Cannot find that file! (" + inputFile + ")");
         e.printStackTrace();
      } catch (IOException e1) {
         System.out.println("IO Exception!  Message: " + e1.getMessage());
      }
   }
   
   
}
