// Scala by default enforces usage of immutable values declared with val keyword
// val <variableName> :<Type>? = <value>
// The result type is optional, if its not explicitlly set the result type of variable would infered from it's right hand side
val openingMessage = "Hello world"
val otherMesage: String = "Hello Scala"
// openingMessage = "" // Kompilator nie pozwoli nam przypisać ponownie do zmienej

// In case if you need to use mutable variable declare it using var keyword
// Good practive is to treat mutable variables as advanced feature and used them only critical sections of the code
var luckyNumber = 42
luckyNumber += 1
luckyNumber
// luckyNumber = Math.sqrt(2.0)
// luckyNumber

// Funckję tworzymy za pomocą słowa kluczowego def
// Ciało funkcji zostanie wykonane za każdym razem
def getSomeNumber =
  println("getting some number")
  lazyInitializedNumber

// lazy val służy stworzenia zmiennej która zostanie zainicjowana dopiero przy pierwszym użyciu
// Jest to szczególnie przydatne przy cachowaniu obliczeń lub rozwiązywaniu cyklicznych zależności
lazy val lazyInitializedNumber =
  println("creating lazy number")
  luckyNumber * 2 + 3

getSomeNumber
getSomeNumber

// Dostępne typy primitywne:
val b: Byte = 10
val s: Short = 10
val c: Char = 10
val i: Int = 10
val l: Long = 10L
val f: Float = 10.0f
val d: Double = 10.0

// Ciągi znaków
val str = "Hello"

// Interpelacja stringow
// Możliwe jest użycie zmiennych bądź wyrażeń do stworzenia nowego ciągu znaków
val str2 = s"$str Steve, it's ${(c + 12) % 24} o'clock"
// A także ich dokładniejsze formatiwanie, np. określając liczbe miejsc po przecinku
val str3 = f"Height: $d%2.3f, unformatted $d"

// Instrukcja warunkowa w Scali ma dwie dostępne formy
if luckyNumber < 10 then println("<10") // Nowa składnia: if <cond> then <then>
else if (luckyNumber > 50)
  println(">50") // Składnia Scali 2: if (<cond>) (then)
else {
  println("Between 10 and 50")
}

// Match w może być używany jak switch w innych językach
util.Random.nextInt(10) match {
  case 0 => "Boom"
  case 1 => "Almost boom"
  case _ => "Not boom"
}

// Ale może służyć również do matchowanie na stringach oraz może używać pattern matchingu

"foo" match {
  case "bar" => 0
  case "foo" => 1
  case _     => -1
}

Option.when(true)("Hello") match {
  case Some(msg) => msg
  case None      => "no msg"
}

// Pętle

// Do iterowanie po sekwencji możemy użyć pętli for-do
for n <- 0.until(5)
do
  luckyNumber = util.Random.nextInt(100)
  println(luckyNumber)

// Aby zapamiętać wynik iteracji można wykorzystać słowo kluczowe yield zamiast do
val coords = for
  i <- 0 until 5 if i % 2 == 0
  j <- 0 until 5 if j % 2 != 0
yield (i, j)

// Możemy również użyć pętli while-do
val coordsIterator = coords.iterator
while (coordsIterator.hasNext) {
  println(coordsIterator.next)
}

// // W Scali 3 nie jest dostępna pętla do-while, ale możemy zastąpić ją odpowiednio skonstrukowaną pętla while-do
var iter2 = coords.iterator
while {
  val next = iter2.next
  println(next)
  iter2.hasNext && next._1 < 3
} do ()

// Input / output

println("Hello world")

// import scala.io.StdIn
// val line = StdIn.readLine()
// val boolean = StdIn.readBoolean()
