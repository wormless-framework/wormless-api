package com.wormless.services;

import com.wormless.models.Arquivo;
import com.wormless.repositories.ArquivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ArquivoService {

    @Autowired
    private ArquivoRepository arquivoRepository;

    public Arquivo processarEAnalisarArquivo(MultipartFile multipartFile, String origem) throws IOException, NoSuchAlgorithmException {
        String hashSha256 = calcularHashSha256(multipartFile);

        Optional<Arquivo> arquivoExistente = arquivoRepository.findByHashSha256(hashSha256);
        if (arquivoExistente.isPresent()) {
            return arquivoExistente.get();
        }

        Arquivo arquivo = new Arquivo();
        arquivo.setHashSha256(hashSha256);
        arquivo.setNomeArquivo(multipartFile.getOriginalFilename());
        arquivo.setDataHora(LocalDateTime.now());
        arquivo.setOrigem(origem);

        // TODO: Integrar com a API do VirusTotal e com a LLM
        
        
        return arquivoRepository.save(arquivo);
    }

    public List<Arquivo> listarTodos() {
        return arquivoRepository.findAll();
    }

    // Método utilitário para calcular o SHA-256 de um MultipartFile
    private String calcularHashSha256(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(file.getBytes());
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}