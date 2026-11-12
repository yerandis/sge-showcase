package com.yerandis.sge.service;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.security.LoginAttemptService;
import com.yerandis.sge.service.serviceImpl.security.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private LoginAttemptService loginAttemptService;
    @InjectMocks private AuthService authService;

    @Test
    void login_conCredencialesInvalidas_lanzaBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("incorrecta");

        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incorrectos");

        verify(loginAttemptService).recordFailure("admin");
    }

    @Test
    void login_conUsuarioBloqueado_lanzaBusinessExceptionSinIntentarAutenticar() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("cualquiera");

        when(loginAttemptService.isBlocked("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(authenticationManager);
    }
}
