package main.scala

import java.time.{LocalDate, Year}
import java.util.UUID
import canoe.api._
import canoe.models.{Chat, PrivateChat}
import canoe.methods.chats.GetChat
import canoe.syntax._
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import cats.effect._
import cats.implicits._
import db.DbTransactor
import db.DbScenarios._
import adt.{User, Product}
import fs2.Stream
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._
import main.scala.BotConfig.token

/**
 * Example using compositional property of scenarios
 * by combining them into more complex registration process
 */
object TelegramBot extends IOApp {

    private val transactor = DbTransactor.make[IO]

    trait Service[F[_]] {
        def userExists(id: Long): F[Boolean]

        def register(id: Long, firstname: String, lastname: String, username: String): F[User]

        def createProduct(userId: Long, name: String, price: Int): F[Product]

        def getUsers(): F[List[User]]

        def getProducts(): F[List[Product]]

        def getProductsByUserId(userId: Long) : F[List[Product]]

        def insertUserIfNotExist(user: User): F[Unit] // ToDO Return Success or Error

        def insertProductF(product: Product): F[Unit] // ToDO Return Success or Error

        def createTable(): F[Unit] // ToDO Return Success or Error

    }

    val service: Service[IO] = new Service[IO] {
        def register(id: Long, firstname: String, lastname: String, username: String): IO[User] = IO { User(id, firstname, lastname, username) }

        def createProduct(userId: Long, name: String, price: Int): IO[Product] = IO { Product(UUID.randomUUID().toString, userId, name, price) }

        def createTable(): IO[Unit] = for {
            _ <- transactor.use(xa => setupUsers.update.run.transact(xa))
        } yield ()

        def insertUserIfNotExist(user: User): IO[Unit] = for {
            userExist <- userExists(user.id)
            _ <- if(!userExist) transactor.use(xa => insertUser(user).update.run.transact(xa)) else IO.unit
        } yield ()

        def insertProductF(product: Product): IO[Unit] = for {
            _ <- transactor.use(xa => insertProduct(product).update.run.transact(xa))
        } yield ()

        def userExists(id: Long): IO[Boolean] = for {
            res <- transactor.use { xa =>
                selectUserById(id).query[User]
                  .to[List]
                  .transact(xa)
            }
        } yield if(res.nonEmpty) true else false

        def getUsers(): IO[List[User]] =
            for {
                res <- transactor.use { xa =>
                    usersSelect.query[User]
                        .to[List]
                        .transact(xa)
                }
            } yield res

        def getProducts(): IO[List[Product]] =
            for {
                res <- transactor.use { xa =>
                    productsSelect.query[Product]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getProductsByUserId(userId: Long) : IO[List[Product]] =
            for {
                res <- transactor.use { xa =>
                    selectProductsById(userId).query[Product]
                      .to[List]
                      .transact(xa)
                }
            } yield res
    }

    def run(args: List[String]): IO[ExitCode] = {
        Stream
            .resource(TelegramClient.global[IO](token))
            .flatMap { implicit client =>
                Bot.polling[IO].follow(start(service), users(service), add(service), products(service), myProducts(service))
            }
            .compile.drain.as(ExitCode.Success)
    }

    def createDb[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            _ <- Scenario.eval(service.createTable())
        } yield()

    def users[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("users").chat)
            users <- Scenario.eval(service.getUsers())
            _ <- Scenario.eval(chat.send(s"Users: $users"))
        } yield ()

    def products[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("products").chat)
            products <- Scenario.eval(service.getProducts())
            _ <- Scenario.eval(chat.send(s"Products: $products"))
        } yield ()

    def myProducts[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("my").chat)
            detailedChat <- Scenario.eval(chat.details)
            products <- Scenario.eval(service.getProductsByUserId(detailedChat.id))
            _ <- Scenario.eval(chat.send(s"My Products: $products"))
        } yield ()

    def start[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            userExists <- Scenario.eval(service.userExists(detailedChat.id))
            _   <- Scenario.eval(chat.send(s"Exist?: $userExists"))
            _   <- Scenario.eval(chat.send(s"Id: ${detailedChat.id}, Name: ${detailedChat.firstName}"))
            user   <- Scenario.eval(service.register(detailedChat.id, detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
            _ <- Scenario.eval(service.insertUserIfNotExist(user))
        } yield ()

    def add[F[_]: TelegramClient](service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("add").chat)
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(chat.send("Enter product name"))
            name <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter product price"))
            price <- Scenario.expect(text)
            product <- Scenario.eval(service.createProduct(detailedChat.id, name , price.toInt))
            _ <- Scenario.eval(service.insertProductF(product))
        } yield ()
}