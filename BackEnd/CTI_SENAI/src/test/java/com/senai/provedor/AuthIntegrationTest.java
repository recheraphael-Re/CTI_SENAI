package com.senai.provedor;
import com.senai.provedor.models.*;
import com.senai.provedor.repository.UsuarioRepository;
import com.senai.provedor.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:auth;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", "ADMIN_EMAIL=", "ADMIN_PASSWORD="
})
class AuthIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    HttpClient client = HttpClient.newHttpClient();
    @BeforeEach void limpar() { repository.deleteAll(); }
    HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    Usuario user(String email, Perfil perfil) {
        Usuario u = new Usuario(); u.setNome("Teste"); u.setEmail(email); u.setSenhaHash(encoder.encode("senha123"));
        u.setPerfil(perfil); u.setStatus(StatusUsuario.ATIVO); return repository.saveAndFlush(u);
    }
    @Test void cadastroLoginEAprovacao() throws Exception {
        String cadastro = "{\"nome\":\"Ana\",\"email\":\"Ana@example.com\",\"senha\":\"senha123\",\"perfil\":\"ADMIN\",\"status\":\"ATIVO\"}";
        var res = request("POST", "/auth/register", cadastro, null);
        assertEquals(201, res.statusCode()); assertTrue(res.body().contains("PENDENTE")); assertFalse(res.body().contains("senha"));
        Usuario ana = repository.findByEmailIgnoreCase("ana@example.com").orElseThrow();
        assertEquals(Perfil.USER, ana.getPerfil()); assertTrue(encoder.matches("senha123", ana.getSenhaHash()));
        var duplicado = request("POST", "/auth/register", cadastro, null);
        assertEquals(409, duplicado.statusCode()); assertTrue(duplicado.body().contains("EMAIL_JA_CADASTRADO"));
        String login = "{\"email\":\"ANA@example.com\",\"senha\":\"senha123\"}";
        assertEquals(403, request("POST", "/auth/login", login, null).statusCode());
        Usuario admin = user("admin@example.com", Perfil.ADMIN);
        assertEquals(200, request("PATCH", "/admin/usuarios/" + ana.getId() + "/status", "{\"status\":\"ATIVO\"}", jwt.gerar(admin)).statusCode());
        res = request("POST", "/auth/login", login, null); assertEquals(200, res.statusCode()); assertTrue(res.body().contains("token"));
        assertEquals(401, request("POST", "/auth/login", login.replace("senha123", "errada"), null).statusCode());
    }
    @Test void protegeRotasERevogaBloqueado() throws Exception {
        assertEquals(200, request("GET", "/api/publico", null, null).statusCode());
        assertEquals(401, request("GET", "/clientes", null, null).statusCode());
        assertEquals(401, request("GET", "/auth/me", null, "invalido").statusCode());
        Usuario u = user("user@example.com", Perfil.USER); String token = jwt.gerar(u);
        assertEquals(200, request("GET", "/auth/me", null, token).statusCode());
        assertEquals(403, request("GET", "/admin/usuarios", null, token).statusCode());
        assertEquals(403, request("DELETE", "/clientes/1", null, token).statusCode());
        u.setStatus(StatusUsuario.BLOQUEADO); repository.saveAndFlush(u);
        assertEquals(401, request("GET", "/auth/me", null, token).statusCode());
    }
    @Test void rejeitaTokenExpiradoAdulteradoESenhaLongaEmBytes() throws Exception {
        Usuario u = user("user@example.com", Perfil.USER);
        var key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="));
        String expired = io.jsonwebtoken.Jwts.builder().subject(u.getEmail())
            .expiration(java.util.Date.from(java.time.Instant.now().minusSeconds(120))).signWith(key).compact();
        assertEquals(401, request("GET", "/auth/me", null, expired).statusCode());
        String token = jwt.gerar(u); int dot = token.lastIndexOf('.') + 1;
        String adulterado = token.substring(0,dot) + (token.charAt(dot) == 'A' ? "B" : "A") + token.substring(dot+1);
        assertEquals(401, request("GET", "/auth/me", null, adulterado).statusCode());
        String senha = "\u00e9".repeat(40);
        assertEquals(400, request("POST", "/auth/register", "{\"nome\":\"Teste\",\"email\":\"novo@example.com\",\"senha\":\""+senha+"\"}", null).statusCode());
    }
    @Test void validaCamposCORSENaoBloqueiaProprioAdmin() throws Exception {
        assertEquals(400, request("POST", "/auth/register", "{}", null).statusCode());
        Usuario admin = user("admin@example.com", Perfil.ADMIN); String token = jwt.gerar(admin);
        assertEquals(409, request("PATCH", "/admin/usuarios/"+admin.getId()+"/status", "{\"status\":\"BLOQUEADO\"}", token).statusCode());
        var preflight = HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/auth/login"))
            .header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type,authorization").method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var res = client.send(preflight, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode()); assertEquals("http://localhost:5173", res.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
    }
}
