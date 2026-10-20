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
import java.util.regex.Pattern;

/**
 * Verificação estrutural de executáveis do Windows ({@code .exe}, formato PE).
 *
 * <p>Lê o cabeçalho PE e a tabela de seções sem executar nada: procura
 * empacotadores conhecidos, seções que são ao mesmo tempo graváveis e
 * executáveis e funções da API do Windows típicas de injeção de código,
 * keyloggers e download de cargas adicionais.</p>
 */
@Component
public class TesteExecutavelPe extends TesteSegurancaTemplate {

    /** Posição do campo {@code e_lfanew}, que aponta para o cabeçalho PE. */
    private static final int OFFSET_E_LFANEW = 0x3C;
    private static final String ASSINATURA_PE = "PE\u0000\u0000";

    private static final int TAMANHO_CABECALHO_COFF = 20;
    private static final int TAMANHO_SECAO = 40;
    private static final int LIMITE_SECOES = 96;

    private static final long SECAO_EXECUTAVEL = 0x20000000L;
    private static final long SECAO_GRAVAVEL = 0x80000000L;

    /** Nomes de seção deixados por empacotadores e protetores conhecidos. */
    private static final List<String> SECOES_DE_EMPACOTADOR = List.of(
            "upx0", "upx1", "upx2", ".aspack", ".adata", ".mpress1", ".mpress2",
            ".themida", ".vmp0", ".vmp1", ".petite", ".nsp0", ".enigma1"
    );

    private static final List<String> API_INJECAO = List.of(
            "VirtualAllocEx", "WriteProcessMemory", "CreateRemoteThread",
            "NtUnmapViewOfSection", "QueueUserAPC", "SetThreadContext"
    );

    private static final List<String> API_KEYLOGGER = List.of(
            "SetWindowsHookEx", "GetAsyncKeyState", "GetKeyboardState"
    );

    private static final List<String> API_DOWNLOAD = List.of(
            "URLDownloadToFile", "InternetOpenUrl", "WinHttpOpen"
    );

    private static final List<String> API_ANTI_DEBUG = List.of(
            "IsDebuggerPresent", "CheckRemoteDebuggerPresent"
    );

    /** Nome com duas extensões, a última executável: {@code fatura.pdf.exe}. */
    private static final Pattern EXTENSAO_DUPLA = Pattern.compile(
            "(?i).+\\.(pdf|docx?|xlsx?|pptx?|txt|jpe?g|png|zip|rar)\\s*\\.exe$"
    );

    @Override
    public String id() {
        return "PE_EXECUTAVEL";
    }

    @Override
    public String nome() {
        return "Estrutura e importações do executável Windows";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.ESTRUTURAL;
    }

    @Override
    public int ordem() {
        return 20;
    }

