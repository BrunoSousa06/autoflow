-- Corrige o estado do seed do reparo adicional PENDENTE_APROVACAO da OS-8.
-- O serviço "Troca de velas de ignição" foi incorretamente inserido na OS antes da aprovação.
-- Para um reparo em PENDENTE_APROVACAO, o serviço deve existir sem ordem_servico_id,
-- sendo copiado para a OS apenas ao aprovar o orçamento complementar.
UPDATE ordem_servico_servico_solicitado ss
SET ordem_servico_id = NULL
FROM reparo_adicional ra
JOIN ordem_servico os ON os.id = ra.ordem_servico_id AND os.numero_os = 'OS-1746144000008'
WHERE ss.reparo_adicional_id = ra.id
  AND ra.status = 'PENDENTE_APROVACAO'
  AND ss.nome = 'Troca de velas de ignição';

UPDATE ordem_servico_servico_solicitado ss
SET ordem_servico_id = NULL
    FROM reparo_adicional ra
WHERE ss.reparo_adicional_id = ra.id
  AND ra.status = 'PENDENTE_APROVACAO'
  AND ss.ordem_servico_id IS NOT NULL;
