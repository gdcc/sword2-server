package org.swordapp.server;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Map;

public class OREStatement extends Statement {
    private final String remUri;
    private final String aggUri;

    public OREStatement(final String remUri, final String aggUri) {
        this.remUri = remUri;
        this.aggUri = aggUri;
        this.contentType = "application/rdf+xml";
    }

    @Override
    public void writeTo(final Writer out) throws IOException {
        // create the default model (in memory) to start with
        Model model = ModelFactory.createDefaultModel();

        // set up some sensible namespaces for the prefixes
        model.setNsPrefix(UriRegistry.ORE_PREFIX, UriRegistry.ORE_NAMESPACE);
        model.setNsPrefix(UriRegistry.SWORD_PREFIX, UriRegistry.SWORD_TERMS_NAMESPACE);

        // create the resource map in the model
        Resource rem = model.createResource(this.remUri);
        rem.addProperty(RDF.type, model.createResource(UriRegistry.ORE_NAMESPACE + "ResourceMap"));

        // create the aggregation
        Resource agg = model.createResource(this.aggUri);
        agg.addProperty(RDF.type, model.createResource(UriRegistry.ORE_NAMESPACE + "Aggregation"));

        // add the aggregation to the resource (and vice versa)
        rem.addProperty(model.createProperty(UriRegistry.ORE_NAMESPACE + "describes"), agg);
        agg.addProperty(model.createProperty(UriRegistry.ORE_NAMESPACE + "isDescribedBy"), rem);

        // now go through and add all the ResourceParts as aggregated resources
        for (ResourcePart rp : this.resources) {
            Resource part = model.createResource(rp.getUri());
            part.addProperty(RDF.type, model.createResource(UriRegistry.ORE_NAMESPACE + "AggregatedResource"));
            agg.addProperty(model.createProperty(UriRegistry.ORE_NAMESPACE + "aggregates"), part);
        }

        // now go through all the original deposits and add them as both aggregated
        // resources and as originalDeposits (with all the trimmings)
        for (OriginalDeposit od : this.originalDeposits) {
            Resource deposit = model.createResource(od.getUri());
            deposit.addProperty(RDF.type, model.createResource(UriRegistry.ORE_NAMESPACE + "AggregatedResource"));
            if (od.getDepositedBy() != null) {
                deposit.addLiteral(model.createProperty(UriRegistry.SWORD_DEPOSITED_BY_URI), od.getDepositedBy());
            }

            if (od.getDepositedOnBehalfOf() != null) {
                deposit.addLiteral(model.createProperty(UriRegistry.SWORD_DEPOSITED_ON_BEHALF_OF_URI), od.getDepositedOnBehalfOf());
            }

            if (od.getDepositedOn() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                deposit.addLiteral(model.createProperty(UriRegistry.SWORD_DEPOSITED_ON_URI), sdf.format(od.getDepositedOn()));
            }

            for (String packaging : od.getPackaging()) {
                deposit.addLiteral(model.createProperty(UriRegistry.SWORD_PACKAGING.toString()), packaging);
            }

            agg.addProperty(model.createProperty(UriRegistry.ORE_NAMESPACE + "aggregates"), deposit);
            agg.addProperty(model.createProperty(UriRegistry.SWORD_ORIGINAL_DEPOSIT_URI), deposit);
        }

        // now add the state information
        for (Map.Entry<String, String> state : this.states.entrySet()) {
            Resource s = model.createResource(state.getKey());
            if (state.getValue() != null) {
                s.addProperty(model.createProperty(UriRegistry.SWORD_STATE_DESCRIPTION_URI), state.getValue());
            }
            agg.addProperty(model.createProperty(UriRegistry.SWORD_STATE_URI), s);
        }

        // write the model directly to the output
        model.write(out, "RDF/XML");
    }
}
