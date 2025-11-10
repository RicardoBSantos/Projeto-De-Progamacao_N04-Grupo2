package com.example.ProjetoProgamacao2.repository;

import com.example.ProjetoProgamacao2.entity.Evento;
import com.example.ProjetoProgamacao2.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    
    @Query("SELECT e FROM Evento e WHERE " +
           "(:titulo IS NULL OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:categoriaId IS NULL OR e.categoria.id = :categoriaId) AND " +
           "(:dataInicio IS NULL OR e.inicio >= :dataInicio)")
    List<Evento> findByFilters(
            @Param("titulo") String titulo,
            @Param("categoriaId") Long categoriaId,
            @Param("dataInicio") LocalDateTime dataInicio);
    
    
    @Query("SELECT e FROM Evento e WHERE e.local = :local AND " +
           "e.id <> :eventoId AND " +
           "e.inicio <= :dataFim AND e.fim >= :dataInicio")
    List<Evento> findOverlappingEvents(
            @Param("local") Local local,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("eventoId") Long eventoId);
}
