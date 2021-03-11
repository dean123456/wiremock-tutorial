package ru.chameleon.wiremocktutorial.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Group {
    @JsonProperty
    private UUID id;
    @JsonProperty
    private String name;
    @JsonProperty
    private int year;
    @JsonProperty
    private List<String> songs;

    public Group() {
    }

    public Group(String name, int year, List<String> songs) {
        this(UUID.randomUUID(), name, year, songs);
    }

    public Group(UUID id, String name, int year, List<String> songs) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.songs = songs;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<String> getSongs() {
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", songs=" + songs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return year == group.year &&
                Objects.equals(id, group.id) &&
                Objects.equals(name, group.name) &&
                Objects.equals(songs, group.songs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, year, songs);
    }
}
