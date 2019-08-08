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
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * PUT method to create a logbook.
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
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to find logbook with id: " + id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Failed to find logbook with id: " + id, null);
        }
        return Optional.empty();
    }
}