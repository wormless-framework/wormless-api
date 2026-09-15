package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusJob status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public void executar() {}
    public StatusJob consultarStatus() { return this.status; }
}
