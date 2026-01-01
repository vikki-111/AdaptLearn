package com.projects.adaptlearn.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import lombok.NoArgsConstructor;


@Entity
@Table(name = "topics")
@Getter @Setter
@NoArgsConstructor
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;
}