package edu.unisabana.tyvs.registry.domain.model.rq;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class PersonDTO {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Positive(message = "ID must be a positive number")
    private int id;

    @Min(value = 0, message = "Age cannot be negative")
    private int age;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    @NotNull(message = "Alive status must be specified")
    private boolean alive;

    public PersonDTO() {
    }

    public PersonDTO(String name, int id, int age, String gender, boolean alive) {
        this.name = name;
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.alive = alive;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
