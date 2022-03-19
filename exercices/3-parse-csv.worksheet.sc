// Przetwórz poniższy string zawierające w formacie CSV
// Pozbądź się wierszy zawierających niedozwolone dane - jesli którekolwiek pole zawiery pusty string, oraz nagłówka
// Oblicz średnią wartość 'age' dla wszystkich danych
// Pogrupuj dane ze względu na sectionId i oblicz średnią wartość salary
// Znajdź element o największej wartości salary

// Do podzielnia ciągu znaków możesz użyć metody String.split(), zwraca ona Array[String]
// Przykladowo "foo%bar%baz".split("%") zwróci Array("foo", "bar", "baz")
val input =
  """id, name, sectionId, salary, age
    |1,Joe,A,1000,20
    |2,Steve,B,1500,25
    |13,,C,0,10
    |3,Little Mick,C,1800,30
    |4,Big Mick,A,1200,25
    |5,Foo,C,2000,40
    |,,,1500,15
    |6,FooBar,C,3000,40
    |7,Joe Doe,B,1500,30
    |11,B,1500,15
    |8,Adam Kowolaski,A,900,35
    |9,Adam Nowak,B,2000,25
    |10,The choosen one,B,1660,30
    |""".stripMargin

class Entry(
    val id: String,
    val name: String,
    val sectionId: String,
    val salary: Int,
    val age: Int
)

def isNumber(s: String) = s.forall(_.isDigit)

val entries = input
  .split("\n")
  .map(_.split(",").map(_.trim))
  .toList
  .filter(line => !line.exists(field => field.isEmpty))
  .collect {
    case Array(id, name, sectionId, salary, age)
        if isNumber(salary) && isNumber(age) =>
      Entry(id, name, sectionId, salary.toInt, age.toInt)
  }
  .toList
assert(entries.size == 10)

val averageAge = entries.map(_.age).sum / entries.size
assert(averageAge == 30)

val salariesBySection = entries
  .groupBy(_.sectionId)
  .view
  .mapValues {group => group.map(_.salary).sum / group.size  }
  .toMap
assert(salariesBySection("A") == 1033)
assert(salariesBySection("B") == 1665)
assert(salariesBySection("C") == 2266)

val withMaxSalary = entries.maxBy(_.salary)
assert(withMaxSalary.id == "6" && withMaxSalary.salary == 3000)
