package hm.binkley.xml;/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

import org.intellij.lang.annotations.Language;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code XMLFuzzy} marks an interface for code generation by {@link XMLFuzzyProcessor}.  Given an
 * interface named {@code Foo} annotated with {@code XMLFuzzy} and methods marked with {@link
 * Field}, the result is a class named {@code FooFactory} with an {@code of(org.w3c.dom.Document)}
 * static factory method which maps XPaths into the document onto return values for the methods.
 * <p>
 * Example: <pre>&#64;XMLFuzzy
 * public interface WasHe {
 *     &#64;XMLFuzzy.Field("//wasHe/needsNoConversion/text()")
 *     String needsNoConversion();
 *
 *     &#64;XMLFuzzy.Field("//wasHe/isAPrimitive/text()")
 *     int isAPrimitive();
 *
 *     &#64;XMLFuzzy.Field("//wasHe/usesParse/text()")
 *     Instant usesParse();
 *
 *     &#64;XMLFuzzy.Field("//wasHe/usesConstructor/text()")
 *     BigDecimal usesConstructor();
 *
 *     &#64;XMLFuzzy.Field("//wasHe/nullOk/text()")
 *     String nullOk();
 *
 *     &#64;XMLFuzzy.Field("//wasHe/throwsAnException/text()")
 *     &#64;Nonnull
 *     URI throwsAnException();
 * }</pre> then {&#64;code XMLFuzzyProcessor} generates this class: <pre>public final class WasHeFactory
 *         implements WasHe {
 *     private final java.lang.String needsNoConversion;
 *     private final int isAPrimitive;
 *     private final java.time.Instant usesParse;
 *     private final java.math.BigDecimal usesConstructor;
 *     private final java.lang.String nullOk;
 *     private final java.net.URI throwsAnException;
 *
 *     public static WasHe of(&#64;javax.annotation.Nonnull final org.w3c.dom.Node node) throws java.lang.Exception {
 *         return new WasHeFactory(node);
 *     }
 *
 *     private WasHeFactory(final org.w3c.dom.Node node) throws java.lang.Exception {
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/needsNoConversion/text()");
 *             if ("".equals($value))
 *                 this.needsNoConversion = null;
 *             else
 *                 this.needsNoConversion = $value;
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::needsNoConversion", "//wasHe/needsNoConversion/text()")));
 *             throw $e;
 *         }
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/isAPrimitive/text()");
 *             this.isAPrimitive = java.lang.Integer.valueOf($value);
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::isAPrimitive", "//wasHe/isAPrimitive/text()")));
 *             throw $e;
 *         }
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/usesParse/text()");
 *             if ("".equals($value))
 *                 this.usesParse = null;
 *             else
 *                 this.usesParse = java.time.Instant.parse($value);
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::usesParse", "//wasHe/usesParse/text()")));
 *             throw $e;
 *         }
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/usesConstructor/text()");
 *             if ("".equals($value))
 *                 this.usesConstructor = null;
 *             else
 *                 this.usesConstructor = new java.math.BigDecimal($value);
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::usesConstructor", "//wasHe/usesConstructor/text()")));
 *             throw $e;
 *         }
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/nullOk/text()");
 *             if ("".equals($value))
 *                 this.nullOk = null;
 *             else
 *                 this.nullOk = $value;
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::nullOk", "//wasHe/nullOk/text()")));
 *             throw $e;
 *         }
 *         try {
 *             final String $value = hm.binkley.xml.XMLFuzzyProcessor.evaluate(node, "//wasHe/throwsAnException/text()");
 *             this.throwsAnException = new java.net.URI($value);
 *         } catch (final java.lang.Exception $e) {
 *             $e.addSuppressed(new java.lang.Exception(java.lang.String.format("%s: %s", "WasHe::throwsAnException", "//wasHe/throwsAnException/text()")));
 *             throw $e;
 *         }
 *     }
 *
 *     &#64;Override
 *     public java.lang.String needsNoConversion() { return needsNoConversion; }
 *
 *     &#64;javax.annotation.Nonnull
 *     &#64;Override
 *     public int isAPrimitive() { return isAPrimitive; }
 *
 *     &#64;Override
 *     public java.time.Instant usesParse() { return usesParse; }
 *
 *     &#64;Override
 *     public java.math.BigDecimal usesConstructor() { return usesConstructor; }
 *
 *     &#64;Override
 *     public java.lang.String nullOk() { return nullOk; }
 *
 *     &#64;javax.annotation.Nonnull
 *     &#64;Override
 *     public java.net.URI throwsAnException() { return throwsAnException; }
 * }</pre>
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @see XMLFuzzyProcessor
 * @see Field
 * @see <a href="http://xml.org/">XMLBeam</a>
 * @see <a href="http://en.wikipedia.org/wiki/Little_Fuzzy"><cite>Little Fuzzy</cite></a>
 */
@Documented
@Inherited
@Retention(SOURCE)
@Target(TYPE)
public @interface XMLFuzzy {
    /**
     * Marks interface methods with XPath expressions to extract method values from an XML
     * node.Assigns missing values {@code null} unless marked with {@link Nonnull} in which case
     * they throw {@code NullPointerException}.
     * <p>
     * Primitive return values are always treated as if marked with {@code
     * javax.annotation.Nonnull}.  Void returns cause compilation errors.
     */
    @Documented
    @Inherited
    @Retention(SOURCE)
    @Target(METHOD)
    @interface Field {
        @Language("XPath") String value();
    }
}
