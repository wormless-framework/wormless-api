package com.wormless.analise.testes;

import com.wormless.analise.Achado;
import com.wormless.analise.CategoriaTeste;
import com.wormless.analise.ContextoAnalise;
import com.wormless.analise.TesteSegurancaTemplate;
import com.wormless.analise.TipoArquivo;
import com.wormless.analise.mitre.TecnicaMitre;
import com.wormless.entities.enums.Severidade;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Análise estática de documentos do Word 97-2003 ({@code .doc}).
 *
 * <p>O {@code .doc} é um contêiner OLE: os nomes dos fluxos internos ficam
 * gravados em UTF-16LE no diretório do arquivo, e os identificadores das
 * macros (nomes de procedimentos e objetos chamados) aparecem em texto no
 * projeto VBA. O teste procura esses dois tipos de marcador sem precisar
 * descompactar o código das macros.</p>
 *
 * <p>Também cobre o disfarce clássico de um RTF salvo com extensão
 * {@code .doc}: o Word abre o arquivo normalmente, e o RTF é o formato mais
 * usado para explorar o Editor de Equações (CVE-2017-11882).</p>
 */
@Component
public class TesteDocumentoWord extends TesteSegurancaTemplate {

    private static final String CABECALHO_RTF = "{\\rt";

    /** Fluxos do contêiner OLE que só existem quando há macros. */
    private static final List<String> FLUXOS_VBA = List.of("_VBA_PROJECT", "Macros", "VBA");

    /** Procedimentos que o Office executa sozinho ao abrir ou fechar o documento. */
    private static final List<String> AUTO_EXECUCAO = List.of(
            "AutoOpen", "Auto_Open", "Document_Open", "AutoExec",
            "AutoClose", "Document_Close", "Workbook_Open"
    );

    /** Chamadas usadas por macros para rodar comandos ou baixar arquivos. */
    private static final List<String> CHAMADAS_SUSPEITAS = List.of(
            "WScript.Shell", "Shell.Application", "CreateObject", "URLDownloadToFile",
            "MSXML2.XMLHTTP", "ADODB.Stream", "powershell", "cmd.exe", "CallByName"
    );

    @Override
    public String id() {
        return "DOC_OFFICE";
    }

    @Override
    public String nome() {
        return "Macros, DDE e exploits em documentos do Word";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.ESTATICA;
    }

    @Override
    public int ordem() {
        return 15;
    }

    @Override
    protected boolean aplicavel(ContextoAnalise contexto) {
        return TipoArquivo.DOC.assinaturaConfere(contexto) || ehRtfComoDoc(contexto);
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        String conteudo = contexto.conteudo();
        String minusculo = conteudo.toLowerCase(Locale.ROOT);
        List<Achado> achados = new ArrayList<>();

        if (ehRtfComoDoc(contexto)) {
            achados.addAll(detectarRtf(minusculo));
        }

        List<String> fluxosVba = FLUXOS_VBA.stream()
                .filter(fluxo -> conteudo.contains(utf16(fluxo)))
                .toList();

        if (!fluxosVba.isEmpty()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1059_005,
                    "Macros VBA: o documento contém código que pode ser executado pelo Word.",
                    "fluxos " + String.join(", ", fluxosVba)
            ));
        }

        List<String> autoExecucao = encontrar(minusculo, AUTO_EXECUCAO);

        if (!autoExecucao.isEmpty()) {
            achados.add(achado(
                    fluxosVba.isEmpty() ? Severidade.MEDIA : Severidade.CRITICA,
                    TecnicaMitre.T1204_002,
                    "Execução automática: a macro roda sozinha quando o documento é aberto ou fechado.",
                    String.join(", ", autoExecucao)
            ));
        }

        List<String> chamadas = encontrar(minusculo, CHAMADAS_SUSPEITAS);

        if (!chamadas.isEmpty()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1105,
                    "Chamadas perigosas: o documento referencia comandos do sistema ou download de arquivos.",
                    String.join(", ", chamadas)
            ));
        }

        if (contemTextoOuUtf16(minusculo, "ddeauto")) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1559_002,
                    "Campo DDE automático: o documento pode executar um comando sem usar macros.",
                    "DDEAUTO"
            ));
        }

        if (minusculo.contains("equation.3")) {
            achados.add(achado(
                    Severidade.CRITICA,
                    TecnicaMitre.T1203,
                    "Objeto do Editor de Equações: componente explorado pela CVE-2017-11882.",
                    "Equation.3"
            ));
        }

        if (conteudo.contains(utf16("EncryptionInfo"))) {
            achados.add(achado(
                    Severidade.MEDIA,
                    TecnicaMitre.T1027,
                    "Documento protegido por senha: o conteúdo cifrado não pode ser inspecionado.",
                    "fluxo EncryptionInfo"
            ));
        }

        return achados;
    }

    private List<Achado> detectarRtf(String minusculo) {

        List<Achado> achados = new ArrayList<>();

        achados.add(achado(
                Severidade.MEDIA,
                TecnicaMitre.T1036_005,
                "O arquivo é um RTF com extensão .doc: disfarce comum para entregar exploits.",
                "cabeçalho {\\rt no byte 0"
        ));

        if (minusculo.contains("\\objdata")) {
            boolean atualizaSozinho = minusculo.contains("\\objupdate");

            achados.add(achado(
                    atualizaSozinho ? Severidade.ALTA : Severidade.MEDIA,
                    TecnicaMitre.T1203,
                    atualizaSozinho
                            ? "Objeto embutido com atualização automática: carregado assim que o RTF é aberto."
                            : "Objeto embutido no RTF: pode carregar um componente vulnerável.",
                    atualizaSozinho ? "\\objdata + \\objupdate" : "\\objdata"
            ));
        }

        // "Equation.3" gravado em hexadecimal dentro do \objdata
        if (minusculo.contains("4571756174696f6e2e33")) {
            achados.add(achado(
                    Severidade.CRITICA,
                    TecnicaMitre.T1203,
                    "Objeto do Editor de Equações: componente explorado pela CVE-2017-11882.",
                    "Equation.3 em hexadecimal"
            ));
        }

        return achados;
    }

    private static boolean ehRtfComoDoc(ContextoAnalise contexto) {
        return contexto.conteudo().startsWith(CABECALHO_RTF)
                && TipoArquivo.porNome(contexto.nomeArquivo()).orElse(null) == TipoArquivo.DOC;
    }

    private static List<String> encontrar(String minusculo, List<String> marcadores) {
        return marcadores.stream()
                .filter(marcador -> contemTextoOuUtf16(minusculo, marcador.toLowerCase(Locale.ROOT)))
                .toList();
    }

    /** O texto do documento pode estar em ASCII ou em UTF-16LE. */
    private static boolean contemTextoOuUtf16(String minusculo, String marcador) {
        return minusculo.contains(marcador) || minusculo.contains(utf16(marcador));
    }

    /** Marcador como aparece em UTF-16LE, lido em ISO-8859-1: um byte nulo após cada letra. */
    private static String utf16(String texto) {

        StringBuilder resultado = new StringBuilder(texto.length() * 2);

        for (char letra : texto.toCharArray()) {
            resultado.append(letra).append('\u0000');
        }

        return resultado.toString();
    }
}
