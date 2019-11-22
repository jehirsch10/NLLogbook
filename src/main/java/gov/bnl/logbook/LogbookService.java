package gov.bnl.logbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.bnl.logbook.ElasticSearchClient;

@RestController
@RequestMapping("LogFinder/resources/logbooks")
@EnableAutoConfiguration
public class LogbookService {
    static Logger logbookManagerAudit = Logger.getLogger(LogbookService.class.getName() + ".audit");
    static Logger log = Logger.getLogger(LogbookService.class.getName());

    @Value("${elasticsearch.logbook.index:logfinder}")
    private String ES_LOGBOOK_INDEX;
    @Value("${elasticsearch.logbook.type:logbook}")
    private String ES_LOGBOOK_TYPE;
    
    @Autowired
    ElasticSearchClient esService;
    
    ObjectMapper objectMapper = new ObjectMapper();
            
    /**
     * PUT method to create a logbook
     * 
     * @param id - id of the log to be created
     * @param src - src of the log to be created
     * @return the created logbook
     */
    @PutMapping("/{id}")
    public Logbook create(@PathVariable long id, @RequestBody String src) {        
        // create new logbook
        Logbook logbook = new Logbook(id,src);
        // index logbook
        RestHighLevelClient client = esService.getIndexClient(); 
        try {
            IndexRequest indexRequest = new IndexRequest(ES_LOGBOOK_INDEX, ES_LOGBOOK_TYPE)
                    .id(id+"")
                    .source(objectMapper.writeValueAsBytes(logbook), XContentType.JSON);
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            // verify the creation of the logbook
            Result result = indexResponse.getResult();
            if (result.equals(Result.CREATED) || result.equals(Result.UPDATED)) {
                return findById(id).get();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to index logbook " + logbook.toLog(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to index logbook: " + logbook, null);
        }
        return null;  
    }

    /**
     * GET method for retrieving an instance of Logbook identified by id
     *
     * @param id - logbook id to search for
     * @return found logbook
     */
    @GetMapping("/{id}")
    public Logbook read(@PathVariable("id") long id) {
        logbookManagerAudit.info("getting logbook with id: " + id);

        Optional<Logbook> foundLogbook = findById(id);
        if (foundLogbook.isPresent())
            return foundLogbook.get();
        else {
            log.log(Level.SEVERE, "The logbook with the id " + id + " does not exist", new ResponseStatusException(HttpStatus.NOT_FOUND));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "The logbook with the id " + id + " does not exist");
        }
    }
    
    /**
     * find logbook using the given logbook id
     * 
     * @param id - id of logbook to be found
     * @return the found logbook
     */
    public Optional<Logbook> findById(long id) {
        RestHighLevelClient client = esService.getSearchClient();
        GetRequest getRequest = new GetRequest(ES_LOGBOOK_INDEX, ES_LOGBOOK_TYPE, id+"");
        try {
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            if (response.isExists()) {
                Logbook logbook = objectMapper.readValue(response.getSourceAsBytesRef().streamInput(), Logbook.class);               
                return Optional.of(logbook);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to find logbook with id: " + id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Failed to find logbook with id: " + id, null);
        }
        return Optional.empty();
    }
    
    /**
     * more-like-this search for a logbook(s)
     */
    @GetMapping("/moreLikeThis")
    public List<Logbook> moreLikeThis(@RequestBody String like_this) {
        RestHighLevelClient client = esService.getSearchClient();
        try {
            int size = 10000;
            int from = 0;
            
            String[] fields = {"description"};               
            String[] texts = {like_this};
            MoreLikeThisQueryBuilder qb = QueryBuilders.moreLikeThisQuery(fields, texts, null).minDocFreq(1).minTermFreq(1).minimumShouldMatch("1%");
            
            SearchRequest searchRequest = new SearchRequest(ES_LOGBOOK_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size);
            if (from >= 0) {
                searchSourceBuilder.from(from);
            }
            searchSourceBuilder.query(qb);
            searchRequest.types(ES_LOGBOOK_TYPE);
            searchRequest.source(searchSourceBuilder);
            final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<Logbook> result = new ArrayList<Logbook>();
            searchResponse.getHits().forEach(hit -> {
                try {
                    result.add(objectMapper.readValue(hit.getSourceRef().streamInput(), Logbook.class));
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Search failed for: " + like_this + ", CAUSE: " + e.getMessage(), e);
        }
    }
    
    /**
     * fuzzy search for a logbook(s)
     */
    @GetMapping("/fuzzy")
    public List<Logbook> fuzzy(@RequestBody String fuzzy) {
        RestHighLevelClient client = esService.getSearchClient();
        try {
            int size = 10000;
            int from = 0;
            
            QueryBuilder qb = QueryBuilders.fuzzyQuery("description", fuzzy);
            
            SearchRequest searchRequest = new SearchRequest(ES_LOGBOOK_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size);
            if (from >= 0) {
                searchSourceBuilder.from(from);
            }
            searchSourceBuilder.query(qb);
            searchRequest.types(ES_LOGBOOK_TYPE);
            searchRequest.source(searchSourceBuilder);
            final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<Logbook> result = new ArrayList<Logbook>();
            searchResponse.getHits().forEach(hit -> {
                try {
                    result.add(objectMapper.readValue(hit.getSourceRef().streamInput(), Logbook.class));
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Search failed for: " + fuzzy + ", CAUSE: " + e.getMessage(), e);
        }
    }
    
    /**
     * match query with fuzziness search for a logbook(s)
     */
    @GetMapping("/search")
    public List<Logbook> search(@RequestBody String search) {
        RestHighLevelClient client = esService.getSearchClient();
        try {
            int size = 10000;
            int from = 0;
            
            String[] fields = {"description"};               
            MatchQueryBuilder qb = QueryBuilders.matchQuery("description", search).minimumShouldMatch("1%");
            qb.fuzziness(1);
            SearchRequest searchRequest = new SearchRequest(ES_LOGBOOK_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size);
            if (from >= 0) {
                searchSourceBuilder.from(from);
            }
            searchSourceBuilder.query(qb);
            searchRequest.types(ES_LOGBOOK_TYPE);
            searchRequest.source(searchSourceBuilder);
            final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<Logbook> result = new ArrayList<Logbook>();
            searchResponse.getHits().forEach(hit -> {
                try {
                    result.add(objectMapper.readValue(hit.getSourceRef().streamInput(), Logbook.class));
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Search failed for: " + search + ", CAUSE: " + e.getMessage(), e);
        }
    }

    /**
     * match query with fuzziness search for a logbook(s)
     */
    @GetMapping("/multiSearch/{other_field}")
    public List<Logbook> multiFieldSearch(@RequestBody String search, @PathVariable("otherField") String field) {
        RestHighLevelClient client = esService.getSearchClient();
        try {
            int size = 10000;
            int from = 0;
            
            String[] fields = {"description",field};               
            MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(fields, search).minimumShouldMatch("1%");
            qb.fuzziness(1);
            SearchRequest searchRequest = new SearchRequest(ES_LOGBOOK_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size);
            if (from >= 0) {
                searchSourceBuilder.from(from);
            }
            searchSourceBuilder.query(qb);
            searchRequest.types(ES_LOGBOOK_TYPE);
            searchRequest.source(searchSourceBuilder);
            final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<Logbook> result = new ArrayList<Logbook>();
            searchResponse.getHits().forEach(hit -> {
                try {
                    result.add(objectMapper.readValue(hit.getSourceRef().streamInput(), Logbook.class));
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Search failed for: " + search + ", CAUSE: " + e.getMessage(), e);
        }
    }
}