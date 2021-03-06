package utils

import canoe.syntax.{any, text}
import models.apartment.{Area, Floor, RoomAmount}
import models.base.{Description, Price}
import models.car.{Mileage, Year}
import models.computer.{Ram, Storage, VideoCard}
import models.user.PhoneNumber
import canoe.models.{Chat, PhotoSize}
import canoe.models.messages.PhotoMessage
import canoe.api._
import canoe.api.models.Keyboard
import canoe.syntax._
import services.DbService

object InputValidation {
    //Base
    def providePrice[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Price] =
        for {
            _ <- Scenario.eval(chat.send("Enter price", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Price.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for price. Try again!")) >> providePrice(chat)
            }
        } yield result

    def provideImage[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, PhotoSize] =
        for {
            _ <- Scenario.eval(chat.send("Enter product image", keyboard = Keyboard.Remove))
            input <- Scenario.expect(any)
            image <- input match {
                case image: PhotoMessage => Scenario.pure[F](image.photo.head)
                case _ => Scenario.eval(chat.send("It's not an image. Try again!")) >> provideImage(chat)
            }
        } yield image

    def provideDescription[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Description] =
        for {
            _ <- Scenario.eval(chat.send("Enter product description", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            description <- Scenario.pure[F](Description(input))
        } yield description

    def providePhoneNumber[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, PhoneNumber] =
        for {
            _ <- Scenario.eval(chat.send("Please enter your phone number as [+3712*******]", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](PhoneNumber.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect phone number format. Try again!")) >> providePhoneNumber(chat)
            }
        } yield result

    //Car
    def provideYear[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Year] =
        for {
            _ <- Scenario.eval(chat.send("Enter car year [1900 - 2021]", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Year.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for year. Try again!")) >> provideYear(chat)
            }
        } yield result

    def provideMileage[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Mileage] =
        for {
            _ <- Scenario.eval(chat.send("Enter car mileage in kilometres [0 - 1000000]", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Mileage.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for mileage. Try again!")) >> provideMileage(chat)
            }
        } yield result

    //Apartment
    def provideArea[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Area] =
        for {
            _ <- Scenario.eval(chat.send("Enter apartment area in m^2 ", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Area.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for area. Try again!")) >> provideArea(chat)
            }
        } yield result

    def provideRoomAmount[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, RoomAmount] =
        for {
            _ <- Scenario.eval(chat.send("Enter apartment amount of rooms", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](RoomAmount.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for amount of rooms. Try again!")) >> provideRoomAmount(chat)
            }
        } yield result

    def provideFloor[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Floor] =
        for {
            _ <- Scenario.eval(chat.send("Enter floor of apartment", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Floor.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for floor. Try again!")) >> provideFloor(chat)
            }
        } yield result

    //Computer
    def provideRam[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Ram] =
        for {
            _ <- Scenario.eval(chat.send("Enter computer RAM in GB", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Ram.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for RAM. Try again!")) >> provideRam(chat)
            }
        } yield result

    def provideStorage[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, Storage] =
        for {
            _ <- Scenario.eval(chat.send("Enter computer storage in GB", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            option <- Scenario.pure[F](Storage.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Incorrect input for storage. Try again!")) >> provideStorage(chat)
            }
        } yield result

    def provideVideoCard[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, VideoCard] =
        for {
            _ <- Scenario.eval(chat.send("Enter computer's video card name", keyboard = Keyboard.Remove))
            input <- Scenario.expect(text)
            videoCard <- Scenario.pure[F](VideoCard(input))
        } yield videoCard
}
