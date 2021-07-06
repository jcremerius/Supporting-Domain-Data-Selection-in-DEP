# Guiding-Information-Filtering-in-DEP

The jupyter notebook "ICPM_Event_Log_Generation.ipynb" in this repository creates the event log for the evaluation in the paper "Guiding Information Filtering in Data-Enhanced Process Models". It uses the MIMIC-IV (https://physionet.org/content/mimiciv/1.0/) database, stored in a Postgres database. 
First, all cases related to acute Heart Failure are retrieved. Then, the hospital data for their respective hospital admission is fetched, including laboratory values, admission/discharge data, and intensive care unit measurements. 
The database can be accessed via physionet, which requires CITI training for access (https://mimic.mit.edu/iv/).
The information filtering has been implemented as an extension of the Inductive visual Miner (IvM), which is also available in this repository.

1. Generate the event log by executing the jupyter notebook "ICPM_Event_Log_Generation.ipynb".
2. Use the generated event log csv to classify event attributes and calculate the measurement of variability (CV) in the jupyter notebook "Guiding Information Filtering.ipynb". The script is also able to use any other event log (csv) as input. Then, the activity and case_id in the script need to be adjusted. The generated files attributesForActivity.csv and attributeClasses.csv need to be included in the IvM implementation. These are already available for the event log generated at step 1.
3. Create the XES file from the event log csv with any tool of your preference. We used Disco (https://fluxicon.com/disco/).
4. Execute PROM from your preferred java ide (e.g. Eclipse) and use the Inductive Visual Miner. Do not update any packages in the ProM Package Manager, as this might overwrite the IvM implementation. On the right hand side, the button "attribute selection" opens the GUI to filter event attributes and allows to add these as event attirbute aggregations. 


Link to the Setup of PROM in an IDE: https://svn.win.tue.nl/trac/prom/wiki/Contribute


The main contribution of this paper can be found in the following file: InductiveVisualMiner/src/org/processmining/plugins/inductiveVisualMiner/dep/DepView.java. The class DepView defines the visualization proposed in the contribution and provides the mechanisms to highlight event attributes which guide the user to find the event attribute of interest.
