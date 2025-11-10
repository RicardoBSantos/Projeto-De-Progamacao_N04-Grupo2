package com.novaaurora.events.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Category model aligned with backend Categoria entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private Long id;

    @JsonProperty("nome")
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}