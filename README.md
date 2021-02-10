# Instalación

## Despliegue
Para desplegar el HAgente, usaremos el instalador del mismo que podemos encontrar en la carpeta de scripts `scripts/install.$OS.sh`. 
Es un script `ksh` que podremos ejecutar en sistemas *NIX.

```bash
OS=$(uname|tr '[:upper:]' '[:lower:]')
wget -O /tmp/hagent.install.sh  --no-check-certificate \
	https://raw.githubusercontent.com/AlejandroAbad/hagent/main/scripts/install.$OS.sh
	
chmod u+x /tmp/hagent.install.sh
/tmp/hagent.install.sh

# Una vex instalado
rm /tmp/hagent.install.sh

```

Al ejecutar el instalador, se realizará la descarga del código fuente y se compilará en la carpeta `/usr/local/hagent`.
Para almacenar los logs, se utiliza el directorio `/var/log/hagent`.

Una vez finalizada la instalación, el HAgente habrá quedado configurado para arrancarse en cada inicio del host.

## Ficheros
En el directorio de instalación del HAgente, encontraremos los siguientes ficheros:
- `hagent.jar`. Es el binario java que contiene el código del HAgente.
- `hagent.sh`. Es el ejecutable que utilizaremos para arrancar, parar o conocer el estado del HAgente.
- `log4j2.xml`. Es el fichero de configuración de Log4j2. Log4j2 es una librería para escribir logs y trazas en java. Con este fichero podemos determinar parámetros como el nivel de traza, la ubicación, la rotación del log, etc...
- `config.json`. Es el fichero de configuración del HAgente. A lo largo de este documento iremos detallando las diferentes secciones del mismo.

## Configuración
La configuración del HAgente se realiza mediante el fichero `config.json`, como hemos explicado. El HAgente lee este fichero al arrancar y lo recarga automáticamente cada cierto intervalo de tiempo.

El fichero está en formato JSON, y la configuración mínima para poder arrancar el HAgente es un objeto `agent` sobre la raíz, como la siguiente:
```json
{
	"agent": {
		"port": 55555,
		"domain": "hefame.es",
		"registration_url": "https://bitacora.hefame.es/agent/register",
		"configuration_reload": 3600
	}
}
```

El objeto `agent` constará de los siguientes campos:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**port** | `integer` | 55555 | Es el puerto donde el HAgente escuchará peticiones HTTP al API.
**domain** | `string` | hefame.es | Es el dominio de la máquina. Este campo se utiliza si queremos tener varios grupos de HAgentes.
**registration_url** | `url` | https://bitacora.hefame.es/agent/register | Esta es la URL donde el HAgente se registrará periodicamente. El registro permite tener en una base de datos la lista de HAgentes.
**configuration_reload** | `integer` | 3600 | Especifica cada cuanto tiempo el HAgente releerá su fichero de configuración. Este intervalo también define cada cuanto tiempo el HAgente intentará registrarse.


# Conceptos generales

## Sampler
Un sampler no es mas que un proceso que el HAgente ejecuta en fondo, en bucle infinito, y que realiza alguna acción que da como resultado unos datos (o "samples"), donde almacena únicamente el último sample. Resulta útil para la siguiente situación: imaginemos que tenemos un proceso que cuando se ejecuta devuelve datos de uso de los filesystems. Si queremos consultar estos resultados a través del API del HAgente, deberíamos ejecutar el proceso para recopilar los datos antes de poder devolver nada, lo cual haría que la respuesta del API fuera muy pesada. Gracias al uso de un Sampler, podemos tener precargados datos recientes, y por tanto podremos devolver los últimos datos obtenidos por el Sampler.

```java
// Arrancamos un Sampler que ejecuta el comando de recoleccion de datos de CPU para AIX cada 60 segundos.
Sampler cpuSampler = new Sampler(new AIXProcessorCommand()), 60);
cpuSampler.start();
// Lo registramos en la clase BgJobs para poder acceder al mismo en el futuro.
BgJobs.addJob("cpuSampler1", cpuSampler);

...

// Recuperamos la referencia del sampler y obtenemos el último resultado guardado en el mismo.
Sampler cpuSampler = (Sampler) BgJobs.getJob("cpuSampler1");
AIXProcessorResult samplerResult = (AIXProcessorResult) cpuSampler.getLastResult();
```

## Checker
Un checker es un proceso que se ejecuta en fondo y que realiza algún checkeo. Como resultado del checkeo puede lanzar alertas, como por ejemplo, enviar un correo. Por lo general, no vamos a consultar datos devueltos por un Checker, ya que para esto tenemos los Samplers. Lo único que podemos consultar de un checker es el tiempo que ha transcurrido desde la última vez que el checkeo se ejecutó. El Checker también implementa un mecanismo para detener/reanudar las alertas.

Más adelante en esta documentación detallaremos las implementaciones concretas de Checkers que existen así como la llamada al API que permite ver el estado de los mismos.

## Canales de alertas
El HAgente es capaz de enviar notificaciones al exterior para avisar de ciertos eventos. Para configurar estas notificaciones, utilizamos los canales de alertas. Los canales de alertas se definen dentro de la configuración, bajo el nodo `alert_channels`, y todos deben tener un identidicador y un tipo que define su comportamiento. Ya en función del tipo podrán requerir parámetros adicionales.

Por defecto, se crean siempre dos canales de tipo `mailer`, uno lladado **`default`** y otro llamado **`success`**. La configuración por defecto de ambos es la que podemos ver en el siguiente código:

```json
{
    "alert_channels": [
        {
            "name": "default",
            "type": "mailer",
			"to": ["cpd.sistemas.unix@hefame.es"]
        },
        {
            "name": "success",
            "type": "mailer",
			"to": []
        }
    ]
}
```


### Notificaciones por mail
Los canales de configuración de tipo `mailer` se utilizan para enviar notificaciones por correo. Para configurar el comportamiento de este canal, es posible definir cierta configuración en el fichero de configuración.
Esta configuración se realiza bajo el nodo de configuración `mailer`, tal como vemos a continuación:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**reply_to** | `string` | noresponder@*<fqdn_hostname>* | La dirección de correo que aparecerá en la cabecera `reply_to` de los correos enviados.
**mail_from** | `string` | hagent@*<fqdn_hostname>* | La dirección de correo que aparecerá como el remitente de los correos enviados.
**smtp_server** | `string` | correo.hefame.es | La dirección del servidor SMTP
**smtp_user** | `string` | `null` | El usuario para logearse en el servidor SMTP
**smtp_pass** | `string` | `null` | La contraseña para logearse en el servidor SMTP
**smtp_tls** | `boolean` | `false` | Determina si se usará TLS para el envío de los correos.
**smtp_port** | `numericString`, `numeric` | `25` | El puerto del servidor SMTP


```json
{
    "mailer": {
        "reply_to": "noresponder@ssm.hefame.es",
        "mail_from": "hagent@ssm.hefame.es",
        "smtp_server": "correo.hefame.es",
        "smtp_user": "jonnhyCash",
        "smtp_pass": "******",
        "smtp_tls": false,
        "smtp_port": 25
	}
}
```