    @Override
    protected boolean aplicavel(ContextoAnalise contexto) {
        return TipoArquivo.EXE.assinaturaConfere(contexto);
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        String conteudo = contexto.conteudo();
        List<Achado> achados = new ArrayList<>();

        if (contexto.nomeArquivo() != null && EXTENSAO_DUPLA.matcher(contexto.nomeArquivo()).matches()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1036_007,
                    "Extensão dupla: o nome imita um documento, mas o arquivo é um programa.",
                    contexto.nomeArquivo()
            ));
        }

        int cabecalhoPe = (int) Math.min(lerU32(conteudo, OFFSET_E_LFANEW), Integer.MAX_VALUE);

        if (cabecalhoPe <= 0 || !conteudo.startsWith(ASSINATURA_PE, cabecalhoPe)) {
            achados.add(achado(
                    Severidade.MEDIA,
                    TecnicaMitre.T1027,
                    "Cabeçalho PE ausente ou corrompido: o executável foi montado de forma irregular.",
                    "e_lfanew = " + cabecalhoPe
            ));
            return achados;
        }

        achados.addAll(analisarSecoes(conteudo, cabecalhoPe));
        achados.addAll(analisarImportacoes(conteudo));

        return achados;
    }

    private List<Achado> analisarSecoes(String conteudo, int cabecalhoPe) {

        int coff = cabecalhoPe + ASSINATURA_PE.length();
        int quantidade = Math.min(lerU16(conteudo, coff + 2), LIMITE_SECOES);
        int tamanhoOpcional = lerU16(conteudo, coff + 16);
        int tabela = coff + TAMANHO_CABECALHO_COFF + tamanhoOpcional;

        List<String> empacotadores = new ArrayList<>();
        List<String> graveisExecutaveis = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {

            int secao = tabela + i * TAMANHO_SECAO;

            if (secao + TAMANHO_SECAO > conteudo.length()) {
                break;
            }

            String nome = conteudo.substring(secao, secao + 8).replace("\u0000", "").trim();
            long caracteristicas = lerU32(conteudo, secao + 36);

            if (SECOES_DE_EMPACOTADOR.contains(nome.toLowerCase(Locale.ROOT))) {
                empacotadores.add(nome);
            }

            if ((caracteristicas & SECAO_EXECUTAVEL) != 0 && (caracteristicas & SECAO_GRAVAVEL) != 0) {
                graveisExecutaveis.add(nome.isEmpty() ? "#" + i : nome);
            }
        }

        List<Achado> achados = new ArrayList<>();

        if (!empacotadores.isEmpty()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1027_002,
                    "Executável empacotado: o código real fica comprimido ou cifrado até a execução.",
                    "seções " + String.join(", ", empacotadores)
            ));
        }

        if (!graveisExecutaveis.isEmpty()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1027_002,
                    "Seção gravável e executável: o programa pode se descompactar ou se modificar em memória.",
                    "seções " + String.join(", ", graveisExecutaveis)
            ));
        }

        return achados;
    }

    private List<Achado> analisarImportacoes(String conteudo) {

        List<Achado> achados = new ArrayList<>();

        List<String> injecao = encontrar(conteudo, API_INJECAO);

        if (!injecao.isEmpty()) {
            achados.add(achado(
                    injecao.size() >= 2 ? Severidade.CRITICA : Severidade.ALTA,
                    TecnicaMitre.T1055,
                    "Injeção de código: o programa usa funções para escrever e rodar código em outro processo.",
                    String.join(", ", injecao)
            ));
        }

        List<String> keylogger = encontrar(conteudo, API_KEYLOGGER);

        if (!keylogger.isEmpty()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1056_001,
                    "Captura de teclado: o programa pode registrar o que o usuário digita.",
                    String.join(", ", keylogger)
            ));
        }

        List<String> download = encontrar(conteudo, API_DOWNLOAD);

        if (!download.isEmpty()) {
            achados.add(achado(
                    Severidade.MEDIA,
                    TecnicaMitre.T1105,
                    "Download de arquivos: o programa pode baixar outras cargas da internet.",
                    String.join(", ", download)
            ));
        }

        List<String> antiDebug = encontrar(conteudo, API_ANTI_DEBUG);

        if (!antiDebug.isEmpty()) {
            achados.add(achado(
                    Severidade.BAIXA,
                    TecnicaMitre.T1622,
                    "Detecção de depurador: o programa verifica se está sendo analisado.",
                    String.join(", ", antiDebug)
            ));
        }

        return achados;
    }

    private static List<String> encontrar(String conteudo, List<String> funcoes) {
        return funcoes.stream()
                .filter(conteudo::contains)
                .toList();
    }

    private static int lerU16(String conteudo, int posicao) {

        if (posicao < 0 || posicao + 2 > conteudo.length()) {
            return 0;
        }

        return (conteudo.charAt(posicao) & 0xFF) | (conteudo.charAt(posicao + 1) & 0xFF) << 8;
    }

    private static long lerU32(String conteudo, int posicao) {

        if (posicao < 0 || posicao + 4 > conteudo.length()) {
            return 0;
        }

        return (long) lerU16(conteudo, posicao) | (long) lerU16(conteudo, posicao + 2) << 16;
    }
}
