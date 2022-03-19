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

// val entries = List.empty
// assert(entries.size == 10)

// val averageAge = ???
// assert(averageAge == 30)

// val salariesBySection = Map("A" -> -1, "B" -> -1, "C" -> -1)
// assert(salariesBySection("A") == 1033)
// assert(salariesBySection("B") == 1665)
// assert(salariesBySection("C") == 2266)

// val withMaxSalary = ???
// assert(withMaxSalary.id == "6" && withMaxSalary.salary == 3000)
