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
    private String nombreCliente;
    private String realizadoPor;
    private String realizadoPorCuenta;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoFinal;

    public Transaccion() {
    }

    public Transaccion(LocalDateTime fecha, String tipo, String numeroCuenta, BigDecimal monto, BigDecimal saldoFinal) {
        this(fecha, tipo, numeroCuenta, "", "Propio", numeroCuenta, monto, null, saldoFinal);
    }

    public Transaccion(LocalDateTime fecha, String tipo, String numeroCuenta, String nombreCliente,
                       String realizadoPor, String realizadoPorCuenta, BigDecimal monto,
                       BigDecimal saldoAnterior, BigDecimal saldoFinal) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.numeroCuenta = numeroCuenta;
        this.nombreCliente = nombreCliente;
        this.realizadoPor = realizadoPor;
        this.realizadoPorCuenta = realizadoPorCuenta;
        this.monto = monto;
        this.saldoAnterior = saldoAnterior;
        this.saldoFinal = saldoFinal;
    }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATTER);
    }

    public String toCsvLine() {
        return getFechaTexto() + "," + tipo + "," + numeroCuenta + "," + nombreCliente + ","
                + realizadoPor + "," + realizadoPorCuenta + "," + monto + ","
                + (saldoAnterior == null ? "" : saldoAnterior) + "," + saldoFinal;
    }

    public String getCuentaEnmascarada() {
        if (numeroCuenta == null || numeroCuenta.length() < 4) {
            return "****";
        }
        return "******" + numeroCuenta.substring(numeroCuenta.length() - 4);
    }

    public String getTipoCss() {
        if ("Deposito".equalsIgnoreCase(tipo)) {
            return "tx-deposit";
        }
        if ("Retiro".equalsIgnoreCase(tipo)) {
            return "tx-withdraw";
        }
        return "";
    }

    public String getSaldoAnteriorTexto() {
        return saldoAnterior == null ? "N/D" : "L " + saldoAnterior;
    }

    public boolean isOperacionPropia() {
        return realizadoPorCuenta == null || realizadoPorCuenta.equals(numeroCuenta);
    }

    public String getModalidadTexto() {
        if ("Retiro".equalsIgnoreCase(tipo)) {
            return "Retiro propio";
        }
        return isOperacionPropia() ? "Deposito propio" : "Deposito a terceros";
    }

    public String getResumenTexto() {
        if ("Retiro".equalsIgnoreCase(tipo)) {
            return nombreCliente + " retiro de su cuenta";
        }

        if (isOperacionPropia()) {
            return nombreCliente + " deposito a su propia cuenta";
        }

        return realizadoPor + " deposito a " + nombreCliente;
    }

    public String getOrigenTexto() {
        return isOperacionPropia() ? nombreCliente : realizadoPor;
    }

    public String getDestinoTexto() {
        return nombreCliente;
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getRealizadoPor() {
        return realizadoPor;
    }

    public void setRealizadoPor(String realizadoPor) {
        this.realizadoPor = realizadoPor;
    }

    public String getRealizadoPorCuenta() {
        return realizadoPorCuenta;
    }

    public void setRealizadoPorCuenta(String realizadoPorCuenta) {
        this.realizadoPorCuenta = realizadoPorCuenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public void setSaldoAnterior(BigDecimal saldoAnterior) {
        this.saldoAnterior = saldoAnterior;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }
}
