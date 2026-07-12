# ProyectoGrupal2 - Cajero Automatico JSF

Aplicacion web desarrollada con Jakarta Faces que simula un cajero automatico. Permite iniciar sesion con numero de cuenta y PIN, consultar saldo, realizar depositos, retirar efectivo y revisar el historial de transacciones.

## Proyecto Grupal miembros
- Luis Alberto Colindres Ventura 201930060108
- Emerson Ricardo Jimenez 202310080212
- Marlon Jared Saenz Blanco 202230010131

## Funcionalidades

- Inicio de sesion con cuenta de 10 digitos y PIN de 4 digitos.
- Pantalla de bienvenida antes de mostrar el menu principal.
- Deposito a la cuenta propia.
- Deposito a terceros con validacion de cuenta destino.
- Favoritos para cuentas destino usadas en depositos a terceros.
- Retiro con validacion de saldo disponible.
- Consulta de saldo de la cuenta autenticada.
- Historial de movimientos con origen, destino, monto, saldo anterior y saldo final.
- Persistencia simple mediante archivos CSV.

## Tecnologias

- Java 17
- Maven
- Jakarta Faces 4
- CDI con Weld
- Aplicacion empaquetada como WAR

## Requisitos

- JDK 17 o superior.
- Maven, o usar los wrappers incluidos `mvnw` / `mvnw.cmd`.
- Servidor compatible con Jakarta EE / Servlet para Jakarta, por ejemplo Apache Tomcat 10.1.

## Ejecutar

1. Compile el proyecto.
2. Copie el archivo WAR generado a la carpeta `webapps` de Tomcat 10.1.
3. Inicie Tomcat.
4. Abra la aplicacion en el navegador:

Si el servidor despliega el WAR con otro contexto, ajuste la URL segun el nombre usado por Tomcat.

## Paginas principales

- `index.xhtml`: inicio de sesion, bienvenida y menu principal.
- `deposito.xhtml`: deposito propio, deposito a terceros y favoritos.
- `retiro.xhtml`: retiro de fondos.
- `saldo.xhtml`: consulta de saldo e historial.
- `WEB-INF/templates/layout.xhtml`: plantilla compartida; no se abre directamente desde el navegador.

## Clientes de prueba

| Cuenta | Cliente | PIN |
| --- | --- | --- |
| `1002003001` | Carlos Mejia | `1234` |
| `1002003002` | Andrea Lopez | `4321` |
| `1002003003` | Marvin Reyes | `2468` |
| `1002003004` | Daniela Castillo | `1357` |
| `1002003005` | Roberto Aguilar | `9876` |
| `2022300101` | Valeria Santos | `9503` |
| `2019300601` | Fernando Rivera | `1234` |

## Archivos CSV

Los datos se encuentran en:

```text
src/main/webapp/WEB-INF/data/
```

### `clientes.csv`

Guarda las cuentas disponibles, nombres, saldos y PIN.

```text
numeroCuenta,nombreCliente,saldo,pin
1002003001,Carlos Mejia,12800.75,1234
```

### `historial.csv`

Guarda las operaciones realizadas.

```text
fecha,tipo,numeroCuenta,nombreCliente,realizadoPor,realizadoPorCuenta,monto,saldoAnterior,saldoFinal
2026-07-05 19:16:23,Deposito,1002003001,Carlos Mejia,Propio,1002003001,500.00,,13000.75
```

### `favoritos.csv`

Guarda las cuentas favoritas para depositos a terceros.

```text
cuentaPropietario,cuentaFavorita,nombreFavorito
2022300101,2019300601,Fernando Rivera
```

## Estructura principal

```text
src/main/java/hn/uth/atm/
  CajeroBean.java      Logica de negocio, validaciones y persistencia CSV.
  Cliente.java         Modelo de cliente.
  Favorito.java        Modelo de cuenta favorita.
  Transaccion.java     Modelo de movimiento del historial.

src/main/webapp/
  index.xhtml          Inicio de sesion y menu principal.
  deposito.xhtml       Pantalla de depositos.
  retiro.xhtml         Pantalla de retiros.
  saldo.xhtml          Consulta de saldo e historial.
  resources/css/       Estilos de la interfaz.
  WEB-INF/data/        Archivos CSV.
  WEB-INF/templates/   Plantilla base de las vistas.
```

## Notas de uso

- El monto maximo por operacion es de `L 50,000.00`.
- Los montos aceptan numeros positivos con maximo 2 decimales.
- Las pantallas de deposito, retiro y saldo requieren una sesion activa.
- Al realizar depositos o retiros se actualizan los CSV cuando la aplicacion tiene permisos de escritura sobre la ruta de datos.
- Tambien puede definirse una carpeta externa para los CSV usando la propiedad `atm.data.dir` o la variable de entorno `ATM_DATA_DIR`.

## Metodos de `CajeroBean`

### Inicializacion y sesion

