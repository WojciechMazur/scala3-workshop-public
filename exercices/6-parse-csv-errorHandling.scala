//> using:
//>   lib "com.lihaoyi::utest::0.7.10"
//>   target scope "test"
package ex6

import utest._

case class Entry(
    id: Int,
    name: String,
    sectionId: String,
    salary: Int,
    age: Byte
)

sealed trait DecodingFailure
case class NumberConversionFailure(input: String, field: String)
    extends DecodingFailure
case class IncorrectNumberOfFields(fields: List[String]) extends DecodingFailure
case class EmptyString(field: String) extends DecodingFailure

// Parse the string containg CSV data to List[Entry]
// Input might contain malformed data and might or might not contain header:
// id, name, sectionId, salary, age
//
// id, salary and age need to be valid integers
// name and sectionId cannot be empty
// Parser should ignore empty lines and header, for each other row it should return Either[DecodingFailure, Entry]
type DecoderResult[T] = Either[DecodingFailure, T]

sealed trait FieldDecoder[T] {
  def apply(field: String)(input: String): DecoderResult[T]
}
implicit object IntDecoder extends FieldDecoder[Int] {
  override def apply(field: String)(input: String): DecoderResult[Int] = ???
}

implicit object NonEmptyStringDecoder extends FieldDecoder[String] {
  override def apply(field: String)(input: String): DecoderResult[String] = ???
}

def parseInput(input: String): List[DecoderResult[Entry]] = Nil

object ParseCsvErrorHandlingSuite extends TestSuite {
  val header = "id, name, sectionId, salary, age"
  override val tests = Tests {
    test("decoder") {
      test("empty strings") {
        val input =
          s"""$header
             |1,  ,foo,10,10
             |2,name, ,11,12
             |3,name,sectionId, 12, 13
          """.stripMargin
        assert(
          parseInput(input) == Seq(
            Left(EmptyString("name")),
            Left(EmptyString("sectionId")),
            Right(Entry(3, "name", "sectionId", 12, 13.toByte))
          )
        )
      }

      test("number decoding") {
        val input =
          s"""$header
              |id,a,b,foo,10
              |2,a,b,small,12
              |3,a,b, 12, young
              |4,a,b, 13,30
           """.stripMargin
        assert(
          parseInput(input) == Seq(
            Left(NumberConversionFailure("id", field = "id")),
            Left(NumberConversionFailure("small", field = "salary")),
            Left(NumberConversionFailure("young", field = "age")),
            Right(Entry(4, "a", "b", 13, 30.toByte))
          )
        )
      }

      test("incorrect number of columns") {
        val input =
          s"""$header
                |1,name,section,1250
                |2,name,section,1250,20
                |3,name,section,1250,20,foobar
             """.stripMargin
        assert(
          parseInput(input) == Seq(
            Left(IncorrectNumberOfFields(List("1", "name", "section", "1250"))),
            Right(Entry(2, "name", "section", 1250, 20.toByte)),
            Left(
              IncorrectNumberOfFields(
                List("3", "name", "section", "1250", "20", "foobar")
              )
            )
          )
        )
      }

      test("no header") {
        val input = s"""
          |1,name,section,1250,20
          |""".stripMargin
        assert(
          parseInput(input) == Seq(
            Right(Entry(1, "name", "section", 1250, 20.toByte))
          )
        )
      }
    }
  }
}