Y por otro lado, cuando definimos el canal de alertas, debemos añadir un parámetro específico `to`, que es un array con las direcciones de correo a las que se enviará el mensaje cuando se utilice el canal. Por ejemplo:

```json
{
    "alert_channels": [
        {
            "name": "errpt_alerts",
            "type": "mailer",
            "to": [
                "administradores.aix@hefame.es"
            ]
        },
        {
            "name": "archivelog_alerts",
            "type": "mailer",
            "to": [
                "administradores.oracle@hefame.es",
				"administradores.sap@hefame.es",
				"operacion@hefame.es",
            ]
        }
    ]
}
```

#Consultas desde PRTG

A continuación detallamos las distintas consultas que pueden realizarse al API. Todas estas consultas cuelgan del nodo `/prtg/` y devuelven datos en formato JSON y con los campos adecuados para que el PRTG los entienda. Este formato se especidica en la documentación de PRTG. Para mas información, ver el apartado **`Advanced Script, HTTP Data, And REST Custom Sensors`** en el siguiente enlace (usuario: `demo`, password: `demodemo`):

https://prtg.paessler.com/api.htm?tabid=7


## `/prtg/jvm`
Muestra el estado de la JVM donde se ejecuta el HAgente.

**Ejemplo de salida:**
```json
{
	"Value": "11189432",
	"Channel": "Memoria usada",
	"Unit": "BytesMemory"
}
```

## `/prtg/processor`
Muestra el uso de CPU de la máquina.
Nótese que los datos se extraen desde un Sampler, ya que lo normal es que se devuelva la media de uso de CPU durante un intervalo de tiempo considerable, para evitar picos puntuales que hagan saltar falsas alarmas.

Dependiendo del sistema operativo, los resultados incluiran canales específicos. Por ejemplo, para *NIX se indican valores del uso de CPU por usuario/systema/iowait, y para AIX se incluyen datos de virtual processors y threads (si procede por el tipo de procesador).

> Los datos obtenidos son de la salida del comando `vmstat` en caso de sistemas Linux, o de la salida del comando `lparstat` en el caso de sistemas AIX. El valor devuelto es la media del intervalo de tiempo que configuremos.

**Configuración:**
La configuración se espera encontrar en el nodo `prtg.processor` de la configuración. Sus variables son las siguientes:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**error_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en error.
**warn_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en alerta.
**sample_time** | `integer` | 60 | El tiempo (en segundos) que transcurre entre muestras de datos. *(Este valor establece el `minSampleTime` del Sampler)*

Ejemplo:
```json
{
	"prtg":
		"processor": {
            "error_percent": "95",
			"warn_percent": "90",
            "sample_time": 60
        }
}
```

**Ejemplo de salida:**
```json
{
	"Float": 1,
	"LimitMode": 1,
	"Value": "24.2",
	"Channel": "Uso de CPU",
	"LimitMaxError": "95",
	"LimitMaxWarning": "90",
	"Unit": "Percent"
}
```


## `/prtg/memory`
Muestra los datos de memoria de la máquina. Esto incluye RAM y SWAP, al margen de que pueden incluirse canales adicionales en función del SO como el espacio usado en buffer/cache/... o el dispositivo especial `/dev/shm`.

> Los datos devueltos son los obtenidos por el comando `free -b -o` en sistemas Linux, o por el comando `svmon` en el caso de AIX. En el caso de existir el dispositivo `/dev/shm`, el uso del mismo se mide como el de un filesystem, como veremos en el sensor de filesystems.

**Configuración:**
La configuración se espera encontrar en el nodo `prtg.memory` de la configuración. Sus variables son las siguientes:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**physical_error_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en error si la memoria RAM usada lo supera.
**physical_warn_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en alerta si la memoria RAM usada lo supera.
**swap_error_percent** | `numericString`, `numeric` | `null` | El tiempo Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en error si el espacio SWAP usado lo supera.
**swap_warn_percent** | `numericString`, `numeric` | `null` | El tiempo Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en alerta si el espacio SWAP usado lo supera.

Ejemplo:
```json
{
	"prtg":
        "memory": {
            "physical_error_percent": "95",
			"physical_warn_percent": "90",
            "swap_error_percent": "50",
            "swap_warn_percent": "25"
        }
}
```

**Ejemplo de salida:**
```json
{
    "LimitMaxWarning": "90",
    "Float": 1,
    "Channel": "Uso de memoria fisica",
    "Value": "33.0",
    "LimitMode": 1,
    "Unit": "Percent",
    "LimitMaxError": "95"
},
{
    "LimitMaxWarning": "25",
    "Float": 1,
    "Channel": "Uso de SWAP",
    "Value": "41.0",
    "LimitMode": 1,
    "Unit": "Percent",
    "LimitMaxError": "50"
},
{
    "Float": 1,
    "Channel": "Uso de /dev/shm",
    "Value": "54.0",
    "Unit": "Percent"
}

```


## `/prtg/filesystems`
Muestra los datos de uso de los filesystems de la máquina. El HAgente es capaz de abstraerse del tipo de filesystem, y actualmente es capaz de entender los siguientes tipos:
	- JFS
	- JFS2
	- EXT2
	- EXT3
	- EXT4
	- XFS
	- BTRFS
	- GPFS (tanto en cliente como servidor)
	- Oracle ASM Diskgroups
	- Oracle ACFS
	- FAT32
	- NTFS

El sensor PRTG tendrá 3 canales por cada FS que se encuentre monitorizado en el host: el uso del FS en porcentaje, la cantidad de bytes libres en el mismo y la cantidad de bytes en total.

> Para la mayoría de los tipos de FS, los datos se obtienen a través de la ejecución del comando `df`, con distintos parámetros en función de si estamos en Linux (`df -klP`) o AIX (`df -kiMv`).


### Filesystems en Clúster: `/prtg/filesystems/cluster`
Como veremos en la sección de configuración, podemos determinar si un determinado FS está en un clúster o no. Esto no está influido por el tipo de filesystem, es decir, el HAgente no asume que un FS por ser ASM es de cluster, ya que podríamos estar en una ASM Single Instance. De igual modo, no se asume que un JFS2 no esté en clúster, ya que podríamos tenerlo en varias máquinas con `varyoffvg`/`varyonvg`. El objetivo de separarlos en grupos es para poder consultarlos desde PRTG de manera cómoda y natural. Imaginemos que tenemos un clúster de 2 nodos, `nodo1.hefame.es` y `nodo2.hefame.es`, cuya IP de servicio es `nodo.hefame.es`. En PRTG definiremos los dispositivos para los nodos físicos, y consultaremos el endpoint de los filesystems locales de cada uno (`/prtg/filesystems`). Sin embarg, los filesystems del clúster los consultaremos desde un tercer dispositivo en PRTG, cuya IP será la dirección del clúster. En este dispositivo, el endpoint consultado será **`/prtg/filesystems/cluster`**. De esta manera, si la IP de servicio del clúster cambia de nodo, no tendremos problemas con sensores vacíos en PRTG, ya que con este sistema da igual donde se halle la IP de servicio.



