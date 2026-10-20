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

/**
 * Detecção de arquivos estranhos embutidos no documento (polyglot).
 *
 * <p>Procura assinaturas de formato — executáveis, instaladores, planilhas do
 * Office, scripts — em qualquer posição do arquivo. Um PDF que carrega um
 * executável dentro de si continua abrindo normalmente no leitor, e é
 * justamente esse disfarce que a verificação quebra.</p>
 *
 * <p>A assinatura do próprio formato declarado não conta como embutida: o
 * cabeçalho PE de um {@code .exe}, o contêiner OLE de um {@code .doc} e as
 * entradas de um {@code .zip} fazem parte da estrutura normal do arquivo.</p>
 */
@Component
public class TestePolyglotMagicBytes extends TesteSegurancaTemplate {

    /**
     * Assinatura de formato procurada dentro do conteúdo.
     *
     * @param formato     nome do formato identificado
     * @param assinatura  sequência de bytes, como texto ISO-8859-1
     * @param severidade  risco atribuído à presença do formato
     * @param tecnica     técnica MITRE correspondente
     * @param descricao   explicação em linguagem acessível
     * @param nativoDe    tipo de arquivo em que a assinatura é estrutura normal,
     *                    ou {@code null}
     */
    private record Assinatura(
            String formato,
            String assinatura,
            Severidade severidade,
            TecnicaMitre tecnica,
            String descricao,
            TipoArquivo nativoDe) {
    }

    private static final List<Assinatura> ASSINATURAS = List.of(
            new Assinatura("executável Windows (PE)", "PE\u0000\u0000", Severidade.CRITICA,
                    TecnicaMitre.T1204_002,
                    "Executável do Windows embutido: o documento transporta um programa pronto para rodar.",
                    TipoArquivo.EXE),
            new Assinatura("executável Linux (ELF)", "\u007FELF", Severidade.CRITICA,
                    TecnicaMitre.T1204_002,
                    "Executável do Linux embutido: o documento transporta um binário nativo.", null),
            new Assinatura("documento OLE do Office", "ÐÏ\u0011à¡±\u001Aá",
                    Severidade.ALTA, TecnicaMitre.T1204_002,
                    "Documento antigo do Office embutido: formato usado para entregar macros maliciosas.",
                    TipoArquivo.DOC),
            new Assinatura("pacote ZIP ou Office moderno", "PK\u0003\u0004", Severidade.MEDIA,
                    TecnicaMitre.T1027,
                    "Arquivo compactado embutido: pode esconder outros arquivos dentro do documento.",
                    TipoArquivo.ZIP),
            new Assinatura("instalador do Windows (MSI/CAB)", "MSCF", Severidade.ALTA,
                    TecnicaMitre.T1204_002,
                    "Pacote de instalação embutido: pode instalar software sem o conhecimento do usuário.", null),
            new Assinatura("script de shell", "#!/bin/", Severidade.ALTA,
                    TecnicaMitre.T1059_004,
                    "Script de shell embutido: o documento carrega comandos prontos para execução.", null),
            new Assinatura("script PHP", "<?php", Severidade.ALTA,
                    TecnicaMitre.T1059_007,
                    "Código PHP embutido: indica tentativa de usar o arquivo como webshell.", null),
            new Assinatura("classe Java", "Êþº¾", Severidade.ALTA,
                    TecnicaMitre.T1204_002,
                    "Bytecode Java embutido: o documento transporta código compilado.", null)
    );

    @Override
    public String id() {
        return "POLYGLOT_MAGIC";
    }

    @Override
    public String nome() {
        return "Formatos embutidos e arquivos polyglot";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.ESTRUTURAL;
    }

    @Override
    public int ordem() {
        return 60;
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        List<Achado> achados = new ArrayList<>();
        String conteudo = contexto.conteudo();
        TipoArquivo declarado = TipoArquivo.porNome(contexto.nomeArquivo()).orElse(null);

        for (Assinatura assinatura : ASSINATURAS) {

            if (assinatura.nativoDe() != null && assinatura.nativoDe() == declarado) {
                continue;
            }

            int posicao = conteudo.indexOf(assinatura.assinatura());

            if (posicao >= 0) {
                achados.add(achado(
                        assinatura.severidade(),
                        assinatura.tecnica(),
                        assinatura.descricao(),
                        assinatura.formato() + " no byte " + posicao
                ));
            }
        }

        if (conteudo.startsWith("MZ") && declarado != TipoArquivo.EXE) {
            String extensao = declarado == null ? "outro tipo" : "." + declarado.getExtensao();
            achados.add(achado(
                    Severidade.CRITICA,
                    TecnicaMitre.T1036_005,
                    "O arquivo é, na verdade, um executável do Windows enviado como "
                            + extensao + ".",
                    "assinatura MZ no byte 0"
            ));
        }

        return achados;
    }
}
