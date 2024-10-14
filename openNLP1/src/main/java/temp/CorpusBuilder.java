package temp;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CorpusBuilder {

    private String text;
    private String[] sentences;
    private List<List<String>> tokens = new ArrayList<>();
    private List<List<String>> posTags = new ArrayList<>();
    private List<List<String>> lemmas = new ArrayList<>();


    /**
     * Create a CorpusBuilder which generates POS tags and Lemmas for text.
     *
     * @param text The text which should be annotated.
     */
    CorpusBuilder(String text) throws IOException {
        this.text = text;

        try (InputStream modelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin")) {
            if (modelIn == null) {
                throw new FileNotFoundException("Resource not found: opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
            }
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            this.sentences = sentenceDetector.sentDetect(this.text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream modelTk = Thread.currentThread().getContextClassLoader().getResourceAsStream("opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin")) {
            if (modelTk == null) {
                throw new FileNotFoundException("Resource not found: opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
            }
            TokenizerModel tkModel = new TokenizerModel(modelTk);
            Tokenizer tokenizer = new TokenizerME(tkModel);
            for (String s : this.sentences) {
                List<String> temp = Arrays.asList(tokenizer.tokenize(s));
                tokens.add(temp);
            }
        }

        try (InputStream modelPOS = Thread.currentThread().getContextClassLoader().getResourceAsStream("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin")) {
            if (modelPOS == null) {
                throw new FileNotFoundException("Resource not found: opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
            }
            POSModel POSModel = new POSModel(modelPOS);
            POSTaggerME POStagger = new POSTaggerME(POSModel);
            for (List<String> sentence : tokens) {
                String[] temp = new String[sentence.size()];
                temp = sentence.toArray(temp);
                List<String> aPOSList = Arrays.asList(POStagger.tag(temp));
                posTags.add(aPOSList);
            }
        }

        try (InputStream modelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("en-lemmatizer.bin")) {
            if (modelIn == null) {
                throw new FileNotFoundException("Resource not found: en-lemmatizer.bin");
            }
            LemmatizerModel model = new LemmatizerModel(modelIn);
            LemmatizerME lemmatizer = new LemmatizerME(model);
            for (int i = 0; i < tokens.size(); i++) {
                List<String> tempSent = tokens.get(i);
                List<String> tempPOSTag = posTags.get(i);
                String[] lemmasArray = lemmatizer.lemmatize(tempSent.toArray(new String[0]),
                        tempPOSTag.toArray(new String[0]));
                lemmas.add(Arrays.asList(lemmasArray));
            }
        }
    }

    /**
     * Returns the text of this CorpusBuilder
     *
     * @return The text of this CorpusBuilder
     */
    public String getText() {
        return text;
    }

    /**
     * Return an array with the sentences of the CorpusBuilder
     *
     * @return An array with the sentences of the CorpusBuildr
     */
    public String[] getSentences() {
        return sentences;
    }

    /**
     * Return a List of List with the tokens/words of the text of CorpusBuilder. The first list holds the words of the
     * first sentence, the second list holds the words of the second sentence and so on.
     *
     * @return A List of List the tokens/words of the text of the CorpusBuilder.
     */
    public List<List<String>> getTokens() {
        return tokens;
    }

    /**
     * Return a List of List with the POS tags of the text of CorpusBuilder. The first list holds the POS tags of the
     * first sentence, the second list holds the POS tags of the second sentence and so on.
     *
     * @return A List of List with the POS tags of the text of CorpusBuilder.
     */
    public List<List<String>> getPosTags() {
        return posTags;
    }

    /**
     * Return a List of List with the Lemmas of the text of CorpusBuilder. The first list holds the lemmas of the
     * first sentence, the second list holds the Lemmas of the second sentence and so on.
     *
     * @return A List of List with the Lemmas of the text of CorpusBuilder.
     * @return
     */
    public List<List<String>> getLemmas() {
        return lemmas;
    }

    public List<List<List<String>>> retrieveAllResults(String searchWord, int windowSize, String wantedPOS, boolean byLemma) {
        List<List<List<String>>> resultsToBeUsed = new ArrayList<>();
        // determine whether to look for lemma list (search by lemma) or tokens list (search by word)
        List<List<String>> searchReferences = byLemma? this.getLemmas(): this.getTokens();
        System.out.println(searchReferences);
        for (List<String> aReference : searchReferences) {
            System.out.println(aReference);
            // get the position of the current reference sent in the corpus
            int sentInd = searchReferences.indexOf(aReference);
            System.out.println(sentInd);
            for (int wordAt = 0; wordAt < aReference.size(); wordAt++) {
                String sentToken = aReference.get(wordAt);
                if (sentToken.equalsIgnoreCase(searchWord)) {
                    // create a list of list to store the result lemma/POS/sentence fragments
//                    List<List<String>> tokenPOSLemmaListsContainer = new ArrayList<>();
                    System.out.println(wordAt);
                    // get the POS list for the sentence
                    List<String> posList = this.getPosTags().get(sentInd);
                    // get the LemmaList for the sentence
                    List<String> lemmaList = this.getLemmas().get(sentInd);
                    // get the tokenList for the sentence
                    List<String> tokenList = this.getTokens().get(sentInd);
                    // get the position of the searchWord in the sentence
                    String wordPOS = posList.get(wordAt);
                    // check if the word POS matches the required one or just null POS
                    if (wantedPOS == null || wordPOS.equals(wantedPOS)) {
                        List<List<String>> tokenPOSLemmaListsContainer = new ArrayList<>();
                        int startAt = Math.max(wordAt - windowSize, 0);
                        int endAt = Math.min(wordAt + windowSize + 1, aReference.size());

                        List<String> fragment = tokenList.subList(startAt, endAt);
                        List<String> posFragment = posList.subList(startAt, endAt);
                        List<String> lemmaFragment = lemmaList.subList(startAt, endAt);

                        System.out.println("token");
                        System.out.println(fragment);
                        System.out.println(posFragment);
                        System.out.println(lemmaFragment);
                        System.out.println("-----");

                        tokenPOSLemmaListsContainer.add(fragment);
                        tokenPOSLemmaListsContainer.add(posFragment);
                        tokenPOSLemmaListsContainer.add(lemmaFragment);
                        resultsToBeUsed.add(tokenPOSLemmaListsContainer);
                    }
                }
            }
        }
        return resultsToBeUsed;
    }

    public List<List<Integer>> getIndicesResults (String searchWord, String wantedPOS, boolean byLemma){
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> sentInds = new ArrayList<>();
        List<Integer> wordInds = new ArrayList<>();
        List<List<String>> searchReferences = byLemma? this.getLemmas(): this.getTokens();
        for (List<String> aRef: searchReferences){
            int sentId = searchReferences.indexOf(aRef);
            for (int i = 0; i < aRef.size(); i++){
                String token = aRef.get(i);
                if (token.equalsIgnoreCase(searchWord)){
                    String pos = this.getPosTags().get(sentId).get(i);
                    if (wantedPOS == null || pos.equalsIgnoreCase(wantedPOS)){
                        sentInds.add(sentId);
                        wordInds.add(i);
                    }
                }
            }
        }
        result.add(sentInds);
        result.add(wordInds);
        return result;
    }



    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        int countResults = 0;
        int window = 3;
        // initialise the String text for the Corpus object
        String textInput = "";
        Scanner keyboard = new Scanner(System.in);
        System.out.print("input the text for search (either wiki URL or .txt file): ");
        // https://en.wikipedia.org/wiki/Franz_Delitzsch
        // ./src/main/java/temp/sample.txt
        String input = keyboard.nextLine();
        if (input.endsWith(".txt")){
            try(BufferedReader reader = new BufferedReader(new FileReader(new File(input)))){
                String current = reader.readLine();
                while(current != null){
                    textInput += current;
                    current = reader.readLine();
                }
                System.out.println(textInput);
            }catch(FileNotFoundException e){
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            String url = input;
            try {
                Document doc = Jsoup.connect(url).get();
                textInput = doc.text();
            } catch (Exception e){
                System.out.println("URL/ file reading error!");
                System.exit(0);
            }
            System.out.println(textInput);
        }
        CorpusBuilder sample = new CorpusBuilder(textInput);
        boolean noResult = true;
        boolean searchByPOS = false;
        String wantedPOS = null;
        System.out.print("enter your search word: ");
        String searchWord = keyboard.next();
        System.out.print("search by POS? (Y/N):  ");
        if (keyboard.next().equalsIgnoreCase("y")){
            searchByPOS = true;
            System.out.print("enter your desired POS (NOUN/ VERB/ DET): ");
            wantedPOS = keyboard.next();
            System.out.println();
        } else{
            System.out.println("non-Y >>> not search by POS");
        }
        String[] POSOptions = {"NOUN", "VERB"};
        String testing = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is " +
                "chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and former " +
                "chairman of Consolidated Gold Fields PLC, was named a director of this " +
                "British industrial conglomerate.";

        List<List<Integer>> result = sample.getIndicesResults(searchWord, null, false);
        List<Integer> sentIndList = result.get(0);
        List<Integer> wordIndList = result.get(1);
        for (int i = 0; i < sentIndList.size(); i++){
            int sentId = sentIndList.get(i);
            int wordId = wordIndList.get(i);
            System.out.println(sentId + ": " + wordId);
            List<String> sentTokens = sample.getTokens().get(sentId);
            List<String> sentPOS = sample.getPosTags().get(sentId);
            List<String> sentLemma = sample.getLemmas().get(sentId);
            String targetToken = sentTokens.get(wordId);
            String targetPOS = sentPOS.get(wordId);
            String targetLemma = sentLemma.get(wordId);
            System.out.println(targetToken + "; " + targetPOS + "; " + targetLemma);
        }
//        CorpusBuilder test = new CorpusBuilder(testing);
//        for (String current : test.getSentences()) {
//            System.out.println(current);
//        }
//        for (List<String> aSent : test.getTokens()) {
//            System.out.println(aSent);
//        }
//        for (List<String> POStags : test.getPosTags()) {
//            System.out.println(POStags);
//        }
//        for (List<String> lemma : test.getLemmas()) {
//            System.out.println(lemma);
//        }
//        for (List<String> aSent : sample.getTokens()) {
////            System.out.println(aSent);
//            if (aSent.contains(searchWord)){
//                // get the position of the sentence in the corpus
//                int sentInd = sample.getTokens().indexOf(aSent);
//                // get the POS list for the sentence
//                List<String> posList = sample.getPosTags().get(sentInd);
//                // get the LemmaList for the sentence
//                List<String> lemmaList = sample.getLemmas().get(sentInd);
//                // get the position of the searchWord in the sentence
//                int wordAt = aSent.indexOf(searchWord);
//                if (searchByPOS){
//                    if (posList.get(wordAt).equalsIgnoreCase(wantedPOS)){
//                        countResults += 1;
//                        System.out.println(aSent);
//                        System.out.println(lemmaList.get(wordAt));
//                        System.out.println(posList.get(wordAt));
//                        int startAt = (wordAt - window < 0) ? 0 : wordAt - window;
//                        int endAt = (wordAt + window + 1 >= aSent.size()) ? aSent.size() - 1 : wordAt + window + 1;
//                        List<String> fragment = aSent.subList(startAt, endAt);
//                        System.out.println(fragment);
//                        System.out.println("---");
//                        noResult = false;
//                    }
//                }else {
//                    countResults += 1;
//                    System.out.println(aSent);
//                    System.out.println(lemmaList.get(wordAt));
//                    System.out.println(posList.get(wordAt));
//                    int startAt = (wordAt - window < 0) ? 0 : wordAt - window;
//                    int endAt = (wordAt + window + 1 >= aSent.size()) ? aSent.size() - 1 : wordAt + window + 1;
//                    List<String> fragment = aSent.subList(startAt, endAt);
//                    System.out.println(fragment);
//                    System.out.println("---");
//                    noResult = false;
//                }
//            }
//        }
//        System.out.println(countResults + " results found.");

    }
}