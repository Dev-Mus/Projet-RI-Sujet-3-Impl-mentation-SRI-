package project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Indexing
{

    public ArrayList<String> eliminateEmptyTerm(ArrayList<String> list){
        List<String> toRemove = new ArrayList<>();
        for (String term:list) {
            if(term.matches("[-0-9.]+"))
                toRemove.add(term);

            if (Database.getInstance().isStopWord(term)) {
                toRemove.add(term);
            }
        }
        list.removeAll(toRemove);
        return list;
    }

    public ArrayList<String> normalisation(ArrayList<String> terms){
        ArrayList<String> str = new ArrayList<>();
        for (String term : terms){
            str.add(norma(term));
        }
        return str;
    }

    public ArrayList<String> token(String str){
        return  new ArrayList<>(
                Arrays.asList(str.toLowerCase()
                        .split("[,\\s_\".;:?!<<)\\[\\]=%(&#$*/+>>]"))
                        .stream()
                        .filter(str1 -> !str1.isEmpty()).collect(Collectors.toList())
        );
    }

    public static int voye_cons(String term){
        return Arrays.asList(term.toLowerCase().split("[aeiou]((?![aeiou]))")).size()-1;  //  voy suivi par con
    }
    public static  String norma(String term){
        term = term.replaceAll("^-+","");
        term = term.replaceAll("-+$","");
        term = term.replaceAll("^'","");
        term = term.replaceAll("'$","");
        term = term.replaceAll("sess$","es");
        term = term.replaceAll("ies$","i");
        term = term.replaceAll("s$","");
        term = term.replaceAll("y$","i");

        if(term.endsWith("ed") && voye_cons(term.replaceAll("ed$","")) > 0) {
            term = term.replaceAll("ed$", "");
        }
        if( term.endsWith("ing") && voye_cons(term.replaceAll("ing$","")) > 0){
            term = term.replaceAll("ing$","");
        }
        if( term.endsWith("ational") && voye_cons(term.replaceAll("ational$","")) > 0){
            term = term.replaceAll("ational$","ate");
        }
        if( term.endsWith("tional") && voye_cons(term.replaceAll("tional$","")) > 0){
            term = term.replaceAll("tional$","tion");
        }
        if( term.endsWith("izer") && voye_cons(term.replaceAll("izer$","")) > 0){
            term = term.replaceAll("izer","ize");
        }
        if( term.endsWith("alize") && voye_cons(term.replaceAll("alize$","")) > 0){
            term = term.replaceAll("alize$","al");
        }
        if( term.endsWith("ize") && voye_cons(term.replaceAll("ize$","")) > 1){
            term = term.replaceAll("ize$","");
        }
        return term;
    }

}
