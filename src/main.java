 /**   
*	Copyright <c> Claude Bernard - University Lyon 1 -  2013
* 	License : This file is part of the DataConf application, which is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License. See details at : http://liris.cnrs.fr/lionel.medini/wiki/doku.php?id=dataconf&#licensing 
*   Author: Lionel MEDINI(supervisor), Florian BACLE, Fiona LEPEUTREC, Benoît DURANT-DE-LA-PASTELLIERE, NGUYEN Hoang Duy Tan
*   Description: This JSON object contains all the configurations of the application. It is a crutial part of the system, it desribes :
*				-> The conference informations, the uri, the logo uri and the name.
*				-> All the datasources defined by their uris, the cross domain  mode they use, and the commandStore (see /model) related to them.
*				   This command store contains the definition of all the command (a specific parameters+query+callback implementation) that can be send on it.
*				-> All the routes that the app will use. Each route is configured to display a specific view, if a template exist for this view name (see /templates)
				   it is rendered, otherwise a generic view is used. The commands we want to send are specified in a "command" array to explicit which command has to be send when the route is catched
				   
*   Tags: Ontology, Keywords, Recommendation,ACM
**/


import java.io.File;
import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.*;//JDK7 reading and writing file
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


import java.util.StringTokenizer;

//Import OWLApi 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;


import static java.util.Arrays.asList;



public class main {

/**
 * FIlE_NAME : real adress file of keywords to insert in the ACM ontology
 **/
  final static String FILE_NAME = "C:/Users/Fiona Le Peutrec/Desktop/keywords.txt";
  /**
   *OWL IRI ONTOLOGY_IRI : create save file for the ontology, already contain ACM ontology
   **/
  public static final IRI ONTOLOGY_IRI = IRI.create("C:/Users/Fiona Le Peutrec/Desktop/ontoDataConf.owl");
  final static Charset ENCODING = StandardCharsets.UTF_8;
  /**
   *List of word to delete from keywords because they haven't meaning
   **/
  static List<String> forbiddenWord = new ArrayList<String>(asList("the", "and", "for","to","a","an","in","of","over","with", "," , "/"));
  
  /**
   *Suffix for OWLClasse IRI
   **/
  static String classIRISuffixe = "http://www.heppnetz.de/ontologies/skos2owl/11033#";
 
/**
 * @param aFileName
 * @return List<String> who contain keywords
 * @throws IOException
 * 
 * Read the keywords file.
 **/
  public static List<String> readKeywords(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
		  }
 /**
  * @return HashMap<ArrayList<String>>,ArrayList<String>> Map of normalize and regular keywords
  * @throws IOException
  *Create map with key : normalized(Porter Algorithm) Tokenized, splited, keywords value : Original keyword
  **/
  
