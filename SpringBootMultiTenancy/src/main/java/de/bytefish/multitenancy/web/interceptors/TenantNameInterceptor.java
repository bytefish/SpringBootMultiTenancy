package de.bytefish.multitenancy.web.interceptors;

import de.bytefish.multitenancy.core.ThreadLocalStorage;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TenantNameInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String tenantName = request.getHeader("X-TenantID");

        if(tenantName != null) {
            ThreadLocalStorage.setTenantName(tenantName);
        }

        return true;
    }
}
