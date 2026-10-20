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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Inspeção do conteúdo de arquivos compactados ({@code .zip}).
 *
 * <p>Lê apenas o diretório central do ZIP — a lista de entradas que fica no
 * fim do arquivo —, sem descompactar nada. Assim a análise enxerga nomes,
 * tamanhos e flags de cada entrada e não corre o risco de ser derrubada por
 * uma zip bomb.</p>
 */
@Component
public class TesteConteudoZip extends TesteSegurancaTemplate {

    private static final String FIM_DIRETORIO = "PK\u0005\u0006";
    private static final String ENTRADA_DIRETORIO = "PK\u0001\u0002";

    private static final int TAMANHO_FIM_DIRETORIO = 22;
    private static final int TAMANHO_ENTRADA = 46;
    private static final int LIMITE_COMENTARIO = 0xFFFF;
    private static final int LIMITE_ENTRADAS = 10_000;
    private static final int LIMITE_EVIDENCIAS = 5;

    private static final int FLAG_CIFRADO = 0x1;

    /** Valor usado pelo ZIP64 quando o tamanho real fica no campo extra. */
    private static final long TAMANHO_ZIP64 = 0xFFFFFFFFL;

    /** Total descompactado a partir do qual o arquivo é tratado como zip bomb. */
    private static final long LIMITE_DESCOMPACTADO = 1L << 30;
    private static final long LIMITE_TAXA_COMPRESSAO = 100;

    private static final Set<String> EXTENSOES_EXECUTAVEIS = Set.of(
            "exe", "dll", "scr", "com", "pif", "msi", "bat", "cmd", "ps1", "vbs",
            "vbe", "js", "jse", "wsf", "hta", "lnk", "jar", "cpl", "reg"
    );

    private static final Set<String> EXTENSOES_COMPACTADAS = Set.of(
            "zip", "rar", "7z", "iso", "img", "cab", "gz", "tar"
    );

    /** Documento ou imagem seguido de uma extensão executável: {@code fatura.pdf.exe}. */
    private static final Pattern EXTENSAO_DUPLA = Pattern.compile(
            "(?i).+\\.(pdf|docx?|xlsx?|pptx?|txt|jpe?g|png)\\s*\\.("
                    + String.join("|", EXTENSOES_EXECUTAVEIS) + ")$"
    );

    /**
     * Entrada lida do diretório central.
     *
     * @param nome           caminho da entrada dentro do ZIP
     * @param cifrada        se a entrada está protegida por senha
     * @param compactado     tamanho compactado, em bytes
     * @param descompactado  tamanho descompactado, em bytes
     */
    private record Entrada(String nome, boolean cifrada, long compactado, long descompactado) {
    }

    @Override
    public String id() {
        return "ZIP_CONTEUDO";
    }

    @Override
    public String nome() {
        return "Conteúdo do arquivo compactado";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.ESTRUTURAL;
    }

    @Override
    public int ordem() {
        return 25;
    }

    @Override
    protected boolean aplicavel(ContextoAnalise contexto) {
        return TipoArquivo.ZIP.assinaturaConfere(contexto);
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        List<Entrada> entradas = lerDiretorio(contexto.conteudo());

        if (entradas == null) {
            return List.of(achado(
                    Severidade.MEDIA,
                    TecnicaMitre.T1027,
                    "Estrutura do ZIP corrompida: o índice de arquivos não pôde ser lido.",
                    "diretório central ausente ou inválido"
            ));
        }

        List<Achado> achados = new ArrayList<>();

        adicionar(achados, entradas.stream()
                        .map(Entrada::nome)
                        .filter(nome -> EXTENSAO_DUPLA.matcher(nome).matches())
                        .toList(),
                Severidade.CRITICA, TecnicaMitre.T1036_007,
                "Extensão dupla: um programa dentro do ZIP se passa por documento ou imagem.");

        adicionar(achados, entradas.stream()
                        .map(Entrada::nome)
                        .filter(nome -> EXTENSOES_EXECUTAVEIS.contains(extensao(nome)))
                        .toList(),
                Severidade.ALTA, TecnicaMitre.T1204_002,
                "Executáveis ou scripts dentro do ZIP: o usuário pode rodá-los ao extrair.");

        adicionar(achados, entradas.stream()
                        .map(Entrada::nome)
                        .filter(TesteConteudoZip::escapaDoDestino)
                        .toList(),
                Severidade.ALTA, null,
                "Caminho fora da pasta de extração (zip slip): a entrada pode sobrescrever arquivos do sistema.");

        adicionar(achados, entradas.stream()
                        .map(Entrada::nome)
                        .filter(nome -> EXTENSOES_COMPACTADAS.contains(extensao(nome)))
                        .toList(),
                Severidade.MEDIA, TecnicaMitre.T1027,
                "Arquivos compactados aninhados: camadas extras costumam esconder o conteúdo da análise.");

        adicionar(achados, entradas.stream()
                        .filter(Entrada::cifrada)
                        .map(Entrada::nome)
                        .toList(),
                Severidade.MEDIA, TecnicaMitre.T1027,
                "Entradas protegidas por senha: o conteúdo cifrado não pode ser inspecionado.");

        achados.addAll(detectarZipBomb(entradas));

        return achados;
    }

