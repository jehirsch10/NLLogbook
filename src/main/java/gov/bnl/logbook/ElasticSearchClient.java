package gov.bnl.logbook;

import java.io.IOException;
import java.util.logging.Level;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/application.properties")
public class ElasticSearchClient implements ServletContextListener {
    private static Logger log = Logger.getLogger(ElasticSearchClient.class.getCanonicalName());

    private RestHighLevelClient searchClient;
    private RestHighLevelClient indexClient;

    @Value("${elasticsearch.cluster.name:elasticsearch}")
    private String clusterName;
    @Value("${elasticsearch.network.host:localhost}")
    private String host;
    @Value("${elasticsearch.http.port:9200}")
    private int port;

    public RestHighLevelClient getSearchClient() {
        if(searchClient == null) {
            searchClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        }
        return searchClient;
    }

    public RestHighLevelClient getIndexClient() {
        if(indexClient == null) {
            indexClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        }
        return indexClient;
    }

    /**
     * Returns a new {@link TransportClient} using the default settings
     * **IMPORTANT** it is the responsibility of the caller to close this client
     * 
     * @return es transport client
     */
    @SuppressWarnings("resource")
    public RestHighLevelClient getNewClient() {
        try {
            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
            return client;
        } catch (ElasticsearchException e) {
            log.log(Level.SEVERE, "failed to create elastic client", e.getDetailedMessage());
            return null;
        }
    }

    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing a new Transport clients.");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Closing the default Transport clients.");
        try {
            searchClient.close();
            indexClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
