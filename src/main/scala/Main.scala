package main.scala

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

    val setupTables: IO[Unit] = for {
        _ <- Presentation.createTableUsers()
        _ <- Presentation.createTableCars()
        _ <- Presentation.createTableApartments()
        _ <- Presentation.createTableComputers()
    } yield ()

    val setupUsers: IO[Unit] = for {
        _ <- Presentation.insertUserF(Presentation.user1)
        _ <- Presentation.insertUserF(Presentation.user2)
        _ <- Presentation.insertUserF(Presentation.user3)
    } yield ()

    val setupCars: IO[Unit] = for {
        _ <- Presentation.insertCarF(Presentation.car1)
        _ <- Presentation.insertCarF(Presentation.car2)
        _ <- Presentation.insertCarF(Presentation.car3)
    } yield ()

    val setupApartments: IO[Unit] = for {
        _ <- Presentation.insertApartmentF(Presentation.apartment1)
        _ <- Presentation.insertApartmentF(Presentation.apartment2)
        _ <- Presentation.insertApartmentF(Presentation.apartment3)
    } yield ()

    val setupComputers: IO[Unit] = for {
        _ <- Presentation.insertComputerF(Presentation.computer1)
        _ <- Presentation.insertComputerF(Presentation.computer2)
        _ <- Presentation.insertComputerF(Presentation.computer3)
    } yield ()

    val setup: IO[Unit] = for {
        _ <- setupTables
        _ <- setupUsers
        _ <- setupApartments
        _ <- setupComputers
        _ <- setupCars
    } yield ()

    override def run(args: List[String]): IO[ExitCode] = {
        setup.unsafeRunSync()
        TelegramBot.run(args)
    }
}
