package db

import java.time.Year
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._


object QueryBuilder extends App{

    implicit class FragmentImprovements(fr: Fragment) {
        def from(table: String) = fr ++ fr"FROM" ++ Fragment.const(table)

        def into(table: String) = fr ++ fr"INTO" ++ Fragment.const(table)

        def where(condition: String) = fr++ fr"WHERE" ++ Fragment.const(condition)
        def whereAnd(conditions: String*) = fr ++ fr"WHERE" ++ Fragment.const(conditions.mkString(" AND "))
        def whereOr(conditions: String*) = fr ++ fr"WHERE" ++ Fragment.const(conditions.mkString(" OR "))

    }

    println(fr"SELECT id".from("USERS").whereAnd("id=2", "name>7","value=6"))

}
