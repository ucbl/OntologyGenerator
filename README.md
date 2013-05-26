OntologyGenerator
=================

Enrich the ACM ontology with your keywords.
It use the OWL Api.

Files :
	- Porter.java contain the Porter Algorithm.It's use for normalized English Keywords.
	- main.java contain all functions and the excution for enrich the ACM ontology.
	- keywords.txt contains keywords you want to insert in the ACM ontology.
	- ontology.owl file where your ontology will generate.
	- acm.owl original ACM ontology file.
	- example : Contains example of keywords and genarate ontology.
	
How to use the ontology Generator ? 

-------------- 1
Firts,erase olds keywords and copy your keywords list in the keywords.txt file.
Your file must look like that :

//Start file keywords.txt
	Aggregate Views
    Attempto Controlled English
    B2B
    BMEcat
    Bipolar abstract argumentation
    BnF
    Cloud computing
    Controlled Natural Languages
    Creating Linked Data
    CubicWeb
    Culture
    DBpedia
    Database mapping tools
    Digital Libraries and Cultural Heritage
    E-Commerce
    Encoded Archival Description
    Entity linking
    Evaluation Measures
	....
	....
	//End file keywords.txt
	
-------------- 2 
Replace the onto.owl with th acm.owl 
At start, onto.owl must seem like : 
	//Start file 
	
	<?xml version="1.0"?>
<!--
========================================================================
11033.owl
This file contains generic and taxonomy classes.
See the documentation at http://www.heppnetz.de/skos2owl/ for details.
========================================================================
-->

<!DOCTYPE rdf:RDF
[<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
 <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
 <!ENTITY owl "http://www.w3.org/2002/07/owl#">
 <!ENTITY ptop "http://proton.semanticweb.org/2005/04/protont#">
 <!ENTITY baseURI "http://www.heppnetz.de/ontologies/skos2owl/11033">
]>

<rdf:RDF xmlns="&baseURI;#"
         xml:base="&baseURI;"
         xmlns:baseURI="&baseURI;#"
	 xmlns:rdf="&rdf;"
	 xmlns:rdfs="&rdfs;"
	 xmlns:ptop="&ptop;"
	 xmlns:owl="&owl;">


<!-- Ontology Header -->
<owl:Ontology rdf:about="&baseURI;">
	<rdfs:label xml:lang="en">The ACM Computing Classification System (CCS)</rdfs:label>
	<rdfs:comment xml:lang="en">OWL version ov the 2012 ACM classification</rdfs:comment>
	<rdfs:comment xml:lang="en">This ontology was derived from a SKOS vocabulary using the SKOS2OWL online tool.
The GenTax algorithm was developed by Martin Hepp and Jos de Bruijn. The SKOS2OWL tool was developed by Martin Hepp and Andreas Radinger.
For more information see http://www.heppnetz.de/skos2owl/
The GenTax algorithm is explained in the following publication:
Martin Hepp, Jos de Bruijn: GenTax: A Generic Methodology for Deriving OWL and RDF-S Ontologies from Hierarchical Classifications, Thesauri, and Inconsistent Taxonomies, Proceedings of the 4th European Semantic Web Conference (ESWC 2007), June 3-7, Innsbruck, Austria, in: E. Fraconi, M. Kifer, and W. May (Eds.): ESWC 2007, LNCS 4519,  Springer 2007, pp.129-144.
An authors' version is available at
http://www.heppnetz.de/publications/#gentax
	</rdfs:comment>
	<owl:imports rdf:resource="http://proton.semanticweb.org/2005/04/protont"/>
</owl:Ontology>


<owl:Class rdf:ID="10002944-gen">
	<rdfs:label xml:lang="en">General and reference </rdfs:label>
	<rdfs:comment xml:lang="en">General and reference : Objects are entities that could be claimed to exist - in some sense of existence. An object can play a certain role in some happenings. Objects could be substantially real - as the Buckingham Palace or a hardcopy book - or substantially imperceptible - say, an electronic document that exists only virtually, one cannot touch it.</rdfs:comment>
	<rdfs:subClassOf rdf:resource="#10002944-tax"/>
	<rdfs:subClassOf rdf:resource="&ptop;Object"/>
</owl:Class>
<owl:Class rdf:ID="10002944-tax">
	<rdfs:label xml:lang="en">General and reference </rdfs:label>
	<rdfs:comment xml:lang="en">Anything that can in any relevant context be classified under the respective label.</rdfs:comment>
	<rdfs:subClassOf rdf:resource="&ptop;Object"/>
</owl:Class>
....
.....
.....

///End file 

-------------- 3   
Now we can generate our enrich ontology.
For that, execute the main.java, and let it RUN.It can take many hours, that depends of the number of keywords.

You have many execution options : 

In the main.java file you can see the execution : 
You comment the line, if you don't her execution.

					----- Delete keywords in ACM how have no relation wih our keywords.-----
					  clearInitialOnto(ontology,manager,df,labelContainer,keywordMap);
					  
					----- Change all classes IRI.Put label classes in the IRI. -----
					  changeInitialOnto(df,ontology,manager);
					  manager.saveOntology(ontology);
					  
					----- Add class for all keywords.(Required) -----
					  addNewClassFromKeywords(keywordMap,ontology,df,manager);
					  manager.saveOntology(ontology);
					  
					----- Add relation of subClass, superClass and equivalentClass between our keywords classes(Required) -----
					  addAxiomBetweenOurKeyword(manager,df,ontology,keywordMap,keywordMap);
					  manager.saveOntology(ontology);
					  
					----- Add relation of subClass, superClass and equivalentClass between our keywords classes and ACM classes (keywords class insertion)(Required) -----
					  addAxiomBetweenKeywordAndOntology(manager,df,ontology,labelContainer,keywordMap); 
					  manager.saveOntology(ontology);
					
					----- Create instances for each classes. -----
					  addInstances( ontology, df, manager);
					  manager.saveOntology(ontology);
					  
					  
					  
					----- Reduce ontology size -----
					
					----- Erase comments -----
				      eraseComments( ontology, df, manager);
					  manager.saveOntology(ontology);
					----- Erase all labels (they can be replaced by intances(OWLNamedIndividual) -----
				      eraseLabels( ontology, df, manager);
				      manager.saveOntology(ontology);
					  

 
At the end of the execution, your ontology will be saved in the onto.owl file.
	
	