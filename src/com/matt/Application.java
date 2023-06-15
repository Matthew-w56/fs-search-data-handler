package com.matt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Application {
   
   private static final int DEFAULT_BUCKET_SIZE = 20;
   private static final int DEFAULT_STOP_YEAR = 2024;
   
   //For info on how these choices were chosen, see the Confluence page
   private static final int BIRTH_YEAR_LIVING_CUTOFF = 1985; //1965
   private static final int OTHER_YEAR_LIVING_CUTOFF = 2000; //1980
   
   public static void main(String[] args) {
      
      DataHandler.searchedYearsByType("searchYearsAcrossAllSystemsShortBucket.csv", 4, DEFAULT_STOP_YEAR);
      DataHandler.birthYearSearchesBySystem("birthYearsBySystem.csv", DEFAULT_BUCKET_SIZE, DEFAULT_STOP_YEAR);
      DataHandler.yearTypesSearchedBySystem("yearTypesBySystem.csv");
      DataHandler.livingPersonSearchesBySystem("livingPersonsBySystem.csv", BIRTH_YEAR_LIVING_CUTOFF, OTHER_YEAR_LIVING_CUTOFF, DEFAULT_STOP_YEAR);
      DataHandler.dateRangeLengths("dateRangeLengths.csv", 1, 105);
      
   }
   
}
