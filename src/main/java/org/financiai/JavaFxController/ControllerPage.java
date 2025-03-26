package org.financiai.JavaFxController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ControllerPage {

    // FXML components
    @FXML
    private TextField nomeClienteField;
    @FXML
    private TextField rendaClienteField;
    @FXML
    private TextField valorFinanciamentoField;
    @FXML
    private TextField prazoField;

    @FXML
    private Label resultadoLabel;

    @FXML
    private TextArea tabelaParcelasArea;

    // Método para calcular o financiamento
    @FXML
    public void calcularFinanciamento(ActionEvent event) {
        try {
            String nomeCliente = nomeClienteField.getText();
            double rendaMensal = Double.parseDouble(rendaClienteField.getText());
            double valorFinanciamento = Double.parseDouble(valorFinanciamentoField.getText());
            int prazo = Integer.parseInt(prazoField.getText());

            // Simulação simples
            double valorParcela = valorFinanciamento / prazo;
            double limiteParcela = rendaMensal * 0.3; // Máx. 30% da renda

            if (valorParcela > limiteParcela) {
                resultadoLabel.setText("Financiamento negado. Parcela acima do limite!");
                tabelaParcelasArea.setText("");
            } else {
                resultadoLabel.setText("Financiamento aprovado!");
                tabelaParcelasArea.setText(gerarTabelaParcelas(prazo, valorParcela));
            }
        } catch (NumberFormatException e) {
            resultadoLabel.setText("Por favor, insira valores numéricos válidos.");
            tabelaParcelasArea.setText("");
        }
    }

    // Método auxiliar para gerar a tabela de parcelas
    private String gerarTabelaParcelas(int prazo, double valorParcela) {
        StringBuilder tabela = new StringBuilder("Parcela\tValor\n");
        tabela.append("-----------------\n");
        for (int i = 1; i <= prazo; i++) {
            tabela.append(i).append("\tR$ ").append(String.format("%.2f", valorParcela)).append("\n");
        }
        return tabela.toString();
    }
}