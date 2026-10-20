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

/**
 * Análise estática dos marcadores declarados no PDF.
 *
 * <p>Sucessor direto do mapa de marcadores que vivia dentro do
 * {@code SandboxService}: os mesmos quatro marcadores, agora com severidade e
 * técnica MITRE por marcador, em vez de severidade derivada da contagem de
 * detecções.</p>
 */
@Component
public class TesteMarcadoresPdf extends TesteSegurancaTemplate {

    /**
     * Marcador procurado no documento e o que a presença dele significa.
     *
     * @param marcador   token literal buscado no conteúdo
     * @param severidade risco atribuído à presença do marcador
     * @param tecnica    técnica MITRE correspondente
     * @param descricao  explicação em linguagem acessível
     */
    private record MarcadorPdf(
            String marcador,
            Severidade severidade,
            TecnicaMitre tecnica,
            String descricao) {
    }

    private static final List<MarcadorPdf> MARCADORES = List.of(
            new MarcadorPdf("/Launch", Severidade.CRITICA, TecnicaMitre.T1204_002,
                    "Execução de programa: o PDF tenta abrir um programa externo."),
            new MarcadorPdf("/JavaScript", Severidade.ALTA, TecnicaMitre.T1059_007,
                    "JavaScript embutido: o PDF contém código que pode ser executado ao abrir."),
            new MarcadorPdf("/OpenAction", Severidade.ALTA, TecnicaMitre.T1204_002,
                    "Ação automática: o PDF executa uma ação assim que é aberto."),
            new MarcadorPdf("/EmbeddedFile", Severidade.ALTA, TecnicaMitre.T1027,
                    "Arquivo embutido: o PDF carrega outro arquivo dentro dele.")
    );

    @Override
    public String id() {
        return "PDF_MARCADORES";
    }

    @Override
    public String nome() {
        return "Marcadores estáticos do PDF";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.ESTATICA;
    }

    @Override
    public int ordem() {
        return 10;
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        List<Achado> achados = new ArrayList<>();

        for (MarcadorPdf marcador : MARCADORES) {

            int ocorrencias = contexto.contar(marcador.marcador());

            if (ocorrencias > 0) {
                achados.add(achado(
                        marcador.severidade(),
                        marcador.tecnica(),
                        marcador.descricao(),
                        marcador.marcador() + " x" + ocorrencias
                ));
            }
        }

        return achados;
    }
}
