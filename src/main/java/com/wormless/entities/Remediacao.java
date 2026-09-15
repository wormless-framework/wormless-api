package com.wormless.entities;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Remediacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orientacao;

    @ElementCollection
    private List<String> referencias;
}