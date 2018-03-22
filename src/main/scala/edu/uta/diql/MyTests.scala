package test

import edu.uta.diql._

object MyTests extends App {
  
  val X = List(1->2,2->3,4->6,8->10)
  val Y = List(1->3,4->0)
  
  //explain(true) // does not seem to do anything
  
  //val res = debug(
  val res = q(
    "select b from (a,b) <- X where b > +/(select d from (c,d) <- Y where a==c)")
  
  println(res)
  
}
