package main.scala

import cats.effect.{ExitCode, IO, IOApp}
import db.DbTransactor
import db.DbScenarios._
import doobie.implicits._
import adt.User

object Main extends IOApp {
    private val transactor = DbTransactor.make[IO]

    def createTableUsers(): IO[Unit] = for {
        _ <- transactor.use(xa => setupUsers.update.run.transact(xa))
    } yield ()

    def createTableProducts(): IO[Unit] = for {
        _ <- transactor.use(xa => setupProducts.update.run.transact(xa))
    } yield ()

    override def run(args: List[String]): IO[ExitCode] = {
        createTableUsers().unsafeRunSync()
        createTableProducts().unsafeRunSync()
        TelegramBot.run(args)
    }
}
