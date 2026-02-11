package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for test methods to provide explicit OpenAPI metadata.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DocumentApi {
  
  /**
   * Explicit OpenAPI path pattern override.
   */
  String path() default "";
  
  /**
   * Operation summary.
   */
  String summary() default "";
  
  /**
   * Operation description.
   */
  String description() default "";
  
  /**
   * Tags for grouping operations.
   */
  String[] tags() default {};
}
