package project.model;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

public class Parsing extends Indexing implements DuplicateCounting
{
    /**
     * This class is used to ADD ALL FILES TO DATABASE IF THEY ARE NOT ALREADY EXISTED "INCLUDING INVERTED INDEX".
     * @param stopList_file This is the source of stop list file
     * @param docs_file  This is the source of corpus file
     * @param term_freq  This is the list of all terms with there freq,positions,docId,...
     * @param duplicate  This var is used to find the frequencies of  terms
     * @param corpus  This var is represent a list of docs of corpus file
     * {@code}
     */

    private static String stopList_file = "src/files/stoplist.txt";
    private static String docs_file = "src/files/Corpus_OHSUMED.txt";
    private List<TermOfDoc> term_freq;
    private List<project.model.Document> list;
    private Map<String, Integer> duplicates = null;
    private Map<Integer,Map<String,String>> corpus = null;
    private Double ln = Math.log(10);
    private Map<Integer,List<Pair<String,Double>>> poidsTitre = null;
    private Map<Integer,List<Pair<String,TermOfDoc>>> poidsAbstr= null;



    public Parsing(){
//        startParsing();
//        invertedToDatabase();
        Database.getInstance().addStopList(parseFile(stopList_file)); // add stopList to database if not exists
        if (Database.getInstance().corpusIsEmpty()){ // add corpus to database if not exists
            corpus = new TreeMap<>();
            startParsing();
            Database.getInstance().addCorpusDoc(corpus);
        }
        if(Database.getInstance().invertedIsEmpty()){ // add  invertedIndex to database if not exists
            if(corpus == null )
                startParsing();
            invertedToDatabase();
        }
        System.out.println("----------------------DONE-WITH-FILES------------------");
    }

