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

    /**
     * Busca eventos com filtros avançados (por nome, categoria OU data de início)
     */
    public List<Evento> buscarEventosFiltrados(String titulo, Long categoriaId, LocalDateTime dataInicio) {
        return eventoRepository.findByFilters(titulo, categoriaId, dataInicio);
    }

    /**
     * Salva um novo evento com validações de negócio
     */
    @Transactional
    public Evento salvar(Evento evento, Long usuarioId) {
        // Validação de permissão: organizador deve estar autorizado
        validarPermissaoOrganizador(evento.getOrganizacao(), usuarioId);
        
        // Validação de data: data de início deve ser no futuro
        validarDataInicio(evento.getInicio());
        
        // Validação de capacidade: limite de vagas não pode exceder capacidade máxima do local
        validarCapacidade(evento);
        
        // Validação de conflito de agendamento
        validarConflitosAgendamento(evento);
        
        return eventoRepository.save(evento);
    }

    /**
     * Atualiza um evento existente com validações de negócio
     */
    @Transactional
    public Evento atualizar(Long id, Evento eventoAtualizado, Long usuarioId) {
        Evento eventoExistente = buscarPorId(id);
        
        // Validação de bloqueio de edição: eventos não podem ser modificados após a data de início
        if (eventoExistente.getInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível modificar um evento que já começou");
        }
        
        // Validação de permissão: organizador deve estar autorizado
        validarPermissaoOrganizador(eventoExistente.getOrganizacao(), usuarioId);
        
        // Validação de data: data de início deve ser no futuro
        validarDataInicio(eventoAtualizado.getInicio());
        
        // Validação de capacidade: limite de vagas não pode exceder capacidade máxima do local
        validarCapacidade(eventoAtualizado);
        
        // Validação de conflito de agendamento (excluindo o próprio evento)
        validarConflitosAgendamento(eventoAtualizado);
        
        // Atualiza os campos do evento
        eventoExistente.setTitulo(eventoAtualizado.getTitulo());
        eventoExistente.setDescricao(eventoAtualizado.getDescricao());
        eventoExistente.setInicio(eventoAtualizado.getInicio());
        eventoExistente.setFim(eventoAtualizado.getFim());
        eventoExistente.setLocal(eventoAtualizado.getLocal());
        eventoExistente.setLimiteVagas(eventoAtualizado.getLimiteVagas());
        eventoExistente.setCategorias(eventoAtualizado.getCategorias());
        
        return eventoRepository.save(eventoExistente);
    }

    /**
     * Exclui um evento existente com validações de negócio
     */
    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Evento evento = buscarPorId(id);
        
        // Validação de bloqueio de exclusão: eventos não podem ser excluídos após a data de início
        if (evento.getInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível excluir um evento que já começou");
        }
        
        // Validação de permissão: organizador deve estar autorizado
        validarPermissaoOrganizador(evento.getOrganizacao(), usuarioId);
        
        eventoRepository.deleteById(id);
    }

    /**
     * Busca um evento por ID
     */
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    /**
     * Lista todos os eventos
     */
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    // Métodos privados para validações de negócio

    private void validarPermissaoOrganizador(Organizacao organizacao, Long usuarioId) {
        // Implementação da validação de permissão
        // Verifica se o usuário tem papel de organizador na organização
        if (organizacao == null || !organizacao.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId) && u.getPapel() == papelUsuario.ORGANIZADOR)) {
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
        // Verifica se há conflitos de agendamento no mesmo local
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
