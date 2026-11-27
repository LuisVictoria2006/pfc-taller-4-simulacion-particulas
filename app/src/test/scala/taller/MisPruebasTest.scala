package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class MisPruebasTest extends AnyFunSuite {

  val sim = new SimulacionParticulas()
  import sim._

  def casi(a: Double, b: Double): Boolean =
    math.abs(a - b) <= 1e-6 * math.max(1.0, math.abs(a))

  def casiV(a: Vector2, b: Vector2): Boolean =
    casi(a.x, b.x) && casi(a.y, b.y)

  val quieto = Vector2(0.0, 0.0)
  val p1 = Particula(Vector2(1.0, 1.0), Vector2(2.0, -1.0), 3.0)
  val p2 = Particula(Vector2(-2.0, 4.0), Vector2(0.5, 0.5), 1.0)
  val p3 = Particula(Vector2(0.0, 0.0), Vector2(0.0, 3.0), 2.0)
  val sistema: Array[Particula] = Array(p1, p2, p3)

  val grande: Array[Particula] = Array.tabulate(1000)(i =>
    Particula(Vector2(i.toDouble, 2.0 * i), Vector2(1.0 + i % 3, 0.5), 1.0 + i % 5))

  // Punto 1

  test("mover: avanza la posición velocidad por dt y deja velocidad y masa") {
    assert(mover(p1, 2.0) == Particula(Vector2(5.0, -1.0), Vector2(2.0, -1.0), 3.0))
  }

  test("mover: con dt negativo retrocede") {
    assert(mover(p2, -2.0).pos == Vector2(-3.0, 3.0))
  }

  test("mover: con dt cero la partícula queda igual") {
    assert(mover(p1, 0.0) == p1)
  }

  test("pasoSecuencial: mueve cada partícula de un sistema de tres") {
    val posiciones = pasoSecuencial(sistema, 1.0).map(p => p.pos).toList
    assert(posiciones == List(Vector2(3.0, 0.0), Vector2(-1.5, 4.5), Vector2(0.0, 3.0)))
  }

  test("pasoSecuencial: no cambia velocidades ni masas") {
    val antes = sistema.map(p => (p.vel, p.masa)).toList
    val despues = pasoSecuencial(sistema, 3.0).map(p => (p.vel, p.masa)).toList
    assert(antes == despues)
  }

  test("pasoSegmento: solo mueve el tramo pedido") {
    val tramo = pasoSegmento(sistema, 1.0, 1, 3).toList
    assert(tramo == List(mover(p2, 1.0), mover(p3, 1.0)))
  }

  test("pasoSegmento: un tramo vacío no devuelve partículas") {
    assert(pasoSegmento(sistema, 1.0, 2, 2).length == 0)
  }

  test("pasoParalelo: da lo mismo que el secuencial con varios umbrales") {
    val esperado = pasoSecuencial(grande, 0.5).toList
    assert(List(1, 3, 10, 999, 1000, 5000).forall(u => pasoParalelo(grande, 0.5, u).toList == esperado))
  }

  test("pasoParalelo: un sistema vacío da un sistema vacío") {
    assert(pasoParalelo(Array[Particula](), 1.0, 4).length == 0)
  }

  // Punto 2

  test("energiaSecuencial: suma la energía de tres partículas") {
    assert(casi(energiaSecuencial(sistema), 16.75))
  }

  test("energiaSecuencial: un sistema quieto no tiene energía") {
    assert(energiaSecuencial(Array(Particula(Vector2(1.0, 1.0), quieto, 4.0))) == 0.0)
  }

  test("masaTotal: suma las masas") {
    assert(masaTotal(sistema) == 6.0)
  }

  test("masaTotal: un sistema vacío pesa cero") {
    assert(masaTotal(Array[Particula]()) == 0.0)
  }

  test("centroDeMasaSecuencial: promedio ponderado de tres partículas") {
    assert(casiV(centroDeMasaSecuencial(sistema), Vector2(1.0 / 6.0, 7.0 / 6.0)))
  }

  test("centroDeMasaSecuencial: sin masa es el origen") {
    assert(centroDeMasaSecuencial(Array(Particula(Vector2(5.0, 5.0), quieto, 0.0))) == Vector2(0.0, 0.0))
  }

  test("centroDeMasaSecuencial: una sola partícula es su propia posición") {
    assert(centroDeMasaSecuencial(Array(Particula(Vector2(3.0, -2.0), quieto, 5.0))) == Vector2(3.0, -2.0))
  }

  test("centroDeMasaSecuencial: la partícula más pesada atrae el centro") {
    val dos = Array(Particula(Vector2(0.0, 0.0), quieto, 1.0), Particula(Vector2(10.0, 0.0), quieto, 3.0))
    assert(centroDeMasaSecuencial(dos) == Vector2(7.5, 0.0))
  }

  // Punto 3

  test("energiaParalela: coincide con la secuencial con varios umbrales") {
    val esperado = energiaSecuencial(grande)
    assert(List(1, 2, 16, 999, 1000, 4000).forall(u => casi(energiaParalela(grande, u), esperado)))
  }

  test("energiaParalela: un sistema vacío tiene energía cero") {
    assert(energiaParalela(Array[Particula](), 4) == 0.0)
  }

  test("centroDeMasaParalelo: coincide con el secuencial con varios umbrales") {
    val esperado = centroDeMasaSecuencial(grande)
    assert(List(1, 2, 16, 999, 1000, 4000).forall(u => casiV(centroDeMasaParalelo(grande, u), esperado)))
  }

  test("centroDeMasaParalelo: no promedia los promedios de las mitades") {
    val tres = Array(
      Particula(Vector2(1.0, 0.0), quieto, 1.0),
      Particula(Vector2(2.0, 0.0), quieto, 1.0),
      Particula(Vector2(3.0, 0.0), quieto, 1.0))
    assert(casiV(centroDeMasaParalelo(tres, 2), Vector2(2.0, 0.0)))
  }

  test("centroDeMasaParalelo: respeta los pesos distintos de cada mitad") {
    val dos = Array(Particula(Vector2(0.0, 0.0), quieto, 1.0), Particula(Vector2(10.0, 0.0), quieto, 3.0))
    assert(casiV(centroDeMasaParalelo(dos, 1), Vector2(7.5, 0.0)))
  }

  test("centroDeMasaParalelo: un sistema vacío da el origen") {
    assert(centroDeMasaParalelo(Array[Particula](), 4) == Vector2(0.0, 0.0))
  }

  test("centroDeMasaParalelo: con masa total cero da el origen") {
    val sinMasa = Array(Particula(Vector2(5.0, 5.0), quieto, 0.0), Particula(Vector2(7.0, 1.0), quieto, 0.0))
    assert(centroDeMasaParalelo(sinMasa, 1) == Vector2(0.0, 0.0))
  }

  // Punto 4

  test("trabajo: una operación por partícula") {
    assert(trabajo(0) == 0)
    assert(trabajo(7) == 7)
  }

  test("profundidad: ocho partículas partidas hasta una dan 4") {
    assert(profundidad(8, 1) == 4)
  }

  test("profundidad: con el umbral igual al tamaño no se parte") {
    assert(profundidad(8, 8) == 8)
  }

  test("profundidad: ocho partículas con umbral dos dan 4") {
    assert(profundidad(8, 2) == 4)
  }

  test("profundidad: un tamaño impar se parte por el tramo mayor") {
    assert(profundidad(5, 1) == 4)
  }

  test("profundidad: un umbral de cero se trata como uno") {
    assert(profundidad(8, 0) == profundidad(8, 1))
  }

  test("tiempoEstimado: profundidad más trabajo entre procesadores") {
    assert(tiempoEstimado(8, 2, 1) == 8.0)
    assert(tiempoEstimado(100, 5, 100) == 120.0)
    assert(tiempoEstimado(1024, 4, 1024) == 1280.0)
  }

  test("tiempoEstimado: con más procesadores nunca estima más tiempo") {
    assert(tiempoEstimado(1000, 8, 10) <= tiempoEstimado(1000, 2, 10))
  }

  // Punto 5

  test("aceleracion: cinco veces más rápido") {
    assert(aceleracion(50.0, 10.0) == 5.0)
  }

  test("aceleracion: si el paralelo es más lento, es menor que uno") {
    assert(aceleracion(10.0, 40.0) == 0.25)
  }

  test("aceleracion: tiempos iguales dan uno, que es la fila de control") {
    assert(aceleracion(123.5, 123.5) == 1.0)
  }

  test("aceleracion: un tiempo secuencial de cero da cero") {
    assert(aceleracion(0.0, 5.0) == 0.0)
  }

  test("aceleracion: nueve entre tres da tres") {
    assert(aceleracion(9.0, 3.0) == 3.0)
  }

  test("umbralConveniente: con la lista vacía devuelve el tamaño del sistema") {
    assert(umbralConveniente(sistema, 1.0, List()) == 3)
  }

  test("umbralConveniente: con la lista vacía y el sistema grande devuelve 1000") {
    assert(umbralConveniente(grande, 1.0, List()) == 1000)
  }

  test("umbralConveniente: con la lista vacía y un sistema vacío devuelve 0") {
    assert(umbralConveniente(Array[Particula](), 1.0, List()) == 0)
  }
}