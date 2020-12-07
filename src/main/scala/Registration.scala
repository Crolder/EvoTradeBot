package main.scala

import java.time.{LocalDate, Year}
import java.util.UUID

import main.scala.DbCommon._
import canoe.api._
import canoe.models.{Chat, PrivateChat}
import canoe.methods.chats.GetChat
import canoe.syntax._
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import cats.effect._
import cats.implicits._
import fs2.Stream
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._

/**
 * Example using compositional property of scenarios
 * by combining them into more complex registration process
 */
object Registration extends IOApp {

    //    final case class Author(id: UUID, name: String, birthday: LocalDate)

    //    // setup
    //    val ddl1 = Fragment.const(createTableAuthorsSql)
    //    val ddl2 = Fragment.const(createTableBooksSql)
    //    val dml = Fragment.const(populateDataSql)
    //
    //    def setup(): ConnectionIO[Unit] =
    //        for {
    //            _ <- ddl1.update.run
    //            _ <- ddl2.update.run
    //            _ <- dml.update.run
    //        } yield ()

    private val transactor = DbTransactor.make[IO]

    private val setup = sql"CREATE TABLE USERS (id LONG NOT NULL, firstname VARCHAR, lastname VARCHAR, username VARCHAR)"
    private val count = sql"SELECT id, firstname, lastname, username FROM USERS"

    def insertUser(user: User) = sql"INSERT INTO USERS VALUES (${user.id},${user.firstname},${user.lastname},${user.username})"


    //    val authors: Fragment =
    //        fr"SELECT id, name, birthday FROM authors"
    //
    //    val books: Fragment =
    //        fr"SELECT id, author, title, year FROM books"
    //
    //    def fetchAuthors(): doobie.Query0[String] =
    //        (authors).query[String]

    final case class User(id: Long,
                          firstname: String,
                          lastname: String,
                          username: String
                         )

    val token: String = "1258604971:AAGUQ61qVLVJy54FBt5e1sgZ-0dTTKbiht4"

    trait Service[F[_]] {
        def userExists(id: Long): F[Boolean]

        def register(id: Long, firstname: String, lastname: String, username: String): F[User]

        def addToDb (user: User): F[Unit]

        def printTable(): F[List[User]]

        def insertUserF(user: User): F[Unit]

        def createTable(): F[Unit]

    }

    val service: Service[IO] = new Service[IO] {
        def userExists(userId: Long): IO[Boolean] = IO { db.exists(_.id == userId) }
        def register(id: Long, firstname: String, lastname: String, username: String): IO[User] = IO { User(id, firstname, lastname, username) }
        def addToDb (user: User): IO[Unit] = IO {
            db = db :+ user
        }

        def createTable(): IO[Unit] = for {
            _ <- transactor.use(xa => setup.update.run.transact(xa))
        } yield ()

        def insertUserF(user: User): IO[Unit] = for {
            _ <- transactor.use(xa => insertUser(user).update.run.transact(xa))
        } yield ()

        def printTable(): IO[List[User]] =
            for {
                res <- transactor.use { xa =>
                    count.query[User]
                        .to[List]
                        .transact(xa)
                }
            } yield res
    }

    var db: List[User] = Nil

    def run(args: List[String]): IO[ExitCode] =
        Stream
            .resource(TelegramClient.global[IO](token))
            .flatMap { implicit client =>
                Bot.polling[IO].follow(start(service), print(), add(service))
            }
            .compile.drain.as(ExitCode.Success)

    def print[F[_]: TelegramClient](): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("print").chat)
            _ <- Scenario.eval(chat.send(s"USER: ${service.printTable().unsafeRunSync()}"))
        } yield ()

    def start[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(service.createTable())
            userExists <- Scenario.eval(service.userExists(detailedChat.id))
            _   <- Scenario.eval(chat.send(s"Exist?: $userExists"))
            _   <- Scenario.eval(chat.send(s"Id: ${detailedChat.id}, Name: ${detailedChat.firstName}"))
            user   <- Scenario.eval(service.register(detailedChat.id, detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
            _ <- Scenario.eval(service.insertUserF(user))
            _    <- Scenario.eval(service.addToDb(user))
            _    <- Scenario.eval(chat.send(s"User: $db"))
        } yield ()

    def add[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("add").chat)
            detailedChat <- Scenario.eval(chat.details)
            userExists <- Scenario.eval(service.userExists(detailedChat.id))
            user   <- Scenario.eval(service.register(detailedChat.id, detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
            _ <- if(!userExists) Scenario.eval(service.insertUserF(user)) else Scenario.eval(chat.send("User exist"))
        } yield ()
}