1_puntos de mejora: Ejecutar el action index del controller sales, de manera asyncrona para poder procesar 
varios request al mismo tiempo y asi poder resolverlos en simultaneo. Esto se ejecutaria, generando un pool de threads fixed o cached dependiendo 
del volumen de datos a procesar t el tiempo de ejecucion de las tareas, al entrar un request, crear una task runnable/callable la cual maneje las operaciones.
Bajar tiempos de timeOut,para no encolar con tiempos innecesarios si algo sale mal, y poder controlar el tiempo de ejecucion asegurandose que no va a pasar de ciertos margenes. Agregar iteraciones. Esto va conseguir que si alguna request falla, se ejecute nuevamente, y no asumir que la api siempre va a responder.si responde bien no iterar. 
Solamente recuperar los productos y calcular total si, el usuario es de tipo vendedor. 
Despues de recuperar los productos, ejecutar la suma de forma asyncrona, para poder sumar todos los productos a la vez y no esperar a hacerlo uno por uno. 
Para calcular la tasa del dolar del total, hacerlo una sola vez al iniciar el servidor // sirve para este caso en el que no tengo parametros establecidos, seria bueno ponerlo dentro de un job e ir actualizando este valor cada cierto tiempo. 
el metodo de notificar respuesta, tambien puede ser ejecutado asyncronicamente ya que su logica no afecta, ni es necesario esperarlo para continuar. 
generar una clase, respuesta la cual haga de wrapper para los metodos de retornar la respuesta, del main controller. y asi poder manejarse mas facilmente.
generar un metodo para checkear la disponibilidad de la api y ver si esta levantada, ejecutar antes de cada request? cada cierto tiempo? //posible job 
	
Para dejar la tasa de error en 0% encerre las llamadas entre do while, mientras no devuelva un estado 200 siga pidiendo, pero, personalmente no me gusta, yo lo que haria seria elegir una cantidad maximas de intentos con un for, para no bloquear la execucion.  

2_ en el response , el header de content length es 139 y los demas tienen un length de 138 por lo cual no coincide, en el caso de buscar el usuario, que es el largo de los demás lo cual significa que el mensaje esta incompleto/fallado/demasiado grande . se puede controlar junto al status. tambien es una condicion para salir de las iteraciones. 

Adjunto una clase pinger / la cual es para medir por consola la disponibilidad del servidor corriendo local. 

No logre el objetivo de Ejecutar las task en paralelo, ya que si ejecuto el future.get para bloquear la ejecucion hasta resolver la tarea en el controller del index, Bloqueo la ejecucion del hilo principal, lo cual lo vuelve secuencial nuevamente, y no encontre la manera de pasar el contexto al thread para poder delegarle la tarea de retornar o mejor dicho asignar los valores de respuesta. 




