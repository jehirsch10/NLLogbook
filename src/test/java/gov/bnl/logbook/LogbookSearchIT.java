package gov.bnl.logbook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import gov.bnl.logbook.LogbookService;

@RunWith(SpringRunner.class)
@WebMvcTest(LogbookService.class)
public class LogbookSearchIT {

    @Autowired
    LogbookService logbookService;
    
    @Before
    public void setup() throws InterruptedException {
        logbookService.create(1,"This is *Sparta*!");
    }
    
    /**
     * index a logbook
     */
    @Test
    public void create() {
        Logbook logbook = new Logbook(1,"This is *Sparta*!");
        Logbook createdLogbook = logbookService.create(1,"This is *Sparta*!");
        // verify the logbook was created as expected
        assertEquals("Failed to create the logbook", logbook, createdLogbook);
    }

    /**
     * read a logbook
     */
    @Test
    public void read() {
        Logbook logbook = new Logbook(1,"This is *Sparta*!");
        Logbook readLogbook = logbookService.read(1);
        // verify the logbook was read as expected
        assertEquals("Failed to read the logbook", logbook, readLogbook);
    }
    
    /**
     * search for a logbook using more-like-this
     */
    @Test
    public void moreLikeThis() {      
        // SHOULD FIND THESE QUERIES
        // full src 
        assertTrue("Failed to find the logbook using moreLikeThis when the query was: full src",
                logbookService.moreLikeThis("This is *Sparta*!").size()==1);
        // caps don't matter
        assertTrue("Failed to find the logbook using moreLikeThis when the query was: caps don't matter",
                logbookService.moreLikeThis("sparta").size()==1);
        // doesn't care about md
        assertTrue("Failed to find the logbook using moreLikeThis when the query was: doesn't care about md",
                logbookService.moreLikeThis("Sparta*!").size()==1);
        // not all words necessary 
        assertTrue("Failed to find the logbook using moreLikeThis when the query was: not all words necessary",
                logbookService.moreLikeThis("fdsdf is fddfs fsdd fdsf").size()==1);
        
        // SHOULD NOT FIND THESE QUERIES
        // gobbledygook
        assertTrue("Found the logbook using moreLikeThis when the query was: gobbledygook",
                logbookService.moreLikeThis("cvber piodf csddf qwedf").size()==0);
        // off by one letter
        assertTrue("Found the logbook using moreLikeThis when the query was: off by one letter",
                logbookService.moreLikeThis("tis").size()==0);
        // multiple words off by one letter
        assertTrue("Found the logbook using moreLikeThis when the query was: multiple words off by one letter",
                logbookService.moreLikeThis("i s").size()==0);
    }
    
    /**
     * search for a logbook using fuzzy searching
     */
    @Test
    public void fuzzy() {
        // SHOULD FIND THESE QUERIES
        // caps don't matter
        assertTrue("Failed to find the logbook using fuzzy when the query was: caps don't matter",
                logbookService.fuzzy("sparta").size()==1);
        // off by one letter
        assertTrue("Failed to find the logbook using fuzzy when the query was: off by one letter",
                logbookService.fuzzy("parta").size()==1);
        // one correct word 
        assertTrue("Failed to find the logbook using fuzzy when the query was: one correct word",
                logbookService.fuzzy("is").size()==1);
        
        // SHOULD NOT FIND THESE QUERIES
        // gobbledygook
        assertTrue("Found the logbook using fuzzy when the query was: gobbledygook",
                logbookService.fuzzy("cvber piodf csddf qwedf").size()==0);
        // off by two letters
        assertTrue("Found the logbook using fuzzy when the query was: off by two letters",
                logbookService.fuzzy("part").size()==0);
        // multiple correct words 
        assertTrue("Found the logbook using fuzzy when the query was: multiple correct words",
                logbookService.fuzzy("This is").size()==0);
        // does care about md
        assertTrue("Found the logbook using fuzzy when the query was: does care about md",
                logbookService.fuzzy("*Sparta*!").size()==0);
    }
    
    /**
     * search for a logbook using match and fuzzy searching
     */
    @Test
    public void search() {// SHOULD FIND THESE QUERIES
        // full src 
        assertTrue("Failed to find the logbook using search when the query was: full src",
                logbookService.search("This is *Sparta*!").size()==1);
        // caps don't matter
        assertTrue("Failed to find the logbook using search when the query was: caps don't matter",
                logbookService.search("sparta").size()==1);
        // doesn't care about md
        assertTrue("Failed to find the logbook using search when the query was: doesn't care about md",
                logbookService.search("Sparta*!").size()==1);
        // not all words necessary 
        assertTrue("Failed to find the logbook using search when the query was: not all words necessary",
                logbookService.search("fdsdf is fddfs fsdd fdsf").size()==1);
        // off by one letter and other wrong words
        assertTrue("Failed to find the logbook using search when the query was: off by one letter and other wrong words",
                logbookService.search("parta xfr").size()==1);
        
        // SHOULD NOT FIND THESE QUERIES
        // gobbledygook
        assertTrue("Found the logbook using search when the query was: gobbledygook",
                logbookService.search("cvber piodf csddf qwedf").size()==0);
        // off by two letters
        assertTrue("Found the logbook using search when the query was: off by two letters",
                logbookService.search("part").size()==0);        
    }
    
    /**
     * search for a logbook using multi-match and fuzzy searching
     */
    // TODO: fix this
    @Test
    public void multiFieldSearch() {
        Logbook logbook = new Logbook(1,"This is *Sparta*!");
        
        Logbook foundLogbook = logbookService.multiFieldSearch("This is *Sparta*!", "topic").get(0);
        // verify the logbook was found as expected
        assertEquals("Failed to find the logbook", logbook, foundLogbook);
    }
}