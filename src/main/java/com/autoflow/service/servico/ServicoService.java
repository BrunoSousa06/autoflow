package com.autoflow.service.servico;


import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.mapper.ServicoMapper;
import com.autoflow.repository.servico.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    public ServicoResponse cadastrar(ServicoRequest request) {

        if (servicoRepository.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Serviço já foi cadastrado");
        }

        ServicoEntity entity = servicoRepository.save(servicoMapper.mapToEntity(request));

        return servicoMapper.toResponse(entity);
    }

    public List<ServicoResponse> listar() {
        return servicoMapper.toResponseList(servicoRepository.findAll());
    }

    public ServicoResponse buscarPorId(Long id) {
        return servicoMapper.toResponse(buscarServicoPorId(id));
    }

    public ServicoEntity buscarEntityPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + id
                ));
    }
    public ServicoResponse atualizar(ServicoRequest request, Long id) {

        ServicoEntity servico = buscarServicoPorId(id);

        servicoMapper.updateEntity(request, servico);

        ServicoEntity atualizado = servicoRepository.save(servico);

        return servicoMapper.toResponse(atualizado);
    }

    public void deletar(Long id) {

        if (!servicoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ID informado para exclusão não existe: " + id);
        }

        servicoRepository.deleteById(id);
    }

    private ServicoEntity buscarServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado com o ID: " + id)
                );
    }
}
