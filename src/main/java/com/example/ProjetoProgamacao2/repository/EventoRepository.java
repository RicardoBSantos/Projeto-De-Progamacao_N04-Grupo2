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
    
    // Advanced filtered search by name, category OR start date
    @Query("SELECT e FROM Evento e LEFT JOIN e.categorias c WHERE " +
           "(:titulo IS NULL OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:categoriaId IS NULL OR c.id = :categoriaId) AND " +
           "(:dataInicio IS NULL OR e.inicio >= :dataInicio)")
    List<Evento> findByFilters(
            @Param("titulo") String titulo,
            @Param("categoriaId") Long categoriaId,
            @Param("dataInicio") LocalDateTime dataInicio);
    
    // Find events that overlap with a given time period at a specific location
    @Query("SELECT e FROM Evento e WHERE e.local = :local AND " +
           "e.id <> :eventoId AND " +
           "e.inicio <= :dataFim AND e.fim >= :dataInicio")
    List<Evento> findOverlappingEvents(
            @Param("local") Local local,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("eventoId") Long eventoId);
}
