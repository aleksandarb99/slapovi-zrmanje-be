package com.slapovizrmanje.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CorsInterceptor implements HandlerInterceptor {
  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                         final Object handler, final ModelAndView modelAndView) throws Exception {
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

}
