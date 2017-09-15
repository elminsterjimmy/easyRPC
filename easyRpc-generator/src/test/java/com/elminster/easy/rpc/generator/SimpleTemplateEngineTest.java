package com.elminster.easy.rpc.generator;

import org.junit.Assert;
import org.junit.Test;


public class SimpleTemplateEngineTest {

  @Test
  public void testTemplateLoad() {
    Assert.assertEquals("  ${type} ${name};\r\n", SimpleTemplateEngine.renderTemplate(SimpleTemplateEngine.VARIABLE_DEFINITION_TEMPLATE, null));
  }
}
