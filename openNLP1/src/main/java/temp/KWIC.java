package temp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class KWIC extends JFrame {

    private CorpusBuilder theCorpus;

    private List<List<String>> XMLSession = new ArrayList<>();

    private JFrame frame;

    private JButton urlLoadButton, searchButton;
    private JTextField urlField, searchWordField;
    private JComboBox windowSizeComboBox, POSComboBox;
    private JRadioButton searchByWord, searchByPOS, searchByLemma;
    private JLabel POSOptions, messageLabel;

    JTextPane concordanceArea;
    JPanel noWrapPanel;

    JScrollPane concordanceScrollPane;

    KWIC() {
        // Create the main frame
        frame = new JFrame("Keyword in Context Program");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        // Create the main panel with BorderLayout
        BoxLayout aBoxLayout = new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS);
        frame.getContentPane().setLayout(aBoxLayout);


        // create north panel for input fields and search options
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // URL or txt file input field
        JPanel url = new JPanel(new FlowLayout(FlowLayout.LEFT));
        url.setBackground(new Color(201, 112, 100));
        JLabel urlLabel = new JLabel("URL or txt file:", JLabel.LEFT);
        urlField = new JTextField(40);

        DocumentListener aDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updated();
            }

            private void updated() {
                searchButton.setEnabled(!searchWordField.getText().trim().isEmpty());
                urlLoadButton.setEnabled(!urlField.getText().trim().isEmpty());
            }
        };
        urlField.getDocument().addDocumentListener(aDocListener);
        urlLoadButton = new JButton("load");
        urlLoadButton.setEnabled(false);
        urlLoadButton.addActionListener(new loadTextButtonListener());
        url.add(urlLabel);
        url.add(urlField);
        url.add(urlLoadButton);
        northPanel.add(url);

        // Search word input field, window size select
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.setBackground(new Color(232, 187, 100));
        JLabel searchWordLabel = new JLabel("Search word:", JLabel.LEFT);
        searchWordField = new JTextField(10);
        searchWordField.getDocument().addDocumentListener(aDocListener);
        JLabel windowSizeLabel = new JLabel("Window size:");
        windowSizeComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        searchButton = new JButton("search");
        searchButton.setEnabled(false);
        searchButton.addActionListener(new SearchButtonListener());
        search.add(searchWordLabel);
        search.add(searchWordField);
        search.add(windowSizeLabel);
        search.add(windowSizeComboBox);
        search.add(searchButton);
        northPanel.add(search);

        // Search options
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        options.setBackground(new Color(166, 176, 126));
        JLabel searchOptionLabel = new JLabel("Search by:");
        searchByWord = new JRadioButton("by word (default)");
        searchByWord.addActionListener(new SearchByActionListener());
        searchByPOS = new JRadioButton("by POS");
        searchByPOS.addActionListener(new SearchByActionListener());
        searchByLemma = new JRadioButton("by lemma");
        searchByLemma.addActionListener(new SearchByActionListener());
        searchByWord.setSelected(true);
        ButtonGroup searchOptionsGroup = new ButtonGroup();
        searchOptionsGroup.add(searchByWord);
        searchOptionsGroup.add(searchByLemma);
        searchOptionsGroup.add(searchByPOS);

        // POSlist dropdown list
        POSOptions = new JLabel("POS options:", JLabel.LEFT);
        POSOptions.setForeground(Color.GRAY);
        POSComboBox = new JComboBox<>(new String[]{"NOUN", "VERB", "DET", "PRON", "PROPN", "AUX", "ADJ", "CCONJ", "PUNCT", "ADP", "PART", "NUM"});
        POSComboBox.setEnabled(false);
        options.add(searchOptionLabel);
        options.add(searchByWord);
        options.add(searchByPOS);
        options.add(searchByLemma);
        options.add(POSOptions);
        options.add(POSComboBox);
        northPanel.add(options);

        frame.getContentPane().add(northPanel);

        // create center panel for result display area
        JPanel centerPanel = new JPanel();
        BoxLayout aBoxLayout2 = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(aBoxLayout2);

        //Message label for search result bar "XXX search result: "
        messageLabel = new JLabel();
        centerPanel.add(messageLabel);

        // Concordance area
        concordanceArea = new JTextPane();
        noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(concordanceArea);
        concordanceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        concordanceArea.setEditable(false);
        concordanceArea.setFocusable(false);
        concordanceArea.setBorder(BorderFactory.createTitledBorder("Context/ Sentence Results"));
        concordanceScrollPane = new JScrollPane(noWrapPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        concordanceScrollPane.setPreferredSize(new Dimension(1000, 400));
        centerPanel.add(concordanceScrollPane);


        frame.getContentPane().add(centerPanel);

        // create south panel for save button
        JPanel southPanel = new JPanel();
        JButton saveButton = new JButton("Save to XML File");
        saveButton.addActionListener(new SaveButtonListener());
        southPanel.add(saveButton);

        frame.getContentPane().add(southPanel);
        frame.setVisible(true);

    }

    private class loadTextButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String urlName = urlField.getText();
            StringBuilder textInput = new StringBuilder();
            try (BufferedReader reader = getReader(urlName)) {
                String current = reader.readLine();
                while (current != null) {
                    textInput.append(current).append("\n");
                    current = reader.readLine();
                }
                theCorpus = new CorpusBuilder(textInput.toString());
                concordanceArea.setText("Text loaded. Please enter a search word.");
            } catch (IOException err) {
                if (isValidURL(urlName)) {
                    JOptionPane.showMessageDialog(frame, "Unable to load content from URL. Please enter a valid URL.", "Text Loading Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter valid file path.", "Text Loading Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private BufferedReader getReader(String urlName) throws IOException {
            if (isValidURL(urlName)) {
                String scrapedText = Scrap.extractAndCleanText(urlName);
                return new BufferedReader(new StringReader(scrapedText));
            } else {
                return new BufferedReader(new FileReader(new File(urlName)));
            }
        }

        private boolean isValidURL(String urlName) {
            return urlName.startsWith("http://") || urlName.startsWith("https://");
        }
    }


    private class SearchByActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (searchByPOS.isSelected()) {
                POSComboBox.setEnabled(true);
                POSOptions.setForeground(Color.BLACK);
            } else if (searchByWord.isSelected() || searchByLemma.isSelected()) {
                POSComboBox.setEnabled(false);
                POSOptions.setForeground(Color.GRAY);
            }
        }
    }

    /**
     * !!!! NEW search method - use index as reference.
     */

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // initialize the text pane to empty
            concordanceArea.setText("");
            // Get the document and create styles
            StyledDocument doc = concordanceArea.getStyledDocument();
            Style defaultStyle = doc.addStyle("default", null);
            StyleConstants.setFontFamily(defaultStyle, Font.MONOSPACED);
            Style highlightStyle = doc.addStyle("highlight", null);
            StyleConstants.setFontFamily(highlightStyle, Font.MONOSPACED);
            StyleConstants.setForeground(highlightStyle, Color.RED);
            StyleConstants.setBold(highlightStyle, true);
            // get the search word from searchWordField
            String searchWord = searchWordField.getText().toLowerCase();
            // get the window size
            Integer windowSize = (Integer) windowSizeComboBox.getSelectedItem();
            if (searchWord != null) {
                // if search by POS button selected, get the selected POS
                String wantedPOS = searchByPOS.isSelected() ? (String) POSComboBox.getSelectedItem() : null;
                // get the results from the method in CorpusBuilder
                List<List<Integer>> indicesResult = theCorpus.getIndicesResults(searchWord, wantedPOS, searchByLemma.isSelected());
                if (!indicesResult.get(0).isEmpty()) {
                    List<Integer> sentIndList = indicesResult.get(0);
                    List<Integer> wordIndList = indicesResult.get(1);
                    // retrieve each result for process
                    for (int i = 0; i < sentIndList.size(); i++) {
                        List<String> results = new ArrayList<>(); // store info for each search result

                        int sentId = sentIndList.get(i); // locate the sentence in the corpus
                        int wordId = wordIndList.get(i); // locate the word in the sentence
                        int startAt = Math.max(wordId - windowSize, 0); // locate where to start and end
                        int endAt = Math.min(wordId + windowSize + 1, theCorpus.getTokens().get(sentId).size());

                        // retrieve the corresponding truncated sentence for display
                        List<String> sentTokens = theCorpus.getTokens().get(sentId).subList(startAt, endAt);
                        List<String> sentPOS = theCorpus.getPosTags().get(sentId).subList(startAt, endAt);
                        List<String> sentLemma = theCorpus.getLemmas().get(sentId).subList(startAt, endAt);

                        // create lists of the tokens, POS, and lemmas of words in context before the keyword
                        List<String> beforeKWTokens = new ArrayList<>();
                        List<String> beforeKWPOS = new ArrayList<>();
                        List<String> beforeKWLemma = new ArrayList<>();
                        if (sentTokens.get(sentTokens.size() / 2).equals(searchWord)) {
                            for (int k = 0; k < (sentTokens.size() / 2); k++) {
                                beforeKWTokens.add(sentTokens.get(k));
                                beforeKWPOS.add(sentPOS.get(k));
                                beforeKWLemma.add(sentLemma.get(k));
                            }
                        } else if (sentTokens.size() == wordId) {
                            for (int k = 0; k < sentTokens.size(); k++) {
                                beforeKWTokens.add(sentTokens.get(k));
                                beforeKWPOS.add(sentPOS.get(k));
                                beforeKWLemma.add(sentLemma.get(k));
                            }
                        }


                        // create lists of the tokens, POS, and lemmas of words in context after the keyword
                        List<String> afterKWTokens = new ArrayList<>();
                        List<String> afterKWPOS = new ArrayList<>();
                        List<String> afterKWLemma = new ArrayList<>();
                        if (sentTokens.get(sentTokens.size() / 2).equals(searchWord)) {
                            for (int k = (sentTokens.size() / 2 + 1); k < sentTokens.size(); k++) {
                                afterKWTokens.add(sentTokens.get(k));
                                afterKWPOS.add(sentPOS.get(k));
                                afterKWLemma.add(sentLemma.get(k));
                            }
                        } else if (wordId == 0) {
                            for (int k = 1; k < sentTokens.size(); k++) {
                                afterKWTokens.add(sentTokens.get(k));
                                afterKWPOS.add(sentPOS.get(k));
                                afterKWLemma.add(sentLemma.get(k));
                            }
                        }

                        // Determine which tokens should be highlighted
                        boolean[] highlight = new boolean[sentTokens.size()];
                        for (int j = 0; j < sentTokens.size(); j++) {
                            if (startAt + j == wordId) {
                                highlight[j] = true;
                            }
                        }
                        // Append rows to the document
                        appendRow(doc, sentTokens, highlight, defaultStyle, highlightStyle);
                        appendRow(doc, sentPOS, highlight, defaultStyle, highlightStyle);
                        appendRow(doc, sentLemma, highlight, defaultStyle, highlightStyle);
                        // Add an extra newline after each set of results
                        appendText(doc, "\n", defaultStyle);


                        results.add(theCorpus.getTokens().get(sentId).get(wordId)); // add token to xml
                        results.add(theCorpus.getPosTags().get(sentId).get(wordId)); // add pos to xml
                        results.add(theCorpus.getLemmas().get(sentId).get(wordId)); // add lemma to xml
                        results.add(String.join(" ", beforeKWTokens)); // add context tokens before keyword
                        results.add(String.join(" ", beforeKWPOS)); // add context POS before keyword
                        results.add(String.join(" ", beforeKWLemma)); // add context lemma before keyword
                        results.add(String.join(" ", afterKWTokens)); // add context tokens after keyword
                        results.add(String.join(" ", afterKWPOS)); // add context POS after keyword
                        results.add(String.join(" ", afterKWLemma)); // add context lemma after keyword



                        // add each result to the session list
                        XMLSession.add(results);
                    }
                    // Set the scroll bar at the top
                    concordanceArea.setCaretPosition(0);
                }
                else{
                    JOptionPane.showMessageDialog(null, "No result", "No result", JOptionPane.PLAIN_MESSAGE);
                }

                // end testing
                messageLabel.setText(searchWord + ": " + indicesResult.get(0).size() + " result(s)");
            }
        }

        private void appendRow(StyledDocument doc, List<String> row, boolean[] highlight, Style defaultStyle, Style highlightStyle) {
            for (int i = 0; i < row.size(); i++) {
                Style style = highlight[i] ? highlightStyle : defaultStyle;
                appendText(doc, String.format("%-17s", row.get(i)), style);
            }
            appendText(doc, "\n", defaultStyle);
        }

        private void appendText(StyledDocument doc, String text, Style style) {
            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }


    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            String fileName = JOptionPane.showInputDialog(frame, "Enter file name", "Save to XML", JOptionPane.PLAIN_MESSAGE);
            if (fileName != null && !fileName.trim().isEmpty()) {

                File file = new File(fileName.trim() + ".xml");
                if (file.exists()) {
                    int choice = JOptionPane.showConfirmDialog(frame, "File already exists. Continue?", "File Exists", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        saveToXML(fileName.trim() + ".xml");
                    }
                } else {
                    saveToXML(fileName.trim() + ".xml");
                }
            }
        }
    }

    private void saveToXML(String fileName) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Sentences");
            doc.appendChild(rootElement);

            for (List<String> result : XMLSession) {
                Element sentenceElement = doc.createElement("Sentence");

                if (!Objects.equals(result.get(3), "")) {
                    Element preContextElement = doc.createElement("Pre_Keyword_Context");
                    sentenceElement.appendChild(preContextElement);

                    List<String> previousTokens = List.of(result.get(3).split(" "));
                    List<String> previousPOS = List.of(result.get(4).split(" "));
                    List<String> previousLemma = List.of(result.get(5).split(" "));
                    for (int i = 0; i < previousTokens.size(); i++) {
                        Element wordElement = doc.createElement("Word");
                        wordElement.appendChild(doc.createTextNode(previousTokens.get(i)));
                        preContextElement.appendChild(wordElement);

                        Element posElement = doc.createElement("POS");
                        posElement.appendChild(doc.createTextNode(previousPOS.get(i)));
                        wordElement.appendChild(posElement);

                        Element lemmaElement = doc.createElement("Lemma");
                        lemmaElement.appendChild(doc.createTextNode(previousLemma.get(i)));
                        wordElement.appendChild(lemmaElement);
                    }
                }


                Element keywordElement = doc.createElement("Keyword");
                keywordElement.appendChild(doc.createTextNode(result.get(0)));
                sentenceElement.appendChild(keywordElement);

                Element kwposElement = doc.createElement("POS");
                kwposElement.appendChild(doc.createTextNode(result.get(1)));
                keywordElement.appendChild(kwposElement);

                Element kwlemmaElement = doc.createElement("Lemma");
                kwlemmaElement.appendChild(doc.createTextNode(result.get(2)));
                keywordElement.appendChild(kwlemmaElement);

                if (!Objects.equals(result.get(6), "")) {
                    Element postContextElement = doc.createElement("Post_Keyword_Context");
                    sentenceElement.appendChild(postContextElement);

                    List<String> postTokens = List.of(result.get(6).split(" "));
                    List<String> postPOS = List.of(result.get(7).split(" "));
                    List<String> postLemma = List.of(result.get(8).split(" "));
                    for (int i = 0; i < postTokens.size(); i++) {
                        Element wordElement = doc.createElement("Word");
                        wordElement.appendChild(doc.createTextNode(postTokens.get(i)));
                        postContextElement.appendChild(wordElement);

                        Element posElement = doc.createElement("POS");
                        posElement.appendChild(doc.createTextNode(postPOS.get(i)));
                        wordElement.appendChild(posElement);

                        Element lemmaElement = doc.createElement("Lemma");
                        lemmaElement.appendChild(doc.createTextNode(postLemma.get(i)));
                        wordElement.appendChild(lemmaElement);
                    }
                }

                rootElement.appendChild(sentenceElement);

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

            JOptionPane.showMessageDialog(frame, "File saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (ParserConfigurationException | TransformerException e) {
            JOptionPane.showMessageDialog(frame, "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        KWIC demo = new KWIC();
    }
}

