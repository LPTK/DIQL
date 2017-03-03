# DIQL: Data Intensive Query Language

To compile DIQL:
```bash
mvn clean install
```

To test few DIQL queries:
```bash
export SPARK_HOME= ... path to Spark home ...
cd tests
mkdir -p classes
./build test.scala
./run
```

## Macros:

DIQL syntax          | meaning
---------------------|-------------------------------------------------------
`debug(true)`        | to turn debugging on
`monoid("+",0)`      | to define a new monoid for an infix operation
`q(""" ... """)`     | compile a DIQL query to Spark/Scala code
`qs(""" ... """)`    | compile many DIQL queries to code that returns `List[Any]`

## Query syntax:

A DIQL query is any functional Scala expression extended with the following query syntax:

### DIQL expressions:
```
e ::=  any functional Scala expression (no blocks, no val/var declarations)
    |  select [ distinct] q,...,q [ where e ]
              [ group by p [ : e ] [ having e ] ]
              [ order by [ asc | desc ]? e, ..., [ asc | desc ]? e ]
    |  some q,...,q: e             (existential quantification)
    |  all q,...,q: e              (universal quantification)
    |  +/e                         (aggregation using some monoid +)
    |  let p = e in e              (binding)
```
### DIQL patterns:
```
p ::= any Scala pattern
```
### DIQL qualifiers:
```
q ::=  p <- e                 (generator over an RDD or an Iterable sequence)
    |  p <-- e                (like p <- e but for a small dataset)
    |  p = e                  (binding)
```
## Example:
```scala
    q("""
      select (i/2.0,z._2,max/xs)
        from (i,3,xs) in S,
             x in xs,
             z in (select (i,+/j) from (i,j,_) in S group by i)
        where (some k in xs: k>3) && i==z._1
    """)
```

## k-means example:
```scala
    case class Point ( X: Double, Y: Double )

    def distance ( x: Point, y: Point ): Double
      = Math.sqrt(Math.pow(x.X-y.X,2)+Math.pow(x.Y-y.Y,2))

    val points = sc.textFile("points.txt")
                  .map( line => { val List(x,y) = line.split(",").toList
                                  Point(x.toDouble,y.toDouble) 
                                } )

    var centroids = List( Point(0,0), Point(10,0), Point(0,10), Point(10,10) )

    for ( i <- 1 to 10 )
       centroids = q("""
          select Point( avg/x, avg/y )
          from p@Point(x,y) <- points
          group by k: (select c from c <- centroids order by distance(c,p)).head
       """).collect.toList
```