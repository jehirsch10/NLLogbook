package gov.bnl.logbook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Logbook object that can be represented as XML/JSON in payload data.
 */
@XmlRootElement(name="Logbook")
@XmlType (propOrder={"id","src","description"})
public class Logbook {
    private long id;
    private String src;
    private String description;

    /**
     * Creates a new instance of Logbook.
     *
     */
    public Logbook() {
    }

    /**
     * Creates a new instance of Logbook.
     *
     * @param id
     */
    public Logbook(long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Logbook.
     *
     * @param id
     * @param src
     */
    public Logbook(long id, String src) {
        this.id = id;
        this.src = src;
        createDescription();
    }

    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data the Label to log
     * @return string representation for log
     */
    public String toLogger() {
        return this.getId()+"";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Logbook other = (Logbook) obj;
        
        if (!(id == other.id)) {
            return false;
        }
        if (src == null) {
            if (other.src != null) {
                return false;
            }
        } else if (!src.equals(other.src)) {
            return false;
        }
        return true;
    }
    
    public void createDescription() {
        Parser parser = Parser.builder().build();
        TextContentRenderer text_renderer = TextContentRenderer.builder().build();
        this.description = text_renderer.render(parser.parse(src));
    }
}