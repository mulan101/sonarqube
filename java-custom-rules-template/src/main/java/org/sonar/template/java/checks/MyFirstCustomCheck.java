package org.sonar.template.java.checks;

import com.google.common.collect.ImmutableList;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.List;

@Rule(
		  key = "UseFastDateFormatV2",
		  name = "Use FastDateFormat instead of SimpleDateFormat as it is faster ans safer",
		  description = "FastDateFormat is much faster and thread¡©safe than latter."
		  		+ "Read more at https://commons.apache.org/proper/commons-"
		  		+ "lang/apidocs/org/apache/commons/lang3/time/FastDateFormat.html",
		  priority = Priority.MAJOR,
		  tags = {"aem"})

public class MyFirstCustomCheck extends IssuableSubscriptionVisitor {

	@Override
	public List<Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.NEW_CLASS);
	}

	@Override
	public void visitNode(Tree tree) {
		if (hasSemantic()) {
			NewClassTree nct = (NewClassTree) tree;
			if (nct.symbolType().is("java.text.SimpleDateFormat")) {
				reportIssue(tree, "Use FastDateFormat instead");
			}
		}
	}
}
