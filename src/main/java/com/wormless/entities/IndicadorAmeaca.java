package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorAmeaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String trechoArquivo;
    private int linhaOcorrencia;
    private String descricaoComportamento;

    @Enumerated(EnumType.STRING)
    private StatusIndicador status;

    @ManyToOne
    private Ameaca ameaca;
}