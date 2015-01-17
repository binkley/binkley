<#--
TODO: FreeMarker template fixes
    * Whitespace
    * Condense with macros
    * Recurive data types
    * Get rid of "type" for builtin types
-->
<#macro jvalue type value="\n">
    <#if value?is_string>
        <#if "\n" == value>null<#else>"${value}"</#if>
    <#elseif value?is_boolean || value?is_number>${value?c}<#if "double" == type>d</#if>
    <#else>${type}.valueOf("${value}")</#if>
</#macro>
<#if package?has_content>package ${package};

</#if>@javax.annotation.Generated(
    value="${generator}",
    date="${now}",
    comments="${comments}")
<#if type == "Enum">public enum ${name} {
    ${values?join(", ")};
}<#else>public class ${name}<#if parent??> extends ${parent}</#if> {
    // TODO: Remove blank line from first method when no fields
<#list data?keys as key>
    <#if data[key].value??><#if data[key].value?is_sequence>private final java.util.List<Object> ${key} = new java.util.ArrayList<>(${data[key].value?size});
    {
        <#list data[key].value as each>
        ${key}.add(<@jvalue type=data[key].type value=each/>);
        </#list>
    }
    <#elseif data[key].value?is_hash>private final java.util.Map<String, Object> ${key} = new java.util.HashMap<>(${data[key].value?size});
    {
        <#list data[key].value?keys as each>
        ${key}.put("${each}", <@jvalue type=data[key].type value=data[key].value[each]/>);
        </#list>
    }</#if></#if>
</#list>
<#list data?keys as key>

    <#if data[key].doc??>
    /** ${data[key].doc} */
    @hm.binkley.annotation.YamlGenerate.Doc("${data[key].doc}")</#if><#if data[key].override>
    @Override</#if>
    public <#if "text" == data[key].type>String<#elseif "list" == data[key].type>java.util.List<Object><#elseif "map" == data[key].type>java.util.Map<String, Object><#else>${data[key].type}</#if> ${key}() {
        return <#if data[key].value?? && (data[key].value?is_sequence || data[key].value?is_hash)>${key}
        <#else><@jvalue type=data[key].type value=data[key].value/></#if>;
    }
</#list>
}</#if>
