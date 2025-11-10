package com.novaaurora.events.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Event model mapped to backend JSON fields.
 * Uses Portuguese field names to match Spring entities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private Long id;

    @JsonProperty("titulo")
    private String title;

    @JsonProperty("descricao")
    private String description;

    @JsonProperty("inicio")
    private LocalDateTime start;

    @JsonProperty("fim")
    private LocalDateTime end;

    @JsonProperty("limiteVagas")
    private Integer seatLimit;

    @JsonProperty("categoria")
    private Category category;

    @JsonProperty("organizacao")
    private Organization organization;

    // UI-only fields
    private boolean authorized;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public Integer getSeatLimit() { return seatLimit; }
    public void setSeatLimit(Integer seatLimit) { this.seatLimit = seatLimit; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }
}