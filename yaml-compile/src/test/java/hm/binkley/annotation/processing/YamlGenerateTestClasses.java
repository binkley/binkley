package hm.binkley.annotation.processing;

import hm.binkley.annotation.YamlGenerate;

/**
 * {@code YamlGenerateTestClasses} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@SuppressWarnings("all") // Unrelated annos do not croak the processor
@YamlGenerate(inputs = {"/foo/*.yml", "classpath:/bar/3.yml",
        "classpath:/bar/inheritance.yml"},
        namespace = "fooby")
public interface YamlGenerateTestClasses {
    default int someCommonThing() { return -42; }
}
