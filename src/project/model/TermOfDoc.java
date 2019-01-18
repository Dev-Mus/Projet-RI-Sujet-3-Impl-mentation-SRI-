package project.model;

import java.util.Map;

public class TermOfDoc {
    Integer frequency;
    Integer docId;
    Integer position;
    private Integer docSize;
    String term;
    Map<Boolean,Boolean> balise; // <titre-abstract>

    public TermOfDoc(String term, Integer freq, Integer docId, Integer position, Integer docSize, Map<Boolean,Boolean> balise)
    {
        this.frequency = freq;
        this.docId = docId;
        this.term = term;
        this.position = position;
        this.docSize = docSize;
        this.balise = balise;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getDocSize() {
        return docSize;
    }


}
