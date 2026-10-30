package com.cti.provedor.dto;
import jakarta.validation.constraints.NotNull;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.PerfilUsuario;
public record StatusRequest(@NotNull StatusUsuario status, PerfilUsuario perfil) {}