 public static HashMap<ArrayList<String>, ArrayList<String>> initilizationKeyword( ) throws IOException
 {
	//Key : normalized keyword value : none normalized keyword + label
     HashMap<ArrayList<String>,ArrayList<String>> keywordMap = new HashMap<ArrayList<String>,ArrayList<String>>();
	//Porter Algorithm 
     Porter p = new Porter();
	 //Get all keywords from keywords file
     List<String> keywordsToNormalize = readKeywords("C:/Users/Fiona Le Peutrec/Desktop/keywords.txt");
     
     Iterator<String> itr = keywordsToNormalize.iterator();
     while(itr.hasNext()){
     	String orgWord = itr.next().trim();
     	String word = orgWord.replaceAll("_"," ");
     			word.replaceAll("-", " ");
     	 StringTokenizer keyword = new StringTokenizer(word," ");
     	 
     	ArrayList<String> completeKeywordNormalize = new  ArrayList<String>();
     	
     	 String str = null;
     	 //Normalize each token with Porter
     	 while (keyword.hasMoreTokens()) {
     		  str = keyword.nextToken();
     		 if(!forbiddenWord.contains(str)){
              String normalizeKeyword = p.stripAffixes(str);
              completeKeywordNormalize.add(normalizeKeyword);
              }
     	 }
     	 //If we have an equivalent normalized keyword, put it in the value list.
     	 //Two keywords with same normalization are equals in meaning.
     	 if(keywordMap.containsKey(completeKeywordNormalize)){
     		 keywordMap.get(completeKeywordNormalize).add(orgWord);

     	 }else{
     		 //Else we add a new entry with this keywprd in the map
         		 ArrayList<String> value = new  ArrayList<String>();
         		 value.add(orgWord);
         		 keywordMap.put(completeKeywordNormalize,value);
     	 }
  }
    
System.out.println(keywordMap.toString());
return keywordMap;
	 
}
 


 
 /**
  * 
  * @param HashMap<ArrayList<String>,String> container.Contain label and normalized label form.
  * @param OWLOntology ontology
  * @param OWLDataFactory df
  * @return void
  * Put all label class from the ontology and their normalized form in the HashMap container.
  **/
 public static void normalizationLabelClass(HashMap<ArrayList<String>,String> container,OWLOntology ontology,OWLDataFactory df)
 {
	 //Normalization System (see class Porter)
	 Porter p = new Porter();
	 
	 //Course all classes in the ACM ontology
	 for (OWLClass cls : ontology.getClassesInSignature()) {
	    	// Get the annotations on the class that use the label property
		 for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSLabel())) {
	    		if (annotation.getValue() instanceof OWLLiteral) {
			    	OWLLiteral val = (OWLLiteral) annotation.getValue();
			    	String label="";
			    	// look for English labels
			    	if (val.hasLang("en")){
			    		label = val.getLiteral();
			    		String word = label.replaceAll("_"," ");
		     			word.replaceAll("-", " ");
		     			StringTokenizer keyword = new StringTokenizer(word," ");
	     			
		     			ArrayList<String> completeKeywordNormalize = new  ArrayList<String>();
	     	     	
		     			String str = null;
			     	     	 //On passe chacun des token dans l'algo de porter 
			     	     	 while (keyword.hasMoreTokens()) {
			     	     		  str = keyword.nextToken();
			     	              String normalizeKeyword = p.stripAffixes(str);
			     	             // System.out.println(str+" => "+normalizeKeyword);
			     	              //Permet la reconstruction des keywords à plusieurs mot
			     	             if(!forbiddenWord.contains(str)){
			     	         		 ArrayList<String> value = new  ArrayList<String>();
			     	         		 value.add(label);
			     	         		 completeKeywordNormalize.add(normalizeKeyword);
			     	     		 }    
			     	     	}
			         		container.put(completeKeywordNormalize,label);
				    }
	    		}
		 }

	 }
	 System.out.println("End of normalizationLabelClass");
	 
 }
 
 
/**
 * @param ArrayList<String> label, contain Tokenized label.
 * @param HashMap<ArrayList<String>,ArrayList<String>> keywordsMap, fill out with initilizationKeyword( ). 
 * @return boolean 
 * Check if a normalized, tokenized label(ACM) is contain in one of tokenized, normalized keyword.
 * Return true, if ArrayList<String> label is contain in one of HasMap key.
 **/
public static boolean isContainInKeywordsConf(ArrayList<String> label,HashMap<ArrayList<String>,ArrayList<String>> keywordsMap){

	Iterator<String> it = label.iterator();
	while(it.hasNext()){
		String lab = it.next();
		for(Entry<ArrayList<String>,ArrayList<String>> entry2 : keywordsMap.entrySet()) {
			  ArrayList<String> key2 = entry2.getKey();
			  System.out.println(key2 + "contains " + lab +"  "+key2.contains(lab));
			  if(key2.contains(lab)){
				  return true;
			  }
		}
	}
	return false;
}

/**
 * @param OWLDataFactory df
 * @param OWLOntology ontology
 * @param OWLOntologyManager manager
 * @throws OWLOntologyStorageException
 * Change IRI class.The new IRI contain the label of the class.
 **/
