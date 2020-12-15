package main.scala

import adt.Product.{Apartment, Car, Computer}
import cats.effect.{ExitCode, IO, IOApp}
import db.DbTransactor
import db.DbScenarios._
import doobie.implicits._
import adt.{Product, User}

object Main extends IOApp {
    private val transactor = DbTransactor.make[IO]

    def createTableUsers(): IO[Unit] = for {
        _ <- transactor.use(xa => setupUsers.update.run.transact(xa))
    } yield ()

    def createTableProducts(): IO[Unit] = for {
        _ <- transactor.use(xa => setupProducts.update.run.transact(xa))
    } yield ()

    def createTableCars(): IO[Unit] = for {
        _ <- transactor.use(xa => setupCars.update.run.transact(xa))
    } yield ()

    def createTableApartments(): IO[Unit] = for {
        _ <- transactor.use(xa => setupApartments.update.run.transact(xa))
    } yield ()

    def createTableComputers(): IO[Unit] = for {
        _ <- transactor.use(xa => setupComputers.update.run.transact(xa))
    } yield ()

    def createTableImages(): IO[Unit] = for {
        _ <- transactor.use(xa => setupImages.update.run.transact(xa))
    } yield ()

    def insertProductF(product: Product): IO[Unit] = for {
        _ <- transactor.use(xa => insertProduct(product).update.run.transact(xa))
    } yield ()

    def insertCarF(car: Car): IO[Unit] = for {
        _ <- transactor.use(xa => insertCar(car).update.run.transact(xa))
    } yield ()

    def insertApartmentF(apartment: Apartment): IO[Unit] = for {
        _ <- transactor.use(xa => insertApartment(apartment).update.run.transact(xa))
    } yield ()

    def insertComputerF(computer: Computer): IO[Unit] = for {
        _ <- transactor.use(xa => insertComputer(computer).update.run.transact(xa))
    } yield ()

    val imageKey = "AgACAgQAAxkBAAIGmV_XrqVeAAFW7af6-x90xzQwpb5c2AACybUxG7rfwFLx-iHEWkU-H8FrriddAAMBAAMCAANtAAMYQAMAAR4E"

    override def run(args: List[String]): IO[ExitCode] = {
        createTableUsers().unsafeRunSync()
        createTableProducts().unsafeRunSync()
        createTableCars().unsafeRunSync()
        createTableApartments().unsafeRunSync()
        createTableComputers().unsafeRunSync()
        createTableImages().unsafeRunSync()
        insertCarF(Car("123",123,6666,"Audi",2007,120000,imageKey)).unsafeRunSync()
        insertCarF(Car("123",123,1111,"BMW",1999,120000,imageKey)).unsafeRunSync()
        insertApartmentF(Apartment("123",123,66666,"DAUGAVAS",50,2,12,imageKey)).unsafeRunSync()
        insertApartmentF(Apartment("123",123,77777,"DAUG",70,2,12,imageKey)).unsafeRunSync()
        insertApartmentF(Apartment("123",123,88888,"D",150,2,12,imageKey)).unsafeRunSync()
        insertComputerF(Computer("123",123,888,"AMD",32,"GTX 3070",520,imageKey)).unsafeRunSync()
        insertComputerF(Computer("123",123,777,"INTEL",16,"GTX 2070",1000,imageKey)).unsafeRunSync()
        TelegramBot.run(args)
    }
}
