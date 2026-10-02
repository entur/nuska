package no.entur.nuska;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Reject requests carrying query parameters that are not declared by the target endpoint.
 * <p>
 * Spring silently ignores unknown query parameters, and parameter names are case-sensitive: a
 * client sending {@code dsjCompatibility} instead of {@code dsjcompatibility} would otherwise
 * receive the default variant of the dataset without noticing the mistake.
 */
public class UnknownQueryParameterInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }
    MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
    if (acceptsAnyParameter(methodParameters)) {
      return true;
    }
    Set<String> declaredParameters = Arrays
      .stream(methodParameters)
      .filter(p -> p.hasParameterAnnotation(RequestParam.class))
      .map(UnknownQueryParameterInterceptor::parameterName)
      .collect(Collectors.toSet());

    for (String parameter : request.getParameterMap().keySet()) {
      if (!declaredParameters.contains(parameter)) {
        throw new BadRequestException(
          "Unknown query parameter: '" + parameter + "'"
        );
      }
    }
    return true;
  }

  /**
   * A {@code @RequestParam Map} binds all query parameters, there is nothing to validate.
   */
  private static boolean acceptsAnyParameter(MethodParameter[] parameters) {
    return Arrays
      .stream(parameters)
      .anyMatch(p ->
        p.hasParameterAnnotation(RequestParam.class) &&
        Map.class.isAssignableFrom(p.getParameterType())
      );
  }

  private static String parameterName(MethodParameter parameter) {
    RequestParam requestParam = parameter.getParameterAnnotation(
      RequestParam.class
    );
    if (requestParam != null && !requestParam.name().isEmpty()) {
      return requestParam.name();
    }
    return parameter.getParameterName();
  }
}
