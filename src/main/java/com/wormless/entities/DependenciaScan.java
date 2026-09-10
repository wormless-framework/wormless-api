package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependenciaScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomePacote;
    private String versao;

    @OneToMany
    private List<Vulnerabilidade> vulnerabilidadesEncontradas;

    public List<Vulnerabilidade> varrer() {
        return this.vulnerabilidadesEncontradas;
    }
}
