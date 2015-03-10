README
GeoLocator v1.0

The geolocation algorithm contains both geoparser that extract locations and a geo-coder that assigns latitude and longitude to each location.

The geolocation algorithm contains 4 English parsers (building parser, Toponym heuristic parser, Stanford NER and our CRF tweet-trained parser) and 3 Spanish parsers (building parser, toponym heuristic parser, CRF trained parser) which are included in edu/cmu/geoparser/ folder. The common interface for those parsers is in the folder too.

The algorithm takes a .txt file as input, or else, use the command line tool by entering one sentence per line. The best way to use it is to look at the CmdInputParser.java included in edu.cmu.geoparser.ui.CommandLine.

In addition to a gelocation algorithm, the package contains a fuzzy match algorithm that takes web 2.0 tags plus latitude and longitude as input, and compares them with location entries in the GeoNames gazetteer to determine whether the web 2.0 entries match with the gazetteer entries or they are novel.

//*************************************************************************//
                      Introduction
//*************************************************************************//


Tagging the command line input

The output format for the commandline and batch file: Each recognized location is wraped as XX{location}XX, where XX could be any of the eight tags: TP,tp, ST,st,BD,bd,AB,ab. TP, ST, BD, AB are output from the Named Entity Recognizer. tp,st,bd,ab are the output from the rule based and toponym lookup parsers.

//*************************************************************************//
                      How to install
//*************************************************************************//

The algorithm can run on Windows, Mac, or Linux/Unix platforms.

1.Check out the project.
In eclipse, try import ->project from git.

2. After checked out the project into Eclipse workspace,
Go to the terminal (if you are using linux or mac osx), or cygwin for windows, cd to the geo-locator folder, run isntall.sh to install the software.
This is a long process because we have to download jar files, resources from geonames, and most time-consuming is the indexing of the geoname.
The estimate time is about 1 hour. It varies with your machine. 


//*************************************************************************//
                      The fuzzy match algorithm
//*************************************************************************//

The fuzzy match project is included in edu.cmu.nlp.spelling. LearningToRankDictionaryMatching.java is the tool to train and test a gazetteer matcher. It's based on several features extracted from word form, soundex, and more. See generateFeature() within to get a whole list of features used. To convert this experimental code to a matching tool, we can do the following:

First, as indicated in the main() function, first define a name (argv[2]) for the machine learning model name (svm model), the input file name (argv[0]). Then, use the model to do classification with Test_Re_ranking(). This function is written to be a testing function, but you should convert this function into a function that takes two location strings, with lat. and lon. information attached to each. Then, the function should classify the pair and output if the two pair is a match or not.

Note that, before passing the pair to Test_Re_ranking(), you should have the pairs prepared. This can be done by building a gazetteer index, looking up the target location string you would check for matches in the index, get a couple of top matches, and examine each returned match with the machine learning model. Buildling a lucene index of gazetteer can be down by running install.sh, and checking for match can be done by using the function in EuroLangMisspellParser.java.

/////////////
Contact
/////////////

Please send email to gelern@cs.cmu.edu if you find any bug or have any question, or any suggestions.
