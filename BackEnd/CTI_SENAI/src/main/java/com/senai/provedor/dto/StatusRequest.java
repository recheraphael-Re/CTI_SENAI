package com.senai.provedor.dto;
import jakarta.validation.constraints.NotNull;
import com.senai.provedor.models.StatusUsuario;
public record StatusRequest(@NotNull StatusUsuario status) {}
