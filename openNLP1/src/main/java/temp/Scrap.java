package temp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scrap {

    public static void main(String[] args) throws IOException {
        String url = "https://de.wikipedia.org/wiki/Pythagoras";
        String cleanedText = extractAndCleanText(url);
        System.out.println(cleanedText);

    }

    /**
     * Extracts text from a given URL and cleans it by removing footnotes and metadata.
     *
     * @param url the URL of the website to extract text from
     * @return a cleaned text string without footnotes and metadata
     * @throws IOException if an I/O error occurs
     */
    public static String extractAndCleanText(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        // Select the main content area of the Wikipedia page
        Element content = doc.select("#mw-content-text").first();

        if (content == null) {
            return "";
        }

        // Remove unwanted elements
        content.select("sup, .reference, .mw-editsection, table, .thumb, .infobox, .toc, .navbox, .metadata").remove();

        // Remove remaining elements that might be unnecessary
        for (Element element : content.select("*")) {
            if (element.tagName().matches("h[1-6]|img|table")) {
                element.remove();
            }
        }

        // Extract the cleaned text
        String text = content.text();

        // Remove footnotes and metadata patterns
        text = text.replaceAll("\\[\\d+\\]", ""); // Remove footnotes like [1], [2], etc.
        text = text.replaceAll("\\(.*?\\)", "");  // Remove metadata in parentheses

        return text;
    }


}