public static void changeInitialOnto(OWLDataFactory df,OWLOntology ontology,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	//HashMap<IRI,IRI> mapIRI = new HashMap<IRI,IRI>();
	//Create an ontologies set for renamer
	Set<OWLOntology> setOfOntologies = new HashSet<OWLOntology>();
	//Add our ontology
	setOfOntologies.add(ontology);
	//Create the OWLEntityRenamer for rename IRI class
	OWLEntityRenamer renamer = new OWLEntityRenamer(manager, setOfOntologies);
	
	 //Course all classes in the ACM ontology and get their current IRI
	for (OWLClass cls : ontology.getClassesInSignature()) {
		IRI oldIRI = cls.getIRI();
    	// Get the annotations on the class that use the label property (rdf:label)
    	for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSLabel())) {
	    	if (annotation.getValue() instanceof OWLLiteral) {
	    		OWLLiteral val = (OWLLiteral) annotation.getValue();
	    		//Label = val without space in the end or begin (trim()). Erase space problem in string compare. 
	    		String label = val.getLiteral().trim();
	    		String Label = label.replaceAll(" ","_");
	    		//Create the new IRI with a clean label
	    		IRI newIRI = IRI.create(classIRISuffixe+Label);
	    		//Change the old IRI for the new one for the class cls.
	    		List<OWLOntologyChange> changes = renamer.changeIRI(cls,newIRI);
	    		//Apply changes with manager
	    		manager.applyChanges(changes);
	    		//mapIRI.put(oldIRI,newIRI);
	    	}
    	}
	}
	System.out.println("End of changeInitialOnto");
	
}

/**
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @throws OWLOntologyStorageException
 * @return void
 * Erase duplicate Class in the ontology.Class with "-tax" suffixe
 **/
public static void eraseDuplicate(OWLOntology ontology,OWLDataFactory df,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	for (OWLClass cls : ontology.getClassesInSignature()) {
    	IRI Iri = cls.getIRI();
    	String iri = Iri.toString();
    	if(iri.contains("-tax")){
			  OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
			  cls.accept(remover);
	          manager.applyChanges(remover.getChanges());
	          manager.saveOntology(ontology);
    	}
	}
	System.out.println("End of eraseDuplicate");

}

/**
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @param HashMap<ArrayList<String>,String> labelContainer
 * @param HashMap<ArrayList<String>,ArrayList<String>> keywordsMap
 * @throws OWLOntologyStorageException
 * @throws InterruptedException
 * Erase all classes from the ACM ontology if they have no meaning link with our keywords. If his label doesn't contain one of 
 * our normalized keywords in his normalized form, erased it.
 **/
public static void clearInitialOnto(OWLOntology ontology,OWLOntologyManager manager,OWLDataFactory df,HashMap<ArrayList<String>,String> labelContainer,HashMap<ArrayList<String>,ArrayList<String>> keywordsMap) throws OWLOntologyStorageException, InterruptedException{
	
	for(Entry<ArrayList<String>,String> entry1 : labelContainer.entrySet()) {
		ArrayList<String> key1 = entry1.getKey();
		String value1 = entry1.getValue();
			  
		 if(!isContainInKeywordsConf(key1, keywordsMap)){
			//Create entity Remover  
			  OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		        OWLClass cls = getClassByLabel(ontology,value1,df);
		        //Remove class
		            cls.accept(remover);
		            //Apply changes with Manager
		            manager.applyChanges(remover.getChanges());
		            remover.reset();
			  }
	}
	//Save ontology
    manager.saveOntology(ontology);
	System.out.println("End of clearInitialOnto");
}

/**
 * @param HashMap<ArrayList<String>,ArrayList<String>> classToAdd
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @throws OWLOntologyStorageException
 * @return void 
 * Add OWLClasses in the ontology for each of our keywords.
 **/
