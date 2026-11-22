package com.cti.provedor.controller;

import com.cti.provedor.dto.AuthDtos.LoginResponse;
import com.cti.provedor.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Profile("login-local")
@RestController
@RequestMapping("/auth/login-local")
public class LoginLocalController {
    private static final String LOOPBACK = "127.0.0.1";
    private static final Pattern ENDERECO_LOCAL = Pattern.compile("(?:localhost|127\\.0\\.0\\.1):[0-9]{1,5}");
    private final AuthService auth;

    public LoginLocalController(AuthService auth, @Value("${server.address:}") String endereco) {
        if (!LOOPBACK.equals(endereco)) {
            throw new IllegalStateException("O perfil login-local exige server.address=127.0.0.1.");
        }
        this.auth = auth;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verificarDisponibilidade(HttpServletRequest request) {
        verificarOrigemLocal(request);
    }

    @PostMapping
    public LoginResponse entrar(HttpServletRequest request) {
        verificarOrigemLocal(request);
        return auth.entrarComoAdminLocal();
    }

    private void verificarOrigemLocal(HttpServletRequest request) {
        String host = request.getHeader("Host");
        String origem = request.getHeader("Origin");
        if (!LOOPBACK.equals(request.getRemoteAddr()) || !LOOPBACK.equals(request.getLocalAddr())
            || host == null || !ENDERECO_LOCAL.matcher(host).matches()
            || origem == null || !origem.startsWith("http://") || !ENDERECO_LOCAL.matcher(origem.substring(7)).matches()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Abra o frontend local para usar este acesso.");
        }
    }
}
