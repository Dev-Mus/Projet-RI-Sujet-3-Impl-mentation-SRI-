package project.model;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Projections.excludeId;




public class Database
{
    /*-------------------------------------VARIABLES------------------------*/
    private String user = "moumen_admin"; // the user name
    private String database = "ri"; // the name of the database in which the user is defined
    private static String inverse = "inverse";
    private static String inverseTest = "invertedIndex";
    private static String collection = "poidsDoc";
    private static String stopList = "stopList";
    private static String file = "corpus";
    public static String DOCS = "ptr";
    char[] password = {'r','o','o','o','t'}; // the password as a character array
    static private Database instance = new Database();
    public  static  String TERM = "term";
    static  String NDOC = "ndoc";
    public static  String DOCID = "docid";
    public static  String POID = "poid";
    public static  String POSITION = "position";
    static  String BALISE = "balise";
    static  String CLE = "cle";
    static  String TITRE = "titre";
    static  String ABSTRACT = "abstract";
    private MongoCollection<BasicDBObject> stopList_coll;
    private MongoCollection<Document> poidsDoc;
    private MongoCollection<Document> inverse_coll;
    private MongoCollection<Document> file_coll;
    private MongoDatabase db;
    List<BasicDBObject> foundDocument = null;


    /* this is the only way to create an object of Database class */
    static public Database getInstance(){return instance;}

    private Database()
    {
        MongoCredential credential = MongoCredential.createCredential(user, database, password);
        MongoClient mongoClient = new MongoClient(new ServerAddress("localhost",27017), Arrays.asList(credential));
        try{
            db = mongoClient.getDatabase(database);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        poidsDoc = db.getCollection(collection,  Document.class);
        stopList_coll = db.getCollection(stopList,  BasicDBObject.class);
        inverse_coll = db.getCollection(inverseTest, Document.class);
        file_coll = db.getCollection(file, Document.class);
    }

    /* add stopList To DATABASE */
    public void addStopList(ArrayList<String> list)
    {
        List<BasicDBObject> foundDocument = stopList_coll.find().into(new ArrayList<>());
        if(foundDocument.isEmpty()){
            for (String term: list) {
                stopList_coll.insertOne(new BasicDBObject(TERM, term));
            }
        }
        else {
            System.out.println("stopList is already exists");
        }
    }

    /* ADD CORPUS FILE TO DATABASE */
    public void addCorpusDoc(Map<Integer,Map<String,String>> list){
        List<Document> foundDocument = file_coll.find().limit(1).into(new ArrayList<>());
        if(foundDocument.isEmpty()){
            foundDocument = new ArrayList<>();
            for (Map.Entry<Integer,Map<String,String>> map:list.entrySet()) {
                   for (Map.Entry<String,String> map2 : map.getValue().entrySet()){
                       foundDocument.add(new Document(CLE,map.getKey()).append(TITRE,map2.getKey()).append(ABSTRACT,map2.getValue()));
                   }
            }
            file_coll.insertMany(foundDocument);
        }
        else {
            System.out.println("Corpus is already exists");
        }
    }

    /* GET CORPUS DOCS FROM DATABASE */
    public List<Document> getCorpus(Integer docId){
        return  file_coll.find(eq(CLE,docId)).into(new ArrayList<>());
    }


    public void invertedIndex(List<Collect> collects){
        if(!invertedIsEmpty()){ System.out.println("is not empty"); return;}
        System.out.println("Start inserting in DATABASE");
        List<Document> termRrow = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        Map.Entry<Boolean,Boolean> balises;
        for (Collect collect:collects) {
            int i=0;
            for (Map<Integer, List<Integer>> entry1:collect.docs_positions) {
                for (Map.Entry<Integer, List<Integer>> entry:entry1.entrySet() ){
                    balises = collect.balise.get(i).entrySet().iterator().next();
                    termRrow.add(new Document()
                            .append(DOCID, entry.getKey())
                            .append(POID, collect.poids.get(i))
                            .append(POSITION, entry.getValue())
                            .append(BALISE, Arrays.asList(balises.getKey(), balises.getValue())));
                }
                i++;
            }
                docs.add(new Document(TERM, collect.term).append(NDOC, collect.nbDoc).append(DOCS,termRrow));
                termRrow= new ArrayList<>();

        }
        inverse_coll.insertMany(docs);
        System.out.println("Inserting Ended");
    }


    public boolean isStopWord(String term){
        final boolean[] bool = {false};
        if(foundDocument == null){
            foundDocument  = stopList_coll.find().into(new ArrayList<>());
        }
        Consumer<BasicDBObject> consumer = basicDBObject -> {
            if(basicDBObject.get(TERM).equals(term)){
                bool[0] = true;
            }
        };
        foundDocument.forEach(consumer);
        return  bool[0];
    }

    public Boolean invertedIsEmpty(){
        List<Document> foundDocument = inverse_coll.find().limit(1).into(new ArrayList<>());
        if(!foundDocument.isEmpty()){
            System.out.println("inverse file is already exists");
            return false;
        }else {
            return true;
        }
    }
    public Boolean corpusIsEmpty(){
        List<Document> foundDocument = file_coll.find().limit(1).into(new ArrayList<>());
        if(foundDocument.isEmpty()){
            System.out.println("corpus file is already exists");
            return true;
        }else {
            return false;
        }
    }

    public HashSet<Document> getDocuments(ArrayList<String> terms){
              return inverse_coll.find(in(TERM,terms)).into(new HashSet<>());
    }

    public List<Document> getTermsByDocId(Integer docID)
    {
        Bson projection = Projections.fields( excludeId(), elemMatch(DOCS, eq(DOCID, docID))); // Add Projections
        FindIterable<Document> findIterable =  inverse_coll.find().projection(projection);
        return  findIterable.into(new ArrayList<>()).stream().filter(str1 -> str1.get(DOCS) != null).collect(Collectors.toList());
    }

    public int count(){
        return inverse_coll.find().into(new ArrayList<>()).size();
    }

    public void addPoids(String toString, Double a) {
        if(poidsDoc.find().limit(1).into(new ArrayList<>()).isEmpty())
            poidsDoc.insertOne(new Document(DOCID,toString).append(POID,a));
    }
    public List<Document> getPoids(String docId){
       return poidsDoc.find(eq(DOCID,docId)).into(new ArrayList<>());
    }
}

/* create user in mongodb console
db.createUser({
                user:"moumen_admin",
                pwd:"rooot",
                roles:["readWrite","dbAdmin"]
              })
*/