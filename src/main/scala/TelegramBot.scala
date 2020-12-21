package main.scala

import models.base._
import main.scala.BotConfig.token
import canoe.api._
import canoe.models.messages.{PhotoMessage, TelegramMessage}
import canoe.syntax.{command, _}
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import services.{DbService, ObjectFactory}
import services.ObjectFactory.objectFactory
import services.DbService.dbService
import utils.DisplayContent._
import main.scala.BotActions._

/**
  * Example using compositional property of scenarios
  * by combining them into more complex registration process
  */
object TelegramBot extends IOApp {
    def run(args: List[String]): IO[ExitCode] = {
        Stream
          .resource(TelegramClient.global[IO](token))
          .flatMap { implicit client =>
              Bot.polling[IO].follow(start)
          }
          .compile.drain.as(ExitCode.Success)
    }

    def start[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            user <- Scenario.eval(service.getUserById(UserId(detailedChat.id)))
            userExists <- Scenario.pure[F](user.isDefined)
            _ <- if(!userExists) {
                for {
                    user <- Scenario.eval(objectFactory.createUser(UserId(detailedChat.id), detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
                    _ <- Scenario.eval(service.insertUserToDb(user))
                    _ <- Scenario.eval(chat.send(s"You was successfully registered!"))
                } yield ()
            } else Scenario.eval(chat.send(s"Welcome ${detailedChat.firstName.getOrElse("dear friend!")}"))
            _ <- displayAction(chat).stopOn(command("start").isDefinedAt)
        } yield ()
}