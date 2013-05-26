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
Your keywords must be copy in the same way as olds keywords (one keyword by line and no empty line)
	
-------------- 2 


Replace the onto.owl with th acm.owl 
At start, onto.owl must be exactly the same as acm.owl .

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
					  

 
At the end of the execution, your ontology will be saved in the ontology.owl file.
	
	
