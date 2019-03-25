package io.bittiger.crawler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bittiger.ad.Ad;
import io.bittiger.util.CrawlerUtil;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrawlerMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: Crawler <rawQueryDataFilePath> <adsDataFilePath> <proxyFilePath> <logFilePath>");
            System.exit(0);
        }
        ObjectMapper mapper = new ObjectMapper();
        String rawQueryDataFilePath = args[0];
        String adsDataFilePath = args[1];
        String proxyFilePath = args[2];
        String logFilePath = args[3];
        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath, logFilePath);
        File file = new File(adsDataFilePath);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        Set<String> queries = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty())
                    continue;
                System.out.println(line);
                String[] fields = line.split(",");
                String query = fields[0].trim();
                queries.add(query);
                double bidPrice = Double.parseDouble(fields[1].trim());
                int campaignId = Integer.parseInt(fields[2].trim());
                int queryGroupId = Integer.parseInt(fields[3].trim());

                List<Ad> ads = crawler.getAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId, null);
                updateAds(ads, mapper, bw);
                if (ads != null && ads.size() > 0) {
                    String category = ads.get(0).category;
                    String newQuery = ads.get(0).query; // use cleaned query to avoid stop words: ex: '&' will cause problem in subquery
                    // Can also use ShingleFilter for sub queries
//                    List<String> subQueries = CrawlerUtil.getSubQueriesWithShingleFilter(newQuery);
                    List<String> subQueries = CrawlerUtil.getSubQueries(newQuery);
                    for (String q : subQueries) {
                        if (!queries.add(q)) {
                            continue;
                        }
                        List<Ad> subAds = crawler.getAdBasicInfoByQuery(q, bidPrice, campaignId, queryGroupId, category);
                        updateAds(subAds, mapper, bw);
                    }
                }
                Thread.sleep(2000);
            }
            bw.close();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        crawler.cleanup();
    }

    private static void updateAds(List<Ad> ads, ObjectMapper mapper, BufferedWriter bw) throws IOException {
        for (Ad ad : ads) {
            String jsonInString = mapper.writeValueAsString(ad);
            //System.out.println(jsonInString);
            bw.write(jsonInString);
            bw.newLine();
        }
    }
}
