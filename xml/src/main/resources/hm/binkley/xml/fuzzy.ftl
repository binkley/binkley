package ${package};

/**
 * {@code ${simpleName}Factory} creates new instances of the {@code ${simpleName}} interface.
 * <p>
 * Instance creation extracts and converts values from an XML node, saving the typed results to
 * {@code final} fields.  Implemnted methods return the field values.  The instances are
 * immutable.
 */
@javax.annotation.Generated(value="${generator}", date="${date}")
public final class ${simpleName}Factory
        implements ${simpleName} {
<#list methods as method>
    private final ${method.returnType} ${method.simpleName};
</#list>

    /**
     * Creates a new {@code ${simpleName} for the given XML <var>node</var>.
     * <p>
     * If XML parsing or field conversion fails, attaches a suppressed exception describing the
     * field and XPath.
     *
     * @param node the XML node, never missing
     *
     * @return the new {@code ${simpleName}, never missing
     * @throws java.lang.Exception if XML parsing or field conversion fails
     */
    public static ${simpleName} of(@javax.annotation.Nonnull final org.w3c.dom.Node node) throws java.lang.Exception {
        return new ${simpleName}Factory(node);
    }

    private ${simpleName}Factory(final org.w3c.dom.Node node) throws java.lang.Exception {
<#list methods as method>
        try {
            final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "${method.xpath}");
            if ("".equals($value))
    <#if method.nullable>
                this.${method.simpleName} = null;
    <#else>
                throw new java.lang.NullPointerException();
    </#if>
            else
    <#if "java.lang.String" == method.returnType>
                this.${method.simpleName} = $value;
    <#else>
                this.${method.simpleName} = ${method.converter}($value);
    </#if>
        } catch (final java.lang.Exception $e) {
            $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "${simpleName}::${method.simpleName}", "${method.xpath}")));
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