**Configuración:**
La configuración se espera encontrar en el nodo `prtg.filesystems` de la configuración. El HAgente espera que este nodo sea un array JSON, cuyos elementos sean objetos JSON, cada uno describiendo un filesystem. Los elementos que tienen/pueden que tener cada elemento son:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**filesystem** | `string` |  | **OBLIGATORIO**. Indica cual es el punto de montaje del FS. (Por ejemplo: `/var`, `+ORADATA`, `C:`)
**check_mounted** | `boolean` | `true` | Indica si el sensor debe ponerse en error si el FS no está montado. En caso de que no se active esta propiedad, el HAgente ignorará el filesystem si no se encuentra.
**check_inodes** | `boolean` | `false` | Indica si el sensor debe incluir datos del uso de inodos. Esta opción solo tiene efecto para aquellos FS que utilizan inodos. En caso de no soportarlos, se impone un valor`false`.
**warn_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en alerta si el uso del FS supera este umbral.
**error_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en error si el uso del FS supera este umbral.
**warn_free** | `diskSize` | `null` | Establece el espacio en disco mínimo que debe haber en el filesystem. Si el espacio libre es menor que este valor, PRTG debe establecer el sensor en alerta.
**error_free** | `diskSize` | `null` | Establece el espacio en disco mínimo que debe haber en el filesystem. Si el espacio libre es menor que este valor, PRTG debe establecer el sensor en error.
**inodes_warn_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en alerta si el uso de indos del FS supera este umbral. Solo se aplica si `check_inodes` es `true`.
**inodes_error_percent** | `numericString`, `numeric` | `null` | Establece el valor (en porcentaje) a partir del cual PRTG debe establecer el sensor en error si el uso de indos del FS supera este umbral. Solo se aplica si `check_inodes` es `true`.
**cluster** | `boolean` | `false` | Indica si el filesystem debe marcarse como un FS en clúster.


Ejemplo:
```json
{
	"prtg":
		"filesystems": [
			{"filesystem": "/usr/sap/DAA", "error_percent":90, "warn_percent":80, "check_mounted": false},
			{"filesystem": "/interfaces/ARCHIVING", "error_free": "300G", "warn_free": "500G", "cluster": true},
			{"filesystem": "+P01_ARCH", "error_percent":70, "warn_percent":60, "cluster": true},
			{"filesystem": "/trans", "check_inodes": true, "inodes_warn_percent": "70", "inodes_error_percent": "80"}
		]
}
```

**Ejemplo de salida:**
```json
{
    "Float": 1,
    "LimitMode": 1,
    "Value": "40.0",
    "Channel": "Uso de /var",
    "LimitMaxError": "90",
    "LimitMaxWarning": "80",
    "Unit": "Percent"
},
{
    "LimitMode": 1,
    "Value": "1298161664",
    "Channel": "Bytes libres en /var",
    "Unit": "BytesDisk"
},
{
    "Value": "2147483648",
    "Channel": "Bytes total en /var",
    "Unit": "BytesDisk"
}
```


### Filesystems ASM
En el caso especial de los Filesystems ASM, la manera de monitorizarlos es  muy distinta, pues debemos realizar una conexión Oracle TNS a la instancia ASM. Para esto, debemos proporcionar información adicional, la cual debe especificarse dentro del nodo de configuración `monitorized_elements`, en un objeto con `type = asm_diskgroups` donde especificamos el usuario, la contraseña y la cadena de conexión TNS de la siguiente forma:

```json
{
	"type": "asm_diskgroups",
	"asm_user": "HAGENT",
	"asm_password": "AchoPij0",
	"asm_tns": "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=rac1p01.hefame.es)(PORT=1529))(CONNECT_DATA=(SERVICE_NAME=+ASM)))"
}
```

> Internamente, la consuta ejecutada para obtener datos es:
```sql
SELECT
	NAME, STATE, TYPE, TOTAL_MB, REQUIRED_MIRROR_FREE_MB, USABLE_FILE_MB, OFFLINE_DISKS 
FROM
	V$ASM_DISKGROUP_STATS
```

Los discos ASM incluyen un canal extra, que indica el número de discos offline para dicho diskgroup, de modo que el sensor se pone en error si se encuentra algún disco offline.




## `/prtg/interfaces`
Muestra información de las interfaces de red del host. Los datos mostrados son las tasa de transmisión, tanto en entrada como en salida del interfaz.

> Para determinar los datos del interfaz en sistemas Linux, se utiliza un Sampler que compara cada minuto el valor de bytes TX/RX en el fichero especial `/sys/class/net/<ethX>/statistics/*` para obtener lo bytes transferidos en ambos sentidos. En el caso de AIX, el Sampler obtiene los mismos valores de la salida del comando `entstat -t enX`, y tras cada comprobación los reinicia a 0 con `entstat -r enX`.

**Configuración:**
La configuración se espera encontrar en el nodo `prtg.filesystems` de la configuración. El HAgente espera que este nodo sea un array JSON, cuyos elementos sean objetos JSON, cada uno describiendo un filesystem. Los elementos que tienen/pueden que tener cada elemento son:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**interface_name** | `string` |  | **OBLIGATORIO**. Indica cual es el nombre del interfaz a monitorizar. (Por ejemplo: `ent1`, `en0`, `Realtek PCIe GBE Family Controller`)
**check_online** | `boolean` | `true` | Indica si el sensor debe ponerse en error si el interfaz no está online.


Ejemplo:
```json
{
	"prtg":
		"interfaces": [
			{"interface_name": "en0", "check_online": true},
			{"interface_name": "en1", "check_online": true}
		]
}
```


**Ejemplo de salida:**
```json
{
	"Value": "4440",
	"Channel": "Tasa de transmision en 'en0'",
	"Unit": "SpeedNet"
},
{
	"Value": "1941",
	"Channel": "Tasa de recepcion en 'en0'",
	"Unit": "SpeedNet"
}
```

## `/prtg/diskpaths`
Muestra el estado de los paths de disco en sistemas AIX.
Este sensor no necesita configuración alguna. Devuelve tres canales, cada uno indica el número total/online/offline de paths en el host. Cualquier valor del número de paths offline distinto de cero provocará que el sensor se ponga en error en PRTG.

> El HAgente realiza llamadas a `lspath -F 'name:parent:status:path_id'` para obtener los datos de todos los paths.

**Ejemplo de salida:**
```json
{
    "Value": "616",
    "Channel": "Paths Total",
    "Unit": "Count"
},
{
    "Value": "616",
    "Channel": "Paths Online",
    "Unit": "Count"
},
{
    "Float": 1,
    "LimitMode": 1,
    "Value": "0",
    "Channel": "Paths Offline",
    "LimitMaxError": "0.5",
    "Unit": "Count"
}
```

## `/prtg/processes`
Permite conocer si determinados procesos se encuentran corriendo en el host. El sensor no está soportado en Windows. Para cada proceso determinado, el sensor incluye un canal con el número de procesos en ejecución encontrados.

