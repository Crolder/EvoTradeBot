package services

import cats.effect.IO
import models.Product.{Apartment, Car, Computer}
import models.User
import models.apartment.{Area, District, Floor, RoomAmount}
import models.base.{Description, ImageKey, Price, ProductId, UserId}
import models.car.{Manufacturer, Mileage, Year}
import models.computer.{Cpu, Ram, Storage, VideoCard}
import models.user.PhoneNumber

import java.util.UUID


trait ObjectFactory[F[_]] {
    def createUser(id: UserId,
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
}

object ObjectFactory {
    implicit val objectFactory: ObjectFactory[IO] = new ObjectFactory[IO] {
        def createUser(id: UserId,
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
    }
}
