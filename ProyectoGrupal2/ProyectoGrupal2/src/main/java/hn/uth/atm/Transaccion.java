package hn.uth.atm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaccion implements Serializable {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime fecha;
    private String tipo;
    private String numeroCuenta;
    private BigDecimal monto;
    private BigDecimal saldoFinal;

    public Transaccion() {
    }

    public Transaccion(LocalDateTime fecha, String tipo, String numeroCuenta, BigDecimal monto, BigDecimal saldoFinal) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.numeroCuenta = numeroCuenta;
        this.monto = monto;
        this.saldoFinal = saldoFinal;
    }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATTER);
    }

    public String toCsvLine() {
        return getFechaTexto() + "," + tipo + "," + numeroCuenta + "," + monto + "," + saldoFinal;
    }

    public String getCuentaEnmascarada() {
        if (numeroCuenta == null || numeroCuenta.length() < 4) {
            return "****";
        }
        return "******" + numeroCuenta.substring(numeroCuenta.length() - 4);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }
}