> Este sensor ejecuta internamente un `ps -ef` y filtra aquellos procesos que encajan con los patrones que le indicamos en la configuración. Para cada patrón P especificado en la configuracón, el HAgente ejecutara el equivalente a `ps -ef | grep $P`.

**Configuración:**
Para configurar este sensor es necesario incluir un objeto JSON de tipo `monitorized_element` de tipo **`process_list`** dentro del array de `monitorized_elements`.

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`process_list`** para que el `monitorized_element` se entienda como un monitor de procesos del host.
**process_patterns** | `string[]` | `[]` | Lista de los nombres de procesos que el sensor consultará si están en ejecución.


Ejemplo:
```json
{
	"monitorized_elements": [
		{
			"type": "process_list",
			"process_patterns": [ "httpd-prefork", "squid" ]
		}
	]
}
```
**Ejemplo de salida:**
```json
{
    "Value": "17",
    "Channel": "httpd-prefork",
    "Unit": "Count"
},
{
    "Value": "1",
    "Channel": "squid",
    "Unit": "Count"
}
```


## `/prtg/ping`
Este sensor permite monitorizar la latencia entre el host y una tercera máquina. Por ejemplo, en el SMS, podemos controlar la latencia entre la propia máquina ADAIA y los terminales.

> Este sensor internamente envía un paquete ICMP Echo Reply y espera recibir como respuesta un paquete ICMP Echo Response en el plazo de 1000ms.

Este sensor no requiere configuración, para indicar la lista de hosts que queremos comprobar, pasaremos los nombres de los hosts a continuación en la ruta, separados por `/`. Nótese que si un host no es alcanzable, el valor de latencia devuelto será de -1, por lo que si queremos que se alerte de este hecho, estableceremos el canal en alarma para valores inferiores a 0.

Un ejemplo de uso de este sensor:
`/prtg/ping/` **`ssm`** `/` **`172.30.1.30`** `/` **`1.2.3.4`**:

```json
{
    "Float": 1,
    "Value": "0.303474",
    "Channel": "ssm",
    "CustomUnit": "ms",
    "Unit": "Custom"
},
{
    "Float": 1,
    "Value": "0.207512",
    "Channel": "172.30.1.30",
    "CustomUnit": "ms",
    "Unit": "Custom"
},
{
    "Float": 1,
    "Value": "-1.0",
    "Channel": "1.2.3.4",
    "CustomUnit": "ms",
    "Unit": "Custom"
}

```



## `/prtg/checkers`
Como vimos, un checker es un proceso que se ejecuta en fondo y que, en principio, no tiene que devolver ningún valor. Por ejemplo, un checker que vigila la aparición de nuevo errores de `errpt` en un host no debe actuar salvo que aparezca un error, y en caso de que aparezca, la respuesta del mismo será enviar un correo. Este endpoint está ideado para vigilar si el checker está o no en funcionamiento.

La llamada al endpoint lee el fichero de configuración para determinar que checkers debe haber en funcionamiento y los comprueba, por lo que no es necesario enviar datos con la llamada o indicar configuración alguna.

**Ejemplo de salida:**
```json
{
    "Value": "1",
    "Channel": "ORACLE ALERTLOG P01",
    "Unit": "Custom"
},
{
    "Value": "1",
    "Channel": "ORACLE ALERTLOG ASM",
    "Unit": "Custom"
},
{
    "Value": "1",
    "Channel": "ARCHIVELOG P01",
    "Unit": "Custom"
},
{
    "Value": "1",
    "Channel": "ERRPT",
    "Unit": "Custom"
}
```

## `/prtg/sap/processes`
Este sensor devuelve el estado de la instancia SAP que se pase como parámetro. El número de instancia debe pasarse como parámetro extra en la ruta, como por ejemplo:
- `/prtg/sap/processes/0` Consulta la instancia 00.
- `/prtg/sap/processes/41` Consulta la instancia 41.
- `/prtg/sap/processes/11/12/13/14` Consulta la instancia 11. De no encontrarse procesos de dicha instancia, pasa a consultar la instancia 12, y así sucesivamente hasta que encuentra procesos para alguna instancia o llega al final, en cuyo caso pone el sensor en error.

> Internamente, el HAgent ejecuta el comando `/usr/sap/hostctrl/exe/sapcontrol -nr XX -function GetProcessList` y convierte la salida al formato de canales PRTG.

**Ejemplo de canales para una instancia HANA:**
```json
{
	"Value": "1",
	"Channel": "HDB Daemon (hdbdaemon)",
	"Unit": "Custom"
,
{
	"Value": "1",
	"Channel": "HDB Compileserver (hdbcompileserver)",
	"Unit": "Custom"
}
```

**Ejemplo de canales para una instancia de dialogo Java:**
```json
{
	"Channel": "J2EE Server (jstart)",
	"Value": "1",
	"Unit": "Custom"
}
{
	"Channel": "IGS Watchdog (igswd_mt)",
	"Value": "1",
	"Unit": "Custom"
}
```

**Ejemplo de canal para una instancia ERS:**
```json
{
    "Value": "1",
	"Channel": "EnqueueReplicator (enrepserver)",
	"Unit": "Custom"
}
```




## `/prtg/oracle/standby_gap`
Este sensor comprueba la diferencia en números de secuencia entre una Oracle Standby Database y su Primary Database. El sensor tendrá un canal por cada thread de la base de datos, donde se especificará la diferencia entre el número de secuencia primary y standby. Por tanto, un valor de 0 en el canal indica que ambas bases de datos están en sincronía.

Para configurar este sensor, es preciso configurar un elemento de configuración `monitorized_element` de tipo **`standby_gap`**, donde se indican los parámetros de conexión tanto a la primary como a la stadby database:

```json
{
	"monitorized_elements": [
		{
			"type": "standby_gap",
			"stb_user": "HAGENT AS SYSDBA",
			"stb_password": "whatever",
			"stb_tns": "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=stb)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=STB)(SID=P01)))",
			"primary_user": "HAGENT",
			"primary_password": "whatever",
			"primary_tns": "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=racp01-scan)(PORT = 1521)))(CONNECT_DATA=(SERVICE_NAME=P01)))"
		}
	]
}
```

**Nota**: Es necesario que el usuario sea `SYSDBA` en la standby database para poder conectarse a una instancia que realmente no está abierta.

> Internamente, el HAgente realiza conexiones a ambas instancias y realiza la siguiente consulta para conocer el último número de secuencia de cada thread:
```sql
SELECT 
	THREAD# AS THREAD,
	MAX(SEQUENCE#) AS SEQ
FROM 
	V$ARCHIVED_LOG
GROUP BY
	THREAD#
```

## `/prtg/oracle/clusterware`
Este sensor ayuda a valorar si el estado de los servicios de Oracle Clusterware están de manera óptima. Deberemos especificar en la configuración lo que nosotros entendemos que es el estado óptimo del clúster, indicando que recursos deben estar activos y en qué nodo.

> Para obtener esta información, el HAgent analiza la salida del comando `crsctl status res`.

