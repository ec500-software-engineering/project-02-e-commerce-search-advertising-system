# Ecommerce Ads Search System

## Contributors:    
Zhizhou Qiu, Qinmei Du

## User Story   
While searching keyword in Google, the websites appearing in the top search results are all advertisement. As for shopping website like Amazon, the results with Sponsor are all advertising. They makes customers who have interest in those items tends to improve the possibility of buying the one with advertisement.  
Internet giants such as Google and Facebook rely mainly on advertising revenue. Advertising system design and optimization require a large amount of advertising system knowledge, distributed system optimization, machine learning and big data processing skills.      
By calculating the relativity between the user's query and the ads provided by merchants, this ads searching system displays the most relevant ads that users are most interested in so that user are more likely to click the ads.  
In the meantime, merchants can easily target the most potential customers and put the right ads with the lowest cost.  

## Data Structure    
  *  Advertiser:      
      *  Create Ads associated with bid price   
      *  Bid for position     
      *  Pay by CPC (cost per click)    
  *  User:
      *  Send query to search engine, expressing some intent    
  *   Search Engine:      
      *  execute query against web corpus   
      *  execute query against ads corpus   
      *  Display search result page : web result, ads     

## How does it work:  
1. Merchants provide a list of ads which contain keywords, bid price, and total budget. Our system store all the information and build the forwarded and inverted indexes.    
2. When user types the query on the search box, the system parses the query and calculates the relevant score between the query and the ads. Then the system processes a workflow to select, filter, price, and allocate the relevant ads.    
3. Finally the ads is sent and displayed to the user interface.   
Note:
Search Ads   
  *  logic:      
      *  match ad's key words to users' query    
  *  Ads format:
      *  text
      *  image
  *  Ads position:      
      *  main line, side bar, top, bottom of search result. 
Ads keyword
  * Campaign
    * A campaign focuses on a theme or a group of products
  * set a budget
  * choose your audience
  * write your ads including keywords and content.
  * Google Adwords is also a good place to make your own ads.

## Ads Searching System Schema:  
### Query Understanding:  
   *  Cleaning:
       *  remove stop words: a, an, the ...
       *  remove ending s, ‘s
   *  Query Intent Prediction:  
       *  predict user’s intent
       *  buy Harry Potter DVD online -> Harry Potter DVD
     *  need query history log  
   *  Query Expansion:  
       *  nike running shoe -> nike running sneakers  
       *  software developer -> software engineer  
   Result: a list of query    
### Selevt Ads:   
  *  send query understand result to index and select as much candidates as possible  
  *  apply information retrieval algorithm on index server   
### Rank Ads:  
  *  rank ads according rank scores:  
     *  relevance between query and ad   
     *  click probability   
     *  bid price   
### Select Top K ads
  *  Top K ads are selected to be displayed in front of users according to the rank scores.

## Web Crawler
Making requests and extracting data from the response.
Two design choice
 * Parse HTML page synchronously
 * Parse HTML page asynchronously  
Comparision:  
* Synchronously  
   * Pros: Save disk space  
   * Cons: 1. If need extract more data then have to crawl again  
           2. Crawling is blocked by parsing HTML  
* Asynchronously
   * Pros: 1. Re-parse HTML if needed  
           2. unblock crawling from parsing HTML
   * Cons: 1. Need more storage space  
           2. Need more machines or CPU to parse HTML  
Where to start crawling
   * start with feeds file
   * a list of website url
   * request url with different parameter
   * sample feeds file
  
Challenge:
* Network I/O(using multi-threaded and non-blocking IO)
* Avoid Bot Detection(using Spook Http Headers and Proxy Service)
* Crawler needed to be resilient(log crawled url&failed url&exception)

## Search log data
Devide IP, Device id, Session id, Query, Adld, Clicked(0/1), Ad category_X_Query category_matched  
* Goal: generate click log for query intent prediction and click prediction  
* Method: reverse engineering  
* Positive feature: IP, devide_id, Adld, QueryCategry_AdsCategory match, Query_CampaignLd match, Query_Adld match  
* Negative feature: mismatached query_category ads_category, mismatched Query_Campaignld, mismatched Query_Adld, lowest campaignld weight, lowese adld weight per query group  
### Steps
* Step 1: Segment users to 4 level(10000 IP, 5 device id per IP, user= ip + device_id)
    * level 0: 5% of user who click for each query
    * level 1: 5% of IP whose  1st 2 device id click for each query, reset 3  device_id click on 70% of query, rest of 30% query no click
    * level 2: 40% of IP from where we random select 1 device_id click for 50% of query, rest of 50% no click, rest of device_id no click.
    * level 3: 50% never click
* Step 2: assign campaign ID, Ad id, Click(o/1), Ad category_Query category(0/1) to user
    * assign weight to campaign ID, Ad IDs
    * group ad data by Query group ID  