public static void addNewClassFromKeywords(HashMap<ArrayList<String>,ArrayList<String>> classToAdd,OWLOntology ontology,OWLDataFactory df,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	//Course all keywords in the map.
	for(Entry<ArrayList<String>,ArrayList<String>> entry2 : classToAdd.entrySet()) {
		  ArrayList<String> value2 = entry2.getValue();
		  
		  String valeur = value2.get(0).replaceAll(" ","_");
		  //Create OwlClass.The IRI contain keywords non normalized value.
	  	  OWLClass cls = df.getOWLClass(IRI.create(classIRISuffixe+valeur));
	  	  OWLDeclarationAxiom declarationAxiom = df.getOWLDeclarationAxiom(cls);
	      manager.addAxiom(ontology, declarationAxiom);
	  	 //Add all possible labels to the new class.
	  	  Iterator<String> it = value2.iterator();
	  	     while(it.hasNext()){
	  	    	 String lab = it.next();
	  	    	 //Get label field of the OWLClasse
	  	    	 OWLAnnotation labelValue = df.getOWLAnnotation(df.getRDFSLabel(),df.getOWLLiteral(lab, "en"));
		  	     OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), labelValue);
		  	     manager.applyChange(new AddAxiom(ontology, ax));
	  	     }
	}
    manager.saveOntology(ontology);	
	System.out.println("add end");
}
		
/**
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @param HashMap<ArrayList<String>,ArrayList<String>> keywordsMap1
 * @param HashMap<ArrayList<String>,ArrayList<String>> keywordsMap2
 * @throws InterruptedException
 * @throws IOException
 * @throws OWLOntologyStorageException
 * Create relation axioms subClassOf and equivalentClass between our keywords Class
 **/
public static void addAxiomBetweenOurKeyword(OWLOntologyManager manager,OWLDataFactory df,OWLOntology ontology,HashMap<ArrayList<String>,ArrayList<String>> keywordsMap1, HashMap<ArrayList<String>,ArrayList<String>> keywordsMap2) throws InterruptedException, IOException, OWLOntologyStorageException{
	
	for(Entry<ArrayList<String>,ArrayList<String>> entry1 : keywordsMap1.entrySet()) {
		ArrayList<String> key1 = entry1.getKey();
		ArrayList<String> value1 = entry1.getValue();
	
		for(Entry<ArrayList<String>,ArrayList<String>> entry2 : keywordsMap2.entrySet()) {
			  ArrayList<String> key2 = entry2.getKey();
			  ArrayList<String> value2 = entry2.getValue();
			 if(!value1.get(0).equals(value2.get(0))){
				//Find the class in Ontology
		  		 OWLClass clsA = getClassByLabel(ontology,value1.get(0),df) ;
		  	     OWLClass clsB = getClassByLabel(ontology,value2.get(0),df);
		
			  	   if(FirstEqualTheSecond(key1,key2)){
		  				 OWLAxiom axiom = df.getOWLEquivalentClassesAxiom(clsA, clsB);
				  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
				  	     // We now use the manager to apply the change and save the onto
				  	     manager.applyChange(addAxiom);
			  			
			  	   }else{ 
			  		   
				  		if(FirstContainTheSecond(key2,key1)){//Keyword contain Label
				  			 // Now create the SubClass axiom
					  	     OWLAxiom axiom = df.getOWLSubClassOfAxiom(clsB, clsA);
					  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
					  	     // We now use the manager to apply the change and save the onto
					  	     manager.applyChange(addAxiom);
							  	  
						  }else{
						  		if(FirstContainTheSecond(key1,key2)){//Label contain Keyword 
							  	     // Now create the SubClass axiom
							  	     OWLAxiom axiom = df.getOWLSubClassOfAxiom(clsA, clsB);
							  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
							  	     // We now use the manager to apply the change and save the onto
							  	     manager.applyChange(addAxiom);
							  	}
						  }
				  }
			 }
		}
	}
//Save ontology 
manager.saveOntology(ontology);
System.out.println("fin add axiom");
}		

/**
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @param HashMap<ArrayList<String>,String> labelOnto.Fill out with normalizationLabelClass().
 * @param HashMap<ArrayList<String>,ArrayList<String>> keywords.
 * @throws InterruptedException
 * @throws IOException
 * @throws OWLOntologyStorageException
 * @return void
 * Add axiom subClassOf, equivalentClass between ACM class (labelOnto) and our keywords class (keywords)
 */
