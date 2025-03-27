package org.financiai.JavaFxController;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.financiai.model.entities.Cliente;
import org.financiai.model.entities.Financiamento;
import org.financiai.model.entities.Imovel;
import org.financiai.model.entities.Parcelas; // Adicionado import
import org.financiai.model.enums.TipoAmortizacao;
import org.financiai.model.enums.TipoImovel;
import org.financiai.services.Price;
import org.financiai.services.SAC;
import org.financiai.util.GeradorPDF;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ControllerPage {

    @FXML
    public ImageView logoImage;
    @FXML
    private TextField nomeClienteField;
    @FXML
    private TextField cpfClienteField;
    @FXML
    private TextField rendaClienteField;
    @FXML
    private TextField valorImovelField;
    @FXML
    private TextField valorEntradaField;
    @FXML
    private TextField prazoField;
    @FXML
    private TextField taxaJurosField;
    @FXML
    private ChoiceBox<String> tipoFinanciamentoBox;
    @FXML
    private ChoiceBox<String> tipoImovelBox;
    @FXML
    private Label resultadoLabel;
    @FXML
    private TextArea tabelaParcelasArea;

    // Método para calcular o financiamento e gerar o PDF
    @FXML
    public void calcularFinanciamento(ActionEvent event) {
        try {
            // Obter dados do cliente e do financiamento
            String nomeCliente = nomeClienteField.getText();
            String cpfCliente = cpfClienteField.getText();
            double rendaMensal = Double.parseDouble(rendaClienteField.getText());
            double valorImovel = Double.parseDouble(valorImovelField.getText());
            double valorEntrada = Double.parseDouble(valorEntradaField.getText());
            double taxaJurosAnual = Double.parseDouble(taxaJurosField.getText());
            int prazo = Integer.parseInt(prazoField.getText());
            String tipoFinanciamento = tipoFinanciamentoBox.getValue();

            // Obter tipo de imóvel selecionado
            String tipoImovelString = tipoImovelBox.getValue();
            TipoImovel tipoImovel = TipoImovel.valueOf(tipoImovelString.toUpperCase()); // Converter para TipoImovel

            // Criar entidades Cliente e Imóvel
            Cliente cliente = new Cliente(nomeCliente, cpfCliente, rendaMensal);
            Imovel imovel = new Imovel(tipoImovel, valorImovel);

            // Calcular valor financiado
            double valorFinanciado = valorImovel - valorEntrada;

            // Determinar tipo de amortização
            TipoAmortizacao tipoAmortizacao = TipoAmortizacao.valueOf(tipoFinanciamento.toUpperCase());

            // Criar objeto Financiamento
            Financiamento financiamento = new Financiamento(prazo, taxaJurosAnual, tipoAmortizacao, valorEntrada, valorFinanciado, 0);

            // Calcular parcelas
            double taxaJurosMensal = taxaJurosAnual / 12 / 100;
            double limiteParcela = rendaMensal * 0.3;

            List<Parcelas> listaParcelas = new ArrayList<>(); // Lista de objetos Parcelas

            if (tipoAmortizacao == TipoAmortizacao.PRICE) {
                Price price = new Price();
                List<Double> parcelas = price.calculaParcela(valorFinanciado, taxaJurosMensal, prazo);
                List<Double> amortizacoes = price.calculaAmortizacao(valorFinanciado, taxaJurosMensal, prazo);

                // Preencher listaParcelas com objetos Parcelas
                for (int i = 0; i < parcelas.size(); i++) {
                    listaParcelas.add(new Parcelas(i + 1, parcelas.get(i), amortizacoes.get(i)));
                }

            } else if (tipoAmortizacao == TipoAmortizacao.SAC) {
                SAC sac = new SAC();
                List<Double> parcelasSac = sac.calculaParcela(valorFinanciado, taxaJurosMensal, prazo);
                List<Double> amortizacaoSac = sac.calculaAmortizacao(valorFinanciado, taxaJurosMensal, prazo);

                // Preencher listaParcelas com objetos Parcelas
                for (int i = 0; i < parcelasSac.size(); i++) {
                    listaParcelas.add(new Parcelas(i + 1, parcelasSac.get(i), amortizacaoSac.get(i)));
                }
            }

            // Verificar se o financiamento é aprovado
            if (listaParcelas.get(0).getValorParcela() > limiteParcela || listaParcelas.get(0).getValorAmortizacao() > limiteParcela) {
                resultadoLabel.setText("Financiamento não aprovado! A primeira parcela ou amortização excede 30% da renda mensal.");
                tabelaParcelasArea.setText("");
                return;
            }

            // Adicionar tabela de parcelas no TextArea
            StringBuilder tabelaParcelas = new StringBuilder("Parcela\tValor Parcela\tValor Amortização\n");
            for (Parcelas parcela : listaParcelas) {
                tabelaParcelas.append(parcela.getNumeroParcela())
                        .append("\tR$ ").append(String.format("%.2f", parcela.getValorParcela()))
                        .append("\tR$ ").append(String.format("%.2f", parcela.getValorAmortizacao())).append("\n");
            }

            tabelaParcelasArea.setText(tabelaParcelas.toString());
            resultadoLabel.setText("Financiamento aprovado!");

            // Calcular total a pagar
            double totalPagar = tipoAmortizacao == TipoAmortizacao.PRICE ? new Price().totalPagoPrice(valorFinanciado, taxaJurosMensal, prazo) : new SAC().totalPagoSac(valorFinanciado, taxaJurosMensal, prazo);
            financiamento.setTotalPagar(totalPagar);

            // Gerar PDF com as informações
            gerarPDF(cliente, imovel, listaParcelas, financiamento);

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Erro ao calcular financiamento: " + e.getMessage());
            tabelaParcelasArea.setText("");
        }
    }

    // Método para gerar o PDF usando iText
    public void gerarPDF(Cliente cliente, Imovel imovel, List<Parcelas> listaParcelas, Financiamento financiamento) {
        try {
            // Criar documento
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("relatorio_financiamento.pdf"));

            // Abrir o documento
            document.open();

            // Adicionar título
            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("Relatório de Financiamento", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            // Adicionar informações do cliente
            document.add(new Paragraph("Nome do Cliente: " + cliente.getNome()));
            document.add(new Paragraph("CPF do Cliente: " + cliente.getCpf()));
            document.add(new Paragraph("Renda Mensal: R$ " + cliente.getRendaMensal()));
            document.add(new Paragraph(" ")); // Espaço em branco

            // Adicionar informações do imóvel
            document.add(new Paragraph("Tipo de Imóvel: " + imovel.getTipoImovel()));
            document.add(new Paragraph("Valor do Imóvel: R$ " + imovel.getValorImovel()));
            document.add(new Paragraph("Valor de Entrada: R$ " + financiamento.getValorEntrada()));
            document.add(new Paragraph(" ")); // Espaço em branco

            // Adicionar informações do financiamento
            document.add(new Paragraph("Prazo: " + financiamento.getPrazo() + " meses"));
            document.add(new Paragraph("Taxa de Juros: " + financiamento.getTaxaJuros() + "%"));
            document.add(new Paragraph("Total a Pagar: R$ " + financiamento.getTotalPagar()));
            document.add(new Paragraph(" ")); // Espaço em branco

            // Adicionar tabela de parcelas
            document.add(new Paragraph("Tabela de Parcelas:"));
            PdfPTable table = new PdfPTable(3);
            table.addCell("Parcela");
            table.addCell("Valor Parcela");
            table.addCell("Valor Amortização");

            for (Parcelas parcela : listaParcelas) {
                table.addCell(String.valueOf(parcela.getNumeroParcela()));
                table.addCell(String.format("R$ %.2f", parcela.getValorParcela()));
                table.addCell(String.format("R$ %.2f", parcela.getValorAmortizacao()));
            }
            document.add(table);

            // Fechar o documento
            document.close();

            // Notificar o usuário sobre o PDF gerado
            resultadoLabel.setText("PDF gerado com sucesso!");

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            resultadoLabel.setText("Erro ao gerar PDF: " + e.getMessage());
        }
    }
}







