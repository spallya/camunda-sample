package com.vider.quantum.engine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode convertListToJsonNode(List<String> list) {
        return objectMapper.valueToTree(list);
    }
}