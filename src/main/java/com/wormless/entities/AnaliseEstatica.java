package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseEstatica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    private List<RegraDeteccao> regrasAplicadas;

    public List<IndicadorAmeaca> executarAnaliseEstatica(Arquivo arquivo) {
        return List.of();
    }
}