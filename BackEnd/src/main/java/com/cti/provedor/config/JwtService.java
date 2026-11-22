package com.cti.provedor.config;

import com.cti.provedor.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

@Service // Responsável por assinar e validar JWTs.
public class JwtService {
    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(AppProperties properties, Environment ambiente) {
        var jwt = properties.jwt();
        // Uma sessão sem senha só é válida nesta execução local.
        this.chave = ambiente.acceptsProfiles(Profiles.of("login-local"))
            ? Jwts.SIG.HS256.key().build()
            : Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.secretBase64()));
        if (jwt.expirationMinutes() <= 0) throw new IllegalArgumentException("Expiracao JWT deve ser positiva");
        this.expiracaoMinutos = jwt.expirationMinutes();
    }

    public long getExpiraEmSegundos() { return expiracaoMinutos * 60; }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail()) // Identidade incluída no token.
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoMinutos * 60)))
                .signWith(chave, Jwts.SIG.HS256) // Assinatura HMAC SHA-256.
                .compact();
    }

    public String extrairEmailValidado(String token) {
        // Rejeita assinaturas inválidas e tokens expirados; obtém o subject.
        Claims claims = Jwts.parser().verifyWith(chave).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
