package com.autoflow.service.servico;


import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.mapper.ServicoMapper;
import com.autoflow.infrastructure.persistence.repository.ServicoRepository;
import com.autoflow.infrastructure.persistence.repository.ServicoSolicitadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;
    private final ServicoSolicitadoRepository servicoSolicitadoRepository;
    public ServicoResponse cadastrar(ServicoRequest request) {

        if (servicoRepository.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço já foi cadastrado");
        }

        ServicoEntity entity = servicoRepository.save(servicoMapper.mapToEntity(request));

        return servicoMapper.toResponse(entity);
    }

    public Page<ServicoResponse> listar(Pageable pageable) {
        return servicoRepository.findAllByAtivoTrue(pageable)
                .map(servicoMapper::toResponse);
    }

    public ServicoResponse buscarPorId(Long id) {
        return servicoMapper.toResponse(buscarServicoPorId(id));
    }

    public ServicoEntity buscarEntityPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + id));
    }
    public ServicoResponse atualizar(ServicoRequest request, Long id) {

        ServicoEntity servico = buscarServicoPorId(id);

        servicoMapper.updateEntity(request, servico);

        ServicoEntity atualizado = servicoRepository.save(servico);

        return servicoMapper.toResponse(atualizado);
    }

    public void inativar(Long id) {
        ServicoEntity servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado com o ID: " + id));
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    private ServicoEntity buscarServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado com o ID: " + id));
    }

    public List<TempoMedioServicoResponse> listarTempoMedioPorServico() {


        return servicoSolicitadoRepository.calcularTempoMedioPorServico()
                .stream()
                .map(item -> {
                    Double tempoMedioSegundos = item.getTempoMedioSegundos();
                    Double tempoMedioMinutos = tempoMedioSegundos == null ? null : tempoMedioSegundos / 60;
                    Double tempoMedioHoras = tempoMedioSegundos == null ? null : tempoMedioSegundos / 3600;

                    return new TempoMedioServicoResponse(
                            item.getServicoId(),
                            item.getNomeServico(),
                            item.getQuantidadeExecucoes(),
                            tempoMedioSegundos,
                            tempoMedioMinutos,
                            tempoMedioHoras
                    );
                })
                .toList();
    }
}
