package com.autoflow.domain.cliente;

import com.autoflow.domain.veiculo.VeiculoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


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
