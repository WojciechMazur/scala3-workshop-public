//> using:
//>   lib "com.lihaoyi::utest::0.7.10"
//>   target { scope "test" }
package ex3

import utest._

case class Entry(
    id: Int,
    name: String,
    sectionId: String,
    salary: Int,
    age: Byte
)
case class EntrySalaryView(id: Int, salary: Int)

// Parse the string containg CSV data to List[Entry]
// Input contains only valid input and always starts with header:
// id, name, sectionId, salary, age
// You can use String.split(char) method, eg. "foo%bar%baz".split('%') returns Array("foo", "bar", "baz")
def parseInput(input: String): List[Entry] =
  for line <- input
      .split('\n') // get lines
      .filter(_.nonEmpty)
      .drop(1) // drop header
      .toList
  yield {
    val Array(idStr, name, sectionId, salaryStr, ageStr) =
      line.split(',').map(_.trim)
    Entry(idStr.toInt, name, sectionId, salaryStr.toInt, ageStr.toByte)
  }

// Implement sequence operation of the entires
extension (entries: Seq[Entry]) {
  // Calculate the average age of the entires
  def avarageAge: Int =
    entries.foldLeft(0)(_ + _.age) / entries.size

  // Find the entry with the highest salary, or None if entries is empty
  def withMaxSalary: Option[Entry] = entries.maxByOption(_.salary)

  // Group sections by id, for each group calucate average salary
  def avarageSalaryBySection: Map[String, Int] =
    entries
      .groupBy(_.sectionId)
      .mapValues { entries => entries.foldLeft(0)(_ + _.salary) / entries.size }
      .toMap

  // Filter entries that match given predicate. Convert matching entires to EntrySalaryView
  def salariesView(predicate: Entry => Boolean): Seq[EntrySalaryView] =
    entries.collect {
      case entry if predicate(entry) => EntrySalaryView(entry.id, entry.salary)
    }
}

object ParseCsvSeqOpsSuite extends TestSuite {
  val input =
    """id, name, sectionId, salary, age
      |1,Joe,A,1000,20
      |2,Steve,B,1500,25
      |3,Micky,C,1800,30
      |4,Big Mick,A,1200,25
      |5,Foo,C,2000,40
      |6,FooBar,C,3000,40
      |7,Joe Doe,B,1500,30
      |8,Adam Kowalski,A,900,35
      |9,Adam Nowak,B,2000,25
      |10,The choosen one,B,1660,30
      |""".stripMargin

  val entries = parseInput(input)
  val expectedEntries = List(
    Entry(1, "Joe", "A", 1000, 20.toByte),
    Entry(2, "Steve", "B", 1500, 25.toByte),
    Entry(3, "Micky", "C", 1800, 30.toByte),
    Entry(4, "Big Mick", "A", 1200, 25.toByte),
    Entry(5, "Foo", "C", 2000, 40.toByte),
    Entry(6, "FooBar", "C", 3000, 40.toByte),
    Entry(7, "Joe Doe", "B", 1500, 30.toByte),
    Entry(8, "Adam Kowalski", "A", 900, 35.toByte),
    Entry(9, "Adam Nowak", "B", 2000, 25.toByte),
    Entry(10, "The choosen one", "B", 1660, 30.toByte)
  )
  override val tests = Tests {
    test("parses entries correctly") {
      test("correct entries size") {
        assert(entries.size == 10)
      }
      test("correct values") {
        assert(entries == expectedEntries)
      }
    }
    test("entries sequence ops") {
      test("average age") {
        assert(entries.avarageAge == 30)
      }
      test("with max salary") {
        val result = entries.withMaxSalary
        val expected = expectedEntries.find(_.id == 6)
        assert(result.nonEmpty)
        assert(expected == result)
      }
      test("with max salary on empty sequence") {
        val entires = List.empty[Entry]
        assert(entires.withMaxSalary == None)
      }
      test("salaries by section id") {
        assert(
          entries.avarageSalaryBySection == Map(
            "A" -> 1033,
            "B" -> 1665,
            "C" -> 2266
          )
        )
      }
      test("salaries view for age < 30") {
        val expected = Seq(
          EntrySalaryView(1, 1000),
          EntrySalaryView(2, 1500),
          EntrySalaryView(4, 1200),
          EntrySalaryView(9, 2000)
        )
        assert(entries.salariesView(_.age < 30) == expected)
      }

    }

  }
}
