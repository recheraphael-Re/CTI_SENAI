package com.cti.provedor.controller;

import java.util.List;
import com.cti.provedor.service.ConsultorService;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.model.Consultor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


// Arquivo controller é responsável por realizar as requisições http da API

@RestController // indica que a classe consultor controller irá receber as requisições http
@RequestMapping("/consultores") // cria a rota consultores
public class ConsultorController {

    // Cria a variavel ConsultorService 
    private final ConsultorService service;

    // Cria o construtor
    public ConsultorController(
        ConsultorService service){
            this.service = service;
        }

// ======
//CREATE
// =======

@PostMapping
public Consultor criar(
    @RequestBody Consultor consultor){
        return service.criar(consultor);
    }



// =====
// LOGIN
//

//====
// READ

@GetMapping
public List<Consultor> listar(){
    return service.listar();
}


// === 
// READ por id
// ====


@GetMapping("/{id}")
public Consultor buscar(
    @PathVariable  Long id){
        return service.buscarPorId(id);
    }



// ==== 
// UPDATE
// ====

@PutMapping("/{id}")
public Consultor atualizar(
    @PathVariable Long id, @RequestBody Consultor consultor){
        return  service.atualizar(id, consultor);
    }


// ==== 
// DELETE
// =====

@DeleteMapping("/{id}")
public void excluir(@PathVariable Long id){
    service.excluir(id);
}






    
}


