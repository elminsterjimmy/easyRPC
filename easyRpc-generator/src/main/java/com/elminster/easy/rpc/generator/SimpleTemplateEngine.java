package com.elminster.easy.rpc.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.elminster.common.util.FileUtil;

public class SimpleTemplateEngine {
  
  public static final String VARIABLE_DEFINITION_TEMPLATE = "VariableDefinitionTemplate.tpl";
  
  private static Map<String, String> templateMap = new HashMap<>();
  
  static {
    loadTemplateMap();
  }

  public static String renderTemplate(String templateName, Map<String, String> data) {
    String template = templateMap.get(templateName);
    if (null != data) {
      for (Map.Entry<String, String> entry : data.entrySet()) {
        template = template.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
      }
    }
    return template;
  }

  private static void loadTemplateMap() {
    try {
      loadTempalte(VARIABLE_DEFINITION_TEMPLATE);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static void loadTempalte(String variableDefinitionTemplate) throws IOException {
    String content = FileUtil.readFile2String(SimpleTemplateEngineTest.class.getClassLoader().getResourceAsStream(variableDefinitionTemplate));
    templateMap.put(variableDefinitionTemplate, content);
  }
}
