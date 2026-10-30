package com.senai.provedor.dto;
import jakarta.validation.constraints.NotNull;
import com.senai.provedor.models.StatusUsuario;
import com.senai.provedor.models.Perfil;
public record StatusRequest(@NotNull StatusUsuario status, Perfil perfil) {}
