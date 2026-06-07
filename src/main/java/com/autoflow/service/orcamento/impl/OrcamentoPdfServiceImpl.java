package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.OrcamentoItemNecessarioEntity;
import com.autoflow.domain.orcamento.OrcamentoServicoEntity;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class OrcamentoPdfServiceImpl implements OrcamentoPdfService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OrcamentoPdfServiceImpl(OrdemServicoRepository ordemServicoRepository) {
    }

    @Override
    public byte[] gerarPdf(OrcamentoEntity orcamento) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            adicionarTitulo(document);
            adicionarDadosOrcamento(document, orcamento);
            adicionarServicos(document, orcamento.getServicos());
            adicionarItens(document, orcamento.getItens());
            adicionarTotais(document, orcamento);
            adicionarRodape(document);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao gerar PDF do orçamento.", exception);
        }
    }

    private void adicionarTitulo(Document document) throws DocumentException {
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

        Paragraph titulo = new Paragraph("Orçamento - AutoFlow", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);

        document.add(titulo);
    }

    private void adicionarDadosOrcamento(
            Document document,
            OrcamentoEntity orcamento
    ) throws DocumentException {
        Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Orçamento: #" + orcamento.getId(), textoFont));
        document.add(new Paragraph("Ordem de Serviço: #" + orcamento.getOrdemServicoId(), textoFont));
        document.add(new Paragraph("Tipo: " + orcamento.getTipo(), textoFont));
        document.add(new Paragraph("Versão: " + orcamento.getVersao(), textoFont));
        document.add(new Paragraph("Status: " + orcamento.getStatus(), textoFont));

        if (orcamento.getCriadoEm() != null) {
            document.add(new Paragraph(
                    "Criado em: " + orcamento.getCriadoEm().format(DATA_HORA_FORMATTER),
                    textoFont
            ));
        }

        if (orcamento.getDisponibilizadoEm() != null) {
            document.add(new Paragraph(
                    "Disponibilizado em: " + orcamento.getDisponibilizadoEm().format(DATA_HORA_FORMATTER),
                    textoFont
            ));
        }

        Paragraph espaco = new Paragraph(" ");
        espaco.setSpacingAfter(10);
        document.add(espaco);
    }

    private void adicionarServicos(
            Document document,
            List<OrcamentoServicoEntity> servicos
    ) throws DocumentException {
        Paragraph subtitulo = new Paragraph(
                "Serviços",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)
        );
        subtitulo.setSpacingBefore(10);
        subtitulo.setSpacingAfter(8);
        document.add(subtitulo);

        if (servicos == null || servicos.isEmpty()) {
            document.add(new Paragraph("Nenhum serviço informado."));
            return;
        }

        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{5, 2});

        tabela.addCell("Descrição");
        tabela.addCell("Valor");

        for (OrcamentoServicoEntity servico : servicos) {
            tabela.addCell(String.valueOf(servico.getNome()));
            tabela.addCell(formatarMoeda(servico.getValor()));
        }

        document.add(tabela);
    }

    private void adicionarItens(
            Document document,
            List<OrcamentoItemNecessarioEntity> itens
    ) throws DocumentException {
        Paragraph subtitulo = new Paragraph(
                "Itens necessários",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)
        );
        subtitulo.setSpacingBefore(20);
        subtitulo.setSpacingAfter(8);
        document.add(subtitulo);

        if (itens == null || itens.isEmpty()) {
            document.add(new Paragraph("Nenhum item necessário informado."));
            return;
        }

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{5, 2, 2, 2});

        tabela.addCell("Descrição");
        tabela.addCell("Quantidade");
        tabela.addCell("Valor unitário");
        tabela.addCell("Total");

        for (OrcamentoItemNecessarioEntity item : itens) {
            tabela.addCell(item.getNome());
            tabela.addCell(String.valueOf(item.getQuantidade()));
            tabela.addCell(formatarMoeda(item.getValorUnitario()));
            tabela.addCell(formatarMoeda(item.getValorTotal()));
        }

        document.add(tabela);
    }

    private void adicionarTotais(
            Document document,
            OrcamentoEntity orcamento
    ) throws DocumentException {
        Paragraph espaco = new Paragraph(" ");
        espaco.setSpacingBefore(15);
        document.add(espaco);

        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

        Paragraph totalServicos = new Paragraph(
                "Total serviços: " + formatarMoeda(orcamento.getTotalServicos()),
                totalFont
        );
        totalServicos.setAlignment(Element.ALIGN_RIGHT);

        Paragraph totalItens = new Paragraph(
                "Total itens: " + formatarMoeda(orcamento.getTotalItens()),
                totalFont
        );
        totalItens.setAlignment(Element.ALIGN_RIGHT);

        Paragraph totalGeral = new Paragraph(
                "Total geral: " + formatarMoeda(orcamento.getTotalGeral()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)
        );
        totalGeral.setAlignment(Element.ALIGN_RIGHT);

        document.add(totalServicos);
        document.add(totalItens);
        document.add(totalGeral);
    }

    private void adicionarRodape(Document document) throws DocumentException {
        Paragraph rodape = new Paragraph(
                "Este orçamento foi gerado automaticamente pelo sistema AutoFlow.",
                FontFactory.getFont(FontFactory.HELVETICA, 10)
        );
        rodape.setSpacingBefore(30);
        rodape.setAlignment(Element.ALIGN_CENTER);

        document.add(rodape);
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorSeguro = valor != null ? valor : BigDecimal.ZERO;

        return NumberFormat
                .getCurrencyInstance(new Locale("pt", "BR"))
                .format(valorSeguro);
    }
}
