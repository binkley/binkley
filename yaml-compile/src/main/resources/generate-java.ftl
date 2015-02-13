<#macro rvalue type value="\n"><@compress>
    <#if value?is_string>
        <#if "\n" == value>null<#else>"${value}"</#if>
    <#elseif value?is_boolean || value?is_number>${value?c}<#if "float" == type>d</#if>
    <#else>${type}.valueOf("${value}")</#if>
</@compress></#macro>
<#macro lvalue type><@compress>
    <#if "str" == type>String
    <#-- int is int -->
    <#elseif "bool" == type>boolean
    <#elseif "float" == type>double
    <#elseif "seq" == type>
    java.util.List<Object>
    <#elseif "pairs" == type>
    java.util.Map<String, Object>
    <#else>${type}</#if>
</@compress></#macro>
<#macro defn prefix pairs>
${prefix}@hm.binkley.annotation.YamlGenerate.Definition({<#if pairs?has_content>
<#list pairs as each>${prefix}        ${each}<#if each_has_next>,
</#if></#list></#if>})
</#macro>
<#if package?has_content>
package ${package};

</#if>
<#if doc??>
/** ${doc} */
@hm.binkley.annotation.YamlGenerate.Documentation("${escapedDoc}")
</#if>
@javax.annotation.Generated(
        value="${generator}",
        date="${now}",
        comments="${comments}")
<@defn prefix="" pairs=definition/>
<#if type == "Enum">
public enum ${name} {
<#list values?keys as value>
    <#if values[value].doc??>
    /** ${values[value].doc} */
    <#else>
    /**
    * @{code ${value}} is undocumented.
    *
    * @todo Documentation
    */
    </#if>
    <@defn prefix="    " pairs=values[value].definition/>
    ${value}<#if value_has_next>,<#else>;</#if>
</#list>
<#else>
public class ${name}<#if parent??> <#if 'class' == parentKind>extends<#else>implements</#if> ${parent}</#if> {
<#list methods?keys as key>
<#assign has_init = false/>
<#if methods[key].value??>
<#if methods[key].value?is_sequence>
    <#assign has_init = true/>
    private final java.util.List<Object> ${key} = new java.util.ArrayList<>(${methods[key].value?size});
    <#if methods[key].value?has_content>
    {
        <#list methods[key].value as each>
        ${key}.add(<@rvalue type=each.type value=each.value/>);
        </#list>
    }
    </#if>
<#elseif methods[key].value?is_hash>
    <#assign has_init = true/>
    private final java.util.Map<String, Object> ${key} = new java.util.LinkedHashMap<>(${methods[key].value?size});
    <#if methods[key].value?has_content>
    {
        <#list methods[key].value?keys as eKey>
        <#assign each=methods[key].value[eKey]/>
        ${key}.put("${eKey}", <@rvalue type=each.type value=each.value/>);
        </#list>
    }
</#if>
</#if>
</#if>
</#list>
<#list methods?keys as key>
    <#if has_init || 0 != key_index>

    </#if>
    <#if methods[key].doc??>
    /** ${methods[key].doc} */
    @hm.binkley.annotation.YamlGenerate.Documentation("${methods[key].escapedDoc}")
    </#if>
    <#if methods[key].override>
    @Override
    </#if>
    <@defn prefix="    " pairs=methods[key].definition/>
    public <@lvalue methods[key].type/> ${methods[key].name}() {
        return <#if methods[key].value?? && (methods[key].value?is_sequence || methods[key].value?is_hash)>${key}<#else><@rvalue type=methods[key].type value=methods[key].value/></#if>;
    }
</#list>
</#if>
}
