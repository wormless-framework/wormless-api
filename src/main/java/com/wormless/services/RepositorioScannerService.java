package com.wormless.services;

import com.wormless.dto.request.RepositorioRequestDTO;
import com.wormless.dto.response.ArquivoSuspeitoDTO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class RepositorioScannerService {

    public List<ArquivoSuspeitoDTO> fazerTriagemRapida(RepositorioRequestDTO request) {
        log.info("Iniciando clone do repositório: {}", request.getUrl());
        List<ArquivoSuspeitoDTO> arquivosSuspeitos = new ArrayList<>();
        Path diretorioTemp = null;

        try {
            // 1. Cria um diretório temporário para o clone
            diretorioTemp = Files.createTempDirectory("wormless-repo-");

            // 2. Faz o clone do repositório
            Git.cloneRepository()
                    .setURI(request.getUrl())
                    .setDirectory(diretorioTemp.toFile())
                    .setCloneAllBranches(false)
                    .call();

            log.info("Clone finalizado. Iniciando varredura em: {}", diretorioTemp);

            // 3. Percorre todos os arquivos clonados
            Path finalDiretorioTemp = diretorioTemp;
            try (Stream<Path> paths = Files.walk(diretorioTemp)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> !path.toString().contains(".git")) // Ignora a pasta .git interna
                     .forEach(path -> {
                         try {
                             String nomeArquivo = path.getFileName().toString();
                             String caminhoRelativo = finalDiretorioTemp.relativize(path).toString();
                             
                             // Lemos o conteúdo do arquivo como String
                             String conteudo = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);

                             // 4. Aplica as regras de Triagem (Shallow Scan)
                             String motivo = analisarConteudo(nomeArquivo, conteudo);
                             
                             if (motivo != null) {
                                 arquivosSuspeitos.add(new ArquivoSuspeitoDTO(nomeArquivo, caminhoRelativo, motivo));
                             }
                         } catch (Exception e) {
                             // Ignora arquivos binários pesados ou ilegíveis na triagem rápida
                             log.debug("Ignorando arquivo ilegível na triagem: {}", path);
                         }
                     });
            }

        } catch (Exception e) {
            log.error("Erro ao clonar ou analisar repositório: ", e);
            throw new RuntimeException("Falha na triagem do repositório Git.");
        } finally {
            // 5. Limpa o disco apagando a pasta temporária
            if (diretorioTemp != null) {
                try {
                    FileSystemUtils.deleteRecursively(diretorioTemp);
                } catch (IOException e) {
                    log.warn("Não foi possível apagar o diretório temporário: {}", diretorioTemp);
                }
            }
        }

        return arquivosSuspeitos;
    }

    
   private String analisarConteudo(String nomeArquivo, String conteudo) {
        String nomeMinusculo = nomeArquivo.toLowerCase();

        if (nomeMinusculo.endsWith(".pdf")) {
            return "Ficheiro PDF encontrado";
        }
        if (nomeMinusculo.endsWith(".sh")) {
            return "Potencial injeção de comandos";
        }
        if (conteudo.contains("BEGIN RSA PRIVATE KEY") || conteudo.contains("BEGIN OPENSSH PRIVATE KEY")) {
            return "Chave privada exposta no código-fonte";
        }
        
        return null;
    }
}