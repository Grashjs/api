package com.grash.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.grash.model.Customer;
import com.grash.model.OwnUser;
import com.grash.model.abstracts.Worker;

import java.io.IOException;

public class WorkerDeserializer extends JsonDeserializer<Worker> {

    @Override
    public Worker deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode tree = codec.readTree(p);

        if (tree.has("isUser")) {
            return codec.treeToValue(tree, OwnUser.class);
        } else return codec.treeToValue(tree, Customer.class);

    }
}
