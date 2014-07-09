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
            final String $value = hm.binkley.xml.FuzzyProcessor.evaluate(node, "${method.xpath}");
<#if method.nullable>
            if ("".equals($value))
                this.${method.simpleName} = null;
            else
</#if>
<#if "java.lang.String" == method.returnType>
                this.${method.simpleName} = $value;
<#else>
                this.${method.simpleName} = ${method.converter}($value);
</#if>
        } catch (final java.lang.Exception $e) {
            $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "${simpleName}::${method.simpleName}", "${method.xpath}")));
            <#--$e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s: %s", "${simpleName}::${method.simpleName}", "${method.xpath}", node.getTextContent())));-->
            throw $e;
        }
</#list>
    }

<#list methods as method>
<#if !method.nullable>
    @javax.annotation.Nonnull
</#if>
    @Override
    public ${method.returnType} ${method.simpleName}() { return ${method.simpleName}; }
</#list>
}
