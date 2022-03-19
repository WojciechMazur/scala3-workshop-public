// Podstawowe typy sekwencji są niezmienne (immutable)
// lista
val array = Array('a', 'b', 'c')
val list = List(1, 2, 3)
val list2 = 1 :: 2 :: 3 :: Nil

val vector = Vector(1.0, 2.0, 3.0)
val set = Set("a", "b", "a", "c")
val map = Map(
  "Adam" -> "Nowak",
  ("John", "Smith")
)
val range = 0 until 20

// Elemnty LazyListy są obliczane dopiero w momencie pierwszego użycia
val stream = LazyList.from(0)
// stream.take(5).toList
// Więcej dostępnych kolekcji można znaleźć w pakiecie scala.collection

// Jeśli konkretny typ sekwencji nie jest dla nas ważny możemy użyć po domyślnego typu sekwencij
val seq = Seq("hello", "world")
val iseq = IndexedSeq("hello", "world")

// Dostępny jest również mutowalny wariant sekwencji
import scala.collection.mutable
val mList = mutable.ListBuffer("foo")
mList += "bar"
// mList -= "foo"

// Podstawowe operacje na kolekcjach

// Filter
def isEven(v: Int) = v % 2 == 0
0.until(10).filter(isEven)
0.until(10).filterNot(isEven(_))

// Partition - tworzy dwie nowe kolekcję złożoną z elementow spelniających kryterium (l), oraz nie spełniających (r)
val (even, uneven) = 0.until(100).partition(isEven)

// Map - zaaplikuj daną funckę dla każdego elementu w kolekcji
0.until(10)
  .map(_ * 12 / 3)
  .map(v => v * 2)

// flatMap - podobnie jak map, ale przekazana zwraca kolekcję
0.until(10).flatMap(v => List(-v, v))

// collect - pozwala zaplikować partial function na elementach kolekcji
0.until(20).collect {
  case i if i % 3 == 0 & i % 5 == 0 => "foobar"
  case i if i % 3 == 0              => "foo"
  case i if i % 5 == 0              => "bar"
  case i                            => i
}

// find
0.until(10).find(_ == 5)

// paginacja
val nums = 0
  .to(100)
  .toList
  .tail // Usuń pierwszy element
  .drop(10) // Usuń pierwsze 10 elementów
  .init // Usń ostatni element
  .take(5) // Weż 5 pierwszych elementów
  .reverse // Odwróc kolejność

nums.head
nums.headOption
nums.last
nums.lastOption

nums.indexWhere(_ == 13)
nums.lastIndexWhere(_ == 42)

// Redukcja kolekcji
// Fold przyjmuje początkową wartość oraz funkcję mapującą obecną wartość oraz element kolekcji
val silnia = 1L.to(5L).fold(1L) { (acc, elem) => acc * elem }

// Reduce nie przyjmuje wartości początkowej, zamiast tego używa wartość pierwszego elementu
val silnia2 = 1.to(5).reduce(_ * _)

val groupedByLastDigit = 0.until(100).groupBy(_.toString.last)
groupedByLastDigit.keySet.toList.sorted

groupedByLastDigit.map { (key, seq) =>
  Integer.valueOf(key.toString) -> seq.sum
}



// Option
// Option is a popular way of abstracting optional values
Option(null: String).map(_.length)
Option("msg").map(_.length)

