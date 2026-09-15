package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FalsoPositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String justificativaTecnica;
    private LocalDateTime dataMarcacao;

    @ManyToOne
    private AnalistaSOC analista;

    @OneToOne
    private IndicadorAmeaca indicador;

    public void aplicar() {}
}