**Configuración:**
Para configurar este sensor es necesario incluir un objeto JSON de tipo `monitorized_element` de tipo **`oracle_grid_resources`** dentro del array de `monitorized_elements`.

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`process_list`** para que el `oracle_grid_resources` se entienda como un monitor de recursos del cluster de Oracle GRID.
**conditions** | `object[]` | `[]` | Lista de los recursos del cluster que queremos monitorizar, junto con las restricciones. Cada objeto se espera que sea de la forma que se detalla en la siguiente tabla.

Cada elemento monitorizado dentro de las condiciones debe ser de la siguiente manera:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**resource** | `string` | | **OBLIGATORIO**. El nombre del recurso que vamos a monitorizar, por ejemplo `ora.p01.db` o `sap.abapenq.p01`.
**location** | `string` | `null` | El nodo donde se espera que aparezca el recurso. Si se deja en blanco, el HAgente dará por buena cualquier localización del recurso, siempre y cuando esté esté online. Adicionalmente, existe una sintaxis especial para indicar que este recurso debe estar junto con otro recurso `RES <recurso>` o todo lo contrario, con `!RES <recurso>`.
**on_mismatch** | `string` | error | Indica el nivel de alarma que monstrará sentinel si no se cumple esta conción. Los valores válidos son `error`, `warn` e `ignore`.

Un ejemplo de configuración sería:

```json
{
	"type": "oracle_grid_resources",
	"conditions": [
		{"resource": "ora.p01.p01_d12.svc", "location": "rac1p01", "on_mismatch": "warn"},
		{"resource": "ora.rac1p01-adm.vip", "location": "rac1p01"},
		{"resource": "ora.rac2p01-adm.vip", "location": "rac2p01"},
		{"resource": "ora.rac1p01.vip", "location": "rac1p01"},
		{"resource": "ora.rac2p01.vip", "location": "rac2p01"},
		{"resource": "sap.P01.abapvip", "location": "RES sap.P01.ASCS00.abapenq"},
		{"resource": "sap.P01.abapvip2", "location": "RES sap.P01.ASCS00.abapenq"},
		{"resource": "sap.P01.ERS01.abaprep", "location": "!RES sap.P01.ASCS00.abapenq"}
	]
}
```


**Ejemplo de salida del sensor:**
```json
{
    "Value": "2",
    "Channel": "ora.t01.t01_d00_sap3t01.svc",
    "ValueLookup": "prtg.standardlookups.cisco.ciscoenvmonstate",
    "Unit": "Custom"
},
{
    "Value": "1",
    "Channel": "ora.scan2.vip",
    "ValueLookup": "prtg.standardlookups.cisco.ciscoenvmonstate",
    "Unit": "Custom"
},
{
    "Value": "1",
    "Channel": "ora.rac1t01.vip",
    "ValueLookup": "prtg.standardlookups.cisco.ciscoenvmonstate",
    "Unit": "Custom"
}
```


## `/prtg/proyman/impresoras`
Este sensor contiene datos de la entrada de pedidos en Proyman. En concreto, permite consultar la cantidad de checkeos, pedidos y errores en pedidos, y los tiempos de los mismos.

Internamente, los datos se recogen con un checker `ProymanImpresoraChecker` que lee el fichero de impersora (cuya ubicación es configurable) y almacena los 1000 últimos registros del fichero. Por la naturaleza del fichero de, distinguimos 4 tipos de mensajes: `CHECKEO`, `PEDIDO`, `ERRPEDIDO` y `OTROS`. Nos interesan los 3 primeros:


- **`CHECKEO`**
```
10025|0020827594|0000027594|20170627|115751|001| 000|135C1A25|-Chequeo--|proccli10813812_01| |00000.07|20170627|115751|sap2p01_P|RG19
```

- **`PEDIDO`**
```
10028|0010117369|0000017369|20170629|082237|024| 033|63493B4E|2007606720|proccli4653410_01| |00002.57|20170629|082238|sap2p01_P|RG01
```

- **`ERRPEDIDO`**
```
10025|0010117867|0000017867|20170629|082747|001| 000|B4B75577|ErrPedido-|proccli61210668_01|No se ha indicado so|00000.02|20170629|082747|sap6p01_P|
```

Para consultar los datos, el endpoint admite un parámetro extra con el que podemos indicar como queremos agrupar los datos:

- `/prtg/proyman/impresoras`: Devuelve todos los datos sin agrupar, es decir, el número total de checkeos, pedidos, errores y las medias de tiempo de checkeo y pedido del último minuto.
```json
{
    "Value": "45",
    "Channel": "Chequeos",
    "Unit": "count"
},
{
    "Value": "45",
    "Channel": "Pedidos",
    "Unit": "count"
},
{
    "Value": "0",
    "Channel": "Errores",
    "Unit": "count"
},
{
    "Float": 1,
    "Value": "0.2542222235765722",
    "Channel": "Tiempo chequeo",
    "Unit": "TimeSeconds"
},
{
    "Float": 1,
    "Value": "2.054444468849235",
    "Channel": "Tiempo pedido",
    "Unit": "TimeSeconds"
}
```

- `/prtg/proyman/impresoras/server/<server>`: Consulta los mismos datos que el endpoint normal, pero filtra los de un servidor SAP. Por ejemplo, `/prtg/proyman/impresoras/server/sap1p01`.
- `/prtg/proyman/impresoras/lines`: Devuelve estadísticas del número de líneas de los pedido/checkeo. Esto es, el tiempo medido de pedido/checkeo por linea, la media de lineas por pedido/checkeo, etc...
- `/prtg/proyman/impresoras/vlines`: Consulta los datos agrupando los pedidos/checkeos en dos grupos, uno para los pedidos de menos de 25 líneas y otro para los pedidos con 25 o más líneas.


**Configuración:**
Para configurar este sensor es necesario incluir un objeto `monitorized_element` de tipo **`proyman_impresora`** dentro del array de `monitorized_elements`.

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`proyman_impresora`** para que el HAgente entienda que debe arrancar este checker.
**file** | `string` | | **OBLIGATORIO**. Es la ruta al fichero de salida de impresora. Por ejemplo: `/tmp/impresora`.


Ejemplo:
```json
{
	"monitorized_elements": [
		{
			"type": "proyman_impresora",
			"file": "/tmp/impresora"
		}
	]
}
```


## /prtg/apache/server-status[/hostname]
Este sensor permite conocer el estado de los threads de un servidor Apache. En concreto, muestra la cantidad de threads que están activos, leyendo, escribiendo, cerrándose, etc... 

> Este sensor hace una consulta al endpoint `/server-status` de Apache para acceder a los datos del `mod_status`. Si se especifica el parámetro opcional *`hostname`* en la URL, se consulta dicho nombre del servidor. De no especificarse, se consulta utilizando el nombre del host del HAgente. Por ejemplo, si en la máquina `liferay-ia` llamamos a este endpoint sin especificar *`hostname`*, fallará, pues internamente se llama a `https://liferay-ia.hefame.es/server-status`, lo cual da un error de certificado. Para que funcione correctamente, es preciso indicar el hostname de la web, en este caso: `/prtg/apache/server-status/www.interapothek.es`, de modo que internamenete se hace la llamada a `https://www.interapothek.es/server-status`.

