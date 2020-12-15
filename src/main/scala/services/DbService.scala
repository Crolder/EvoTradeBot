package services

import adt.Product.{Apartment, Car, Computer}
import adt.{Product, User}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toTraverseOps
import db.DbScenarios.{apartmentsSelect, carsSelect, computersSelect, imageSelect, insertApartment, insertCar, insertComputer, insertImage, insertProduct, insertUser, productsSelect, selectImagesById, selectProductsById, selectUserById, setupUsers, usersSelect}
import db.DbTransactor
import doobie.implicits._

import java.util.UUID

trait Service[F[_]] {

    def userExists(id: Long): F[Boolean]

    def register(id: Long, firstname: String, lastname: String, username: String): F[User]

    def createCar(
                   userId: Long,
                   price: Int,
                   manufacturer: String,
                   year: Int,
                   mileage: Int,
                   imageKey: String
                 ): F[Car]

    def createApartment(
                   userId: Long,
                   price: Int,
                   district: String,
                   area: Int,
                   roomAmount: Int,
                   floor: Int,
                   imageKey: String
                 ): F[Apartment]

    def createComputer(
                        userId: Long,
                        price: Int,
                        cpu: String,
                        ram: Int,
                        videoCard: String,
                        storage: Int,
                        imageKey: String
                      ): F[Computer]

    def getUsers(): F[List[User]]

    def getUserById(id: Long): F[List[User]]

    def getProducts(): F[List[Product]]

    def getCars(): F[List[Car]]

    def getComputers(): F[List[Computer]]

    def getApartments(): F[List[Apartment]]

    def getImages(): F[List[String]]

    def getImagesById(id: String): F[List[String]]

    def getProductsByUserId(userId: Long) : F[List[Car]]

    def insertUserIfNotExist(user: User): F[Unit] // ToDO Return Success or Error

    def insertProductToDb(product: Product): F[Unit] // ToDO Return Success or Error
}

object DbService extends IOApp {

    private val transactor = DbTransactor.make[IO]

    implicit val dbService: Service[IO] = new Service[IO] {
        def register(id: Long, firstname: String, lastname: String, username: String): IO[User] = IO { User(id, firstname, lastname, username) }

        def createCar(
                       userId: Long,
                       price: Int,
                       manufacturer: String,
                       year: Int,
                       mileage: Int,
                       imageKey: String
                     ): IO[Car] = IO { Car(UUID.randomUUID().toString, userId, price, manufacturer, year, mileage, imageKey) }

        def createApartment(
                       userId: Long,
                       price: Int,
                       district: String,
                       area: Int,
                       roomAmount: Int,
                       floor: Int,
                       imageKey: String
                     ): IO[Apartment] = IO { Apartment(UUID.randomUUID().toString, userId, price, district, area, roomAmount, floor, imageKey) }

        def createComputer(
                             userId: Long,
                             price: Int,
                             cpu: String,
                             ram: Int,
                             videoCard: String,
                             storage: Int,
                             imageKey: String
                           ): IO[Computer] = IO { Computer(UUID.randomUUID().toString, userId, price, cpu, ram, videoCard, storage, imageKey) }

        def insertUserIfNotExist(user: User): IO[Unit] = for {
            userExist <- userExists(user.id)
            _ <- if(!userExist) transactor.use(xa => insertUser(user).update.run.transact(xa)) else IO.unit
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

        def getUserById(id: Long): IO[List[User]] =
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
