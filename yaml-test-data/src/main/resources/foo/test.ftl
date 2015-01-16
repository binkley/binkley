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
        ${key}.add(<#if each?is_string>"${each}"<#elseif each?is_boolean || each?is_number>${each?c}<#if "double" == data[key].type>d</#if><#else>${data[key].type}.valueOf("${each}")</#if>);
        </#list>
    }
    <#elseif data[key].value?is_hash>private final java.util.Map<String, Object> ${key} = new java.util.HashMap(${data[key].value?size});
    {
        <#list data[key].value?keys as each>
        ${key}.put("${each}", <#if data[key].value[each]?is_string>"${data[key].value[each]}"<#elseif data[key].value[each]?is_boolean || data[key].value[each]?is_number>${data[key].value[each]?c}<#if "double" == data[key].type>d</#if><#else>${data[key].type}.valueOf("${data[key].value[each]}")</#if>);
        </#list>
    }</#if></#if>
</#list>
<#list data?keys as key>

    <#if data[key].doc??>
    /** ${data[key].doc} */
    @hm.binkley.annotation.YamlGenerate.Doc("${data[key].doc}")
</#if>    public <#if "text" == data[key].type>String<#elseif "list" == data[key].type>java.util.List<Object><#elseif "map" == data[key].type>java.util.Map<String, Object><#else>${data[key].type}</#if> ${key}() {
        <#if !data[key].value??>return null;
        <#elseif data[key].value?is_string>return "${data[key].value}";
        <#elseif data[key].value?is_sequence || data[key].value?is_hash>return ${key};
        <#else>return ${data[key].value?c}<#if "double" = data[key].type>d</#if>;</#if>
    }
</#list>
}</#if>
