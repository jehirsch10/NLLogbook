package gov.bnl.logbook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
        logbookService.create(2,"**Bones mend. Regret stays with you forever.**");
        logbookService.create(3,"Inflation allows for **subtle** wage cuts.");
        logbookService.create(4,"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque eros justo, consequat a risus ut, semper tincidunt orci. Etiam ante ipsum, fringilla a velit in, pellentesque pulvinar risus. Suspendisse euismod vel diam a aliquam. Donec vestibulum, turpis quis aliquet placerat, ipsum urna condimentum dolor, et volutpat tellus mi gravida metus. Sed ac urna id nulla sagittis consequat eget sit amet dolor. Cras lobortis augue orci, ac imperdiet est rhoncus at. Ut cursus risus a augue molestie volutpat. Fusce enim purus, pharetra at massa ut, egestas mattis quam. In hac habitasse platea dictumst. Aenean vel vehicula mi.\n\nIn hac habitasse platea dictumst. Donec vitae enim ut orci suscipit ullamcorper ac in est. Morbi sed erat vel leo dapibus auctor vel nec nibh. Praesent facilisis dolor sed gravida pharetra. Fusce ultricies magna eu dignissim molestie. Vestibulum sed sapien nec felis mattis tincidunt. Quisque eget risus sed mauris laoreet fermentum. Phasellus eros tellus, eleifend id diam vel, finibus porttitor elit.\n\nDonec ac est elit. Vestibulum tellus sem, pharetra id viverra in, pharetra sit amet metus. Phasellus eros orci, elementum interdum nulla sed, ullamcorper euismod quam. Nam lobortis venenatis pretium. Nam id vestibulum orci, vel malesuada diam. Integer efficitur neque quam, in consectetur nulla sodales quis. Curabitur a nisi id leo consequat tempor. Praesent ac efficitur justo, ac fermentum orci. In in vehicula ligula. Phasellus placerat a tellus in scelerisque. Ut consequat dapibus mi eget placerat. Integer quis turpis lectus. Morbi quis interdum nisl. Sed augue magna, elementum quis diam sagittis, auctor bibendum lectus. ");
        logbookService.create(5,"This isn't Sparta! Or is it?");
        logbookService.create(6,"Where has my Sparta gone?");
        logbookService.create(7,"Has this always been Sparta?");
        logbookService.create(8,"Sparta (Doric Greek: Σπάρτα, Spártā; Attic Greek: Σπάρτη, Spártē) was a prominent city-state in ancient Greece. In antiquity, the city-state was known as Lacedaemon (Λακεδαίμων, Lakedaímōn), while the name Sparta referred to its main settlement on the banks of the Eurotas River in Laconia, in south-eastern Peloponnese.[1] Around 650 BC, it rose to become the dominant military land-power in ancient Greece. THIS IS COPIED FROM SPARTA WIKIPEDIA PAGE");
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
    public void search() {
        // search: This is *Sparta*! (original test case)
        assertTrue("Failed to only find sparta related logbooks 8,1,5,7,6 using search when the query was: This is *Sparta*!",
                include(logbookService.search("This is *Sparta*!"),Arrays.asList(8L,1L,5L,7L,6L))
                && dontInclude(logbookService.search("This is *Sparta*!"),Arrays.asList(2L,3L,4L)));
        // search: sparta (caps don't matter)
        assertTrue("Failed to only find sparta related logbooks 8,1,6,7,5 using search when the query was: sparta",
                include(logbookService.search("sparta"),Arrays.asList(8L,1L,6L,7L,5L))
                && dontInclude(logbookService.search("sparta"),Arrays.asList(2L,3L,4L)));
        // search: `**sparta**` (md doesn't matter)
        assertTrue("Failed to only find sparta related logbooks 8,1,6,7,5 search when the query was: `**sparta**`",
                include(logbookService.search("`**sparta**`"),Arrays.asList(8L,1L,6L,7L,5L))
                && dontInclude(logbookService.search("`**sparta**`"),Arrays.asList(2L,3L,4L)));
        // search: fdsdf is fddfs fsdd fdsf (matches is with no fuzziness, and junk words) 
        assertTrue("Failed to find the 'is' logbooks 1,5,8 using search when the query was: fdsdf is fddfs fsdd fdsf",
                include(logbookService.search("fdsdf is fddfs fsdd fdsf"),Arrays.asList(1L,5L,8L))
                && dontInclude(logbookService.search("fdsdf is fddfs fsdd fdsf"),Arrays.asList(2L,3L,4L,6L,7L)));
        // search: parta xfr (5 letter word off by one letter, and a junk word)
        assertTrue("Failed to only find sparta related logbooks 1,6,7,5,8 search when the query was: parta xfr",
                include(logbookService.search("parta xfr"),Arrays.asList(1L,6L,7L,5L,8L))
                && dontInclude(logbookService.search("parta xfr"),Arrays.asList(2L,3L,4L)));
        // search: sporto (off by two letters)
        assertTrue("Failed to only find sparta related logbooks 8,1,6,7,5 search when the query was: sporto",
                include(logbookService.search("sporto"),Arrays.asList(8L,1L,6L,7L,5L))
                && dontInclude(logbookService.search("sporto"),Arrays.asList(2L,3L,4L)));
        // search: cvber piodf csddf qwedf (gobbledygook)
        assertTrue("Found a logbook using search when the query was: gobbledygook",
                dontInclude(logbookService.search("cvber piodf csddf qwedf"),Arrays.asList(1L,2L,3L,4L,5L,6L,7L,8L)));
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

    public static boolean include(List<Logbook> results, List<Long> include) {
        if(results.size()<1)
            return false;
        boolean found = false;
        for(long id: include) {
            for(Logbook result: results) {
                if(result.getId()==(id)) {
                    found = true;
                    break;
                }
            }
            if(found)
                found = false;
            else
                return false;
        }
        return true;
    }
    public static boolean dontInclude(List<Logbook> results, List<Long> dontInclude) {
        if(results.size()<1)
            return true;
        boolean found = false;
        for(long id: dontInclude) {
            for(Logbook result: results) {
                if(result.getId()==(id)) {
                    found = true;
                    break;
                }
            }
            if(found)
                return false;
        }
        return true;
    }
}