package io.bittiger.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlerUtil {
    public static List<String> cleanData(String string) throws IOException {
        String[] myStopWordSet = new String[]{".", ",", "\"", "'", "?", "!", ":", ";", "(", ")", "[", "]", "{", "}", "&", "/", "...", "-", "+", "*", "|", "),"};
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_40);
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(string));
        List<String> res = new ArrayList<>();
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, standardAnalyzer.STOP_WORDS_SET);
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, StopFilter.makeStopSet(Version.LUCENE_40, myStopWordSet, true));
        CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            res.add(token.toString().toLowerCase());
        }

        return res;
    }

    public static String joinTokens(List<String> input) {
        return input.stream().collect(Collectors.joining(" "));
    }

    public static List<String> getSubQueriesWithShingleFilter(String query) throws IOException {
        List<String> queries = new ArrayList<>();
        StringReader reader = new StringReader(query);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
        TokenStream tokenstream = analyzer.tokenStream("content", reader);
        ShingleFilter shingleFilter = new ShingleFilter(tokenstream);
        shingleFilter.setOutputUnigrams(false);
        shingleFilter.setOutputUnigramsIfNoShingles(false);
        shingleFilter.setMinShingleSize(2);
        int tokenSize = getTokenSize(query) - 1;
        if (tokenSize < 2) {
            return queries;
        }
        shingleFilter.setMaxShingleSize(tokenSize);
        CharTermAttribute charTermAttribute = shingleFilter.addAttribute(CharTermAttribute.class);
        shingleFilter.reset();

        while (shingleFilter.incrementToken()) {
            queries.add(charTermAttribute.toString());
        }

        return queries;
    }

    public static List<String> getSubQueries(String query) throws IOException {
        List<String> queries = new ArrayList<>();
        String[] tokens = query.split(" ");

        for (int i = 0; i < tokens.length - 1; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(tokens[i]);
            for (int j = i + 1; j < tokens.length; j++) {
                if (i == 0 && j == tokens.length - 1) {
                    continue;
                }
                sb.append(" ");
                sb.append(tokens[j]);
                queries.add(sb.toString());
            }
        }

        return queries;
    }

    public static int getResultIndex(Element element) {
        String idString = element.attr("id");
        if (idString == null || idString.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(idString.substring(idString.indexOf("_") + 1));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getTokenSize(String input) {
        return input.split(" ").length;
    }
}
