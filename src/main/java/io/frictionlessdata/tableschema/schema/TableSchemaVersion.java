package io.frictionlessdata.tableschema.schema;

import com.networknt.schema.*;

import java.util.Arrays;

/**
 * The frictionless table-schema.json does not really adher to the V4 JSON schema specification.
 * This class tells the networknt validator to ignore a couple of keywords that are not part of the V4 spec.
 */
public class TableSchemaVersion implements JsonSchemaVersion {
    private static final String IRI = SchemaId.V4;
    private static final String ID = "$id";

    private static class Holder {
        private static final JsonMetaSchema INSTANCE;
        static {
            JsonMetaSchema.Builder builder = JsonMetaSchema.builder(IRI);
            builder.specification(SpecVersion.VersionFlag.V4);
            builder.idKeyword(ID);
            builder.formats(Formats.DEFAULT);
            builder.keywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V4));
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

    @Override
    public JsonMetaSchema getInstance() {
        return Holder.INSTANCE;
    }
}
