/** W Scali wszystko ma jakiś typ https://docs.scala-lang.org/tour/unified-types.html Any / \ AnyVal
  * AnyRef Int Long Unit Float Double | String Option <custom types> \ | / Nothing
  */

var anyVal: AnyVal = _
anyVal = 1
anyVal = 1L
anyVal = 1.0f
anyVal

var anyRef: AnyRef = _
anyRef = "Hello"
anyRef = Some(1)
anyRef = List(1, 2, 3)

var any: Any = _
any = anyVal
any = anyRef

// Unit jest typem który reprezentuje brak wartości (void z innych języków)
anyVal = ()

// Nothing jest typem opisującym wartości których nie da się stworzyć w żaden sposób
// W praktyce opisuje on operacje które rzucają wyjątek
def fail: Nothing = throw new RuntimeException("Failed")
def notImplemented: Nothing = ???

// any = fail

/// Custom types

// Główny konstructor przymuje 2 argumenty (Int, String) oraz tworzy prywatne pola foo, bar wewnątrz klasy
class MyClass(val foo: Int, bar: String = "") {
  // Drugorzędny kontruktor dla classy
  def this(string: String) = this(string.length, string)

  private val fooBar = s"$foo$bar"

  val length = bar.length

  // Wszystkie wyrażenia które nie są definicjami metod lub pól wewnątrz klasy zostają wykonane w trakcie wykonywanie głownego konstruktowa
  println("MyClass is beining initialized")
}
// val cls: MyClass = MyClass(0)
// cls

// object służy do utworzenia klasy która posiada tylko jedna instancję (singleton)
// jeśli object ma taką samą nazwę jak klasa zdefiniowana w tym samym pliku to nazywamy go 'companion object'
object MyClass {
  // Wszystkie metody i pola wewnątrz obiektu są statyczne
  final val DefaultName = "MyClassConstantString"
  def createInstance(n: Int) = new MyClass(n, DefaultName)
}

// Type może dziedzić po wyłacznie 1 innej klasie
abstract class Foo(x: Int) {
  def foo: Int
  def fooString = foo.toString + x
}

// Type może implementować wiele róznych traitów
trait Bar(x: Int) {
  def bar: Int
  def barString = bar.toString + x
}

class FooBar extends Foo(0) with Bar(42) {
  override def foo: Int = ???
  override def bar: Int = ???
}
// class FooBar extends Bar(0) with Foo(42) // kolejnośc ma znaczenie
new FooBar()

sealed trait Contact

// Case classy podobnie generują klasy podobnie jak zwykłe klasy, ale:
// - pola konstruktora stają się publicznymi, a nie prywatnymi polami
// - posiadają zdefiniowane metody equals, hashCode, toString bazujące na polach konstruktora
// - definiują metodę unapply w konstrukotrze pozwalając na użycie w pattern matching
// - W Scali 2 definiują metodę apply dzięki czemu nie trzeba używa słowa kluczowego new
// - case classa nie może dziedziczyć po innej case klasie!
case class Person(name: String, age: Int) extends Contact
case object Organization extends Contact

val contact: Contact = Person("Steve", 40)
contact match {
  case Person("Steve", _) => "Hey Steve"
  case Organization       => "..."
  case _                  => "Hey stranger"
}

// Klasy oraz traity oznaczone jako sealed mogę być dziedziczone tylko wewnątrz pojedyńczego pliku
sealed trait Animal
// Domyślnie wszystkie typy są publiczne
private class Dog(breed: String) extends Animal
protected object Elephant extends Animal

// Aby zdefiniować typ o dostępie package-private podaj nazwę pakietu
// private[somepackage] abstract class Cat extends Animal

// Enums
enum Color(val hex: Int):
  case Red extends Color(0xff0000)
  case Green extends Color(0x0000ff)
  case Blue extends Color(0x00ff00)

val color = Color.Green
color.hex

enum Shape:
  case Circe, Square, Triangle

// Aliasy typów
type Meter = Double
var distance: Meter = 10

// Union/intersecion types types
trait A {
  def doA = 2
}
trait B {
  def doB = 4
}
type AOrB = A | B
type AWithB = A & B

def doSomething(v: AOrB) = v match {
  case v: A => v.doA
  case v: B => v.doB
}

def doSomething(v: AWithB) = v.doA + v.doB

doSomething(new A {})
doSomething(new B {})
doSomething(new A with B)

// Generics

def log[T <: Any](v: T) = println(s"It's $v")
log("a message")
log(42.0)

trait Pet extends Animal {
  def name: String
}
case class Crocodile(name: String) extends Animal 
case class Cat(name: String) extends Pet

def pet[T <: Pet](toPet: T) = {
  println(s"Petting $toPet with name ${toPet.name}")
}

pet(Cat("Alex"))
// pet(Crocodile("T-rex"))
pet(new Dog("Golden Retriver") with Pet { def name: String = "Scruffy" })
