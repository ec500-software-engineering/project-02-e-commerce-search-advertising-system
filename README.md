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
