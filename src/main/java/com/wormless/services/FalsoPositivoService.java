package com.wormless.services;

import com.wormless.dto.request.MarcarFalsoPositivoDTO;
import com.wormless.entities.AnalistaSOC;
import com.wormless.entities.FalsoPositivo;
import com.wormless.entities.IndicadorAmeaca;
import com.wormless.entities.RegraDeteccao;
import com.wormless.entities.Usuario;
import com.wormless.repository.FalsoPositivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FalsoPositivoService {

    private final FalsoPositivoRepository falsoPositivoRepository;
    private final IndicadorAmeacaService indicadorAmeacaService;
    private final RegraDeteccaoService regraDeteccaoService;
    private final UsuarioService usuarioService;

    public FalsoPositivoService(
            FalsoPositivoRepository falsoPositivoRepository,
            IndicadorAmeacaService indicadorAmeacaService,
            RegraDeteccaoService regraDeteccaoService,
            UsuarioService usuarioService) {
        this.falsoPositivoRepository = falsoPositivoRepository;
        this.indicadorAmeacaService = indicadorAmeacaService;
        this.regraDeteccaoService = regraDeteccaoService;
        this.usuarioService = usuarioService;
    }

    public FalsoPositivo marcarComoFalsoPositivo(MarcarFalsoPositivoDTO dto) {
        // 1. Busca as entidades relacionadas
        IndicadorAmeaca indicador = indicadorAmeacaService.buscarPorId(dto.getIndicadorId());
        RegraDeteccao regra = regraDeteccaoService.buscarPorId(dto.getRegraDeteccaoId());
        Usuario usuario = usuarioService.buscarPorId(dto.getAnalistaId());

        if (!(usuario instanceof AnalistaSOC)) {
            throw new IllegalArgumentException("O usuário informado não é um Analista SOC.");
        }

        // 2. Instancia e preenche o Falso Positivo
        FalsoPositivo falsoPositivo = new FalsoPositivo();
        falsoPositivo.setJustificativaTecnica(dto.getJustificativaTecnica());
        falsoPositivo.setDataMarcacao(LocalDateTime.now());
        falsoPositivo.setAnalistaSOC((AnalistaSOC) usuario);
        falsoPositivo.setIndicadorAmeaca(indicador);
        falsoPositivo.setRegraDeteccao(regra);

        // 3. Aplica a inativação da regra e mudança de status do indicador
        falsoPositivo.aplicar();

        // 4. Salva no banco de dados
        return falsoPositivoRepository.save(falsoPositivo);
    }
    
    public FalsoPositivo salvar(FalsoPositivo falsoPositivo) {
        if (falsoPositivo.getDataMarcacao() == null) {
            falsoPositivo.setDataMarcacao(LocalDateTime.now());
        }

        falsoPositivo.aplicar();

        return falsoPositivoRepository.save(falsoPositivo);
    }

    public void excluir(Long id) {
        FalsoPositivo falsoPositivo = buscarPorId(id);

        // 1. Reverter o status do Indicador de volta para ATIVO
        if (falsoPositivo.getIndicadorAmeaca() != null) {
            falsoPositivo.getIndicadorAmeaca().setStatus(com.wormless.entities.enums.StatusIndicador.ATIVO);
        }

        // 2. Reativar a Regra de Detecção
        if (falsoPositivo.getRegraDeteccao() != null) {
            falsoPositivo.getRegraDeteccao().setAtiva(true);
        }

        // 3. Excluir o registo do falso positivo
        falsoPositivoRepository.delete(falsoPositivo);
    }

    @Transactional(readOnly = true)
    public FalsoPositivo buscarPorId(Long id) {
        return falsoPositivoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Falso positivo não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<FalsoPositivo> listarPorIndicador(
            Long indicadorId) {
        return falsoPositivoRepository
                .findByIndicadorAmeacaId(indicadorId);
    }

    @Transactional(readOnly = true)
    public List<FalsoPositivo> listarPorAnalista(
            Long analistaId) {
        return falsoPositivoRepository
                .findByAnalistaSOCId(analistaId);
    }
}