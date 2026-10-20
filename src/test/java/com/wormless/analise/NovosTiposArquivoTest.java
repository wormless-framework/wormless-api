package com.wormless.analise;

import com.wormless.analise.testes.TesteConteudoZip;
import com.wormless.analise.testes.TesteDocumentoWord;
import com.wormless.analise.testes.TesteExecutavelPe;
import com.wormless.analise.testes.TestePolyglotMagicBytes;
import com.wormless.entities.enums.Severidade;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class NovosTiposArquivoTest {

    @Test
    void aceitaApenasAsExtensoesSuportadas() {
        assertThat(TipoArquivo.porNome("relatorio.PDF")).contains(TipoArquivo.PDF);
        assertThat(TipoArquivo.porNome("carta.doc")).contains(TipoArquivo.DOC);
        assertThat(TipoArquivo.porNome("setup.exe")).contains(TipoArquivo.EXE);
        assertThat(TipoArquivo.porNome("pacote.zip")).contains(TipoArquivo.ZIP);
        assertThat(TipoArquivo.porNome("script.ps1")).isEmpty();
        assertThat(TipoArquivo.porNome("semextensao")).isEmpty();
        assertThat(TipoArquivo.porNome(null)).isEmpty();
    }

    @Test
    void docComMacroDeAutoExecucao() {
        String ole = "ÐÏ\u0011à¡±\u001Aá" + utf16("_VBA_PROJECT")
                + " Sub AutoOpen() CreateObject(\"WScript.Shell\").Run \"powershell\"";

        List<Achado> achados = new TesteDocumentoWord().executar(contexto("carta.doc", ole));

        assertThat(severidades(achados)).contains(Severidade.CRITICA);
        assertThat(descricoes(achados)).anyMatch(d -> d.startsWith("Macros VBA"));
        assertThat(descricoes(achados)).anyMatch(d -> d.startsWith("Chamadas perigosas"));
    }

    @Test
    void rtfDisfarcadoDeDocComEditorDeEquacoes() {
        String rtf = "{\\rtf1{\\object\\objupdate{\\*\\objdata 0105000002000000"
                + "0b0000004571756174696f6e2e3300}}}";

        List<Achado> achados = new TesteDocumentoWord().executar(contexto("fatura.doc", rtf));

        assertThat(severidades(achados)).contains(Severidade.CRITICA);
        assertThat(descricoes(achados)).anyMatch(d -> d.contains("RTF com extensão .doc"));
    }

    @Test
    void docSemMacroNaoGeraAchados() {
        String ole = "ÐÏ\u0011à¡±\u001Aá" + utf16("WordDocument") + "texto comum";

        assertThat(new TesteDocumentoWord().executar(contexto("carta.doc", ole))).isEmpty();
    }

    @Test
    void executavelEmpacotadoComInjecaoDeCodigo() {
        String pe = montarPe("UPX0", 0xE0000060L) + "VirtualAllocEx\u0000WriteProcessMemory\u0000";

        List<Achado> achados = new TesteExecutavelPe().executar(contexto("fatura.pdf.exe", pe));

        assertThat(descricoes(achados))
                .anyMatch(d -> d.startsWith("Extensão dupla"))
                .anyMatch(d -> d.startsWith("Executável empacotado"))
                .anyMatch(d -> d.startsWith("Seção gravável e executável"))
                .anyMatch(d -> d.startsWith("Injeção de código"));
    }

    @Test
    void executavelSemCabecalhoPe() {
        List<Achado> achados = new TesteExecutavelPe().executar(contexto("app.exe", "MZ" + "\u0000".repeat(100)));

        assertThat(descricoes(achados)).anyMatch(d -> d.startsWith("Cabeçalho PE ausente"));
    }

    @Test
    void executavelLimpoNaoGeraAchados() {
        String pe = montarPe(".text", 0x60000020L);

        assertThat(new TesteExecutavelPe().executar(contexto("app.exe", pe))).isEmpty();
    }

    @Test
    void zipComExecutavelDisfarcadoEZipSlip() throws IOException {
        String zip = montarZip("documentos/fatura.pdf.exe", "../../etc/cron.d/x", "leia.txt");

        List<Achado> achados = new TesteConteudoZip().executar(contexto("pacote.zip", zip));

        assertThat(severidades(achados)).contains(Severidade.CRITICA);
        assertThat(descricoes(achados))
                .anyMatch(d -> d.startsWith("Executáveis ou scripts"))
                .anyMatch(d -> d.contains("zip slip"));
    }

    @Test
    void zipBomb() throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(saida)) {
            zip.putNextEntry(new ZipEntry("zeros.bin"));
            zip.write(new byte[5_000_000]);
            zip.closeEntry();
        }

        List<Achado> achados = new TesteConteudoZip().executar(
                contexto("pacote.zip", saida.toString(StandardCharsets.ISO_8859_1)));

        assertThat(descricoes(achados)).anyMatch(d -> d.startsWith("Possível zip bomb"));
    }

    @Test
    void zipComDocumentosNaoGeraAchados() throws IOException {
        String zip = montarZip("relatorio.pdf", "planilha.xlsx");

        assertThat(new TesteConteudoZip().executar(contexto("pacote.zip", zip))).isEmpty();
    }

    @Test
    void polyglotIgnoraAssinaturaDoProprioFormato() throws IOException {
        TestePolyglotMagicBytes teste = new TestePolyglotMagicBytes();

        assertThat(teste.executar(contexto("app.exe", montarPe(".text", 0x60000020L)))).isEmpty();
        assertThat(teste.executar(contexto("carta.doc", "\u00D0\u00CF\u0011\u00E0\u00A1\u00B1\u001A\u00E1"))).isEmpty();
        assertThat(teste.executar(contexto("pacote.zip", montarZip("leia.txt", "fatura.pdf")))).isEmpty();
    }

    @Test
    void polyglotDetectaExecutavelDisfarcado() {
        List<Achado> achados = new TestePolyglotMagicBytes()
                .executar(contexto("fatura.pdf", montarPe(".text", 0x60000020L)));

        assertThat(descricoes(achados))
                .anyMatch(d -> d.startsWith("Executável do Windows embutido"))
                .anyMatch(d -> d.contains("enviado como .pdf"));
    }

    @Test
    void testesNovosIgnoramOutrosTipos() {
        ContextoAnalise pdf = contexto("documento.pdf", "%PDF-1.7 conteúdo");

        assertThat(new TesteDocumentoWord().executar(pdf)).isEmpty();
        assertThat(new TesteExecutavelPe().executar(pdf)).isEmpty();
        assertThat(new TesteConteudoZip().executar(pdf)).isEmpty();
    }

    private static ContextoAnalise contexto(String nome, String conteudo) {
        return ContextoAnalise.de(nome, "application/octet-stream", conteudo.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static List<Severidade> severidades(List<Achado> achados) {
        return achados.stream().map(Achado::severidade).toList();
    }

    private static List<String> descricoes(List<Achado> achados) {
        return achados.stream().map(Achado::descricao).toList();
    }

    private static String utf16(String texto) {
        return new String(texto.getBytes(StandardCharsets.UTF_16LE), StandardCharsets.ISO_8859_1);
    }

    /** Executável mínimo: cabeçalho DOS, cabeçalho PE sem opcional e uma seção. */
    private static String montarPe(String nomeSecao, long caracteristicas) {

        byte[] bytes = new byte[0x40 + 24 + 40];
        bytes[0] = 'M';
        bytes[1] = 'Z';
        bytes[0x3C] = 0x40;

        int pe = 0x40;
        bytes[pe] = 'P';
        bytes[pe + 1] = 'E';
        bytes[pe + 6] = 1;

        int secao = pe + 24;
        byte[] nome = nomeSecao.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(nome, 0, bytes, secao, nome.length);

        for (int i = 0; i < 4; i++) {
            bytes[secao + 36 + i] = (byte) (caracteristicas >> (8 * i));
        }

        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private static String montarZip(String... nomes) throws IOException {

        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(saida)) {
            for (String nome : nomes) {
                zip.putNextEntry(new ZipEntry(nome));
                zip.write(("conteúdo de " + nome).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }

        return saida.toString(StandardCharsets.ISO_8859_1);
    }
}
