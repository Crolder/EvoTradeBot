package services

import cats.effect.IO
import doobie.Transactor
import doobie.scalatest.IOChecker
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import models.base.UserId
import models.user.PhoneNumber
import db.DbMappings


class DbServiceSpec extends AnyFunSuite with Matchers with IOChecker {
    val dbDriverName = "org.h2.Driver"
    val dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val dbUser = ""
    val dbPwd = ""

    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

    val transactor = Transactor.fromDriverManager[IO](
        dbDriverName, dbUrl, dbUser, dbPwd, Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )

    val setupUsers = sql"CREATE TABLE USERS (id LONG NOT NULL UNIQUE, firstname VARCHAR, lastname VARCHAR, username VARCHAR, phoneNumber VARCHAR)".update.run.transact(transactor).unsafeRunSync()

    val trivial =
        sql"""
    select 42, 'foo'::varchar
  """.query[(Int, String)]

    val insert =
        sql"""
          INSERT INTO USERS VALUES (123, "John", "Black", "Snow", "+37129999999");
           """.update

    val users =
        sql"""
    select *
    from USERS
  """.query[String]

    test("trivial") { check(trivial) }
    test("insert") { check(insert)  }
    test("users") { check(users)  }
}