**Ejemplo de salida:**
```json
{"Value": "9", "Channel": "Ready", "Unit": "Count"},
{"Value": "119", "Channel": "Available", "Unit": "Count"},
{"Value": "6", "Channel": "Reading Request", "Unit": "Count"},
{"Value": "3", "Channel": "Sending Reply", "Unit": "Count"},
{"Value": "0", "Channel": "Starting Up", "Unit": "Count"},
{"Value": "0", "Channel": "Closing", "Unit": "Count"},
{"Value": "0", "Channel": "Logging", "Unit": "Count"},
{"Value": "0", "Channel": "Finishing", "Unit": "Count"},
{"Value": "0", "Channel": "Idle cleanup", "Unit": "Count"},
{"Value": "13", "Channel": "Keepalive", "Unit": "Count"},
{"Value": "0", "Channel": "DNS lookup", "Unit": "Count"},
{"Value": "150", "Channel": "TOTAL Threads", "Unit": "Count"},
{"Value": "22", "Channel": "TOTAL Used", "Unit": "Count"},
{"Value": "128", "Channel": "TOTAL Idle", "Unit": "Count"}
```



#Generación de reportes
El HAgente permite generar varios reportes con información del host.

## Updates del OS: `/report/os_updates`
Este reporte recopila información del estado de actualización del sistema operativo. **Nota:** Actualmente solo está soportado en sistemas `SUSE` y `Oracle Linux`. Este checker no requiere ninguna configuración.

> En el caso de SUSE, el HAgente consulta información de parches y updates disponibles con `zypper -x lp` y `zypper -x lu`. También lee el fichero `/var/log/zypper.log` para determinar la fecha de la última vez que se ejecutó un `zypper update`.

**Ejemplo de salida en SUSE**
```json
{
    "os": {
        "code": "LNX",
        "level": {
            "major": 12,
            "string": "12.2",
            "kernel": "4.4.114-92.64-default",
            "sp": 2
        },
        "name": "SUSE Linux",
        "architecture": "amd64"
    },
    "updates": {
		"updates": 28,
        "patches": 5,
        "lastUpdate": 1518621184000
    }
}
```


> En el caso de Oracle Linux, el sistema ejecuta un `yum check-update -q`. En este caso, solo podemos obtener la cantidad de paquetes pendientes de actualizar.

**Ejemplo de salida en Oracle Linux**
```json
{
    "os": {
        "code": "ORALNX",
        "level": {
            "major": 6,
            "minor": 8,
            "string": "6.8",
            "kernel": "3.8.13-118.13.3.el6uek.x86_64"
        },
        "name": "Oracle Linux",
        "architecture": "amd64"
    },
    "updates": {
        "updates": 246
    }
}
```


## Envío de pedidos a proyman: `/report/proyman/isap3060/<RGxx>`
Permite consultar la lista de pedidos que Proyman ha recibido de SAP para un determinado almacén (`RGxx`). La lista devuelta se compondrá de los últimos pedidos que Proyman ha recibido de SAP a través del isap3060, para el almacén especificado.


**Configuración:**
Para configurar este sensor es necesario incluir un objeto `monitorized_element` de tipo **`proyman_isap3060`** dentro del array de `monitorized_elements`.

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`proyman_isap3060`** para que el HAgente entienda que debe arrancar este checker.
**file** | `string` | | **OBLIGATORIO**. Es la ruta al fichero de los del proceso isap3060. Por ejemplo: `/proyman/isap/log/isap3060.log`.
**buffer_size** | `integer` | 1000 | Este es el número máximo de mensajes que se retendrán en memoria. Cuando se supere este número, los mensajes más antiguos se irán descartando.
**werks** | `string[]` | `[]` | Aquí indicaremos los códigos de aquellos almacenes que queremos monitorizar. Por ejemplo, para monitorizar `RG06` y `RG10`, el valor será `["RG06", "RG10"]`.


Ejemplo:
```json
{
	"monitorized_elements": [
		{
			"type": "proyman_isap3060",
			"file": "/proyman/isap/log/isap3060.log",
			"buffer_size": 1000,
			"werks": ["RG06", "RG10"]
		}
	]
}
```

**Ejemplo de salida:**
```json
[
    {"timestamp": 1522236819651, "albaran": "0116338016", "centro": "RG10", "pedido": "0045481986", "hoja": 1},
    {"timestamp": 1522236820006, "albaran": "0116338017", "centro": "RG10", "pedido": "0045481996", "hoja": 1},
	{"timestamp": 1522236820478, "albaran": "0116338018", "centro": "RG10", "pedido": "0045482002", "hoja": 1},
	{"timestamp": 1522236821514, "albaran": "0116338018", "centro": "RG10", "pedido": "0045482002", "hoja": 3},
	{ ... }
]
```


#Consultas de metadatos del HAgente

## `/agent/info`
Devuelve información general del HAgente, datos como el entorno, versión y uso de recursos de la JVM, versión del sistema operativo, la versión del propio HAgente, el uptime

```json
{
    "os": {
        "level": {
            "tl": 9,
            "sp": 4,
            "string": "6100 TL9 SP4(1441)",
            "date": 1441,
            "major": 6100
        },
        "name": "AIX",
        "code": "AIX",
        "architecture": "ppc64"
    },
    "environment": {
        "IBM_JAVA_COMMAND_LINE": "/usr/java71_64/jre/bin/java -Xmx64m -Dlog4j.configurationFile=/usr/local/hagent/log4j2.xml -Dhagent.configurationFile=/usr/local/hagent/config.json -jar /usr/local/hagent/apihagent.jar",
        "SHELL": "/usr/bin/ksh",
        "PATH": "/usr/bin:/etc: ... :/usr/local/bin/util",
        "LIBPATH": "/usr/java71_64/jre/lib/ppc64/j9vm: ... :/usr/lib",
        "USER": "root",
        "LOGIN": "root"
    },
    "runtime": {
        "mem_total": 19333120,
        "mem_used": 9992584,
        "mem_max": 67108864,
        "processors": 4,
        "mem_free": 9340536
    },
    "properties": {
        "java_vm_name": "IBM J9 VM",
        "hagent_configurationFile": "/usr/local/hagent/config.json",
        "java_vm_vendor": "IBM Corporation",
        "log4j_configurationFile": "/usr/local/hagent/log4j2.xml",
        "awt_toolkit": "sun.awt.X11.XToolkit",
        "file_encoding": "ISO8859-1",
        "java_class_path": "/usr/local/hagent/apihagent.jar",
        "java_specification_version": "1.7",
        "java_vm_specification_vendor": "Oracle Corporation",
        "java_home": "/usr/java71_64/jre",
        "java_vendor": "IBM Corporation"
    },
    "agent": {
        "port": 55555,
        "uptime": 177504085,
        "hostname": "proyman2.hefame.es",
        "config_file": "/usr/local/hagent/config.json",
        "version": {
            "built_date": "20180226132509",
            "version": "1.19",
			"build": "9"
        }
    }
}
```