    /* CONVERTING CORPUS FILE TO LIST OF TERMS */
    private void startParsing(){
        term_freq = new ArrayList<>();
        //extract docs from corpus
        for (String xml:parseDocsFile(docs_file))
        {
            try {
                term_freq.addAll(loadXMLFromString(xml).term_freq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /* INSERTING INVERTED INDEX TO DATABASE */
    private void invertedToDatabase(){
//        for (project.model.Document document:list){
//            term_freq.addAll(document.term_freq);
//        }
        Comparator<TermOfDoc> comparator = Comparator.comparing(o -> underScore(o.term));
        comparator = comparator.thenComparing(o -> o.docId);
        term_freq.sort(comparator);
        System.out.println("Strat collecting");
        List<Collect> collects = collecting();
//        for (Collect collect:collects){
//            collect.toStrings();
//        }
        System.out.println("Ending collecting");
        Database.getInstance().invertedIndex(collects);
    }


    /* PARSING FROM XML TAGS */
    private project.model.Document loadXMLFromString(String xml)
            throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =  builder.parse(new InputSource(new StringReader(xml)));
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Integer docno = Integer.valueOf(xpath.evaluate("/DOC/DOCNO", doc));
        String title = xpath.evaluate("/DOC/TITLE", doc);
        String abstra = xpath.evaluate("/DOC/ABSTRACT", doc);
        if(corpus != null){
            Map<String,String> map = new HashMap<>();
            map.put(title,abstra);
            corpus.put(docno,map);
        }
        return new project.model.Document(
                docno,
                normalisation(eliminateEmptyTerm(token(title))),
                normalisation(eliminateEmptyTerm(token(abstra))));
    }


    /* READ CORPUS FILE */
    private ArrayList<String> parseDocsFile(String file_name){
        // extract each <DOC> ... </DOC> from file AS string (corpus)
        ArrayList<String> list = new ArrayList<>();
        Scanner scanner = null;
        try{
            scanner = new Scanner(new BufferedReader(new FileReader(file_name)));
            scanner.useDelimiter("</DOC>"); // END of Next()
            while (scanner.hasNext())
            {
                list.add(scanner.next()+"</DOC>");
            }
            list.remove(list.size()-1);
        }
        catch (Exception e ){ System.out.println("Error reading file -> "+e.getMessage()); }
        finally {
            if(scanner != null ){scanner.close();} // close the file
        }
        return  list;
    }

    /* READ STOP LIST FILE */
    private ArrayList<String> parseFile(String file_name){    // extract each line from file AS string (stopList)
        ArrayList<String> list = new ArrayList<>();
        Scanner scanner = null;
        try{
            scanner = new Scanner(new BufferedReader(new FileReader(file_name)));
            while (scanner.hasNextLine())
            {
                list.add(scanner.nextLine());
            }
        }
        catch (Exception e ){ System.out.println("Error reading file -> "+e.getMessage()); }
        finally {
            if(scanner != null ){scanner.close();} // close the file
        }
        return  list;
    }


    /* CALCULATING THE WEIGHT OF AN ABSTRACT TERM */
    private double poids_Abstract(TermOfDoc s){  // return the weight of  an abstract
        int freq = s.getFrequency();
        int abstract_length = s.getDocSize();
        int nbrDocs = project.model.Document.count; // 2001
        int nbrDocsi = countingDuplicates(s.term);
        Double idf = Double.valueOf(nbrDocs) / Double.valueOf(nbrDocsi);
        return (Math.log(idf) / ln * Double.valueOf(freq)/Double.valueOf(abstract_length));
    }

     /* CALCULATING THE WEIGHT OF A GIVEN  TERM */
    private double poids(TermOfDoc s){ // return the weigh on general (abstract or title)
        if(s.term.endsWith("_"))
            return  s.getFrequency();
        else {
            return poids_Abstract(s);
        }
    }

    // CALCULATING THE NUMBER OF DUPLICATES TERMS IN ALL CORPUS TO USE THEM IN IDF
    @Override
    public Integer countingDuplicates(String match) {
        if(duplicates == null) { // if it's not already located
            duplicates = new HashMap<>();
            String term;
            int i = 0;
            for (TermOfDoc str : term_freq) {
                term = underScore(str.term);
                if (duplicates.containsKey(term) ) {
                    if(i != str.docId){
                        duplicates.put(term, duplicates.get(term) + 1);
                        i=str.docId;
                    }
                } else {
                    duplicates.put(term, 1);
                    i=str.docId;
                }
            }
        }
        for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
            if (match.equals(entry.getKey()))
                return entry.getValue();
        }
        return 0;
    }

    /* COLLECTING ALL KIND OF FIELDS TO CREATE AN INVERTED INDEX */
   private List<Collect> collecting() //
   {
       String current = underScore(term_freq.get(0).term);
       Double a = null;
       for(Map.Entry<Boolean,Boolean> balise :  term_freq.get(0).balise.entrySet()){
           if(balise.getValue() && balise.getKey()){
               if(term_freq.get(0).term.endsWith("_")) {
                   a = term_freq.get(0).getFrequency() + getPoidsAbatr(term_freq.get(0).term.replaceAll("_$",""),term_freq.get(0).docId);
               }
               else {
                   a = poids_Abstract(term_freq.get(0)) + getPoidsTitle(term_freq.get(0).term+"_",term_freq.get(0).docId);
               }
           }else {
               a = poids(term_freq.get(0));
           }
       }
        Collect collect = new Collect(
                current,
                term_freq.get(0).docId,
                term_freq.get(0).position,
                term_freq.get(0).balise,
                a
        );
        List<Collect> listOfCollection = new ArrayList<>();
        for (int i=1;i<term_freq.size();i++)
        {
            Double b = null;
            for(Map.Entry<Boolean,Boolean> balis :  term_freq.get(i).balise.entrySet()){
                if(balis.getKey() && balis.getValue()){
                    if(term_freq.get(i).term.endsWith("_")) {
                        b = term_freq.get(i).getFrequency() + getPoidsAbatr(term_freq.get(i).term.replaceAll("_$",""),term_freq.get(i).docId);
                    }
                    else {
                        b = poids_Abstract(term_freq.get(i)) + getPoidsTitle(term_freq.get(i).term+"_",term_freq.get(i).docId);
                    }
                }else {
                    b = poids(term_freq.get(i));
                }
            }
            if(current.equals(underScore(term_freq.get(i).term))){
                collect.add(term_freq.get(i).docId,
                        term_freq.get(i).position,
                        b,term_freq.get(i).balise);
            }else{
                current = underScore(term_freq.get(i).term);
//                collect.finish(); // adding docs poids
                listOfCollection.add(collect);
                collect = new Collect(
                        current,
                        term_freq.get(i).docId,
                        term_freq.get(i).position,
                        term_freq.get(i).balise,
                        b);
            }
        }
        return listOfCollection;
   }

   /* DETERMINATE IF A TERM WAS IN TITLE OR ABSTRACT */
   private String underScore(String term){
           return term.replaceAll("_$","");
   }

   /* GET THE WEIGHT OF AN ABSTRACT TERM BY DOCID */
    private Double getPoidsAbatr(String match,Integer docid){
        if(poidsAbstr == null){
            poidsAbstr = new HashMap<>();
            for (TermOfDoc termOfDoc :term_freq){
                if (poidsAbstr.containsKey(termOfDoc.docId)){
                    Pair<String,TermOfDoc> pair = new Pair<>();
                    pair.put(termOfDoc.term, termOfDoc);
                    poidsAbstr.get(termOfDoc.docId).add(pair);
                }
                else {
                    Pair<String,TermOfDoc> pair = new Pair<>();
                    pair.put(termOfDoc.term, termOfDoc);
                    List<Pair<String,TermOfDoc>> list = new ArrayList();
                    list.add(pair);
                    poidsAbstr.put(termOfDoc.docId,list);
                }
            }
        }
        for(Pair<String,TermOfDoc> pair :poidsAbstr.get(docid)){
            if(pair.getKey().equals(match)){
                return poids_Abstract(pair.getValue());
            }
        }
        return 0.0;
    }

    /* GET THE WEIGHT OF AN TITLE TERM BY DOCID */
    private Double getPoidsTitle(String match,Integer docid){
        if(poidsTitre == null){
            poidsTitre = new HashMap<>();
            for (TermOfDoc termOfDoc :term_freq){
                if (poidsTitre.containsKey(termOfDoc.docId)){
                    Pair<String,Double> pair = new Pair<>();
                    pair.put(termOfDoc.term, Double.valueOf(termOfDoc.frequency));
                    poidsTitre.get(termOfDoc.docId).add(pair);
                }
                else {
                    Pair<String,Double> pair = new Pair<>();
                    pair.put(termOfDoc.term, Double.valueOf(termOfDoc.frequency));
                    List<Pair<String,Double>> list = new ArrayList();
                    list.add(pair);
                    poidsTitre.put(termOfDoc.docId,list);
                }
            }
        }
        for(Pair<String,Double> pair :poidsTitre.get(docid)){
            if(pair.getKey().equals(match)){
                return pair.getValue();
            }
        }
        return 0.0;
    }
}