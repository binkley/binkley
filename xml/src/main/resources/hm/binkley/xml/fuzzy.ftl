package ${package};

public final class ${simpleName}Factory
        implements ${simpleName} {
<#list methods as method>
    private final ${method.returnType} ${method.simpleName};
</#list>

    public static ${simpleName} of(@javax.annotation.Nonnull final org.w3c.dom.Node node) throws java.lang.Exception {
        return new ${simpleName}Factory(node);
    }

    private ${simpleName}Factory(final org.w3c.dom.Node node) throws java.lang.Exception {
<#list methods as method>
        try {
<#if "java.lang.String" == method.returnType>
            this.${method.simpleName} = hm.binkley.xml.FuzzyProcessor.evaluate(node, "${method.xpath}");
<#else>
            this.${method.simpleName} = ${method.converter}(hm.binkley.xml.FuzzyProcessor.evaluate(node, "${method.xpath}"));
</#if>
        } catch (final java.lang.Exception $e) {
            $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "${simpleName}::${method.simpleName}", "${method.xpath}")));
            <#--$e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s: %s", "${simpleName}::${method.simpleName}", "${method.xpath}", node.getTextContent())));-->
            throw $e;
        }
</#list>
    }

<#list methods as method>
    @Override public ${method.returnType} ${method.simpleName}() { return ${method.simpleName}; }
</#list>
}
