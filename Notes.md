#Features
Some way of doing analysis on data produced by experiments.
The data will be in the database, need some way to select data from it, more
importantly, need to know if there is a population of results or a single result.
Then there are comparison measures, are we comparing a set of numbers or a set of populations?

single number, show it, or pass it along to some other analysis routine
set of numbers, can summarize

pair of numbers, can measure
pair of sets,

what about trajectories?

Priority of the lower goals of the project:
1. Analysis Selector
2. Design UI (Console or Web-based)

#Overall UI
===========
```
Experiment Explore    -> Search             -> Continue        -> Proto Editor      -> 
                                            -> Redo                                 -> Experiment Monitoring -> Results DB
                                            -> New             -> Proto Editor      -> 
                      -> Analyze (Party)    -> Result Selector -> Analysis Selector -> New      -> Presentation Selector
                                                                                    -> Template ->
```

The dispacher needs to start up a server that sends out information and listens for control information.
If it gets a http request, then the website gets rolling and goes into experiment explore.
Does the server run independently of any dispatcher?

##Experiment Explore
* query goes out to server
* result is returned with all experiments known by the given db (should there be a db chooser?)
* Selecting an experiment brings up its relevant info (parse the protocol buffer)
* User then communicates to search/party/back

##Experiment Monitoring
* Not sure how to organize this
* Machine or Experiment centric, but really, some listing of Machines, Experiments, and the parameters.
* Want some overview of how far along in the experiments that were generated we are, or how much time.
* Want some overview on machine health (machines we thought are running + replicates, vs. what we hear back from)
* Can select specific experiment result to analyze (jumps to Result Selector View for the associated tables)

##Result Selector View
* User starts with a table (either experiment or more specific due to catching a live one)
* Can transition to other tables that are related to this experiments
* Need to extract a graph of the relationship of the tables
* Need to have the option to select any result as well (just in case it is a cross experiment comparison)
* Selected results can be named and go into some holding view

##Analysis Selector View
* Generating New Analysis
* Offer analysis options (mean, T-Stat, etc.) based on the type of results that have been selected
* Connect analysis objects to each other (computational graph idea)
* Have options to store these computations
* Update if the connected results are live

##Presentation Selector
* At the end of the analysis select ways to view the results
* Create some standard templates, or use JavaScript (or something to develop new ones).
* Need to offer ways to export the presentation product (SVG/PDF/Latex/etc.)

