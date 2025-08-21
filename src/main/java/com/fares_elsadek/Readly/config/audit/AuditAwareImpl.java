package com.fares_elsadek.Readly.config.audit;

import com.fares_elsadek.Readly.utils.CustomUserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        try{
            CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(principal.userId());
        }catch (Exception ex){
            return Optional.of("Anonymouse");
        }
    }
}
