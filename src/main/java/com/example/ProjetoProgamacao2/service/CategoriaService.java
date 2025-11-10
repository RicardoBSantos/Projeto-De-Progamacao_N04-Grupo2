package com.example.ProjetoProgamacao2.service;

import com.example.ProjetoProgamacao2.entity.Categoria;
import com.example.ProjetoProgamacao2.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria criar(Categoria categoria) {
        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalStateException("Nome da categoria é obrigatório");
        }
        categoriaRepository.findByNome(categoria.getNome()).ifPresent(c -> {
            throw new IllegalStateException("Categoria já existe: " + c.getNome());
        });
        return categoriaRepository.save(categoria);
    }

    public void excluir(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        if (categoria.getEventos() != null && !categoria.getEventos().isEmpty()) {
            throw new IllegalStateException("Categoria possui eventos associados e não pode ser excluída");
        }
        categoriaRepository.deleteById(id);
    }

    public Categoria atualizar(Long id, Categoria dados) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        String novoNome = dados.getNome();
        if (novoNome == null || novoNome.isBlank()) {
            throw new IllegalStateException("Nome da categoria é obrigatório");
        }
        categoriaRepository.findByNome(novoNome).ifPresent(c -> {
            if (!c.getId().equals(existente.getId())) {
                throw new IllegalStateException("Categoria já existe: " + c.getNome());
            }
        });
        existente.setNome(novoNome);
        return categoriaRepository.save(existente);
    }
}