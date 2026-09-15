package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoliticaBloqueio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Severidade limiteSeveridade;

    public boolean avaliar(RelatorioAmeaca relatorio) { return false; }
}
