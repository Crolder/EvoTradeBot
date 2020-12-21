package utils

import models.apartment.District
import models.car.Manufacturer
import models.computer.Cpu
import utils.InputValidation._
import main.scala.BotActions.getImage
import canoe.api.{Scenario, TelegramClient, chatApi}
import models.Product.{Apartment, Car, Computer}
import models.base.UserId
import canoe.models.Chat
import services.{DbService, ObjectFactory}

object CreateProducts {
    def createCar[F[_] : TelegramClient](chat: Chat, manufacturer: Manufacturer)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Car] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            year <- provideYear(chat)
            mileage <- provideMileage(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            car <- Scenario.eval(objectFactory.createCar(
                UserId(detailedChat.id),
                price,
                manufacturer,
                year,
                mileage,
                description,
                imageKey
            ))
        } yield car

    def createApartment[F[_] : TelegramClient](chat: Chat, district: District)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Apartment] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            area <- provideArea(chat)
            roomAmount <- provideRoomAmount(chat)
            floor <- provideFloor(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            apartment <- Scenario.eval(objectFactory.createApartment(
                UserId(detailedChat.id),
                price,
                district,
                area,
                roomAmount,
                floor,
                description,
                imageKey
            ))
        } yield apartment

    def createComputer[F[_] : TelegramClient](chat: Chat, cpu: Cpu)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Computer] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            ram <- provideRam(chat)
            videoCard <- provideVideoCard(chat)
            storage <- provideStorage(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            computer <- Scenario.eval(objectFactory.createComputer(
                UserId(detailedChat.id),
                price,
                cpu,
                ram,
                videoCard,
                storage,
                description,
                imageKey
            ))
        } yield computer


}
