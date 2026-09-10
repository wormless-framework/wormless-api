package com.wormless.entities;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeOriginal;
    private Long tamanho;
    private String tipoMime;
    private String caminhoTemporario;
    private LocalDateTime dataUpload;

    public boolean validarFormato() { return true; }
    public boolean validarTamanho() { return true; }
}