public static void addAxiomBetweenKeywordAndOntology(OWLOntologyManager manager,OWLDataFactory df,OWLOntology ontology,HashMap<ArrayList<String>,String> labelOnto, HashMap<ArrayList<String>,ArrayList<String>> keywords) throws InterruptedException, IOException, OWLOntologyStorageException{
	
	for(Entry<ArrayList<String>, String> entry1 : labelOnto.entrySet()) {
		ArrayList<String> key1 = entry1.getKey();
	    String value1 = entry1.getValue();
	
		for(Entry<ArrayList<String>,ArrayList<String>> entry2 : keywords.entrySet()) {
			  ArrayList<String> key2 = entry2.getKey();
			  ArrayList<String> value2 = entry2.getValue();
		  	
		  		//Find the class in Ontology.Call getClassByLabel() function. 
		  		 OWLClass clsA = getClassByLabel(ontology,value1,df) ;
		  	     OWLClass clsB = getClassByLabel(ontology,value2.get(0),df);
		  	     
		  	   if(FirstEqualTheSecond(key1,key2)){
	  				 OWLAxiom axiom = df.getOWLEquivalentClassesAxiom(clsA, clsB);
			  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			  	     // We now use the manager to apply the change and save the onto
			  	   //System.out.println(key1 + " equal " + key2);
			  	     manager.applyChange(addAxiom);
			  	     manager.saveOntology(ontology);
		  		}else{ 
			  		if(FirstContainTheSecond(key2,key1)){//Keyword contain Label
			  			 // Now create the SubClass axiom
				  	     OWLAxiom axiom = df.getOWLSubClassOfAxiom(clsB, clsA);
				  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
				  	     // We now use the manager to apply the change and save the onto
				  	   //System.out.println(key2 + " contain " + key1);
				  	     manager.applyChange(addAxiom);
				  	     manager.saveOntology(ontology);
			  		}else{
				  		if(FirstContainTheSecond(key1,key2)){//Label contain Keyword 
					  	     // Now create the SubClass axiom
					  	     OWLAxiom axiom = df.getOWLSubClassOfAxiom(clsA, clsB);
					  	     AddAxiom addAxiom = new AddAxiom(ontology, axiom);
					  	     // We now use the manager to apply the change and save the onto
					  	     //System.out.println(key1 + " contain " + key2);
					  	     manager.applyChange(addAxiom);
					  	     manager.saveOntology(ontology);
					 
					  	}
				  }
		  		}
		}
	  
	}
	System.out.println("fin add axiom with onto");
}



/**
 * @param OWLOntology ontology
 * @param OWLDataFactory df
 * @param OWLOntologyManager manager
 * @return
 * @throws InterruptedException
 */
public static OWLClass getClassByLabel(OWLOntology ontology, String lab,OWLDataFactory df) throws InterruptedException{
	
	for (OWLClass cls : ontology.getClassesInSignature()) {
    	// Get the annotations on the class that use the label property
    	for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSLabel())) {
	    	if (annotation.getValue() instanceof OWLLiteral) {
	    		OWLLiteral val = (OWLLiteral) annotation.getValue();
	    		String label = val.getLiteral();
	    		 String val1 = label.trim();
	    		 String val2 = lab.trim();//Split space in string
	    		if(val1.equals(val2)){//Find the class with the corresponding label
	    			return cls;
	    		}
	    	}
    	}
	}

	return null;
}


/**
 * @param key1
 * @param key2
 * @return
 * @throws InterruptedException
 **/
public static boolean FirstContainTheSecond(ArrayList<String> key1 ,ArrayList<String> key2) throws InterruptedException{
boolean res = false;
	
	  if(!FirstEqualTheSecond(key1,key2)){
		  Iterator<String> it1 = key2.iterator();
		  while(it1.hasNext()){
			  res = true;
			  if(key1.contains(it1.next()) & res){
				 res = true;
			  }else{
				  return false;
			  }
			
		  }
	  }
	return res;
}

/**
 * @param ontology
 * @param df
 * @param manager
 * @throws OWLOntologyStorageException
 */
