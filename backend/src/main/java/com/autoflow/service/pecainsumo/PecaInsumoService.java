package com.autoflow.service.pecainsumo;


import com.autoflow.controller.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.repository.pecainsumo.PecaInsumoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.repository.pecainsumo.PecaInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Page<PecaInsumoResponse> listarPaginado(Pageable pageable, String nome, CategoriaPecaInsumo tipo) {
        var spec = PecaInsumoSpecifications.comFiltros(nome, tipo);
        return pecaInsumoRepository.findAll(spec, pageable).map(pecaInsumoMapper::toResponse);
    }

    public PecaInsumoResponse buscarPorId(Long id) {
        return pecaInsumoMapper.toResponse(buscarEntidadePorId(id));
    }

    public PecaInsumoEntity buscarEntityPorId(Long id) {
        return buscarEntidadePorId(id);
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


    public BaixaEstoqueResult verificarDisponibilidadeEBaixar(List<ItemNecessarioEntity> itensNecessarios) {
        if (itensNecessarios == null || itensNecessarios.isEmpty()) {
            return new BaixaEstoqueResult(Collections.emptyList());
        }


        List<Long> ids = itensNecessarios.stream()
                .map(ItemNecessarioEntity::getPecaInsumoId)
                .distinct()
                .toList();

        Map<Long, PecaInsumoEntity> estoquePorId = pecaInsumoRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(
                        PecaInsumoEntity::getId,
                        Function.identity()
                ));
        List<PecaInsumoEntity> pecasAlteradas = new ArrayList<>();

        List<ItemNecessarioEntity> itensAtualizados = itensNecessarios.stream()
                .map(item -> {
                    PecaInsumoEntity itemEstoque = estoquePorId.get(item.getPecaInsumoId());

                    if (itemEstoque == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Peça/Insumo não encontrado com o ID: " + item.getPecaInsumoId()
                        );
                    }

                    boolean disponivel = itemEstoque.getQuantidade() >= item.getQuantidade();

                    if (disponivel) {
                        itemEstoque.setQuantidade(itemEstoque.getQuantidade() - item.getQuantidade());
                        pecasAlteradas.add(itemEstoque);
                    }

                    return ItemNecessarioEntity.criar(
                            itemEstoque.getId(),
                            itemEstoque.getNome(),
                            itemEstoque.getTipo(),
                            itemEstoque.getValor(),
                            item.getQuantidade(),
                            disponivel ? StatusItemNecessario.UTILIZADO : StatusItemNecessario.PENDENTE,
                            new SituacaoEstoque(
                                    itemEstoque.getQuantidade(),
                                    disponivel ? null : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE
                            )
                    );
                })
                .toList();

        pecaInsumoRepository.saveAll(pecasAlteradas);

        return new BaixaEstoqueResult(itensAtualizados);
    }
}
