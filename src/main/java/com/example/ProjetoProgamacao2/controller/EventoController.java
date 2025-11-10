package com.example.ProjetoProgamacao2.controller;

import com.example.ProjetoProgamacao2.entity.Evento;
import com.example.ProjetoProgamacao2.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.PropertyValueException;
import org.hibernate.TransientObjectException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    
    @Autowired
    private EventoService eventoService;
    
    @GetMapping
    public ResponseEntity<List<Evento>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }
    
    @GetMapping("/busca")
    public ResponseEntity<List<Evento>> buscarFiltrado(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio) {
        return ResponseEntity.ok(eventoService.buscarEventosFiltrados(titulo, categoriaId, dataInicio));
    }
    
    @PostMapping
    public ResponseEntity<Evento> criar(@RequestBody Evento evento, @RequestHeader("Usuario-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.salvar(evento, usuarioId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Evento> atualizar(
            @PathVariable Long id, 
            @RequestBody Evento evento, 
            @RequestHeader("Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(eventoService.atualizar(id, evento, usuarioId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("Usuario-Id") Long usuarioId) {
        eventoService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    
    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<String> handlePropertyValueException(PropertyValueException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    @ExceptionHandler(TransientObjectException.class)
    public ResponseEntity<String> handleTransientObjectException(TransientObjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Entidade relacionada não persistida: " + ex.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
