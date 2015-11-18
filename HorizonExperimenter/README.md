#SearchParty
Simple machinery to distribute experiments, organize and analyze results in your search for the best algorithm for your task.


This project is designed to organize experiments and results. It does something like map reduce, it creates a
bunch of jobs with different parameters, farms them out to any available nodes (the mapping step), and then
gathers a bunch of results together from the combined output (the reduce step).

The idea is to allow the experiment designer to focus on the algorithms under test and not spend time messing
around with running experiments and gathering results. Also, the deployment should be as painless as possible,
with minimal installation and configuration needs.

One of the main points of this work is to ensure experimental repeatability, and not restrict what languages
are supported for algorithm development.

Here's the gameplan:
* An algorithm designer isolates all parameters and lists them in a file.
* Probably use Google's protobuf to write a configuration message about what parameters and how to alter them.
* The algorithm must take in each of the listed parameters as a command line argument.

This code will go through, log into each machine, ensure that the endpoint is running, do a quick test.
Then the code will fire up the results data base, and do a few checks on that as well.
Once all the plumbing is checked out, the jobs will begin to be issued according to the protobuf specification.
The user program... should use 0MQ to report results back to endpoint, and then from there, the endpoint will forward the
message to the database writer.

So now we see the requirements on the algorithm developer: take in parameters on the command line, implement a 0MQ client,
and implement two Google protobuf schemas (one that describes the parameter variation, and one that describes the results).

Paths to data should be absolute
All random generators should be seeded, and all seeds should be exposed as parameters

Going to need some kind of experiment browser

Still need to describe a database schema, but it seems like it should be something like:
Key ExperimentName ExperimentDate Git_Repository Branch Tag ExperimentEntryPoint InputProtobuf OutputProtobuf 

From there, each Key maps to one or more experiments
Key_from_master_table, Experiment_table_Name, Parameter_table_Name, String_Exp Description

Each Experiment_table_Name simple has all the output, runs are in the rows, each category of results are in the columns

#Internal Organization
There is a single Dispatcher, and the dispatcher spawns multiple Searchers, one for each machine. The flow is:
Start Dispatcher
Dispatcher: setup REP socket that all Searchers will know where to request experiments
Dispatcher: setup PULL socket that all Searchers will know where to send results
Dispatcher: Spawn Searcher(s) (should this come with the first request??? probably)
Searcher: setup PUSH socket for results publishing
Searcher: publish results to whichever 
Searcher: poll for experiments as replicates are freed (what for a little while and if no experiments quit)



#Structuring an Experiment 
The experiment file that you work with will need to take in command line arguments to set up the various parameters for the
test under study. This can be done in a number of different ways, but outlined some boilerplate way to do this:

 # Passing through all of the options via the command line. The parsing of this isn't as bad as it sounds, and I've given some
 examples in drivers.SampleExperiment.java. The key here is to use the GNU topt functionality (available for most popular languages,
 I've used a ported java version from http://www.urbanophile.com/arenn/hacking/download.html). I've structured that program such
 that all the parsing is done in a separate file (drivers.SampleExperimentKeys.java), and most of that is boilerplate (please review
 the comments inside of those files). The unfortunate thing about that example is that code you need to change is a little
 scattered around in the SampleExperimentKeys.java setup. I've made it more modular in the SampleExperimentKeysOO.java, but it
 makes the enum a little more difficult to read, but not terribly so. The chief drawbacks to this approach is the fact that you'll
 need to supply a default constructor and a bunch of setter methods (one for each of the fields you need to set from the command
 line), or else put the parsing code in the same file as the object you're trying to initialize to allow visibility and read in all
 the values needed for construction prior to object construction. May not be a big deal for you experiment.
 # Creating a file with all the parameters and their values in it. This could be a Google protobuf file or a simple properties file
 which just maps keys to values. In the end, it will look similar to the above method, but you can pull in the values of different
 parameters as you need them, as opposed to all at once in the command line method.


