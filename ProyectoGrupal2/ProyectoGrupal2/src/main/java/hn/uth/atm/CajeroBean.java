package hn.uth.atm;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named("cajeroBean")
@SessionScoped
public class CajeroBean implements Serializable {
    private static final String CLIENTES_CSV = "/WEB-INF/data/clientes.csv";
    private static final String HISTORIAL_CSV = "/WEB-INF/data/historial.csv";
    private static final String CLIENTES_SOURCE_CSV = "src/main/webapp/WEB-INF/data/clientes.csv";
    private static final String HISTORIAL_SOURCE_CSV = "src/main/webapp/WEB-INF/data/historial.csv";

    private List<Cliente> clientes;
    private List<Transaccion> historial;
    private String numeroCuenta;
    private String pin;
    private String monto;
    private Cliente clienteConsultado;
    private String tituloResultado;
    private String detalleResultado;

    @PostConstruct
    public void init() {
        clientes = new ArrayList<>();
        historial = new ArrayList<>();
        cargarClientes();
        cargarHistorial();
    }

    public void depositar() {
        BigDecimal montoOperacion = validarFormularioOperacion(true);
        if (montoOperacion == null) {
            return;
        }

        Optional<Cliente> cliente = validarCuentaYPin();
        if (!cliente.isPresent()) {
            return;
        }

        Cliente actual = cliente.get();
        actual.setSaldo(actual.getSaldo().add(montoOperacion));
        guardarClientes();
        registrarTransaccion("Deposito", actual, montoOperacion);
        prepararResultado("Deposito exitoso", "Se depositaron L " + montoOperacion + ". Nuevo saldo: L " + actual.getSaldo());
        limpiarDatosIngresados();
    }

    public void retirar() {
        BigDecimal montoOperacion = validarFormularioOperacion(true);
        if (montoOperacion == null) {
            return;
        }

        Optional<Cliente> cliente = validarCuentaYPin();
        if (!cliente.isPresent()) {
            return;
        }

        Cliente actual = cliente.get();
        if (actual.getSaldo().compareTo(montoOperacion) < 0) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Saldo insuficiente", "La cuenta no tiene fondos para cubrir el retiro.");
            return;
        }

