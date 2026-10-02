package no.entur.nuska.config;

import no.entur.nuska.UnknownQueryParameterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  public static final String[] QUERY_PARAMETER_CHECK_INCLUDED_PATHS = {
    "/timetable-data/**",
  };

  /**
   * The OpenAPI spec is fetched by external tools that may append cache-busting or tracking
   * query parameters.
   */
  public static final String[] QUERY_PARAMETER_CHECK_EXCLUDED_PATHS = {
    "/timetable-data/openapi.yaml",
  };

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
      .addInterceptor(new UnknownQueryParameterInterceptor())
      .addPathPatterns(QUERY_PARAMETER_CHECK_INCLUDED_PATHS)
      .excludePathPatterns(QUERY_PARAMETER_CHECK_EXCLUDED_PATHS);
  }
}
