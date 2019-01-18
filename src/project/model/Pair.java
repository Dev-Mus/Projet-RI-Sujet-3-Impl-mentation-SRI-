package project.model;

public class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public Pair(){};
    public L getKey(){ return l; }
    public R getValue(){ return r; }
    public void setKey(L l){ this.l = l; }
    public void setValue(R r){ this.r = r; }
    public void put(L l,R r){ this.l = l;
        this.r = r; }
}