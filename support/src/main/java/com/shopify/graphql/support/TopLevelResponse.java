package com.shopify.graphql.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by eapache on 2015-11-17.
 */
public class TopLevelResponse {
    private static final String DATA_KEY = "data";
    private static final String ERRORS_KEY = "errors";
    private JsonObject data = null;
    private final List<Error> errors = new ArrayList<>();

    public TopLevelResponse(JsonObject fields) throws InvalidGraphQLException {
        JsonElement errorsElement = fields.get(ERRORS_KEY);
        JsonElement dataElement = fields.get(DATA_KEY);
        JsonObject dataCustom = fields.getAsJsonObject(DATA_KEY);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(fields.get(DATA_KEY).toString());
        JsonObject obj = element.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
        
        for (Map.Entry<String, JsonElement> entry: entries) {
            try {
                errorsElement = entry.getValue().getAsJsonObject().getAsJsonArray("userErrors");
            }
            catch (IllegalStateException e){
                continue;
            }

        }
        if (dataElement != null && dataElement.isJsonNull()) {
            dataElement = null;
        }

        if (errorsElement == null && dataElement == null) {
            throw new InvalidGraphQLException("Response must contain a top-level 'data' or 'errors' entry");
        }

        if (dataElement != null) {
            if (!dataElement.isJsonObject()) {
                throw new InvalidGraphQLException("'data' entry in response must be a map");
            }
            this.data = dataElement.getAsJsonObject();
        }

        if (errorsElement != null) {
            if (!errorsElement.isJsonArray()) {
                throw new InvalidGraphQLException("'errors' entry in response must be an array");
            }
            for (JsonElement error : errorsElement.getAsJsonArray()) {
                errors.add(new Error(error.isJsonObject() ? error.getAsJsonObject() : new JsonObject()));
            }
        }
    }

    public JsonObject getData() {
        return data;
    }

    public List<Error> getErrors() {
        return errors;
    }
}