- `init()`: inicializa las listas de clientes, historial y favoritos al crear el bean.
- `iniciarSesion()`: valida cuenta y PIN, autentica al cliente y muestra la bienvenida.
- `validarFormularioConsulta()`: revisa que la cuenta y el PIN esten completos y tengan el formato correcto.
- `validarCuentaYPin()`: busca la cuenta ingresada y confirma que el PIN coincida.
- `validarSesionActiva()`: verifica que exista un cliente autenticado antes de permitir operaciones.
- `cerrarSesion()`: limpia la informacion de la gestion actual y termina la sesion.
- `verificarSesion()`: redirige al inicio cuando una pagina protegida se abre sin sesion.
- `protegerPantalla()`: protege pantallas internas redirigiendo al login si no hay sesion activa.

### Depositos

- `depositar()`: realiza un deposito a la cuenta del cliente autenticado.
- `prepararDepositoPropio()`: activa el formulario para depositar a la cuenta propia.
- `prepararDepositoTercero()`: activa el formulario para depositar a otra cuenta.
- `prepararConfirmacionDepositoTercero()`: valida el monto y prepara la confirmacion del deposito a terceros.
- `prepararConfirmacionDepositoTercero(BigDecimal, boolean)`: valida internamente la cuenta destino y guarda los datos a confirmar.
- `seleccionarFavorito(Favorito)`: coloca una cuenta favorita como destino del deposito.
- `guardarFavoritoDestino()`: guarda la cuenta destino validada como favorita del cliente autenticado.
- `confirmarDepositoTercero()`: aplica el deposito a la cuenta destino y registra la transaccion.
- `cancelarConfirmacionDepositoTercero()`: borra los datos de confirmacion del deposito a terceros.

### Retiros y consultas

- `retirar()`: valida el monto, revisa saldo disponible y descuenta el dinero de la cuenta.
- `consultarSaldo()`: carga la cuenta autenticada como cuenta consultada.
- `prepararConsultaSaldo()`: valida la sesion y prepara la pantalla de saldo.
- `validarFormularioOperacion(boolean)`: valida que el formulario de una operacion tenga los datos requeridos.
- `validarMonto()`: valida que el monto sea positivo, numerico y no supere `L 50,000.00`.
- `obtenerMontoValidoSilencioso()`: intenta obtener un monto valido sin mostrar mensajes de error.

### Navegacion y estado de pantalla

- `irDeposito()`: limpia datos temporales y navega a la pantalla de deposito.
- `irRetiro()`: limpia datos temporales y navega a la pantalla de retiro.
- `irSaldo()`: limpia datos temporales y navega a la pantalla de saldo.
- `volverMenu()`: limpia datos temporales y regresa al menu principal.
- `prepararPantalla()`: limpia mensajes de resultado antes de mostrar una pantalla.
- `prepararNuevaGestion()`: limpia datos ingresados, consulta actual y resultado.
- `prepararOperacionAutenticada()`: prepara el estado antes de entrar a una operacion autenticada.
- `prepararResultado(String, String)`: guarda el titulo y detalle que se muestran al terminar una operacion.
- `limpiarDatosIngresados()`: borra cuenta, PIN y monto escritos en formularios.
- `limpiarConfirmacionDeposito()`: borra el cliente destino y monto confirmado.

### Datos y persistencia

- `cargarClientes()`: lee los clientes desde `clientes.csv`.
- `cargarHistorial()`: lee las transacciones desde `historial.csv`.
- `cargarFavoritos()`: lee las cuentas favoritas desde `favoritos.csv`.
- `guardarClientes()`: escribe los saldos actualizados en `clientes.csv`.
- `guardarTransaccion(Transaccion)`: agrega una nueva transaccion al archivo `historial.csv`.
- `guardarFavoritos()`: escribe la lista de favoritos en `favoritos.csv`.
- `registrarTransaccion(String, Cliente, Cliente, BigDecimal, BigDecimal)`: crea una transaccion, la agrega al historial y la guarda.
- `abrirReader(String, String)`: abre un CSV desde la ruta del proyecto o desde los recursos web.
- `recurso(String)`: obtiene un recurso interno de la aplicacion web.
- `resolverRutaPersistente(String, String)`: decide donde guardar los CSV modificados.
- `buscarArchivoProyecto(String)`: busca un archivo CSV usando propiedad, variable de entorno o ruta del proyecto.
- `buscarArchivoDesdeAplicacion(String)`: busca un archivo tomando como base la ruta real de la aplicacion desplegada.
- `buscarArchivoDesdeDirectorio(Path, String)`: sube por los directorios hasta encontrar el archivo solicitado.

### Utilidades y mensajes

- `buscarCliente(String)`: busca un cliente por numero de cuenta.
- `estaVacio(String)`: indica si un texto es nulo o esta en blanco.
- `agregarMensajeFormularioIncompleto(List<String>, int)`: muestra mensajes cuando faltan campos del formulario.
- `agregarMensaje(FacesMessage.Severity, String, String)`: agrega mensajes JSF para mostrarlos en la interfaz.
