package com.wormless.config;

import com.wormless.entities.AnalistaSOC;
import com.wormless.entities.RegraDeteccao;
import com.wormless.repository.RegraDeteccaoRepository;
import com.wormless.repository.UsuarioRepository;
import com.wormless.security.Role;
import com.wormless.security.User;
import com.wormless.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository authUserRepository,
            UsuarioRepository usuarioRepository,
            RegraDeteccaoRepository regraDeteccaoRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String email = "soc@wormless.com";

            // 1. Cria a conta de Login (tabela auth_users) com a senha encriptada
            if (authUserRepository.findByEmail(email).isEmpty()) {
                User authUser = new User(email, passwordEncoder.encode("123"), Role.SOC_ANALYST);
                authUserRepository.save(authUser);
                System.out.println("->Usuário de Autenticação criado: " + email);
            }

            // 2. Cria o Perfil de Negócio (tabela users e analistas_soc)
            if (usuarioRepository.findByEmail(email).isEmpty()) {
                AnalistaSOC analista = new AnalistaSOC();
                analista.setNome("Analista Teste");
                analista.setEmail(email);
                usuarioRepository.save(analista);
                System.out.println("->Perfil de Analista SOC criado.");
            }

            // 3. Cria a Regra de Detecção padrão (tabela regras_deteccao)

            if (regraDeteccaoRepository.count() == 0) {
                RegraDeteccao r1 = new RegraDeteccao();
                r1.setPadrao("/OpenAction");
                r1.setAtiva(true);

                RegraDeteccao r2 = new RegraDeteccao();
                r2.setPadrao("/Launch");
                r2.setAtiva(true);

                RegraDeteccao r3 = new RegraDeteccao();
                r3.setPadrao("/JavaScript");
                r3.setAtiva(true);

                RegraDeteccao r4 = new RegraDeteccao();
                r4.setPadrao("cmd.exe");
                r4.setAtiva(true);

                RegraDeteccao r5 = new RegraDeteccao();
                r5.setPadrao("powershell.exe -enc");
                r5.setAtiva(true);

                RegraDeteccao r6 = new RegraDeteccao();
                r6.setPadrao("/EmbeddedFiles");
                r6.setAtiva(true);

                regraDeteccaoRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6));
                
                System.out.println("->Múltiplas regras de deteção padrão criadas.");
            }
        };
    }
}