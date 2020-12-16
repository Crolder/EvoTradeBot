package services

import models.Product.{Apartment, Car, Computer}
import models.{Product, User}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toTraverseOps
import db.DbScenarios._
import db.DbTransactor
import doobie.implicits._
import doobie._
import models.base._
import models.apartment._
import models.car._
import models.computer._
import db.DbMappings._
import models.user._

import java.util.UUID

trait Service[F[_]] {

    def userExists(id: UserId): F[Boolean]

    def register(id: UserId,
                 firstname: String,
                 lastname: String,
                 username: String,
                 phoneNumber: Option[PhoneNumber] = None
                ): F[User]

    def createCar(
                   userId: UserId,
                   price: Price,
                   manufacturer: Manufacturer,
                   year: Year,
                   mileage: Mileage,
                   description: Description,
                   imageKey: ImageKey
                 ): F[Car]

    def createApartment(
                         userId: UserId,
                         price: Price,
                         district: District,
                         area: Area,
                         roomAmount: RoomAmount,
                         floor: Floor,
                         description: Description,
                         imageKey: ImageKey
                       ): F[Apartment]

    def createComputer(
                        userId: UserId,
                        price: Price,
                        cpu: Cpu,
                        ram: Ram,
                        videoCard: VideoCard,
                        storage: Storage,
                        description: Description,
                        imageKey: ImageKey
                      ): F[Computer]

    def getUsers(): F[List[User]]

    def hasPhoneNumber(id: UserId): F[Boolean]

    def getUserById(id: UserId): F[List[User]]

    def getProducts(): F[List[Product]]

    def getCars(): F[List[Car]]

    def getComputers(): F[List[Computer]]

    def getApartments(): F[List[Apartment]]

    def getImages(): F[List[String]]

    def getImagesById(id: String): F[List[String]]

    def getProductsByUserId(userId: Long) : F[List[Car]]

    def insertUserIfNotExist(user: User): F[Unit] // ToDO Return Success or Error

    def insertProductToDb(product: Product): F[Unit] // ToDO Return Success or Error

    def addPhoneNumberById(id: UserId, phoneNumber: PhoneNumber): F[Unit]
}

object DbService extends IOApp {

    private val transactor = DbTransactor.make[IO]

    implicit val dbService: Service[IO] = new Service[IO] {
        def register(id: UserId,
                     firstname: String,
                     lastname: String,
                     username: String,
                     phoneNumber: Option[PhoneNumber] = None
                    ): IO[User] = IO { User(id, firstname, lastname, username, phoneNumber) }

        def createCar(
                       userId: UserId,
                       price: Price,
                       manufacturer: Manufacturer,
                       year: Year,
                       mileage: Mileage,
                       description: Description,
                       imageKey: ImageKey
                     ): IO[Car] = IO { Car(ProductId(UUID.randomUUID()), userId, price, manufacturer, year, mileage,description, imageKey) }

        def createApartment(
                       userId: UserId,
                       price: Price,
                       district: District,
                       area: Area,
                       roomAmount: RoomAmount,
                       floor: Floor,
                       description: Description,
                       imageKey: ImageKey
                     ): IO[Apartment] = IO { Apartment(ProductId(UUID.randomUUID()), userId, price, district, area, roomAmount, floor,description, imageKey) }

        def createComputer(
                             userId: UserId,
                             price: Price,
                             cpu: Cpu,
                             ram: Ram,
                             videoCard: VideoCard,
                             storage: Storage,
                             description: Description,
                             imageKey: ImageKey
                           ): IO[Computer] = IO { Computer(ProductId(UUID.randomUUID()), userId, price, cpu, ram, videoCard, storage,description, imageKey) }

        def insertUserIfNotExist(user: User): IO[Unit] = for {
            userExist <- userExists(user.id)
            _ <- if(!userExist) transactor.use(xa => insertUser(user).update.run.transact(xa)) else IO.unit
        } yield ()

        def addPhoneNumberById(id: UserId, phoneNumber: PhoneNumber): IO[Unit] = for {
            _ <- transactor.use(xa => updatePhoneNumberById(id, phoneNumber).update.run.transact(xa))
        } yield ()

        def insertProductToDb(product: Product): IO[Unit] = product match {
            case car: Car => for {
                _ <- transactor.use(xa => insertCar(car).update.run.transact(xa))
            } yield ()
            case apartment: Apartment => for {
                _ <- transactor.use(xa => insertApartment(apartment).update.run.transact(xa))
            } yield ()
            case computer: Computer => for {
                _ <- transactor.use(xa => insertComputer(computer).update.run.transact(xa))
            } yield ()
            case _ => IO.unit
        }

        def userExists(id: UserId): IO[Boolean] = for {
            res <- transactor.use { xa =>
                selectUserById(id).query[User]
                  .to[List]
                  .transact(xa)
            }
        } yield if(res.nonEmpty) true else false

        def hasPhoneNumber(id: UserId): IO[Boolean] = for {
            res <- transactor.use { xa =>
                selectUserById(id).query[User]
                  .to[List]
                  .transact(xa)
            }
        } yield res.forall(user => user.phoneNumber.isDefined)

        def getUsers(): IO[List[User]] =
            for {
                res <- transactor.use { xa =>
                    usersSelect.query[User]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getUserById(id: UserId): IO[List[User]] =
            for {
                res <- transactor.use { xa =>
                    selectUserById(id).query[User]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getProducts(): IO[List[Product]] =
            for {
                cars <- transactor.use { xa =>
                    carsSelect.query[Car]
                      .to[List]
                      .transact(xa)
                }
                apartments <- transactor.use { xa =>
                    apartmentsSelect.query[Apartment]
                      .to[List]
                      .transact(xa)
                }
                computers <- transactor.use { xa =>
                    computersSelect.query[Computer]
                      .to[List]
                      .transact(xa)
                }
            } yield cars ++ apartments ++ computers

        def getCars(): IO[List[Car]] =
            for {
                res <- transactor.use { xa =>
                    carsSelect.query[Car]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getApartments(): IO[List[Apartment]] =
            for {
                res <- transactor.use { xa =>
                    apartmentsSelect.query[Apartment]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getComputers(): IO[List[Computer]] =
            for {
                res <- transactor.use { xa =>
                    computersSelect.query[Computer]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getImages(): IO[List[String]] =
            for {
                res <- transactor.use { xa =>
                    imageSelect.query[String]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getImagesById(id: String): IO[List[String]] =
            for {
                res <- transactor.use { xa =>
                    selectImagesById(id).query[String]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getProductsByUserId(userId: Long) : IO[List[Car]] =
            for {
                res <- transactor.use { xa =>
                    selectProductsById(userId).query[Car]
                      .to[List]
                      .transact(xa)
                }
            } yield res
    }

    override def run(args: List[String]): IO[ExitCode] = IO { ExitCode.Success }
}
