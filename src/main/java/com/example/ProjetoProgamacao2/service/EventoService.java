package com.example.ProjetoProgamacao2.service;

import com.example.ProjetoProgamacao2.entity.Evento;
import com.example.ProjetoProgamacao2.entity.Local;
import com.example.ProjetoProgamacao2.entity.Organizacao;
import com.example.ProjetoProgamacao2.entity.papelUsuario;
import com.example.ProjetoProgamacao2.repository.EventoRepository;
import com.example.ProjetoProgamacao2.repository.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private LocalRepository localRepository;

    
    public List<Evento> buscarEventosFiltrados(String titulo, Long categoriaId, LocalDateTime dataInicio) {
        return eventoRepository.findByFilters(titulo, categoriaId, dataInicio);
    }

    
    @Transactional
    public Evento salvar(Evento evento, Long usuarioId) {
        
        if (evento.getOrganizacao() != null && evento.getOrganizacao().getId() == null) {
            evento.setOrganizacao(null);
        }
        
        validarPermissaoOrganizador(evento.getOrganizacao(), usuarioId);
        
        
        validarDataInicio(evento.getInicio());
        
        
        validarCapacidade(evento);
        
        
        validarConflitosAgendamento(evento);
        
        return eventoRepository.save(evento);
    }

    
    @Transactional
    public Evento atualizar(Long id, Evento eventoAtualizado, Long usuarioId) {
        Evento eventoExistente = buscarPorId(id);
        
        
        if (eventoExistente.getInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível modificar um evento que já começou");
        }
        
        
        validarPermissaoOrganizador(eventoExistente.getOrganizacao(), usuarioId);
        
        
        validarDataInicio(eventoAtualizado.getInicio());
        
        
        validarCapacidade(eventoAtualizado);
        
        
        validarConflitosAgendamento(eventoAtualizado);
        
        
        eventoExistente.setTitulo(eventoAtualizado.getTitulo());
        eventoExistente.setDescricao(eventoAtualizado.getDescricao());
        eventoExistente.setInicio(eventoAtualizado.getInicio());
        eventoExistente.setFim(eventoAtualizado.getFim());
        eventoExistente.setLocal(eventoAtualizado.getLocal());
        eventoExistente.setLimiteVagas(eventoAtualizado.getLimiteVagas());
        eventoExistente.setCategoria(eventoAtualizado.getCategoria());
        
        return eventoRepository.save(eventoExistente);
    }

    
    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Evento evento = buscarPorId(id);
        
        
        if (evento.getInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível excluir um evento que já começou");
        }
        
        
        validarPermissaoOrganizador(evento.getOrganizacao(), usuarioId);
        
        eventoRepository.deleteById(id);
    }

    
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    

    private void validarPermissaoOrganizador(Organizacao organizacao, Long usuarioId) {
        
        if (organizacao == null) {
            return;
        }
        
        if (organizacao.getId() == null) {
            return;
        }
        if (organizacao.getUsuarios() == null) {
            return;
        }
        
        boolean autorizado = organizacao.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId) && u.getPapel() == papelUsuario.ORGANIZADOR);
        if (!autorizado) {
            throw new IllegalStateException("Usuário não tem permissão para gerenciar eventos desta organização");
        }
    }

    private void validarDataInicio(LocalDateTime dataInicio) {
        if (dataInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("A data de início do evento deve ser no futuro");
        }
    }

    private void validarCapacidade(Evento evento) {
        Local local = evento.getLocal();
        if (local != null && evento.getLimiteVagas() > local.getCapacidadeMaxima()) {
            throw new IllegalStateException(
                    "O limite de vagas (" + evento.getLimiteVagas() + 
                    ") não pode exceder a capacidade máxima do local (" + local.getCapacidadeMaxima() + ")");
        }
    }

    private void validarConflitosAgendamento(Evento evento) {
        
        if (evento.getLocal() == null) {
            return;
        }
        
        List<Evento> eventosConflitantes = eventoRepository.findOverlappingEvents(
                evento.getLocal(), 
                evento.getInicio(), 
                evento.getFim(), 
                evento.getId() == null ? -1L : evento.getId());
        
        if (!eventosConflitantes.isEmpty()) {
            throw new IllegalStateException(
                    "Já existe um evento agendado neste local e período. " +
                    "Evento conflitante: " + eventosConflitantes.get(0).getTitulo());
        }
    }
}
