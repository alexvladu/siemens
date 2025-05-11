package com.siemens.internship;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name required")
    private String name;

    @NotBlank(message = "Description required")
    private String description;


    @NotNull(message = "Status required")
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotBlank(message = "Email required")
    @Pattern(regexp = "^[A-Za-z0-9]{2,}@[A-Za-z0-9]{2,}.[A-Za-z]{2,}$", message = "Email format is invalid")
    private String email;
}