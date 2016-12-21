package com.smithies.es09.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchIndexHelper {
  final Map<String, Object> properties = new HashMap<String, Object>();

  public Map<String, Object> withTypeAndBase(ElasticSearchType type) {
    final Map<String, Object> typeProperties = new HashMap<String, Object>();
    typeProperties.put(type.getName(), withBase());
    return typeProperties;
  }

  public Map<String, Object> withBase() {
    final Map<String, Object> base = new HashMap<String, Object>();
    base.put("properties", properties);
    return base;
  }

  public void addNotAnalysed(String fieldName) {
    final Map<String, Object> uuid = new HashMap<String, Object>();
    uuid.put("type", "string");
    uuid.put("index", "not_analyzed");
    properties.put(fieldName, uuid);
  }

  public void addNotAnalysed(String fieldName, String type) {
    final Map<String, Object> uuid = new HashMap<String, Object>();
    uuid.put("type", type);
    uuid.put("index", "not_analyzed");
    properties.put(fieldName, uuid);
  }

  public void addIndexEqualsNo(String fieldName) {
    final Map<String, Object> uuid = new HashMap<String, Object>();
    uuid.put("type", "string");
    uuid.put("index", "no");
    properties.put(fieldName, uuid);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void addNotAnalysedStructure(String type, String match, String templateName) {

    Map<String, Object> base = new HashMap<String, Object>();

    List<Map<String, Object>> templates = new ArrayList<Map<String, Object>>();
    base.put("dynamic_templates", templates);

    Map<String, Object> template1 = new HashMap<String, Object>();

    Map<String, Object> template1Content = new HashMap<String, Object>();
    // Map<String, Object> match = new HashMap<String, Object>();
    template1Content.put("match", match);
    template1Content.put("match_mapping_type", "string");
    Map<String, Object> mapping = new HashMap<String, Object>();
    template1Content.put("mapping", mapping);
    mapping.put("type", "string");
    mapping.put("index", "not_analyzed");
    template1.put(templateName, template1Content);
    templates.add(template1);
    properties.put(type, base);
  }

}
