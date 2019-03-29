package io.bittiger.crawler;

import io.bittiger.ad.Ad;
import io.bittiger.util.CrawlerUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AmazonCrawler {
    //https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=nikon+SLR&page=2
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private List<String> proxyList;
    private List<String> titleList;
    private List<String> categoryList;
    private List<String> detailList;
    private List<String> imageList;
    private HashSet crawledUrl;
    BufferedWriter logBFWriter;
    private int index = 0;
    private static int adId = 2000;

    public AmazonCrawler(String proxy_file, String log_file) {
        crawledUrl = new HashSet();
        initProxyList(proxy_file);
        initHtmlSelector();
        initLog(log_file);
    }

    public void cleanup() {
        if (logBFWriter != null) {
            try {
                logBFWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //raw url: https://www.amazon.com/KNEX-Model-Building-Set-Engineering/dp/B00HROBJXY/ref=sr_1_14/132-5596910-9772831?ie=UTF8&qid=1493512593&sr=8-14&keywords=building+toys
    //normalizedUrl: https://www.amazon.com/KNEX-Model-Building-Set-Engineering/dp/B00HROBJXY
    private String normalizeUrl(String url) {
        int i = url.indexOf("ref");
        String normalizedUrl = i == -1 ? url : url.substring(0, i - 1);
        if (normalizedUrl == null || normalizedUrl.trim().isEmpty()) {
            System.out.println(url);
        }
        return normalizedUrl;
    }

    private void initProxyList(String proxy_file) {
        proxyList = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxy_file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort", "61336"); // set proxy port
    }

    private void initHtmlSelector() {
        titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1)  > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a > h2");

        categoryList = new ArrayList<String>();
        //#refinements > div.categoryRefinementsSection > ul.forExpando > li:nth-child(1) > a > span.boldRefinementLink
        categoryList.add("#refinements > div.categoryRefinementsSection > ul.forExpando > li > a > span.boldRefinementLink");
        categoryList.add("#refinements > div.categoryRefinementsSection > ul.forExpando > li:nth-child(1) > a > span.boldRefinementLink");

        detailList = new ArrayList<>();
        detailList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a");
        detailList.add(" > div > div.a-row.a-spacing-none > div.a-row.a-spacing-mini > a");

        imageList = new ArrayList<>();
        imageList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img");
        imageList.add(" > div > div.a-row.a-spacing-base > div > a > img");
    }

    private void initLog(String log_path) {
        try {
            File log = new File(log_path);
            // if file doesnt exists, then create it
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logBFWriter = new BufferedWriter(fw);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setProxy() {
        //rotate
        if (index == proxyList.size()) {
            index = 0;
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy); // set proxy server
        index++;
    }

    private void testProxy() {
        System.setProperty("socksProxyHost", "199.101.97.149"); // set proxy server
        System.setProperty("socksProxyPort", "61336"); // set proxy port
        String test_url = "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            Document doc = Jsoup.connect(test_url).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Ad> getAdBasicInfoByQuery(String query, double bidPrice, int campaignId, int queryGroupId, String category) {
        List<Ad> products = new ArrayList<>();
        try {
            if (false) {
                testProxy();
                return products;
            }

            setProxy();
            Map<String, String> headers = createHeaders();
            String page = "&page=";

            for (int pageNumber = 1; pageNumber <= 10; pageNumber++) {
                String url = AMAZON_QUERY_URL + query + page + pageNumber;
                System.out.println("query url = " + url);
                Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).timeout(100000).get();
                //Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(100000).get();
                //System.out.println(doc.text());
                Elements results = doc.select("li[data-asin]");

                System.out.println("num of results = " + results.size());
                for (int i = 0; i < results.size(); i++) {
                    int index = CrawlerUtil.getResultIndex(results.get(i));
                    if (index == -1) {
                        logBFWriter.write("cannot get result index for element of query: " + query + ", element: " + results.get(i).toString());
                        logBFWriter.newLine();
                        continue;
                    }
                    Ad ad = createAd(doc, index, logBFWriter, query, bidPrice, campaignId, queryGroupId);
                    if (ad == null || (category != null && ad.category != category)) {
                        continue;
                    }
                    products.add(ad);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return products;
    }

    private Map<String, String> createHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
//        headers.put("Accept-Language", "en-US,en;q=0.8");

        headers.put("Accept", "text/html,text/plain");
        headers.put("Accept-Language", "en-us,en");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Accept-Charset", "utf-8");
        return headers;
    }

    private Ad createAd(Document doc, int index, BufferedWriter logBFWriter, String query, double bidPrice, int campaignId, int queryGroupId) throws IOException {
        Ad ad = new Ad();
        if (!updateDetailUrls(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        ad.query = CrawlerUtil.joinTokens(CrawlerUtil.cleanData(query));
        ad.query_group_id = queryGroupId;
        if (!updateTitle(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        if (!updateThumbnail(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        if (!updateBrand(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        ad.bidPrice = bidPrice;
        ad.campaignId = campaignId;
        if (!updatePrice(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        if (!updateCategory(ad, doc, index, logBFWriter, query)) {
            return null;
        }
        ad.adId = AmazonCrawler.adId++;
        return ad;
    }

    private boolean updateDetailUrls(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        for (String detail : detailList) {
            String detail_ele_path = "#result_" + Integer.toString(index) + detail;
            Element detail_url_ele = doc.select(detail_ele_path).first();
            if (detail_url_ele != null) {
                String detail_url = detail_url_ele.attr("href");
                System.out.println("detail = " + detail_url);
                String normalizedUrl = normalizeUrl(detail_url);
                if (crawledUrl.contains(normalizedUrl)) {
                    logBFWriter.write("crawled url:" + normalizedUrl);
                    logBFWriter.newLine();
                    return false;
                }
                crawledUrl.add(normalizedUrl);
                System.out.println("normalized url  = " + normalizedUrl);
                ad.detail_url = normalizedUrl;
                break;
            }
        }

        if (ad.detail_url == "") {
            logBFWriter.write("cannot parse detail for query:" + query + ", detail: " + ad.detail_url + ", index = " + index);
            logBFWriter.newLine();
            return false;
        }

        return true;
    }

    private boolean updateTitle(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        ad.keyWords = new ArrayList<>();
        //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
        //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
        //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
        for (String title : titleList) {
            String title_ele_path = "#result_" + Integer.toString(index) + title;
            Element title_ele = doc.select(title_ele_path).first();
            if (title_ele != null) {
                System.out.println("title = " + title_ele.text());
                ad.title = title_ele.text();
                ad.keyWords = CrawlerUtil.cleanData(ad.title);
                break;
            }
        }

        if (ad.title == "") {
            logBFWriter.write("cannot parse title for query: " + query + ", index = " + index);
            logBFWriter.newLine();
            return false;
        }

        return true;
    }

    //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
    private boolean updateThumbnail(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        for (String image : imageList) {
            String thumbnail_path = "#result_" + Integer.toString(index) + image;
            Element thumbnail_ele = doc.select(thumbnail_path).first();
            if (thumbnail_ele != null) {
                //System.out.println("thumbnail = " + thumbnail_ele.attr("src"));
                ad.thumbnail = thumbnail_ele.attr("src");
                break;
            }
        }

        if (ad.thumbnail == "") {
            logBFWriter.write("cannot parse thumbnail for query:" + query + ", thumbnail: " + ad.thumbnail + ", index = " + index);
            logBFWriter.newLine();
            return false;
        }

        return true;
    }

    private boolean updateBrand(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        String brand_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div > span:nth-child(2)";
        Element brand = doc.select(brand_path).first();
        if (brand != null) {
            //System.out.println("brand = " + brand.text());
            ad.brand = brand.text();
        }

        if (ad.brand == "") {
            logBFWriter.write("cannot parse brand for query:" + query + ", brand: " + ad.brand + ", index = " + index);
            logBFWriter.newLine();
            return false;
        }

        return true;
    }

    //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span
    private boolean updatePrice(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        ad.price = 0.0;
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span

        //price
        String price_whole_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span";
        String price_fraction_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > sup.sx-price-fractional";
        Element price_whole_ele = doc.select(price_whole_path).first();
        if (price_whole_ele != null) {
            String price_whole = price_whole_ele.text();
            //System.out.println("price whole = " + price_whole);
            //remove ","
            //1,000
            if (price_whole.contains(",")) {
                price_whole = price_whole.replaceAll(",", "");
            }

            try {
                ad.price = Double.parseDouble(price_whole);
            } catch (NumberFormatException ne) {
                // TODO Auto-generated catch block
                ne.printStackTrace();
                //log
            }
        }

        Element price_fraction_ele = doc.select(price_fraction_path).first();
        if (price_fraction_ele != null) {
            //System.out.println("price fraction = " + price_fraction_ele.text());
            try {
                ad.price = ad.price + Double.parseDouble(price_fraction_ele.text()) / 100.0;
            } catch (NumberFormatException ne) {
                ne.printStackTrace();
            }
        }
        if (ad.price == 0.0) {
            ad.price = ThreadLocalRandom.current().nextDouble(30, 480);
        }
        //System.out.println("price = " + ad.price );

        return true;
    }

    private boolean updateCategory(Ad ad, Document doc, int index, BufferedWriter logBFWriter, String query) throws IOException {
        for (String category : categoryList) {
            Element category_ele = doc.select(category).first();
            if (category_ele != null) {
                //System.out.println("category = " + category_ele.text());
                ad.category = category_ele.text();
                break;
            }
        }
        if (ad.category == "") {
            logBFWriter.write("cannot parse category for query:" + query + ", category: " + ad.category + ", index = " + index);
            logBFWriter.newLine();
            return false;
        }

        return true;
    }
}