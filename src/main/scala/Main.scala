package main.scala

import models.Product.{Apartment, Car, Computer}
import cats.effect.{ExitCode, IO, IOApp}
import db.DbTransactor
import db.DbScenarios._
import doobie.implicits._
import models.{Product, User}
import models.base.{Description, ImageKey, Price, ProductId, UserId}
import models.car.Manufacturer.Audi
import models.car.{Mileage, Year}

import java.util.UUID

object Main extends IOApp {
    private val transactor = DbTransactor.make[IO]

    def createTableUsers(): IO[Unit] = for {
        _ <- transactor.use(xa => setupUsers.update.run.transact(xa))
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

    val imageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"
    val description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

    override def run(args: List[String]): IO[ExitCode] = {
        createTableUsers().unsafeRunSync()
        createTableCars().unsafeRunSync()
        createTableApartments().unsafeRunSync()
        createTableComputers().unsafeRunSync()
        insertCarF(Car(ProductId(UUID.randomUUID()),UserId(123),Price.of("666.66").get,Audi,Year.of("2007").get,Mileage.of("120000").get,Description(description), ImageKey(imageKey))).unsafeRunSync()
//        insertCarF(Car("123",123,6666,"Audi",2007,120000,imageKey)).unsafeRunSync()
//        insertCarF(Car("123",123,1111,"BMW",1999,120000,imageKey)).unsafeRunSync()
//        insertApartmentF(Apartment("123",123,66666,"DAUGAVAS",50,2,12,imageKey)).unsafeRunSync()
//        insertApartmentF(Apartment("123",123,77777,"DAUG",70,2,12,imageKey)).unsafeRunSync()
//        insertApartmentF(Apartment("123",123,88888,"D",150,2,12,imageKey)).unsafeRunSync()
//        insertComputerF(Computer("123",123,888,"AMD",32,"GTX 3070",520,imageKey)).unsafeRunSync()
//        insertComputerF(Computer("123",123,777,"INTEL",16,"GTX 2070",1000,imageKey)).unsafeRunSync()
        TelegramBot.run(args)
    }
}
