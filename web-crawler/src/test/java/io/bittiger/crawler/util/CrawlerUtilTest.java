package io.bittiger.crawler.util;

import io.bittiger.util.CrawlerUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrawlerUtilTest {
    @Test
    public void testCleanData() throws IOException {
        List<String> res = CrawlerUtil.cleanData("Garden of Life Prenatal DHA Omega 3 Fish Oil Supplement - Minami Natural Prenatal, 30 Softgels");

        assert(res.size() == 14);
        for (String s : res) {
            System.out.println(s);
        }
    }

    @Test
    public void testJoinTokens() {
        List<String> tokens = new ArrayList<>();
        tokens.add("garden");
        tokens.add("life");
        tokens.add("prenatal");

        String res = CrawlerUtil.joinTokens(tokens);

        assert(res.equals("garden life prenatal"));
    }

    @Test
    public void testGetSubQueriesWithShingleFilter() throws IOException {
        List<String> res = CrawlerUtil.getSubQueriesWithShingleFilter("Garden Life Prenatal DHA");

        assert(res.size() == 5);

//        res = CrawlerUtil.getSubQueries("action & shooter video games");
        res.stream().forEach(System.out::println);
    }

    @Test
    public void testGetSubQueries() throws IOException {
        List<String> res = CrawlerUtil.getSubQueries("Garden Life Prenatal DHA");

        assert(res.size() == 5);

        res.stream().forEach(System.out::println);
    }

    @Test
    public void testGetResultIndex() throws IOException {
        String html = "<li id=\"result_0\" data-asin=\"B001U9I0M0\"></li><li id=\"result_a\" data-asin=\"B001U9I0M0\"></li>";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        Elements elements = doc.getElementsByTag("li");
        Element element1 = elements.get(0);
        Element element2 = elements.get(1);

        int res1 = CrawlerUtil.getResultIndex(element1);
        int res2 = CrawlerUtil.getResultIndex(element2);

        assert(res1 == 0);
        assert(res2 == -1);
    }
}
