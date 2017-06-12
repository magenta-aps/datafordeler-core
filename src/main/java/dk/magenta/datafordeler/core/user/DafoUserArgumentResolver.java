package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.stereotypes.DafoUser;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by jubk on 12-06-2017.
 */
@Component
public class DafoUserArgumentResolver implements HandlerMethodArgumentResolver {

  @Autowired
  public void addToMvc(WebMvcConfigurerAdapter configurer) {
    configurer.addArgumentResolvers(Collections.singletonList(this));
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterAnnotation(DafoUser.class) != null
        && methodParameter.getParameterType().equals(DafoUserDetails.class);
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter,
      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    if (this.supportsParameter(methodParameter)) {
      // TODO: Implement this!
      return new DafoUserDetails();
    } else {
      return WebArgumentResolver.UNRESOLVED;
    }
  }
}
