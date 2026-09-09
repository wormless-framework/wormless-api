package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ameaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String tipo;
    private String comoAge;

    @Enumerated(EnumType.STRING)
    private Severidade severidade;
}