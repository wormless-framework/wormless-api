package com.wormless.entities

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegraDeteccao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String padrao;
    private boolean ativa;

    public void ignorarEmMetricasFuturas() {}
}