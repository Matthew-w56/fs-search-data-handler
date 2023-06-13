Author: Matthew Williams<br>
Date: June 2023

# Overview

  This program is used in conjunction with downloaded log summaries to produce the datasets
  needed for charts and statistics in relation to the dates that people search on within
  the system.


# Instructions for use

  To use this program, follow these steps:
  1) Clone the repo to your machine.
  2) In the top level of the repo (in the same level as src/ and the .gitignore),
      create two new folders called "resource" and "output".
  3) Open up the DataHandler.java class (within src/com/matt) and take note of the
      expected file names for the three resource files that you will download from
      the queries given in the Confluence page.
  4) Go to Splunk and run each query, downloading the results of each and naming them
      appropriately.  (If your file names don't match what's expected in DataHandler.java,
      it is set up to easily allow you to change what the program is expecting by changing
      the static constant in that class.)
  5) Move the downloaded files into the /resource/ folder you created.
  5) Go to the Application.java class and specify which methods of DataHandler's you want
      to run in the main method (All methods and their descriptions are found in DataHandler.java).
  6) Make sure to specify what you want each output file name to be, then run the program.
  7) The output files will appear within the /output/ folder you created.
  8) These files are structured specifically to be graphed, so creating graphs from the data with
      tools such as Excel or others should be quite easy.

# Confluence

  The confluence page about this project and it's use can be found on Confluence by searching
  "Living Persons Search Analysis".  It walks through the goals of the overarching Internship
  project that this program is part of, it's steps, and data about when it was run the first time.
