package org.sonar.template.java;

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static org.fest.assertions.Assertions.assertThat;

public class JavaCustomRulesDefinitionTest {

  @Test
  public void registration_test() {
    JavaCustomRulesDefinition definition = new JavaCustomRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository("use-fastdateformat");

    assertThat(repository.key()).isEqualTo("use-fastdateformat");
    assertThat(repository.name()).isEqualTo("Use FastDateFormat");
    assertThat(repository.language()).isEqualTo("java");
    assertThat(repository.rules()).hasSize(JavaCustomRulesList.getChecks().size());
  }
}
