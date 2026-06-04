package io.frictionlessdata.tableschema.schema;

import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Draft4;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;

import java.util.Arrays;

/**
 * The frictionless table-schema.json does not really follow the V4 JSON schema specification.
 * This class tells the networknt validator to ignore a couple of keywords that are not part of the V4 spec.
 */
public class TableSchemaVersion {
    private static final String IRI = SpecificationVersion.DRAFT_4.getDialectId();
    private static final String ID = "$id";

    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            Dialect.Builder builder = Dialect.builder(IRI, Draft4.getInstance());
            builder.specificationVersion(SpecificationVersion.DRAFT_4);
            builder.idKeyword(ID);
            builder.keywords(Arrays.asList(
                    new NonValidationKeyword("$schema"),
                    new NonValidationKeyword("id"),
                    new AnnotationKeyword("title"),
                    new AnnotationKeyword("description"),
                    new AnnotationKeyword("default"),
                    new NonValidationKeyword("definitions"),
                    new NonValidationKeyword("$comment"),
                    new AnnotationKeyword("examples"),
                    new NonValidationKeyword("then"),
                    new NonValidationKeyword("else"),
                    new NonValidationKeyword("FIXME"),
                    new NonValidationKeyword("TODO"),
                    new NonValidationKeyword("context"),
                    new NonValidationKeyword("notes"),
                    new NonValidationKeyword("options"),
                    new NonValidationKeyword("propertyOrder"),
                    new NonValidationKeyword("additionalItems")));// keywords that may validly exist, but have no validation aspect to them
            INSTANCE = builder
                    .build();
        }
    }

    public Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
