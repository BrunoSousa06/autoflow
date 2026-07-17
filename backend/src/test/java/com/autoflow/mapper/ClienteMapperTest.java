package com.autoflow.mapper;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.ClienteMapper;
import com.autoflow.presentation.cliente.request.ClienteRequest;
import com.autoflow.presentation.cliente.response.ClienteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClienteMapperTest {

    private ClienteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ClienteMapper.class);
    }

    @Test
    void shouldMapClienteInputToClienteEntity() {
        ClienteInput input = new ClienteInput("Nome Teste", "12345678901", "999999999", "teste@email.com");
        ClienteEntity entity = mapper.mapToEntity(input);

        assertNotNull(entity);
        assertEquals(input.nome(), entity.getNome());
        assertEquals(input.cpfCnpj(), entity.getCpfCnpj());
        assertEquals(input.telefone(), entity.getTelefone());
        assertEquals(input.email(), entity.getEmail());
    }

    @Test
    void shouldMapClienteEntityToClienteOutput() {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(1L);
        entity.setNome("Nome Teste");
        entity.setCpfCnpj("12345678901");
        entity.setTelefone("999999999");
        entity.setEmail("teste@email.com");

        ClienteOutput output = mapper.mapToOutput(entity);

        assertNotNull(output);
        assertEquals(entity.getId(), output.id());
        assertEquals(entity.getNome(), output.nome());
        assertEquals(entity.getCpfCnpj(), output.cpfCnpj());
        assertEquals(entity.getTelefone(), output.telefone());
        assertEquals(entity.getEmail(), output.email());
    }

    @Test
    void shouldMapClienteRequestToClienteInput() {
        ClienteRequest request = new ClienteRequest("Nome Request", "11122233344", "888888888", "request@email.com");
        ClienteInput input = mapper.mapToInput(request);

        assertNotNull(input);
        assertEquals(request.nome(), input.nome());
        assertEquals(request.cpfCnpj(), input.cpfCnpj());
        assertEquals(request.telefone(), input.telefone());
        assertEquals(request.email(), input.email());
    }

    @Test
    void shouldUpdateClienteEntityFromClienteInput() {
        ClienteInput input = new ClienteInput("Nome Atualizado", "98765432109", "777777777", "atualizado@email.com");
        ClienteEntity entity = new ClienteEntity();
        entity.setId(1L);
        entity.setNome("Nome Antigo");
        entity.setCpfCnpj("12345678901");
        entity.setTelefone("111111111");
        entity.setEmail("antigo@email.com");

        mapper.updateEntity(input, entity);

        assertEquals(1L, entity.getId()); // ID should remain unchanged
        assertEquals(input.nome(), entity.getNome());
        assertEquals(input.cpfCnpj(), entity.getCpfCnpj());
        assertEquals(input.telefone(), entity.getTelefone());
        assertEquals(input.email(), entity.getEmail());
    }

    @Test
    void shouldMapClienteOutputToClienteResponse() {
        ClienteOutput output = ClienteOutput.builder()
                .id(1L)
                .nome("Output Nome")
                .cpfCnpj("12312312312")
                .telefone("123456789")
                .email("output@email.com")
                .build();

        ClienteResponse response = mapper.maptoResponse(output);

        assertNotNull(response);
        assertEquals(output.id(), response.id());
        assertEquals(output.nome(), response.nome());
        assertEquals(output.cpfCnpj(), response.cpfCnpj());
        assertEquals(output.telefone(), response.telefone());
        assertEquals(output.email(), response.email());
    }

    @Test
    void shouldMapListOfClienteEntitiesToListOfClienteOutputs() {
        ClienteEntity entity1 = new ClienteEntity();
        entity1.setId(1L);
        entity1.setNome("Entity 1");
        entity1.setCpfCnpj("11111111111");
        entity1.setEmail("e1@email.com");

        ClienteEntity entity2 = new ClienteEntity();
        entity2.setId(2L);
        entity2.setNome("Entity 2");
        entity2.setCpfCnpj("22222222222");
        entity2.setEmail("e2@email.com");

        List<ClienteEntity> entities = Arrays.asList(entity1, entity2);
        List<ClienteOutput> outputs = mapper.mapToListOutput(entities);

        assertNotNull(outputs);
        assertEquals(2, outputs.size());
        assertEquals(entity1.getId(), outputs.get(0).id());
        assertEquals(entity2.getId(), outputs.get(1).id());
        assertEquals(entity1.getNome(), outputs.get(0).nome());
        assertEquals(entity2.getNome(), outputs.get(1).nome());
    }

    @Test
    void shouldMapListOfClienteEntitiesToListOfClienteResponses() {
        ClienteEntity entity1 = new ClienteEntity();
        entity1.setId(1L);
        entity1.setNome("Entity 1");
        entity1.setCpfCnpj("11111111111");
        entity1.setEmail("e1@email.com");

        ClienteEntity entity2 = new ClienteEntity();
        entity2.setId(2L);
        entity2.setNome("Entity 2");
        entity2.setCpfCnpj("22222222222");
        entity2.setEmail("e2@email.com");

        List<ClienteEntity> entities = Arrays.asList(entity1, entity2);
        List<ClienteResponse> responses = mapper.mapToList(entities);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(entity1.getId(), responses.get(0).id());
        assertEquals(entity2.getId(), responses.get(1).id());
        assertEquals(entity1.getNome(), responses.get(0).nome());
        assertEquals(entity2.getNome(), responses.get(1).nome());
    }

    @Test
    void shouldMapOptionalClienteEntityToOptionalClienteOutput_present() {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(1L);
        entity.setNome("Optional Test");
        entity.setCpfCnpj("12345678901");
        entity.setEmail("optional@email.com");

        Optional<ClienteOutput> outputOpt = mapper.mapToOutputOpt(Optional.of(entity));

        assertTrue(outputOpt.isPresent());
        assertEquals(entity.getId(), outputOpt.get().id());
        assertEquals(entity.getNome(), outputOpt.get().nome());
    }

    @Test
    void shouldMapOptionalClienteEntityToOptionalClienteOutput_empty() {
        Optional<ClienteOutput> outputOpt = mapper.mapToOutputOpt(Optional.empty());
        assertFalse(outputOpt.isPresent());
    }
}
