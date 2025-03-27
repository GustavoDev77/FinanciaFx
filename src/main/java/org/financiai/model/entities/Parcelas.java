package org.financiai.model.entities;

public class Parcelas {
    private int numeroParcela;
    private double valorParcela;
    private double valorAmortizacao;

    public Parcelas(int numeroParcela, double valorParcela, double valorAmortizacao) {
        this.numeroParcela = numeroParcela;
        this.valorParcela = valorParcela;
        this.valorAmortizacao = valorAmortizacao;
    }

    public int getNumeroParcela() {
        return numeroParcela;
    }

    public void setNumeroParcela(int numeroParcela) {
        this.numeroParcela = numeroParcela;
    }

    public double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(double valorParcela) {
        this.valorParcela = valorParcela;
    }

    public double getValorAmortizacao() {
        return valorAmortizacao;
    }

    public void setValorAmortizacao(double valorAmortizacao) {
        this.valorAmortizacao = valorAmortizacao;
    }
}
