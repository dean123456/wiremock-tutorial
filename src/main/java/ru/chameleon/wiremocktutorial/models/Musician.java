package ru.chameleon.wiremocktutorial.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Musician {
    @JsonProperty
    private UUID id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String role;

    public Musician(String name, String role) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Musician{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
