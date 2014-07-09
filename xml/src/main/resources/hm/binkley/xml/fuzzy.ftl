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
<#if "java.lang.String" == method.returnType>
        this.${method.simpleName} = hm.binkley.xml.FuzzyProcessor.evaluate(node, "${method.xpath}");
<#else>
        this.${method.simpleName} = ${method.converter}(hm.binkley.xml.FuzzyProcessor.evaluate(node, "${method.xpath}"));
</#if>
</#list>
    }

<#list methods as method>
    @Override public ${method.returnType} ${method.simpleName}() { return ${method.simpleName}; }
</#list>
}