public static void addInstances(OWLOntology ontology,OWLDataFactory df,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	for (OWLClass cls : ontology.getClassesInSignature()) {
    	// Get the annotations on the class that use the label property
    	for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSLabel())) {
	    	if (annotation.getValue() instanceof OWLLiteral) {
	    		OWLLiteral val = (OWLLiteral) annotation.getValue();
	    		String label = val.getLiteral().trim().replaceAll(" ", "_");
	    		OWLNamedIndividual instance = df.getOWLNamedIndividual(IRI.create("http://www.heppnetz.de/ontologies/skos2owl/11033#"+label));
	    	
	    		OWLClassAssertionAxiom classAssertion = df.getOWLClassAssertionAxiom(cls, instance);
	    		// Add the class assertion
	    		manager.addAxiom(ontology, classAssertion);
	    		manager.saveOntology(ontology);
	    	}
    	}
	}
	System.out.println("End of addInstances");
}


public static boolean FirstEqualTheSecond(ArrayList<String> key1 ,ArrayList<String> key2) throws InterruptedException{
boolean res = false;
	  Iterator<String> it1 = key1.iterator();
	  Iterator<String> it2 = key2.iterator();
	  while(it1.hasNext() && it2.hasNext() && res){
		  res=true;
			  if((it1.next()).equals(it2.next()) ){
				  	res=true;
				  	
			  }else{
				  return false;
			  }
		
		  }
	  if(res){
	/*  System.out.println(key1 +" equals   "+ key2 + " "+res);
	  Thread.sleep(1000);*/
	  }
	return res;
}


/**
 * @param ontology
 * @param df
 * @param manager
 * @throws OWLOntologyStorageException
 * Erase rdf comment for each class
 */
public static void eraseComments(OWLOntology ontology,OWLDataFactory df,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	for (OWLClass cls : ontology.getClassesInSignature()) {
    	// Get the annotations on the class that use the label property
    	for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSComment())) {
	    	
	    		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), annotation);
	    		RemoveAxiom moveAxiom = new RemoveAxiom(ontology, ax);
	            manager.applyChange(moveAxiom);
	    		
	    	
    	}
	}
	
	
}

/**
 * @param ontology
 * @param df
 * @param manager
 * @throws OWLOntologyStorageException
 * Let only one label for each classes
 */
public static void eraseLabels(OWLOntology ontology,OWLDataFactory df,OWLOntologyManager manager) throws OWLOntologyStorageException{
	
	for (OWLClass cls : ontology.getClassesInSignature()) {
    	// Get the annotations on the class that use the label property
    	for (OWLAnnotation annotation : cls.getAnnotations(ontology, df.getRDFSLabel())) {
	    	
	    		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), annotation);
	    		RemoveAxiom moveAxiom = new RemoveAxiom(ontology, ax);
	            manager.applyChange(moveAxiom);
	    		
	    	
    	}
	}
	
	
}
	
 public static void main(String[] args) throws Exception {
      
			      try {
			    	  
			    	  HashMap<ArrayList<String>, ArrayList<String>>  keywordMap = initilizationKeyword();
			    	  File file = new File("C:/Users/Fiona Le Peutrec/Desktop/ontoDataConf.owl");
			    	  
			    	  // Load ontology.
				      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				      OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
				      System.out.println("Loaded ontology: " + ontology);
				      OWLDataFactory df = OWLManager.getOWLDataFactory();
				    
				      HashMap<ArrayList<String>,String> labelContainer = new HashMap<ArrayList<String>,String>(); 
					  normalizationLabelClass(labelContainer, ontology, df);
					  eraseDuplicate(ontology,df, manager);
					  clearInitialOnto(ontology,manager,df,labelContainer,keywordMap);
					  changeInitialOnto(df,ontology,manager);
					  addNewClassFromKeywords(keywordMap,ontology,df,manager);
					  manager.saveOntology(ontology);
					  
					  addAxiomBetweenOurKeyword(manager,df,ontology,keywordMap,keywordMap);
					  addAxiomBetweenKeywordAndOntology(manager,df,ontology,labelContainer,keywordMap); 
	
					  addInstances( ontology, df, manager);
					  
					  //Reduce ontology size 
				      eraseComments( ontology, df, manager);
				      eraseLabels( ontology, df, manager);
				      manager.saveOntology(ontology);
					  
				
			      } catch (OWLOntologyCreationException e) {
			    	  e.printStackTrace();
			      }
}
 
}
 
	 