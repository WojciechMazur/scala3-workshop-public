val lines = List("hello", "world", "content:value123", "and", "something other")
def getContent(): Option[String] = lines.find(_.startsWith("content"))
def getData(str: String): Option[String] =
  Option(str)
    .map(_.stripPrefix("content:"))
    .filter(_.nonEmpty)

val result = for
  content <- getContent()
  data <- getData(content)
  if data.length > 5
yield data

val resultEquivalent = getContent()
  .flatMap(getData(_))
  .withFilter(_.length > 5)
  .map(identity)

// for-comprehension można użyć dla każdego typu który defininuje metodę flatMap(f: In => Out): Out
// filtrowanie używając `if` w for-compreshension tylko jeśli typ definiuje również metodę 'withFilter'

// Replacing yield iwht do replaces final `map` operation with `foreach`
for
  content <- getContent()
  data <- getData(content)
  if data.length > 5
do println(data)

getContent()
  .flatMap(getData(_))
  .withFilter(_.length > 5)
  .foreach(println)