    private List<Achado> detectarZipBomb(List<Entrada> entradas) {

        long compactado = entradas.stream().mapToLong(Entrada::compactado).sum();
        long descompactado = entradas.stream().mapToLong(Entrada::descompactado).sum();

        boolean grandeDemais = descompactado >= LIMITE_DESCOMPACTADO;
        boolean taxaAbsurda = compactado > 0 && descompactado / compactado >= LIMITE_TAXA_COMPRESSAO;

        if (!grandeDemais && !taxaAbsurda) {
            return List.of();
        }

        return List.of(achado(
                Severidade.ALTA,
                null,
                "Possível zip bomb: o conteúdo extraído é desproporcional ao tamanho do arquivo.",
                compactado + " bytes compactados -> " + descompactado + " bytes extraídos"
        ));
    }

    private void adicionar(List<Achado> achados, List<String> nomes,
                           Severidade severidade, TecnicaMitre tecnica, String descricao) {

        if (nomes.isEmpty()) {
            return;
        }

        String evidencia = String.join(", ", nomes.stream().limit(LIMITE_EVIDENCIAS).toList());

        if (nomes.size() > LIMITE_EVIDENCIAS) {
            evidencia += " (+" + (nomes.size() - LIMITE_EVIDENCIAS) + ")";
        }

        achados.add(achado(severidade, tecnica, descricao, evidencia));
    }

    /** Entradas do diretório central, ou {@code null} se a estrutura for inválida. */
    private static List<Entrada> lerDiretorio(String conteudo) {

        int inicioBusca = Math.max(0, conteudo.length() - TAMANHO_FIM_DIRETORIO - LIMITE_COMENTARIO);
        int fim = conteudo.lastIndexOf(FIM_DIRETORIO);

        if (fim < inicioBusca || fim + TAMANHO_FIM_DIRETORIO > conteudo.length()) {
            return null;
        }

        int total = Math.min(lerU16(conteudo, fim + 10), LIMITE_ENTRADAS);
        long inicioDiretorio = lerU32(conteudo, fim + 16);

        if (inicioDiretorio >= fim) {
            return null;
        }

        List<Entrada> entradas = new ArrayList<>();
        int posicao = (int) inicioDiretorio;

        for (int i = 0; i < total; i++) {

            if (posicao + TAMANHO_ENTRADA > conteudo.length()
                    || !conteudo.startsWith(ENTRADA_DIRETORIO, posicao)) {
                return entradas.isEmpty() ? null : entradas;
            }

            int flags = lerU16(conteudo, posicao + 8);
            long compactado = lerU32(conteudo, posicao + 20);
            long descompactado = lerU32(conteudo, posicao + 24);
            int tamanhoNome = lerU16(conteudo, posicao + 28);
            int tamanhoExtra = lerU16(conteudo, posicao + 30);
            int tamanhoComentario = lerU16(conteudo, posicao + 32);

            int inicioNome = posicao + TAMANHO_ENTRADA;
            int fimNome = Math.min(inicioNome + tamanhoNome, conteudo.length());

            entradas.add(new Entrada(
                    conteudo.substring(inicioNome, fimNome),
                    (flags & FLAG_CIFRADO) != 0,
                    compactado == TAMANHO_ZIP64 ? 0 : compactado,
                    descompactado == TAMANHO_ZIP64 ? 0 : descompactado
            ));

            posicao = inicioNome + tamanhoNome + tamanhoExtra + tamanhoComentario;
        }

        return entradas;
    }

    private static boolean escapaDoDestino(String nome) {
        String normalizado = nome.replace('\\', '/');
        return normalizado.startsWith("/")
                || normalizado.matches("^[A-Za-z]:.*")
                || normalizado.equals("..")
                || normalizado.startsWith("../")
                || normalizado.contains("/../");
    }

    private static String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).trim().toLowerCase(Locale.ROOT);
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
