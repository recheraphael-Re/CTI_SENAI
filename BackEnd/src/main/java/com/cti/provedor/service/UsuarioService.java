package com.cti.provedor.service;
import com.cti.provedor.model.*;
import com.cti.provedor.dto.*;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.config.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    public UsuarioService(UsuarioRepository repository) { this.repository = repository; }
    public List<UsuarioDto> listar() { return repository.findAll().stream().map(UsuarioDto::de).toList(); }
    @Transactional
    public UsuarioDto alterar(Long id, StatusUsuario status, PerfilUsuario perfil, String emailAdmin) {
        // Serializa alteracoes de status para proteger o ultimo administrador ativo.
        List<Usuario> usuarios = repository.buscarParaAdministracao();
        Usuario alvo = usuarios.stream().filter(u -> u.getId().equals(id)).findFirst()
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NAO_ENCONTRADO", "Usuario nao encontrado"));
        PerfilUsuario novoPerfil = perfil == null ? alvo.getPerfil() : perfil;
        boolean removeAdminAtivo = alvo.getPerfil() == PerfilUsuario.ADMIN && alvo.getStatus() == StatusUsuario.ATIVO
            && (novoPerfil != PerfilUsuario.ADMIN || status != StatusUsuario.ATIVO);
        if (status == StatusUsuario.PENDENTE ||
            (alvo.getEmail().equalsIgnoreCase(emailAdmin) && (status != StatusUsuario.ATIVO || novoPerfil != PerfilUsuario.ADMIN)) ||
            (removeAdminAtivo && usuarios.stream().filter(u -> u.getPerfil() == PerfilUsuario.ADMIN && u.getStatus() == StatusUsuario.ATIVO).count() <= 1))
            throw new ApiException(HttpStatus.CONFLICT, "ALTERACAO_NAO_PERMITIDA", "Nao e permitido retirar o proprio acesso de administrador nem remover o ultimo administrador ativo");
        alvo.setPerfil(novoPerfil);
        alvo.setStatus(status); return UsuarioDto.de(repository.save(alvo));
    }
}
