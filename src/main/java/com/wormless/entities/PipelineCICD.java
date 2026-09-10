package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineCICD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String repositorio;

    public AnaliseJob enviarParaAnalise(String codigo, List<DependenciaScan> dependencias) {
        return new AnaliseJob();
    }
}