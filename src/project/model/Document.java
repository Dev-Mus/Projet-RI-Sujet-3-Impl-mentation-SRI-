package project.model;

import java.util.*;
import java.util.stream.Collectors;

public class Document {

    Integer m_cle;
    List<String> m_title;
    List<String> m_abstract;
    List<TermOfDoc> term_freq; // terms list and specifications
    static public  int count = 0;
    int i =0;
    int doc =0; //

    public Document(Integer m_cle, List<String> m_title, List<String> m_abstract) {
        this.m_cle = m_cle;
        this.m_title = m_title;
        this.m_abstract = m_abstract;
        term_freq = new ArrayList<>();
        //Counting each term
        poidsTitre();
        poidsAbstract();
//        removing duplicates
//        Set<String> namesAlreadySeen = new HashSet<>();
//        term_freq.removeIf(p -> !namesAlreadySeen.add(p.term));
//        term_freq.sort(Comparator.comparing(o -> o.frequency));
        count++;
    }

    void poidsTitre(){ //counting the number of each term in document title
        for (String term : m_title){
            int j=0;
            for (String term1 : m_title){
                if(term.matches(term1)){
                    j++;
                }
            }
            i++;
            Map<Boolean,Boolean> balise = new HashMap<>();
            if(m_abstract.contains(term))
                balise.put(true,true);
            else balise.put(true,false);
            term_freq.add(new TermOfDoc(term+"_",j,m_cle,i,0,balise));
        }
    }

    void poidsAbstract(){ //counting the number of each term in document abstract
        for (String term : m_abstract){
            int j=0;
            for (String term1 : m_abstract){
                if(term.equals(term1)){
                    j++;
                }
            }
            i++;
            doc++;
            Map<Boolean,Boolean> balise = new HashMap<>();
            if(m_title.contains(term))
                balise.put(true,true);
            else balise.put(false,true);
//            System.out.println(term+" "+j+" "+m_abstract.size());
            term_freq.add(new TermOfDoc(term,j,m_cle,i,m_abstract.size(),balise));
        }
    }
}
