package com.autoflow.repository.cliente;

import com.autoflow.repository.veiculo.VeiculoEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String senha;
    private String cpf;
    private Long telefone;
    private String email;
    @OneToMany(mappedBy = "cliente")
    List<VeiculoEntity> veiculos;

}
