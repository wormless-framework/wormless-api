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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Triagem dos indicadores de rede presentes no documento.
 *
 * <p>Procura três sinais simples de comando e controle ou de phishing: URL
 * apontando para um endereço IP cru, link encurtado e caminho de rede SMB.</p>
 */
@Component
public class TesteIndicadoresRede extends TesteSegurancaTemplate {

    private static final Pattern URL_COM_IP = Pattern.compile(
            "\\b(https?|ftp)://(\\d{1,3}\\.){3}\\d{1,3}",
            Pattern.CASE_INSENSITIVE);

    /** Captura o nome do servidor da URL, sem porta nem caminho. */
    private static final Pattern HOST_DA_URL = Pattern.compile(
            "\\bhttps?://([\\w.-]+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CAMINHO_SMB = Pattern.compile("\\\\\\\\[\\w.-]+\\\\[\\w$.-]+");

    private static final Set<String> ENCURTADORES = Set.of(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "cutt.ly",
            "rb.gy", "ow.ly", "shorturl.at", "rebrand.ly");

    @Override
    public String id() {
        return "IOC_REDE";
    }

    @Override
    public String nome() {
        return "Indicadores de rede e comando e controle";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.REDE;
    }

    @Override
    public int ordem() {
        return 70;
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        List<Achado> achados = new ArrayList<>();
        String conteudo = contexto.conteudo();

        Matcher comIp = URL_COM_IP.matcher(conteudo);

        if (comIp.find()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1071_001,
                    "Endereço IP direto: o documento aponta para um servidor sem nome de domínio, "
                            + "prática comum em infraestrutura descartável de atacantes.",
                    comIp.group()
            ));
        }

        Matcher host = HOST_DA_URL.matcher(conteudo);

        while (host.find()) {
            if (ENCURTADORES.contains(host.group(1).toLowerCase(Locale.ROOT))) {
                achados.add(achado(
                        Severidade.ALTA,
                        TecnicaMitre.T1566_002,
                        "Link encurtado: o destino real fica escondido até o clique.",
                        host.group()
                ));
                break;
            }
        }

        Matcher smb = CAMINHO_SMB.matcher(conteudo);

        if (smb.find()) {
            achados.add(achado(
                    Severidade.ALTA,
                    TecnicaMitre.T1187,
                    "Caminho de rede compartilhado: ao abrir, o leitor pode enviar as credenciais do usuário "
                            + "para o servidor indicado.",
                    smb.group()
            ));
        }

        return achados;
    }
}
