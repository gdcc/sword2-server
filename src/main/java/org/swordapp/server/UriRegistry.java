package org.swordapp.server;

import javax.xml.namespace.QName;

public final class UriRegistry {
    // Helper class - make constructor invisible.
    private UriRegistry() { }

    // Namespace prefixes
    public static final String SWORD_PREFIX = "sword";
    public static final String ORE_PREFIX = "ore";
    public static final String APP_PREFIX = "app";
    public static final String DC_PREFIX = "dcterms";
    public static final String ATOM_PREFIX = "atom";

    // Namespaces
    public static final String SWORD_TERMS_NAMESPACE = "http://purl.org/net/sword/terms/";
    public static final String APP_NAMESPACE = "http://www.w3.org/2007/app";
    public static final String DC_NAMESPACE = "http://purl.org/dc/terms/";
    public static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
    public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

    // QNames for Extension Elements
    public static final QName SWORD_VERSION = new QName(SWORD_TERMS_NAMESPACE, "version");
    public static final QName SWORD_MAX_UPLOAD_SIZE = new QName(SWORD_TERMS_NAMESPACE, "maxUploadSize");
    public static final QName SWORD_COLLECTION_POLICY = new QName(SWORD_TERMS_NAMESPACE, "collectionPolicy");
    public static final QName SWORD_MEDIATION = new QName(SWORD_TERMS_NAMESPACE, "mediation");
    public static final QName SWORD_TREATMENT = new QName(SWORD_TERMS_NAMESPACE, "treatment");
    public static final QName SWORD_ACCEPT_PACKAGING = new QName(SWORD_TERMS_NAMESPACE, "acceptPackaging");
    public static final QName SWORD_SERVICE = new QName(SWORD_TERMS_NAMESPACE, "service");
    public static final QName SWORD_PACKAGING = new QName(SWORD_TERMS_NAMESPACE, "packaging");
    public static final QName SWORD_VERBOSE_DESCRIPTION = new QName(SWORD_TERMS_NAMESPACE, "verboseDescription");
    public static final QName APP_ACCEPT = new QName(APP_NAMESPACE, "accept");
    public static final QName DC_ABSTRACT = new QName(DC_NAMESPACE, "abstract");

    // URIs for the statement
    public static final String SWORD_DEPOSITED_BY_URI = SWORD_TERMS_NAMESPACE + "depositedBy";
    public static final String SWORD_DEPOSITED_ON_BEHALF_OF_URI = SWORD_TERMS_NAMESPACE + "depositedOnBehalfOf";
    public static final String SWORD_DEPOSITED_ON_URI = SWORD_TERMS_NAMESPACE + "depositedOn";
    public static final String SWORD_ORIGINAL_DEPOSIT_URI = SWORD_TERMS_NAMESPACE + "originalDeposit";
    public static final String SWORD_STATE_DESCRIPTION_URI = SWORD_TERMS_NAMESPACE + "stateDescription";
    public static final String SWORD_STATE_URI = SWORD_TERMS_NAMESPACE + "state";

    // QNames for statement elements
    public static final QName SWORD_DEPOSITED_BY = new QName(SWORD_TERMS_NAMESPACE, "depositedBy");
    public static final QName SWORD_DEPOSITED_ON_BEHALF_OF = new QName(SWORD_TERMS_NAMESPACE, "depositedOnBehalfOf");
    public static final QName SWORD_DEPOSITED_ON = new QName(SWORD_TERMS_NAMESPACE, "depositedOn");
    public static final QName SWORD_ORIGINAL_DEPOSIT = new QName(SWORD_TERMS_NAMESPACE, "originalDeposit");
    public static final QName SWORD_STATE_DESCRIPTION = new QName(SWORD_TERMS_NAMESPACE, "stateDescription");
    public static final QName SWORD_STATE = new QName(SWORD_TERMS_NAMESPACE, "state");

    // rel values
    public static final String REL_STATEMENT = "http://purl.org/net/sword/terms/statement";
    public static final String REL_SWORD_EDIT = "http://purl.org/net/sword/terms/add";
    public static final String REL_ORIGINAL_DEPOSIT = "http://purl.org/net/sword/terms/originalDeposit";
    public static final String REL_DERIVED_RESOURCE = "http://purl.org/net/sword/terms/derivedResource";

    // Package Formats
    public static final String PACKAGE_SIMPLE_ZIP = "http://purl.org/net/sword/package/SimpleZip";
    public static final String PACKAGE_BINARY = "http://purl.org/net/sword/package/Binary";

    // Error Codes
    public static final String ERROR_BAD_REQUEST = "http://purl.org/net/sword/error/ErrorBadRequest";
    public static final String ERROR_CONTENT = "http://purl.org/net/sword/error/ErrorContent";
    public static final String ERROR_CHECKSUM_MISMATCH = "http://purl.org/net/sword/error/ErrorChecksumMismatch";
    public static final String ERROR_TARGET_OWNER_UNKNOWN = "http://purl.org/net/sword/error/TargetOwnerUnknown";
    public static final String ERROR_MEDIATION_NOT_ALLOWED = "http://purl.org/net/sword/error/MediationNotAllowed";
    public static final String ERROR_METHOD_NOT_ALLOWED = "http://purl.org/net/sword/error/MethodNotAllowed";
    public static final String ERROR_MAX_UPLOAD_SIZE_EXCEEDED = "http://purl.org/net/sword/error/MaxUploadSizeExceeded";
}
