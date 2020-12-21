package services

import models.Product.{Apartment, Car, Computer}
import models.{Product, User}
import cats.effect.{ContextShift, IO}
import db.DbScenarios._
import db.DbTransactor
import db.DbMappings._
import doobie.implicits._
import models.base._
import models.user._

import scala.concurrent.ExecutionContext

trait DbService[F[_]] {
    def getUsers: F[List[User]]

    def getUserById(id: UserId): F[Option[User]]

    def getProducts: F[List[Product]]

    def getProductsByUserId(id: UserId): F[List[Product]]

    def getCars: F[List[Car]]

    def getComputers: F[List[Computer]]

    def getApartments: F[List[Apartment]]

    def insertUserToDb(user: User): F[Unit]

    def insertProductToDb(product: Product): F[Unit]

    def deleteProductFromDb(product: Product) : F[Unit]

    def addPhoneNumberById(id: UserId, phoneNumber: PhoneNumber): F[Unit]
}

object DbService {
    implicit private val ioContextShift: ContextShift[IO] =
        IO.contextShift(ExecutionContext.global)

    private val transactor = DbTransactor.make[IO]

    implicit val dbService: DbService[IO] = new DbService[IO] {
        def insertUserToDb(user: User): IO[Unit] = for {
            _ <- transactor.use(xa => insertUser(user).update.run.transact(xa))
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

        def deleteProductFromDb(product: Product) : IO[Unit] = product match {
            case car: Car => for {
                _ <- transactor.use(xa => deleteCarById(car).update.run.transact(xa))
            } yield ()
            case apartment: Apartment => for {
                _ <- transactor.use(xa => deleteApartmentById(apartment).update.run.transact(xa))
            } yield ()
            case computer: Computer => for {
                _ <- transactor.use(xa => deleteComputerById(computer).update.run.transact(xa))
            } yield ()
            case _ => IO.unit
        }

        def getUsers: IO[List[User]] =
            for {
                res <- transactor.use { xa =>
                    usersSelect.query[User]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getUserById(id: UserId): IO[Option[User]] =
            for {
                res <- transactor.use { xa =>
                    selectUserById(id).query[User].option
                      .transact(xa)
                }
            } yield res

        def getProducts: IO[List[Product]] =
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

        def getProductsByUserId(id: UserId): IO[List[Product]] =
            for {
                cars <- transactor.use { xa =>
                    selectCarsById(id).query[Car]
                      .to[List]
                      .transact(xa)
                }
                apartments <- transactor.use { xa =>
                    selectApartmentsById(id).query[Apartment]
                      .to[List]
                      .transact(xa)
                }
                computers <- transactor.use { xa =>
                    selectComputersById(id).query[Computer]
                      .to[List]
                      .transact(xa)
                }
            } yield cars ++ apartments ++ computers

        def getCars: IO[List[Car]] =
            for {
                res <- transactor.use { xa =>
                    carsSelect.query[Car]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getApartments: IO[List[Apartment]] =
            for {
                res <- transactor.use { xa =>
                    apartmentsSelect.query[Apartment]
                      .to[List]
                      .transact(xa)
                }
            } yield res

        def getComputers: IO[List[Computer]] =
            for {
                res <- transactor.use { xa =>
                    computersSelect.query[Computer]
                      .to[List]
                      .transact(xa)
                }
            } yield res
    }
}
