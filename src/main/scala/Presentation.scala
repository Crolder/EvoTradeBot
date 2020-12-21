package main.scala

import models.base.{Description, ImageKey, Price, ProductId, UserId}
import models.user.PhoneNumber
import models.Product.{Apartment, Car, Computer}
import models.{Product, User}
import cats.effect.{ContextShift, IO}
import db.DbScenarios._
import db.DbTransactor
import doobie.implicits._
import models.apartment.{Area, Floor, RoomAmount}
import models.apartment.District.{Center, Imanta}
import models.car.{Manufacturer, Mileage, Year}
import models.car.Manufacturer._
import models.computer.Cpu.Amd
import models.computer.{Ram, Storage, VideoCard}

import java.util.UUID
import scala.concurrent.ExecutionContext

object Presentation {
    implicit private val ioContextShift: ContextShift[IO] =
        IO.contextShift(ExecutionContext.global)

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

    def insertUserF(user: User): IO[Unit] = for {
        _ <- transactor.use(xa => insertUser(user).update.run.transact(xa))
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

    val imageKeyDefault = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"

    val imageKeyComputer1 = "AgACAgQAAxkBAAIXK1_hB1WmuzVa5k2Kvi4gKIKiuXa9AAJQtTEb7isJU_AWYzvo8tRLUzRTKF0AAwEAAwIAA20AA8zMAQABHgQ"
    val imageKeyComputer2 = "AgACAgQAAxkBAAIXNV_hB39-K-PpvWKWbC5QMBPrzfNFAAJVtTEb7isJU9WchTFTTLekJPnsJ10AAwEAAwIAA20AAy9nAwABHgQ"

    val imageKeyCarJon = "AgACAgQAAxkBAAIXMV_hB2sXGGP-zBgfn_fG7uT86y_UAAJTtTEb7isJU3oMTVj4OtUQ2JIGJ10AAwEAAwIAA3gAAzqKAwABHgQ"
    val imageKeyCarTyrion = "AgACAgQAAxkBAAIXM1_hB3DIOLg9QugiOh53zpQaSnSwAAJUtTEb7isJU6c-BKpAqaEE9CPZJ10AAwEAAwIAA20AAzGDAwABHgQ"

    val imageKeyApartment1 = "AgACAgQAAxkBAAIXLV_hB11zIos0z4OKti4D_xNEH_zBAAJRtTEb7isJU6LSRJcm9P3DAbi-Kl0AAwEAAwIAA20AA1c2AAIeBA"
    val imageKeyApartment2 = "AgACAgQAAxkBAAIXL1_hB2fR-lhQqGYsl2vATlXj3k55AAJStTEb7isJUxIpqPgSgnjhFMrYJl0AAwEAAwIAA20AA1C_AwABHgQ"

    val description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

    val user1: User = User(
        UserId(111),
        "Jon",
        "Snow",
        "Bastard",
        PhoneNumber.of("+37121111111"))

    val user2: User = User(
        UserId(222),
        "Daenerys",
        "Targaryen",
        """
          |Daenerys Stormborn of the House Targaryen,
          |First of Her Name,
          |The Unburnt,
          |Queen of the Andals and the First Men,
          |Khaleesi of the Great Grass Sea,
          |Breaker of Chains, and Mother of Dragons""".stripMargin,
        PhoneNumber.of("+37122222222"))

    val user3: User = User(
        UserId(333),
        "Tyrion",
        "Lannister",
        "The Imp",
        PhoneNumber.of("+37126666666"))

    val car1: Car = Car(
        ProductId(UUID.randomUUID()),
        user1.id,
        Price.of("7500.99").get,
        Audi,
        Year.of("2007").get,
        Mileage.of("70000").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyCarJon)
    )

    val car2: Car = Car(
        ProductId(UUID.randomUUID()),
        user1.id,
        Price.of("2200").get,
        Volvo,
        Year.of("2003").get,
        Mileage.of("230000").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyDefault)
    )

    val car3: Car = Car(
        ProductId(UUID.randomUUID()),
        user3.id,
        Price.of("1200").get,
        Bmw,
        Year.of("1997").get,
        Mileage.of("420000").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyCarTyrion)
    )

    val apartment1: Apartment = Apartment(
        ProductId(UUID.randomUUID()),
        user1.id,
        Price.of("65000.00").get,
        Center,
        Area.of("78").get,
        RoomAmount.of("3").get,
        Floor.of("13").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyDefault)
    )

    val apartment2: Apartment = Apartment(
        ProductId(UUID.randomUUID()),
        user2.id,
        Price.of("165000.00").get,
        Imanta,
        Area.of("250").get,
        RoomAmount.of("6").get,
        Floor.of("24").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyApartment1)
    )

    val apartment3: Apartment = Apartment(
        ProductId(UUID.randomUUID()),
        user3.id,
        Price.of("25000.00").get,
        Imanta,
        Area.of("40").get,
        RoomAmount.of("2").get,
        Floor.of("2").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyApartment2)
    )

    val computer1: Computer = Computer(
        ProductId(UUID.randomUUID()),
        user1.id,
        Price.of("999.99").get,
        Amd,
        Ram.of("16").get,
        VideoCard("GTX 1660 Super"),
        Storage.of("520").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyComputer1)
    )

    val computer2: Computer = Computer(
        ProductId(UUID.randomUUID()),
        user2.id,
        Price.of("1500.00").get,
        Amd,
        Ram.of("32").get,
        VideoCard("GTX 3090 Super"),
        Storage.of("1000").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyComputer2)
    )

    val computer3: Computer = Computer(
        ProductId(UUID.randomUUID()),
        user3.id,
        Price.of("500.00").get,
        Amd,
        Ram.of("8").get,
        VideoCard("Radeon 5500"),
        Storage.of("520").get,
        Description(Presentation.description),
        ImageKey(Presentation.imageKeyDefault)
    )
}
