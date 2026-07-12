# Cobertura backend — baseline

## Configuração JaCoCo no momento da medição

- Plugin: `jacoco-maven-plugin` versão `0.8.12`
- Meta: `>= 80%` LINE e BRANCH (escopo: BUNDLE)
- Comando: `mvn verify`

## Exclusões ativas antes desta issue (estado inicial)

```xml
<exclude>com/autoflow/config/security/SecurityConfig.class</exclude>
<exclude>com/autoflow/mapper/**</exclude>
<exclude>com/autoflow/domain/**</exclude>   <!-- REMOVIDA nesta issue -->
<exclude>**/*Application.java</exclude>
```

## Exclusões após ajuste desta issue

```xml
<exclude>com/autoflow/config/security/SecurityConfig.class</exclude>
<exclude>com/autoflow/mapper/**</exclude>
<exclude>**/*Application.java</exclude>
```

A exclusão global de `com/autoflow/domain/**` foi removida. O domínio passa a ser
incluído na métrica JaCoCo.

## Resultado antes do ajuste

Não medido com as exclusões originais. O pipeline reportou `branches covered ratio is 0.78`
na primeira execução após remoção da exclusão `domain/**`, antes dos testes adicionais.
Isso confirma que a exclusão original escondia coverage insuficiente no domínio.

## Resultado após ajuste (com domínio incluído)

Executado em 12/07/2026. Comando: `mvn verify` — **BUILD SUCCESS**.

117 classes analisadas no bundle `autoflow`.

| Métrica JaCoCo | Missed | Total | Cobertura | Meta | Status |
|---|---|---|---|---|---|
| Instructions | 291 | 8.067 | 96% | — | ✅ |
| **Branches** | **76** | **494** | **84%** | **>= 80%** | **✅** |
| Lines | 58 | 1.778 | 97% | — | ✅ |
| Methods | 10 | 447 | 98% | — | ✅ |
| Classes | 1 | 117 | 99% | — | ✅ |

O domínio (`com.autoflow.domain.ordemservico`) atingiu **88% de branches** após
os cenários adicionados em `OrdemServicoEntityTest` e `ServicoSolicitadoEntityTest`.

## Testes de domínio existentes confirmados no repositório

| Classe testada | Arquivo de teste |
|---|---|
| `ServicoEntity` | `domain/servico/ServicoEntityTest.java` |
| `OrdemServicoEntity` | `domain/ordemservico/OrdemServicoEntityTest.java` |
| `OrcamentoEntity` | `domain/orcamento/OrcamentoEntityTest.java` |
| `PecaInsumoEntity` | `domain/pecainsumo/PecaInsumoEntityTest.java` |
| `VeiculoEntity` | `domain/veiculo/VeiculoEntityTest.java` |
| `ClienteEntity` | `domain/cliente/ClienteEntityTest.java` |
| `UsuarioEntity` | `domain/usuario/UsuarioEntityTest.java` |
| `NotificacaoEntity` | `domain/notificacao/NotificacaoEntityTest.java` |
| `HistoricoStatusOsEntity` | `domain/ordemservico/HistoricoStatusOsEntityTest.java` |
| `ReparoAdicionalEntity` | `domain/ordemservico/reparoadicional/ReparoAdicionalEntityTest.java` |
| `ServicoSolicitadoEntity` | `domain/ordemservico/ServicoSolicitadoEntityTest.java` |

## Próximos passos

1. Executar `mvn verify` e preencher os valores acima.
2. Se cobertura cair abaixo de 80 %: identificar classes de domínio sem cobertura no relatório
   `backend/target/site/jacoco/` e criar testes correspondentes.
3. Após cobertura estável >= 80 %: commitar o `pom.xml` ajustado.