package org.swordapp.server;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepositReceipt {
    private List<String> packagingFormats = new ArrayList<>();
    private IRI editIRI = null;
    private IRI seIRI = null;
    private IRI emIRI = null;
    private IRI feedIRI = null;
    private IRI location = null;
    private Entry entry;
    private Map<String, String> statements = new HashMap<>();
    private String treatment = "no treatment information available";
    private String verboseDescription = null;
    private String splashUri = null;
    private String originalDepositUri = null;
    private String originalDepositType = null;
    private Map<String, String> derivedResources = new HashMap<>();
    private boolean empty = false;
    private Date lastModified = null;

    public DepositReceipt() {
        Abdera abdera = new Abdera();
        this.entry = abdera.newEntry();
    }

    public Entry getWrappedEntry() {
        return this.entry;
    }

    public Entry getAbderaEntry() {
        Entry abderaEntry = (Entry) this.entry.clone();
        
        if (this.editIRI != null) {
            // use the edit iri as the id
            abderaEntry.setId(this.editIRI.toString());
            // add the Edit IRI Link
            abderaEntry.addLink(this.editIRI.toString(), "edit");
        }

        // add the Sword Edit IRI link
        if (this.seIRI != null) {
            abderaEntry.addLink(this.seIRI.toString(), UriRegistry.REL_SWORD_EDIT);
        }

        // add the atom formatted feed
        if (this.feedIRI != null) {
            Link fl = abderaEntry.addLink(this.feedIRI.toString(), "edit-media");
            fl.setMimeType("application/atom+xml;type=feed");
        }

        // add the edit-media link
        if (this.emIRI != null) {
            abderaEntry.addLink(this.emIRI.toString(), "edit-media");
        }

        // add the packaging formats
        for (String pf : this.packagingFormats) {
            abderaEntry.addSimpleExtension(UriRegistry.SWORD_PACKAGING, pf);
        }

        // add the statement URIs
        for (Map.Entry<String, String> statement : this.statements.entrySet()) {
            Link link = abderaEntry.addLink(statement.getKey(), UriRegistry.REL_STATEMENT);
            link.setMimeType(statement.getValue());
        }

        if (this.treatment != null) {
            abderaEntry.addSimpleExtension(UriRegistry.SWORD_TREATMENT, this.treatment);
        }

        if (this.verboseDescription != null) {
            abderaEntry.addSimpleExtension(UriRegistry.SWORD_VERBOSE_DESCRIPTION, this.verboseDescription);
        }

        if (this.splashUri != null) {
            abderaEntry.addLink(this.splashUri, "alternate");
        }

        if (this.originalDepositUri != null) {
            Link link = abderaEntry.addLink(this.originalDepositUri, UriRegistry.REL_ORIGINAL_DEPOSIT);
            if (this.originalDepositType != null) {
                link.setMimeType(this.originalDepositType);
            }
        }

        for (Map.Entry<String, String> uri : this.derivedResources.entrySet()) {
            Link link = abderaEntry.addLink(uri.getKey(), UriRegistry.REL_DERIVED_RESOURCE);
            if (uri.getValue() != null) {
                link.setMimeType(uri.getValue());
            }
        }

        return abderaEntry;
    }

    public Date getLastModified() {
        return lastModified == null ? null : new Date(lastModified.getTime());
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = new Date(lastModified.getTime());
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(final boolean empty) {
        this.empty = empty;
    }

    public void setMediaFeedIRI(final IRI feedIRI) {
        this.feedIRI = feedIRI;
    }

    public void setEditMediaIRI(final IRI emIRI) {
        this.emIRI = emIRI;
    }

    public void setEditIRI(final IRI editIRI) {
        this.editIRI = editIRI;

        // set the SE-IRI as the same if it has not already been set
        if (this.seIRI == null) {
            this.seIRI = editIRI;
        }
    }

    public IRI getLocation() {
        return this.location == null ? this.editIRI : this.location;
    }

    public void setLocation(final IRI location) {
        this.location = location;
    }

    public IRI getEditIRI() {
        return this.editIRI;
    }

    public IRI getSwordEditIRI() {
        return this.seIRI;
    }

    public void setSwordEditIRI(final IRI seIRI) {
        this.seIRI = seIRI;

        // set the Edit-IRI the same if it has not already been set
        if (this.editIRI == null) {
            this.editIRI = seIRI;
        }
    }

    public void setContent(final IRI href, final String mediaType) {
        this.entry.setContent(href, mediaType);
    }

    public void addEditMediaIRI(final IRI href) {
        this.entry.addLink(href.toString(), "edit-media");
    }

    public void addEditMediaIRI(final IRI href, final String mediaType) {
        Abdera abdera = new Abdera();
        Link link = abdera.getFactory().newLink();
        link.setHref(href.toString());
        link.setRel("edit-media");
        link.setMimeType(mediaType);
        this.entry.addLink(link);
    }

    public void addEditMediaFeedIRI(final IRI href) {
        this.addEditMediaIRI(href, "application/atom+xml;type=feed");
    }

    public void setPackaging(final List<String> packagingFormats) {
        this.packagingFormats = packagingFormats;
    }

    public void addPackaging(final String packagingFormat) {
        this.packagingFormats.add(packagingFormat);
    }

    public void setOREStatementURI(final String statement) {
        this.setStatementURI("application/rdf+xml", statement);
    }

    public void setAtomStatementURI(final String statement) {
        this.setStatementURI("application/atom+xml;type=feed", statement);
    }

    public void setStatementURI(final String type, final String statement) {
        this.statements.put(statement, type);
    }

    public Element addSimpleExtension(final QName qname, final String value) {
        return this.entry.addSimpleExtension(qname, value);
    }

    public Element addDublinCore(final String element, final String value) {
        return this.entry.addSimpleExtension(new QName(UriRegistry.DC_NAMESPACE, element), value);
    }

    public void setTreatment(final String treatment) {
        this.treatment = treatment;
    }

    public void setVerboseDescription(final String verboseDescription) {
        this.verboseDescription = verboseDescription;
    }

    public void setSplashUri(final String splashUri) {
        this.splashUri = splashUri;
    }

    public void setOriginalDeposit(final String originalDepositUri, final String originalDepositType) {
        this.originalDepositUri = originalDepositUri;
        this.originalDepositType = originalDepositType;
    }

    public void setDerivedResources(final Map<String, String> derivedResources) {
        this.derivedResources = derivedResources;
    }

    public void addDerivedResource(final String resourceUri, final String resourceType) {
        this.derivedResources.put(resourceUri, resourceType);
    }
}
