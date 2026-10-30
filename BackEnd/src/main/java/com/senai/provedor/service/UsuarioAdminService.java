package com.senai.provedor.service;
import com.senai.provedor.models.*;
import com.senai.provedor.dto.*;
import com.senai.provedor.repository.UsuarioRepository;
import com.senai.provedor.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class UsuarioAdminService {
    private final UsuarioRepository repository;
    public UsuarioAdminService(UsuarioRepository repository) { this.repository = repository; }
    public List<UsuarioResponse> listar() { return repository.findAll().stream().map(UsuarioResponse::de).toList(); }
    @Transactional
    public UsuarioResponse alterar(Long id, StatusUsuario status, Perfil perfil, String emailAdmin) {
        // Serializa alteracoes de status para proteger o ultimo administrador ativo.
        List<Usuario> usuarios = repository.buscarParaAdministracao();
        Usuario alvo = usuarios.stream().filter(u -> u.getId().equals(id)).findFirst()
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NAO_ENCONTRADO", "Usuario nao encontrado"));
        Perfil novoPerfil = perfil == null ? alvo.getPerfil() : perfil;
        boolean removeAdminAtivo = alvo.getPerfil() == Perfil.ADMIN && alvo.getStatus() == StatusUsuario.ATIVO
            && (novoPerfil != Perfil.ADMIN || status != StatusUsuario.ATIVO);
        if (status == StatusUsuario.PENDENTE ||
            (alvo.getEmail().equalsIgnoreCase(emailAdmin) && (status != StatusUsuario.ATIVO || novoPerfil != Perfil.ADMIN)) ||
            (removeAdminAtivo && usuarios.stream().filter(u -> u.getPerfil() == Perfil.ADMIN && u.getStatus() == StatusUsuario.ATIVO).count() <= 1))
            throw new ApiException(HttpStatus.CONFLICT, "ALTERACAO_NAO_PERMITIDA", "Nao e permitido retirar o proprio acesso de administrador nem remover o ultimo administrador ativo");
        alvo.setPerfil(novoPerfil);
        alvo.setStatus(status); return UsuarioResponse.de(repository.save(alvo));
    }
}
