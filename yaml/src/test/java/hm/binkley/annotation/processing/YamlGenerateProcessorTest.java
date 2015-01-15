package hm.binkley.annotation.processing;

import hm.binkley.annotation.YamlGenerate;

/**
 * {@code YamlGenerateProcessorTest} tests {@link YamlGenerateProcessor}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
@YamlGenerate(template = "/foo/test.ftl",
        inputs = {"/foo/*.yml", "/bar/3.yml"}, namespace = "fooby")
public interface YamlGenerateProcessorTest {}
