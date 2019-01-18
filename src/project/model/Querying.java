package project.model;


import org.bson.Document;

import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;


public class Querying extends Indexing implements DuplicateCounting{
   private ArrayList<String> terms;
    HashSet<Document> documentList;
    Map<String,Integer> duplicates =null;
    Double poidsQuery;  /*  sum of all term frequency in the query */
    Map<Integer,Double> score; /* list of couples (docID, WIGHT) */
    HashSet<String> hashSet;

    public Querying(String query) {
        if(query.isEmpty()){
        }
        else {
            terms =  normalisation(eliminateEmptyTerm(token(query)));
            poidsQuery  = getWeightQuery();
            documentList = Database.getInstance().getDocuments(terms);
            diceIndex();
        }
    }

    /* COUNTING DICE INDEX AND SORTING RESULTS OF SCORES*/
    public void diceIndex(){
        score = new HashMap<>();
        hashSet = new HashSet<>();
        hashSet.addAll(terms);
        /* <docID, freqTerm> */
        for (Document doc:documentList){
            for (Document document1:(ArrayList<Document>) doc.get(Database.DOCS)){
                Integer docId = (Integer) document1.get(Database.DOCID);
                final Double[] a = {0.0};
                final Double[] b = {0.0};
                Thread thread = new Thread( () -> a[0] = getWeightDocument(docId));
                Thread thread1 = new Thread( () -> b[0] = getWeightDocQuery(docId));
                thread.start();
                thread1.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    thread1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                score.put(docId ,b[0] /(poidsQuery+ a[0]));
            }
        }
        /* LAMBDA EXPRESSION SORTING */
        score = score
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
//        score = score
//                .entrySet()
//                .stream()
//                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//                .collect(
//                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
//                                LinkedHashMap::new));
//

    }

    /* GET DOCS THAT MATCH SCORE(DOC) > 0 FROM DATABASE */
    public List<DocFx> getDocFx(){
        List<DocFx> list = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : score.entrySet()) {
            System.out.println(entry.getKey()+" "+entry.getValue());
            if (entry.getValue() > 0.0){
                docs.addAll( Database.getInstance().getCorpus(entry.getKey()));
            }
        }
        if (docs.isEmpty()){  list.add(new DocFx(" No matching document found","")); return list;}
        for (Document document :docs){
            list.add(new DocFx(document.get(Database.TITRE).toString().replaceAll("^-",""),document.get(Database.ABSTRACT).toString().replaceAll("^\n-","")));
        }
        return list;
    }




    @Override
    public Integer countingDuplicates(String match) {
        if(duplicates == null) {
            duplicates = new HashMap<>();
            for (String term : terms) {
                if (duplicates.containsKey(term)) {
                    duplicates.put(term, duplicates.get(term) + 1);
                } else {
                    duplicates.put(term, 1);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
            if(match.equals(entry.getKey()))
                return entry.getValue();
        }
        return 0;
    }


    private Double getWeightQuery()
    {
        Double a =0.0;
        for (String term:terms){
            int as = countingDuplicates(term);
            System.out.println(term+" "+as);
            a += Math.pow(as,2);
        }
        return a;
    }

    private Double getWeightDocument(Integer docID)
    {
        Double a =0.0;
        for(Document document :Database.getInstance().getPoids(String.valueOf(docID))){
                a += Math.pow((Double) document.get(Database.POID),2);
        }
        return a;
    }

    private Double getWeightDocQuery(Integer docId){
        Double a =0.0;
            for (Document doc:documentList){
                    if(hashSet.contains( doc.get(Database.TERM))){
                        ArrayList<Document> ptr = (ArrayList) doc.get(Database.DOCS);
                        for (Document document:ptr){    // parcours tous les pointures du terme
                            if(document.get(Database.DOCID) == docId){ // t'verifiy ida docId ta3 parammetre rahom kif kif ma3 term li 9lato
                                a +=   Double.valueOf((Double) document.get(Database.POID)) * countingDuplicates((String) doc.get(Database.TERM));
                            }
                        }
                }
            }

        return a * 2;
    }
}
