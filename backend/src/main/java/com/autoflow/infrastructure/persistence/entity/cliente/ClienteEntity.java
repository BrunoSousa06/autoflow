package com.autoflow.infrastructure.persistence.entity.cliente;

import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
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
    @Column(unique = true, nullable = false)
    private String cpfCnpj;
    private String telefone;
    @Column(unique = true, nullable = false)
    private String email;
    @OneToMany(mappedBy = "cliente")
    List<VeiculoEntity> veiculos;
    @OneToOne
    @JoinColumn(name = "usuario_id")
    UsuarioEntity usuario;

}