## `/agent/log4j`
Este endpoint retorna el contenido de la configuración de `Log4j2`. Esta es la librería que el HAgente utiliza para dejar logs y trazas. Nótese que es de los pocos endpoints que no devuelven contenido JSON, sino XML.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" monitorInterval="30">
    <Properties>
        <Property name="baseDir">/var/log/hagent</Property>
    </Properties>
    <Appenders>
        <RollingRandomAccessFile name="FileLogging"
			fileName="${baseDir}/hagent.log" filePattern="${baseDir}/hagent-%d{MMddyyyy}.%i.log.gz">
            <PatternLayout>
                <Pattern>%d{DEFAULT} - %t - %-5level - %notEmpty{%marker - }%c{10} - %msg%n</Pattern>
            </PatternLayout>
			<And So="On......" />
		</RollingRandomAccessFile>
	</Appenders>
</Configuration>
```


## `/agent/config`
Este endpoint permite ver, modificar o forzar la recarga de la configuración del HAgente.

- **`GET`** `/agent/config` - Devuelve en JSON la configuración actual del HAgente en formato JSON.
- **`GET`** `/agent/config/reload` - Fuerza la re-lectura del fichero de configuración del HAgente y aplica los cambios. Devuelve en JSON la nueva configuración leída
- **`POST`** `/agent/config` - Espera que en el body de la petición HTTP se incluya la nueva configuración codificada en JSON. Al cambiar la configuración con este método, se fuerza la relectura de la misma inmediatamente.


## `/agent/restart`
Al llamar al este endpoint, el HAgente se reinicia.
Tengase en cuenta que cuando un HAgente se reinicia, intenta contactar con el repositorio en busca de actualizaciones, lo cual hace que este endpoint sea muy util para actualizar HAgentes en masa. Si la ejecución es correcta, el HAgente devolverá un mensaje como el siguiente:
```json
{
    "date": 1519830524795,
    "message": "Iniciado reinicio del HAgente"
}
```

## `/agent/register`
Fuerza al HAgente a que se registre inmediatamente contra el repositorio de HAgentes.


## `/agent/channeltest[/<channel>]`
Realiza la prueba de envío de una notificación a un canal de notificaciones específico. Si no se especifica ningún canal, se utilizará el canal `default`.

> Internamente, se instancia el canal de alertas desde la configuración y se envía un mensaje `Mensaje de prueba desde <hostname>`.

Salvo error crítico, este endpoint devuelve un código `HTTP 202` sin contenido. Deberemos comprobar si la alerta ha funcionado correctamente.







#Checkeos
A continuación detallaremos el funcionamiento de los checkers disponibles en el HAgente
## Checkeo de ERRPT - `ErrptChecker`
Este checker, llamado `ErrptChecker`, vigila constantemente si aparecen nuevos mensajes de error en el log de error de máquinas AIX.

> En concreto, ejecuta el comando `errpt -s HHMMSS -T hhmmss`, donde `HHMMSS` y `hhmmss` es el rango de horas que queremos consultar.

Cuando un nuevo error aparece, el checker realiza una notificación por el canal de alerta especificado en su configuración.

**Configuración:**
Para que se arranque este checker, debe existir un `monitorized_element` de tipo `errpt` en la lista de elementos monitorizados de la configuración. El elemento deberá tener la siguiente configuración:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`errpt`** para que el HAgente entienda que debe arrancar este checker.
**check_interval** | `integer` | 60000 | Es el intervalo de tiempo que debe pasar entre checkeo y checkeo, en milisegundos.
**type_filter** | `string` | INFO,PEND,PERF,PERM,TEMP,UNKN | Este parámentro permite filtar algunos tipos de mensajes del log de errores.
**alert_channel** | `string` | default | Este parámetro indica el nombre del canal de alerta que se usará para enviar alertas cuando se encuentren errores.


Ejemplo:

```json
{
	"type": "errpt",
	"check_interval": 60000,
	"type_filter": "INFO,PEND,PERF,PERM,TEMP,UNKN",
	"alert_channel": "default"
}
```


## Checkeo de Oracle AlertLog - `OracleAlertlogChecker`
El `OracleAlertlogChecker` vigila el contenido nuevo que va apareciendo en el fichero de alertas de una instancia Oracle.

>**NOTA:** Este Checker se mantiene por compatibilidad, se recomienda usar en su lugar el `AlertlogChecker`, que es genérico (no solo de Oracle).

**Configuración:**
Para que se arranque este checker, debe existir un `monitorized_element` de tipo `oracle_alertlog` en la lista de elementos monitorizados de la configuración. El elemento deberá tener la siguiente configuración:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`oracle_alertlog`** para que el HAgente entienda que debe arrancar este checker.
**db_name** | `string` |  | **OBLIGATORIO**. El nombre de la base de datos. No tiene que conincidir con el nombre del SID ni del servicio Oracle, es tan solo para poder tener varios checkers de este tipo en un mismo host.
**alert_log** | `string` |  | **OBLIGATORIO**. Ruta al fichero de Oracle Alert. Por ejemplo: `/oracle/P01/saptrace/diag/rdbms/p01/P011/trace/alert_P011.log`
**include_regex** | `regex` | ORA\\-[0-9]+&#124;WARNING&#124;ERROR&#124;Deadlock | Esta expresión regular es la que filtra que mensajes serán considerados como alertas que deben notificarse.
**exclude_regex** | `regex` | ORA-19815 | Cuando se filtra un mensaje por el filtro `include_regex`, antes de considerarse como alerta se comprueba que no conincida con esta expresión regular.
**alert_channel** | `string` | default | Este parámetro indica el nombre del canal de alerta que se usará para enviar alertas cuando se encuentren errores.

```json
{
	"type": "oracle_alertlog",
	"db_name": "P01",
	"alert_log": "/oracle/P01/saptrace/diag/rdbms/p01/P011/trace/alert_P011.log",
	"include_regex": "ORA\\-[0-9]+|WARNING|ERROR|Deadlock",
	"exclude_regex": "ORA-19815",
	"alert_channel": "default"
}
```

## Checkeo de AlertLog - `AlertlogChecker`
El `AlertlogChecker` vigila si aparecen líneas con un determinado patrón en un fichero de log.

**Configuración:**
Para que se arranque este checker, debe existir un `monitorized_element` de tipo `alertlog` en la lista de elementos monitorizados de la configuración. El elemento deberá tener la siguiente configuración:

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`alertlog`** para que el HAgente entienda que debe arrancar este checker.
**name** | `string` |  | **OBLIGATORIO**. El identificador del checker. Es tan solo para poder tener varios checkers de este tipo en un mismo host. Cada uno deberá tener su nombre único.
**alert_log** | `string` |  | **OBLIGATORIO**. Ruta al fichero que queremos monitorizar. Por ejemplo: ``
**include_regex** | `regex` | *(cadena vacía)* | Esta expresión regular es la que filtra que mensajes serán considerados como alertas que deben notificarse.
**exclude_regex** | `regex` | *(cadena vacía)* | Cuando se filtra un mensaje por el filtro `include_regex`, antes de considerarse como alerta se comprueba que no conincida con esta expresión regular.
**alert_channel** | `string` | default | Este parámetro indica el nombre del canal de alerta que se usará para enviar alertas cuando se encuentren errores.

```json
{
	"type": "alertlog",
	"name": "EBIASAP",
	"alert_log": "/usr/local/ebiasap/log/ebiAdapterSAPSession.log",
	"include_regex": "(Error\\sde\\sintegridad\\sen\\sel\\sadaptador)",
	"exclude_regex": "",
	"alert_channel": "default"
}
```




## Checkeo de Archivado Oracle - `OracleArchivelogChecker`
Los `OracleArchivelogChecker` se encargan de vigilar la ocupación del filesystem donde se almacenan los archivelogs de oracle y de lanzar el comando pertinente para liberar espacio en el mismo en caso de ser necesario.

El `OracleArchivelogChecker` puede configurarse para realizar una de tres acciones:
- **`BrArchiveCommand`**. Realiza la copia de seguridad de los ficheros utilizando las SAP BrTools.
> Internamente, llama a `su - <ORAUSER> -c brarchive -u <DBUSER> -c force -p <UTLFILE> -sd`.

- **`TdpoArchiveCommand`**. Realiza la copia de seguridad de los ficheros utilizando Oracle RMAN.
> Internamente llama a RMAN `su - <ORAUSER> -c rman target /` y ejecuta:
```
run {
	allocate channel sbt_1 type sbt_tape parms '<EXTRAPARAMS>ENV=(TDPO_OPTFILE=<TDPOFILE>)';
	backup archivelog all;"
	release channel sbt_1;"
}
crosscheck archivelog all;"
delete noprompt archivelog all backed up 1 times to sbt_tape;"
```

- **`DeleteArchiveCommand`**. Realiza el borrado de los ficheros utilizando Oracle RMAN.
> Internamente llama a RMAN `su - <ORAUSER> -c rman target /` y ejecuta:
```
delete noprompt archivelog all;
```

**Configuración:**
Para activar un `OracleArchivelogChecker` de cualquier tipo, deberemos incluir en la lista de elementos monitorizados un objeto `monitorizez_element` del tipo `<TYPE>_archive`, donde `<TYPE>` variará según el tipo de acción que queramos ejecutar.

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**type** | `string` | | **OBLIGATORIO**. El valor debe ser igual a **`br_archive`**, **`tdpo_archive`** o **`delete_archive`** en función de la acción que queramos ejecutar.
**db_name** | `string` |  | **OBLIGATORIO**. El nombre de la base de datos. Debe conincidir con el nombre del servicio Oracle o con el SID de SAP en el caso de archivado con BrTools *(del usuario `sidadm`)*
**archive_dest** | `string` |  | **OBLIGATORIO**. El filesystem donde se encuentran los archivelogs. Por ejemplo `+P01_ARCH`.
**archive_percent** | `numericString`, `integer` |  | **OBLIGATORIO**. El umbral de ocupación del filesystem que de superarse debe hacer que se lance el proceso de archivado.
**alert_channel** | `string` | default | El nombre del canal de alertas por el cual se avisará si el archivado falla.

**Parámetros específicos BR Archive:**

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**user** | `string` |  | **OBLIGATORIO**. Es el nombre del usuario de la base de datos que usaremos para conectar con las BrTools. Normalmente estableceremos este valor a `//`, aunque otros ejemplos son `SYS/pass` o `ADMIN/pwd`.
**sap_file** | `string` | `init<SID>.sap` | Este parámetro indica la ruta al fichero .SAP que utilizaremos para llamar a las BrTools.
**br_user** | `string` | `//` | Indica el usuario con el que la BrTools intentan conectarse a la base de datos.
**br_option** | `string` | `-sd` | Permite incluir parámetros adicionales para la llamada a BR Archive. Ver la ayuda de BR*Archive para ver las opciones disponible.
```json
{
	"type":"br_archive",
	"db_name": "P01",
	"user": "//",
	"archive_dest": "+P01_ARCH",
	"archive_percent": 90,
    "br_option": "-ds"
}
```

