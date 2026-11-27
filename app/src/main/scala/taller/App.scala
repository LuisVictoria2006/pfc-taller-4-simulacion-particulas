package taller

object App {

  def main(args: Array[String]): Unit = {
    val sim = new SimulacionParticulas()
    import sim._

    def dos(x: Double): String = "%.2f".formatLocal(java.util.Locale.US, x)

    val dt = 0.5
    val repeticiones = 5
    val tamanos = List(1000000, 2000000)

    println(s"Núcleos disponibles: ${Runtime.getRuntime.availableProcessors}")
    println("| Partículas | Umbral | Secuencial (ms) | Paralelo (ms) | Aceleración |")
    println("|---|---|---|---|---|")

    tamanos.foreach { n =>
      val sistema: Sistema = Array.tabulate(n)(i =>
        Particula(Vector2(i.toDouble, 0.0), Vector2(1.0, 0.5), 1.0))
      val umbrales = List(1000, 10000, 100000, 500000, n)
      val tSecuencial = medir(repeticiones)(pasoSecuencial(sistema, dt))

      umbrales.foreach { u =>
        val tParalelo = medir(repeticiones)(pasoParalelo(sistema, dt, u))
        val etiqueta = if (u == n) s"$u (control)" else u.toString
        println(s"| $n | $etiqueta | ${dos(tSecuencial)} | ${dos(tParalelo)} | ${dos(aceleracion(tSecuencial, tParalelo))} |")
      }

      println(s"Umbral conveniente para $n partículas: ${umbralConveniente(sistema, dt, umbrales)}")
    }
  }
}