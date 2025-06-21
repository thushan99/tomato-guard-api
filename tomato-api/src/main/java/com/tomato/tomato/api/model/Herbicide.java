package com.tomato.tomato.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "herbicide")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Herbicide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "safe_for_tomato")
    private Boolean safeForTomato;

    @Column(name = "mode_of_action")
    private String modeOfAction;

    @Column(name = "application_method")
    private String applicationMethod;

    @Column(name = "resistance_reported")
    private Boolean resistanceReported;

    @Column(name = "alternative_herbicide")
    private String alternativeHerbicide;

    private String toxicity;

    @Column(name = "human_protection", columnDefinition = "TEXT")
    private String humanProtection;

    @Column(name = "environmental_precautions", columnDefinition = "TEXT")
    private String environmentalPrecautions;
}
