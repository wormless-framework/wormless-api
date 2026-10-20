package com.wormless.analise.testes;

import com.wormless.analise.Achado;
import com.wormless.analise.CategoriaTeste;
import com.wormless.analise.ContextoAnalise;
import com.wormless.analise.TesteSegurancaTemplate;
import com.wormless.analise.mitre.TecnicaMitre;
import com.wormless.entities.enums.Severidade;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Heurística estatística: entropia de Shannon do conteúdo.
 *
 * <p>Conteúdo empacotado ou cifrado tende à entropia máxima (8 bits por byte).
 * O teste mede a entropia do arquivo inteiro e compara com dois limiares.</p>
 *
 * <p>PDFs legítimos com imagens comprimidas também têm entropia alta, então os
 * limiares são deliberadamente conservadores e o achado nunca é crítico
 * sozinho — ele pesa em conjunto com os demais testes.</p>
 */
@Component
public class TesteEntropia extends TesteSegurancaTemplate {

    private static final int VALORES_DE_BYTE = 256;
    private static final int TAMANHO_MINIMO = 512;

    private static final double LIMITE_ALTO = 7.80;
    private static final double LIMITE_MODERADO = 7.20;

    private static final double LOG_DE_2 = Math.log(2);

    @Override
    public String id() {
        return "ENTROPIA";
    }

    @Override
    public String nome() {
        return "Entropia do conteúdo (empacotamento e cifragem)";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.HEURISTICA;
    }

    @Override
    public int ordem() {
        return 50;
    }

    @Override
    protected boolean aplicavel(ContextoAnalise contexto) {
        return contexto.conteudo().length() >= TAMANHO_MINIMO;
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        List<Achado> achados = new ArrayList<>();
        String conteudo = contexto.conteudo();

        double entropiaGlobal = entropia(conteudo);

        if (entropiaGlobal >= LIMITE_ALTO) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1027_002,
                    "Conteúdo com aparência de empacotado ou cifrado: quase não há repetição de padrões, "
                            + "o que é típico de carga comprimida para escapar de inspeção.",
                    "entropia global " + formatar(entropiaGlobal)
            ));
        } else if (entropiaGlobal >= LIMITE_MODERADO) {
            achados.add(achado(
                    Severidade.BAIXA,
                    TecnicaMitre.T1027_002,
                    "Entropia acima do usual para um documento de texto; pode ser apenas compressão de imagens.",
                    "entropia global " + formatar(entropiaGlobal)
            ));
        }

        return achados;
    }

    /**
     * Entropia de Shannon, em bits por byte, do conteúdo.
     *
     * <p>O conteúdo é decodificado em ISO-8859-1, então cada caractere equivale
     * a exatamente um byte.</p>
     */
    private double entropia(String conteudo) {

        int[] frequencias = new int[VALORES_DE_BYTE];

        for (int i = 0; i < conteudo.length(); i++) {
            frequencias[conteudo.charAt(i) & 0xFF]++;
        }

        int total = conteudo.length();

        if (total == 0) {
            return 0;
        }

        double entropia = 0;

        for (int frequencia : frequencias) {
            if (frequencia > 0) {
                double probabilidade = (double) frequencia / total;
                entropia -= probabilidade * (Math.log(probabilidade) / LOG_DE_2);
            }
        }

        return entropia;
    }

    private String formatar(double valor) {
        return String.format(Locale.ROOT, "%.2f/8.00", valor);
    }
}
