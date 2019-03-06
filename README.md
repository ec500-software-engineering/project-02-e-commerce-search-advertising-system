# Ecommerce Ads Search System

## Contributors:    
Zhizhou Qiu, Qinmei Du

## User Story   
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
