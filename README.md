[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Reactome Reaction Classifier

## What is the Reactome Reaction Classifier Module

The Reaction Classifier generates a series of reports to help curators in the task of classifying the reactions in Reactome. 

### Reaction Classifier bases 

The Reaction Classifier is based on the following paper published in 2011 in the Database journal by Jupe et al.

####A controlled vocabulary for pathway entities and events
Jupe,S., Jassal,B., Williams,M., Wu,G. A controlled vocabulary for pathway entities and events. Database (2014) Vol. 2014

#####DOI:

[https://doi.org/10.1093/database/bau060](https://doi.org/10.1093/database/bau060)

#####Abstract:

Entities involved in pathways and the events they participate in require descriptive and unambiguous names that are
often not available in the literature or elsewhere. Reactome is a manually curated open-source resource of human 
pathways. It is accessible via a website, available as downloads in standard reusable formats and via Representational 
State Transfer (REST)-ful and Simple Object Access Protocol (SOAP) application programming interfaces (APIs). We have 
devised a controlled vocabulary (CV) that creates concise, unambiguous and unique names for reactions (pathway events) 
and all the molecular entities they involve. The CV could be reapplied in any situation where names are used for pathway 
entities and events. Adoption of this CV would significantly improve naming consistency and readability, with consequent 
benefits for searching and data mining within and between databases.

#### Project components used:

* Reactome graph library 

#### Project usage: 

The Reaction Classifier can be executed by running the executable jar file. Please ensure that Neo4j database is running and correct properties are specified.

**Properties**

When executing the jar file following properties have to be set.

    -h  Reactome Neo4j host. DEFAULT: localhost
    -b  Reactome Neo4j port. DEFAULT: 7474
    -u  Reactome Neo4j user. DEFAULT: neo4j
    -p  Reactome Neo4j password. DEFAULT: neo4j
    -o  Folder where the resports are stores. DEFAULT: ./reports
    -c  The classifier to be run (or 'all' to run all classifiers). DEFAULT: all
    -v  Verbose output.

#### Results:

The Reaction Classifier stores a series of reports (in [CSV format](https://en.wikipedia.org/wiki/Comma-separated_values)) in the specified folder (by default './reports').

The name of the file specifies the section of the paper that has been followed to generated the report (e.g. 'Binding.csv' for 'Binding events').

Additionally to the specific reports, there are two extra files added: 'Classifier_Aggregation_vXX.csv' and 'Classifier_Summary_vXX.csf'.
The first one is an aggregation of all types in a single file and the second is a summary indicating the number of reactions
in each report plus a simple description of the type itself (extracted from the paper). 