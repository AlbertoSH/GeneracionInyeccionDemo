# Quita, que tú no sabes. Ya lo hago yo

*Generación e inyección de código en tiempo de compilación*

La presentación que acompaña a este workshop está disponible 
[en Slideshare](http://es.slideshare.net/AlbertoSanzHerrero/quita-que-t-no-sabes-ya-lo-hago-yo-generacin-e-inyeccin-de-cdigo-en-tiempo-de-compilacin)

## Tareas

1. Entender el funcionamiento del procesador de anotaciones del módulo _analysis_1_compile_
2. Escribir un procesador de anotaciones que busque los constructores de cada clase con una anotación propia e imprima por pantalla las dependencias de cada uno (solución en el módulo _analysis_2_compiler_
3. Escribir un programa que escriba por pantalla un programa que escriba por pantalla un Hello, World! (solución en el módulo _generation_1)
4. Escribir un procesador de anotaciones que dada una clase anotada genere un Builder para dicha clase (solución en el módulo _generation_2_compiler_)
5. Escribir un procesador de anotaciones que inyecte en tiempo de compilación getters para atributos privados. Comparar el rendimiento del uso de dichos getters con respecto al uso de reflection para acceder a dichos valores (solución en el módulo _injection_1_compiler_ + _injection_1_sample_)
6. Completar el ejercicio del apartado 4 inyectando en tiempo de compilación un constructor que reciba como parámetro el builder generado (solución en el módulo _injection_2_sample_

### Notas

Es recomendable el uso de Gradle (simplifica bastante las tareas) pero no es indispensable.
En caso de usar Gradle tened en cuenta que una vez compilado el código fuente no se vuelve a compilar hasta que dichos fuentes no se modifican. Vuestro procesador no se ejecutará en sucesivas compilaciones a no ser que modifiquéis los ficheros de código fuente (o hagáis un `clean`)
La tarea para compilar es `compileJava`. Dicha tarea ya ejecuta los procesadores si se aplica el plugin `net.ltgt.apt`
