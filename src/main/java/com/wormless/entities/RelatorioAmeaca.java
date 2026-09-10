package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioAmeaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dataGeracao;

    @Enumerated(EnumType.STRING)
    private Severidade severidadeGeral;
    private String resumo;

    @OneToMany
    private List<IndicadorAmeaca> indicadores;

    public String gerarResumo() { return this.resumo; }
    public List<IndicadorAmeaca> detalhar() { return this.indicadores; }
}