package org.swordapp.server;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MediaResourceAPI extends SwordAPIEndpoint {
    private static Logger log = LoggerFactory.getLogger(MediaResourceAPI.class);

    protected final MediaResourceManager mrm;

    public MediaResourceAPI(final MediaResourceManager mrm, final SwordConfiguration config) {
        super(config);
        this.mrm = mrm;
    }

    public void get(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.get(req, resp, true);
    }

    public void get(final HttpServletRequest req, final HttpServletResponse resp, final boolean sendBody) throws ServletException, IOException {
        log.debug("GET on Media Resource URL");

        // let the superclass prepare the request/response objects
        super.get(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            // NOTE: if allowUnauthenticated is true, then this will not send a 401 request
            // back if the client doesn't pre-emptively send the authentication credentials.
            // This means that basically you can't mix authenticated and unauthenticated
            // access, which is a nuisance.
            // FIXME: figure out a way to do authenticated and unauthenticated access simultaneously
            boolean allowUnauthenticated = this.config.allowUnauthenticatedMediaAccess();
            auth = this.getAuthCredentials(req, allowUnauthenticated);
            log.debug("Authentication Credentials extracted: " + auth.getUsername() + " obo: " + auth.getOnBehalfOf());
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
        }

        try {
            // get all of the Accept- headers out for content negotiation
            Map<String, String> acceptHeaders = this.getAcceptHeaders(req);

            // get the original request URI
            String editMediaURI = this.getFullUrl(req);

            // delegate to the implementation to get the resource representation
            MediaResource resource = this.mrm.getMediaResourceRepresentation(editMediaURI, acceptHeaders, auth, this.config);

            // now deliver the resource representation to the client

            // if this is a packaged resource, then write the package header
            if (!resource.isUnpackaged()) {
                String packaging = resource.getPackaging();
                if (packaging == null || "".equals(packaging)) {
                    packaging = UriRegistry.PACKAGE_SIMPLE_ZIP;
                }
                resp.setHeader("Packaging", packaging);
            }

            String contentType = resource.getContentType();
            if (contentType == null || "".equals(contentType)) {
                contentType = "application/octet-stream";
            }
            resp.setHeader("Content-Type", contentType);

            // set the last modified header
            // like: Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            Date lastModified = resource.getLastModified() != null ? resource.getLastModified() : new Date();
            resp.setHeader("Last-Modified", sdf.format(lastModified));

            // to set the content-md5 header we need to write the output to
            // a string and checksum it
            String md5 = resource.getContentMD5();
            resp.setHeader("Content-MD5", md5);

            if (sendBody) {
                OutputStream out = resp.getOutputStream();
                try (
                    InputStream in = resource.getInputStream();
                ) {
                    this.copyInputToOutput(in, out);
                    out.flush();
                } catch (IOException e) {
                    // we catch and rethrow here, yet making sure the try-with-resources closes the input stream.
                    // (The servlet output stream is not opened by us, we may not close it)
                    throw e;
                }
            }
        } catch (SwordError se) {
            this.swordError(req, resp, se);
            return;
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    public void head(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.get(req, resp, false);
    }

    public void put(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.put(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            auth = this.getAuthCredentials(req);
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
        }

        Deposit deposit = null;

        try {
            String editMediaIRI = this.getFullUrl(req);
            deposit = new Deposit();

            // add the properties from the binary deposit
            this.addDepositPropertiesFromBinary(deposit, req);

            // now fire the deposit object into the implementation
            DepositReceipt receipt = this.mrm.replaceMediaResource(editMediaIRI, deposit, auth, this.config);

            // no response is expected, if no errors get thrown we just return a success: 204 No Content
            // and the appropriate location header
            resp.setHeader("Location", receipt.getLocation().toString());
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SwordError se) {
            // get rid of any temp files used
            this.cleanup(deposit);

            this.swordError(req, resp, se);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // get rid of any temp files used
            this.cleanup(deposit);

            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } finally {
            // get rid of any temp files used
            this.cleanup(deposit);
        }
    }

    public void post(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.post(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            auth = this.getAuthCredentials(req);
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
        }

        Deposit deposit = null;
        try {
            deposit = new Deposit();

            if (this.getContentType(req).startsWith("multipart/related")) {
                throw new SwordError(UriRegistry.ERROR_METHOD_NOT_ALLOWED, "This server does not support RFC2387 Multipart uploads, to be removed in SWORD v2.1");
            } else {
                this.addDepositPropertiesFromBinary(deposit, req);
            }

            // this method has a special header (Metadata-Relevant) which we need to pull out
            boolean metadataRelevant = this.getMetadataRelevant(req);
            deposit.setMetadataRelevant(metadataRelevant);

            // now send the deposit to the implementation for processing
            DepositReceipt receipt = this.mrm.addResource(this.getFullUrl(req), deposit, auth, this.config);

            // prepare and return the response
            IRI location = receipt.getLocation();
            if (location == null) {
                throw new SwordServerException("No Edit-IRI found in Deposit Receipt; unable to send valid response");
            }

            resp.setStatus(HttpServletResponse.SC_CREATED); // Created
            if (this.config.returnDepositReceipt() && !receipt.isEmpty()) {
                this.addGenerator(receipt, this.config);
                resp.setHeader("Content-Type", "application/atom+xml;type=entry");
                resp.setHeader("Location", location.toString());
                Entry responseEntry = receipt.getAbderaEntry();
                responseEntry.writeTo(resp.getWriter());
                resp.getWriter().flush();
            } else {
                resp.setHeader("Location", location.toString());
            }
        } catch (SwordError se) {
            // get rid of any temp files used
            this.cleanup(deposit);

            this.swordError(req, resp, se);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // get rid of any temp files used
            this.cleanup(deposit);

            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } finally {
            // get rid of any temp files used
            this.cleanup(deposit);
        }
    }

    public void delete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.delete(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            auth = this.getAuthCredentials(req);
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
        }

        try {
            String editMediaIRI = this.getFullUrl(req);

            // delegate to the implementation
            this.mrm.deleteMediaResource(editMediaIRI, auth, this.config);

            // no response is expected, if no errors get thrown then we just return a success: 204 No Content
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SwordError se) {
            this.swordError(req, resp, se);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    protected void addGenerator(final DepositReceipt doc, final SwordConfiguration config) {
        Element generator = this.getGenerator(this.config);
        if (generator != null) {
            doc.getWrappedEntry().addExtension(generator);
        }
    }
}
