package com.autoflow.service.pecaInsumo;


import com.autoflow.controller.pecaInsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecaInsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
import com.autoflow.mapper.PecaInsumoMapper;
import com.autoflow.repository.PecaInsumo.PecaInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;
    private final PecaInsumoMapper pecaInsumoMapper;

    public PecaInsumoResponse cadastrar(PecaInsumoRequest request) {

        if (pecaInsumoRepository.findByNomeIgnoreCase(request.nome()).isPresent()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Peça/Insumo já foi cadastrado");
        }

        PecaInsumoEntity entity =
                pecaInsumoRepository.save(pecaInsumoMapper.mapToEntity(request));

        return pecaInsumoMapper.toResponse(entity);
    }

    public List<PecaInsumoResponse> listar() {
        return pecaInsumoMapper.toResponseList(pecaInsumoRepository.findAll());
    }

    public PecaInsumoResponse buscarPorId(Long id) {
        return pecaInsumoMapper.toResponse(buscarEntidadePorId(id));
    }

    public PecaInsumoResponse atualizar(PecaInsumoRequest request, Long id
    ) {

        PecaInsumoEntity entity = buscarEntidadePorId(id);

        pecaInsumoMapper.updateEntity(request, entity);

        PecaInsumoEntity atualizado = pecaInsumoRepository.save(entity);

        return pecaInsumoMapper.toResponse(atualizado);
    }

    public void deletar(Long id) {

        if (!pecaInsumoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça/Insumo não encontrado com o ID: " + id);
        }

        pecaInsumoRepository.deleteById(id);
    }

    private PecaInsumoEntity buscarEntidadePorId(Long id) {
        return pecaInsumoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Peça/Insumo não encontrado com o ID: " + id));
    }
}