        actual.setSaldo(actual.getSaldo().subtract(montoOperacion));
        guardarClientes();
        registrarTransaccion("Retiro", actual, montoOperacion);
        prepararResultado("Retiro exitoso", "Se retiraron L " + montoOperacion + ". Nuevo saldo: L " + actual.getSaldo());
        limpiarDatosIngresados();
    }

    public void consultarSaldo() {
        clienteConsultado = null;
        if (!validarFormularioConsulta()) {
            return;
        }

        Optional<Cliente> cliente = validarCuentaYPin();
        if (!cliente.isPresent()) {
            return;
        }

        clienteConsultado = cliente.get();
        limpiarDatosIngresados();
        agregarMensaje(FacesMessage.SEVERITY_INFO, "Consulta exitosa", "Los campos fueron limpiados por seguridad.");
    }

    private BigDecimal validarFormularioOperacion(boolean requiereMonto) {
        List<String> faltantes = new ArrayList<>();
        if (estaVacio(numeroCuenta)) {
            faltantes.add("numero de cuenta");
        }
        if (estaVacio(pin)) {
            faltantes.add("PIN");
        }
        if (requiereMonto && estaVacio(monto)) {
            faltantes.add("monto");
        }

        if (!faltantes.isEmpty()) {
            agregarMensajeFormularioIncompleto(faltantes, requiereMonto ? 3 : 2);
            return null;
        }

        if (!numeroCuenta.trim().matches("\\d{10}")) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Cuenta invalida", "El numero de cuenta debe tener 10 digitos numericos.");
            return null;
        }

        if (!pin.trim().matches("\\d{4}")) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "PIN invalido", "El PIN debe tener 4 digitos numericos.");
            return null;
        }

        return validarMonto();
    }

    private boolean validarFormularioConsulta() {
        List<String> faltantes = new ArrayList<>();
        if (estaVacio(numeroCuenta)) {
            faltantes.add("numero de cuenta");
        }
        if (estaVacio(pin)) {
            faltantes.add("PIN");
        }

        if (!faltantes.isEmpty()) {
            agregarMensajeFormularioIncompleto(faltantes, 2);
            return false;
        }

        if (!numeroCuenta.trim().matches("\\d{10}")) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Cuenta invalida", "El numero de cuenta debe tener 10 digitos numericos.");
            return false;
        }

        if (!pin.trim().matches("\\d{4}")) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "PIN invalido", "El PIN debe tener 4 digitos numericos.");
            return false;
        }

        return true;
    }

    private void agregarMensajeFormularioIncompleto(List<String> faltantes, int totalCampos) {
        if (faltantes.size() == totalCampos) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Debe llenar la informacion", "Complete los campos solicitados antes de continuar.");
            return;
        }

        agregarMensaje(FacesMessage.SEVERITY_ERROR,
                "Falta rellenar informacion",
                "Complete: " + String.join(", ", faltantes) + ".");
    }

    private Optional<Cliente> validarCuentaYPin() {
        Optional<Cliente> cliente = buscarCliente(numeroCuenta);
        if (!cliente.isPresent()) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Cuenta no encontrada", "Verifique el numero de cuenta ingresado.");
            return Optional.empty();
        }

        if (pin == null || !pin.equals(cliente.get().getPin())) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "PIN invalido", "El PIN no corresponde con la cuenta.");
            return Optional.empty();
        }

        return cliente;
    }

    private BigDecimal validarMonto() {
        String montoNormalizado = monto.trim().replace(",", "");
        if (!montoNormalizado.matches("\\d+(\\.\\d{1,2})?")) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Monto invalido", "Ingrese solo numeros positivos con maximo 2 decimales.");
            return null;
        }

        BigDecimal valor = new BigDecimal(montoNormalizado).setScale(2, RoundingMode.HALF_UP);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Monto invalido", "Ingrese un monto positivo mayor que cero.");
            return null;
        }

        if (valor.compareTo(new BigDecimal("50000.00")) > 0) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Monto fuera de rango", "Por seguridad, el monto maximo por operacion es L 50,000.00.");
            return null;
        }

        return valor;
    }

    private Optional<Cliente> buscarCliente(String cuenta) {
        if (cuenta == null) {
            return Optional.empty();
        }
        return clientes.stream()
                .filter(cliente -> cuenta.trim().equals(cliente.getNumeroCuenta()))
                .findFirst();
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void cargarClientes() {
        try (BufferedReader reader = abrirReader(CLIENTES_CSV, CLIENTES_SOURCE_CSV)) {
            String linea;
            boolean encabezado = true;
            while ((linea = reader.readLine()) != null) {
                if (encabezado) {
                    encabezado = false;
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length == 3) {
                    clientes.add(new Cliente(partes[0].trim(), new BigDecimal(partes[1].trim()), partes[2].trim()));
                }
            }
        } catch (Exception ex) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "No se cargaron los clientes", "Revise WEB-INF/data/clientes.csv.");
        }
    }

    private void cargarHistorial() {
        try (BufferedReader reader = abrirReader(HISTORIAL_CSV, HISTORIAL_SOURCE_CSV)) {
            String linea;
            boolean encabezado = true;
            while ((linea = reader.readLine()) != null) {
                if (encabezado) {
                    encabezado = false;
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length == 5) {
                    historial.add(new Transaccion(LocalDateTime.parse(partes[0].trim().replace(" ", "T")),
                            partes[1].trim(),
                            partes[2].trim(),
                            new BigDecimal(partes[3].trim()),
                            new BigDecimal(partes[4].trim())));
                }
            }
        } catch (Exception ignored) {
            // El historial puede iniciar vacio.
        }
    }

    private BufferedReader abrirReader(String rutaWeb, String rutaProyecto) throws IOException {
        Optional<Path> archivoProyecto = buscarArchivoProyecto(rutaProyecto);
        if (archivoProyecto.isPresent()) {
            return Files.newBufferedReader(archivoProyecto.get(), StandardCharsets.UTF_8);
        }

        return new BufferedReader(new InputStreamReader(recurso(rutaWeb), StandardCharsets.UTF_8));
    }

    private InputStream recurso(String ruta) throws IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        InputStream input = context.getResourceAsStream(ruta);
        if (input == null) {
            throw new IOException("Recurso no encontrado: " + ruta);
        }
        return input;
    }

    private void registrarTransaccion(String tipo, Cliente cliente, BigDecimal valor) {
        Transaccion transaccion = new Transaccion(LocalDateTime.now(), tipo, cliente.getNumeroCuenta(), valor, cliente.getSaldo());
        historial.add(0, transaccion);
        guardarTransaccion(transaccion);
    }

    private void guardarClientes() {
        Optional<Path> rutaPersistente = resolverRutaPersistente(CLIENTES_CSV, CLIENTES_SOURCE_CSV);
        if (!rutaPersistente.isPresent()) {
            return;
        }

        Path path = rutaPersistente.get();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write("numeroCuenta,saldo,pin");
                writer.newLine();
                for (Cliente cliente : clientes) {
                    writer.write(cliente.getNumeroCuenta() + "," + cliente.getSaldo() + "," + cliente.getPin());
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Clientes no guardados", "El saldo cambio en memoria, pero no se pudo actualizar clientes.csv.");
        }
    }

    private void guardarTransaccion(Transaccion transaccion) {
        Optional<Path> rutaPersistente = resolverRutaPersistente(HISTORIAL_CSV, HISTORIAL_SOURCE_CSV);
        if (!rutaPersistente.isPresent()) {
            return;
        }

        Path path = rutaPersistente.get();
        try {
            Files.createDirectories(path.getParent());
            boolean existe = Files.exists(path) && Files.size(path) > 0;
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                if (!existe) {
                    writer.write("fecha,tipo,numeroCuenta,monto,saldoFinal");
                    writer.newLine();
                }
                writer.write(transaccion.toCsvLine());
                writer.newLine();
            }
        } catch (IOException ex) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Historial no guardado", "La operacion fue aplicada, pero no se pudo escribir el CSV.");
        }
    }

    private Optional<Path> resolverRutaPersistente(String rutaWeb, String rutaProyecto) {
        Optional<Path> archivoProyecto = buscarArchivoProyecto(rutaProyecto);
        if (archivoProyecto.isPresent()) {
            return archivoProyecto;
        }

        String rutaReal = FacesContext.getCurrentInstance().getExternalContext().getRealPath(rutaWeb);
        return rutaReal == null ? Optional.empty() : Optional.of(Path.of(rutaReal));
    }

    private Optional<Path> buscarArchivoProyecto(String rutaProyecto) {
        String carpetaDatos = System.getProperty("atm.data.dir");
        if (!estaVacio(carpetaDatos)) {
            Path archivo = Paths.get(carpetaDatos, Paths.get(rutaProyecto).getFileName().toString());
            return Optional.of(archivo);
        }

        carpetaDatos = System.getenv("ATM_DATA_DIR");
        if (!estaVacio(carpetaDatos)) {
            Path archivo = Paths.get(carpetaDatos, Paths.get(rutaProyecto).getFileName().toString());
            return Optional.of(archivo);
        }

        Path directorio = Paths.get("").toAbsolutePath();
        Optional<Path> archivoProyecto = buscarArchivoDesdeDirectorio(directorio, rutaProyecto);
        if (archivoProyecto.isPresent()) {
            return archivoProyecto;
        }

        return buscarArchivoDesdeAplicacion(rutaProyecto);
    }

    private Optional<Path> buscarArchivoDesdeAplicacion(String rutaProyecto) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return Optional.empty();
        }

        String rutaAplicacion = facesContext.getExternalContext().getRealPath("/");
        if (estaVacio(rutaAplicacion)) {
            return Optional.empty();
        }

        return buscarArchivoDesdeDirectorio(Path.of(rutaAplicacion), rutaProyecto);
    }

    private Optional<Path> buscarArchivoDesdeDirectorio(Path directorio, String rutaProyecto) {
        while (directorio != null) {
            Path candidato = directorio.resolve(rutaProyecto);
            if (Files.exists(candidato)) {
                return Optional.of(candidato);
            }
            directorio = directorio.getParent();
        }
        return Optional.empty();
    }

    public void prepararPantalla() {
        tituloResultado = null;
        detalleResultado = null;
    }

    public String irDeposito() {
        prepararNuevaGestion();
        return "/deposito?faces-redirect=true";
    }

    public String irRetiro() {
        prepararNuevaGestion();
        return "/retiro?faces-redirect=true";
    }

    public String irSaldo() {
        prepararNuevaGestion();
        return "/saldo?faces-redirect=true";
    }

    public String cerrarSesionYVolverMenu() {
        prepararNuevaGestion();
        return "/index?faces-redirect=true";
    }

    private void prepararNuevaGestion() {
        limpiarDatosIngresados();
        clienteConsultado = null;
        tituloResultado = null;
        detalleResultado = null;
    }

    private void prepararResultado(String titulo, String detalle) {
        tituloResultado = titulo;
        detalleResultado = detalle;
    }

    private void limpiarDatosIngresados() {
        numeroCuenta = null;
        pin = null;
        monto = null;
    }

    private void agregarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumen, detalle));
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Transaccion> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public List<Transaccion> getHistorialClienteConsultado() {
        if (clienteConsultado == null) {
            return Collections.emptyList();
        }

        return historial.stream()
                .filter(transaccion -> clienteConsultado.getNumeroCuenta().equals(transaccion.getNumeroCuenta()))
                .collect(Collectors.toList());
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getMonto() {
        return monto;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }

    public Cliente getClienteConsultado() {
        return clienteConsultado;
    }

    public String getTituloResultado() {
        return tituloResultado;
    }

    public String getDetalleResultado() {
        return detalleResultado;
    }

    public boolean isMostrarResultado() {
        return tituloResultado != null && !tituloResultado.trim().isEmpty();
    }
}
