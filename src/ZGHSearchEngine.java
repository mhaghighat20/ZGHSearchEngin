import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//contains a word and indexes of documents which has the word as key and number of rematches in values
class DetailOfWord {
    private String word;
    private HashMap<Integer, Integer> numOfWordInDocs; //A hash map to link indexes and numOfWords in doc
    //todo for multiple occurrences in one doc can add ArrayList of Integer to save indexes
    private HashMap<Integer, Integer> indexInDoc; // key: index of doc ; value: index of word in doc

    public DetailOfWord(String word) {
        this.word = word;
        this.numOfWordInDocs = new HashMap<>();
        this.indexInDoc = new HashMap<>();
    }

    public void addWordToDocIndex(int indexOfDoc, int number) {
        this.numOfWordInDocs.merge(indexOfDoc, number, Integer::sum);
    }

    public void addIndexOfWordInDoc(int indexOfDoc, int indexOfWord) {
        this.indexInDoc.put(indexOfDoc, indexOfWord);
    }

    public HashMap<Integer, Integer> getNumOfWordInDocs() {
        return numOfWordInDocs;
    }
}

//each founded document as a Result; index: index of document; score : score of document
class Result {
    private int index;
    private int score;

    public Result(int index, int score) {
        this.index = index;
        this.score = score;
    }

    public void changeScore(int change) {
        this.score += change;
    }

    public int getIndex() {
        return index;
    }

    public int getScore() {
        return score;
    }
}

class CSVFileReader {
    private static CSVFileReader instance;
    private ArrayList<String> documents = new ArrayList<>();

    public ArrayList<String> getDocuments() {
        return documents;
    }


    public static CSVFileReader getInstance() {
        if (instance == null) {
            instance = new CSVFileReader();
        }
        return instance;
    }

    public void readCSVFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String doc;
            int indexOfDoc = 1;
            while ((doc = br.readLine()) != null) {
                //Each line is a document
                doc = doc.split("\",\"")[1];
                doc = doc.substring(0, doc.length() - 1);
                documents.add(doc);
                indexOfDoc++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PreProcessor {

    private static PreProcessor instance;

    private HashMap<String, DetailOfWord> invertedIndex;

    private PreProcessor() {
        invertedIndex = new HashMap<>();
    }

    public static PreProcessor getInstance() {
        if (instance == null) {
            instance = new PreProcessor();
        }
        return instance;
    }

    public HashMap<String, DetailOfWord> getInvertedIndex() {
        return invertedIndex;
    }

    public void preProcessDocs(ArrayList<String> docs) {
        int indexOfDoc = 0;
        for (String doc : docs) {
            preProcessDoc(doc, indexOfDoc);
            indexOfDoc++;
        }
    }

    private void preProcessDoc(String doc, int indexOfDoc) {
        String[] words = doc.split("[\\s.,()/\"#;'\\\\\\-:$&]+");
        int indexOfWord = 0;
        for (String word : words) {
            if (invertedIndex.get(word) == null) {
                DetailOfWord indexes = new DetailOfWord(word);
                indexes.addWordToDocIndex(indexOfDoc, 1);
                invertedIndex.put(word, indexes);
                indexes.addIndexOfWordInDoc(indexOfDoc, indexOfWord);
            } else {
                invertedIndex.get(word).addWordToDocIndex(indexOfDoc, 1);
            }
            indexOfWord++;
        }
    }
}

class Processor {
    // each doc which has all the words in query has a result
    private HashMap<Integer, Result> results; //HashMap to link doc indexes with results
    private static Processor instance;

    private Processor() {
        results = new HashMap<>();
    }

    public static Processor getInstance() {
        if (instance == null) {
            instance = new Processor();
        }
        return instance;
    }

    public void processQuery(String query) {
        System.out.println("ZGH Search Engine\nSearch Results:");
        String[] wordsToFind = query.split("[\\s.,()/\"#;'\\\\\\-:$]+");

        findAllMatches(wordsToFind);

//        for (int i = 0; i < stringsToFind.length; i++) {
//            Result result = new Result();
//        }
//        if (invertedIndex.get(stringsToFind[0]) != null) {
//            InvertedIndexWord invertedIndexWord = invertedIndex.get(stringsToFind[0]);
//            for (Map.Entry<Integer, Integer> entry : invertedIndexWord.getNumOfWordInDocs().entrySet()) {
//                if (results.get(entry.getKey()) == null) {
//                    Result result = new Result(entry.getKey(), entry.getValue());
//                    results.put(0, result);
//                } else {
//                    Result result = results.get(entry.getKey());
//                    result.changeScore(entry.getValue());
//                }
//            }
//        }
        for (int i = 0; i < wordsToFind.length; i++) {
            HashMap<Integer, Result> results_of_now_word = new HashMap<>(); //HashMap to link doc indexes with results
            if (PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]) != null) {
                DetailOfWord invertedIndexWord = PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]);
                for (Map.Entry<Integer, Integer> entry : invertedIndexWord.getNumOfWordInDocs().entrySet()) {
                    if (results.get(entry.getKey()) == null) {
                        Result result = new Result(entry.getKey(), entry.getValue());
                        results.put(0, result);
                    } else {
                        Result result = results.get(entry.getKey());
                        result.changeScore(entry.getValue());
                    }
                }
            }
//            if (stringsToFind[i].equals("OR")) {
//                i++;
//                try {
//                    indexes.addAll(invertedIndex.get(stringsToFind[i]));
//                } catch (Exception ignored) {
//                }
////            } else {
//            if (invertedIndex.get(stringsToFind[i]) != null)
//                indexes.retainAll(invertedIndex.get(stringsToFind[i]));
//            }
        }
//        printResults(documents, indexes);
    }

    private void findAllMatches(String[] wordsToFind) {
        ArrayList<Integer> foundDocIndexes = null;
        for (int i = 0; i < wordsToFind.length; i++) {
            if (foundDocIndexes == null && PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]) != null)
                foundDocIndexes = new ArrayList<>(PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]).getNumOfWordInDocs().keySet());
            else if (foundDocIndexes != null && PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]) != null)
                foundDocIndexes.retainAll(PreProcessor.getInstance().getInvertedIndex().get(wordsToFind[i]).getNumOfWordInDocs().keySet());
        }
        System.out.println(foundDocIndexes);
        for (int i = 0; i < foundDocIndexes.size(); i++) {
            Result result = new Result(foundDocIndexes.get(i), 0);
            results.put(foundDocIndexes.get(i), result);
        }
    }
}

public class ZGHSearchEngine {

    public static final String FILE_NAME = "English.csv";

    public static void main(String[] args) {
        CSVFileReader.getInstance().readCSVFile(FILE_NAME);
        PreProcessor.getInstance().preProcessDocs(CSVFileReader.getInstance().getDocuments());
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String query = scanner.nextLine();
            Processor.getInstance().processQuery(query);
        }
    }


    private static void printResults(ArrayList<String> documents, ArrayList<Integer> indexes) {
        ArrayList<String> foundDocuments = new ArrayList<>();
        if (indexes.size() == 0) {
            System.out.println("nothing not found");
            return;
        }
        for (int index : indexes) {
            System.out.println(index + " : " + documents.get(index - 1));
            foundDocuments.add(documents.get(index - 1));
        }
        System.out.println(indexes);
    }


}