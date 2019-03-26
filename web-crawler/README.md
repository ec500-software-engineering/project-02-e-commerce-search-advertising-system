# Web Crawler

This is a web crawler to collect merchandise information from an e-commerce web site.

## Development environment
- **Jsoup** is used to retrieve web page content, parse the required text from the page.
- **Lucene** is used for filtering out stop words.
 This was originally written in **Python** using **NLTK(Natural Language Tool Kit)** package.

## Format of input files

File Path Settings:   
args[0] is file path for rawQuery.txt,    
args[1] is file path for adsDataFile,   
ards[2] is file path for proxylist.csv,   
args[3] is file path for logFile.


proxylist:
```java
[ip],[port],[port],[user],[password]
127.0.0.1,60099,61336,user,password
```
Currently, the username and password are hard coded in the AmazonCrawler class. Please modify it when you need.

rawQueryData:
```java
[key words to search], [bid price], [campaignId], [queryGroupId]
Prenatal DHA, 3.4, 8040,10
```

## Features
- Clean and tokenize title and query. (Replace Python code)
- Set Ads keywords with tokenized title.
- For each raw query, parse 10 pages.
- For each raw query, if length of the query >= 3, generate n-gram (length: 2 ~ length(query) - 1).
- For each sub query generated, if it's not in current raw query file, send it to crawler and 
  crawl corresponding data, generate ads if the result is in the same category as raw query's category.

## Getting started
### Installation
```bash
mvn clean install
```
### Start crawling
```bash
sh ./start-my-crawler.sh
```
rawQuery.txt -- list of query strings to use for querying e-commerce web site.
adsDataFile -- the output file for crawler generated ads result.
proxylist.csv -- list of proxies to round-robin to avoid BOT detection.
logFile -- the output file for crawler generated logs.

## LICENSE

[MIT](./License.txt)

