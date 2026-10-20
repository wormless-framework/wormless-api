package com.wormless.analise.testes;

import com.wormless.analise.Achado;
import com.wormless.analise.CategoriaTeste;
import com.wormless.analise.ContextoAnalise;
import com.wormless.analise.TesteSegurancaTemplate;
import com.wormless.analise.mitre.TecnicaMitre;
import com.wormless.entities.enums.Severidade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Verificação de reputação do artefato pelo hash SHA-256.
 *
 * <p>Compara o hash do arquivo com uma lista de bloqueio configurável em
 * {@code wormless.seguranca.hashes-maliciosos}. É o ponto de extensão natural
 * para alimentar a plataforma com listas de IOC de um feed de inteligência sem
 * mexer em código.</p>
 *
 * <p>Diferente dos demais testes, este não olha o conteúdo: um artefato já
 * catalogado é bloqueado mesmo que nenhuma outra verificação aponte nada.</p>
 */
@Component
public class TesteReputacaoHash extends TesteSegurancaTemplate {

    private final Set<String> hashesBloqueados;

    public TesteReputacaoHash(
            @Value("${wormless.seguranca.hashes-maliciosos:}") String hashesConfigurados) {

        this.hashesBloqueados = Arrays.stream(hashesConfigurados.split(","))
                .map(String::trim)
                .filter(hash -> !hash.isEmpty())
                .map(hash -> hash.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String id() {
        return "REPUTACAO_HASH";
    }

    @Override
    public String nome() {
        return "Reputação do arquivo por hash SHA-256";
    }

    @Override
    public CategoriaTeste categoria() {
        return CategoriaTeste.REPUTACAO;
    }

    @Override
    public int ordem() {
        return 5;
    }

    @Override
    protected boolean aplicavel(ContextoAnalise contexto) {
        return !hashesBloqueados.isEmpty() && contexto.sha256() != null;
    }

    @Override
    protected List<Achado> detectar(ContextoAnalise contexto) {

        if (!hashesBloqueados.contains(contexto.sha256().toLowerCase(Locale.ROOT))) {
            return List.of();
        }

        return List.of(achado(
                Severidade.CRITICA,
                TecnicaMitre.T1204_002,
                "Arquivo já catalogado como malicioso: o hash consta na lista de bloqueio da plataforma.",
                "SHA-256 " + contexto.sha256()
        ));
    }
}