**Parámetros específicos TDPO Archive:**

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**user** | `string` |  | **OBLIGATORIO**. El usuario de la base de datos. Este es el usuario que ejecutará el comando RMAN. Por ejemplo, `oracle` o `oratrh`.
**tdpo_optfile** | `string` | | **OBLIGATORIO**. Este parámetro indica la ruta al fichero de condifuración de tdpo que utilizaremos para configurar el canal RMAN. Por ejemplo `/usr/tivoli/tsm/client/oracle/bin64/tdpoDJW.opt`.
**extra_env** | `string` | *(cadena vacía)* | Permite pasar parámetros adicionales para la creación del canal RMAN. Por ejemplo, poner `SBT_LIBRARY=/usr/lib/libobk64.a(shr.o)` es necesario en hosts donde hay un TDPERP y un TDPORA instalados a la vez.
**oracle_home** | `string` | *(cadena vacía)* | Indica cual es el ORACLE\_HOME de la base de datos a hacer backup. Este parámetro es necesario para cuando el usuario de la base de datos es el dueño de varios HOMEs, como en el caso común del usuario `oracle`. Si no se especifica, se asume que el usuario establece el `ORACLE_HOME` desde su entorno. 


```json
{
	"type": "tdpo_archive",
	"db_name": "DJW",
	"user": "oradjw",
	"tdpo_optfile": "/usr/tivoli/tsm/client/oracle/bin64/tdpoDJW.opt",
	"extra_env": "SBT_LIBRARY=/usr/lib/libobk64.a(shr.o)",
	"archive_dest": "/oracle/DJW/oraarch",
	"archive_percent": 25,
	"alert_channel": "default"
}
```

**Parámetros específicos Delete Archive:**

Nombre | Tipo | Defecto | Descripción
--- | --- | --- | ---
**user** | `string` |  | **OBLIGATORIO**. El usuario de la base de datos. Este es el usuario que ejecutará el comando RMAN. Por ejemplo, `oracle` o `oratrh`.
```json
{
	"type": "delete_archive",
    "db_name": "T01",
	"user": "oracle",
    "archive_dest": "+T01_ARCH",
	"archive_percent": 25,
	"alert_channel": "default"
}
```




## Checkeos Proyman
Existen dos checkers para vigilar ficheros de proyman. Sin embargo, estos checkers no realizan notificaciones, sino que almacenan información en un buffer para ser posteriormente consultada. Aunque esto parece que podría hacerse mejor con un `Sampler`, estos procesos requieren mantener un fichero abierto, acción que no es capaz de realizar un `Sampler` (que podría abrirlo y cerrarlo inmediatamente, pero no mantenerlo abierto).

Para consultar los datos recopilados por los checkers de proyman, ya hemos explicado que existen los siguientes endpoints:
- `/report/proyman/isap3060/<RGxx>`
- `/prtg/proyman/impresora`







