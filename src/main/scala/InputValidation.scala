package main.scala

import canoe.models.{Chat, PhotoSize}
import models.base._
import models.car._
import models.apartment._
import models.user._
import models.computer._
import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.messages.PhotoMessage
import canoe.syntax._
import services.Service


object InputValidation {
    //Base
    def providePrice[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Price] =
        for {
            _      <- Scenario.eval(chat.send("Enter price", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Price.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> providePrice(chat)
            }
        } yield result

    def provideImage[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, PhotoSize] =
        for {
            _      <- Scenario.eval(chat.send("Enter product image", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(any)
            image <- input match {
                case image: PhotoMessage => Scenario.pure[F](image.photo.head)
                case _ => Scenario.eval(chat.send("It's not an image. Try again!")) >> provideImage(chat)
            }
        } yield image

    def provideDescription[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Description] =
        for {
            _      <- Scenario.eval(chat.send("Enter product description", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            description <- Scenario.pure[F](Description(input))
        } yield description

    def providePhoneNumber[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, PhoneNumber] =
        for {
            _      <- Scenario.eval(chat.send("Please enter your phone number as [+3712*******]", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](PhoneNumber.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> providePhoneNumber(chat)
            }
        } yield result

    //Car
    def provideYear[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Year] =
        for {
            _      <- Scenario.eval(chat.send("Enter car year", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Year.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideYear(chat)
            }
        } yield result

    def provideMileage[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Mileage] =
        for {
            _      <- Scenario.eval(chat.send("Enter car mileage in kilometres", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Mileage.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideMileage(chat)
            }
        } yield result

    //Apartment
    def provideArea[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Area] =
        for {
            _      <- Scenario.eval(chat.send("Enter apartment area", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Area.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideArea(chat)
            }
        } yield result

    def provideRoomAmount[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, RoomAmount] =
        for {
            _      <- Scenario.eval(chat.send("Enter apartment amount of rooms", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](RoomAmount.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideRoomAmount(chat)
            }
        } yield result

    def provideFloor[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Floor] =
        for {
            _      <- Scenario.eval(chat.send("Enter apartment amount of rooms", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Floor.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideFloor(chat)
            }
        } yield result

    //Computer
    def provideRam[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Ram] =
        for {
            _      <- Scenario.eval(chat.send("Enter computer RAM", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Ram.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideRam(chat)
            }
        } yield result

    def provideStorage[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Storage] =
        for {
            _      <- Scenario.eval(chat.send("Enter computer storage", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            option <- Scenario.pure[F](Storage.of(input))
            result <- option match {
                case Some(value) => Scenario.pure[F](value)
                case None => Scenario.eval(chat.send("Try again")) >> provideStorage(chat)
            }
        } yield result

    def provideVideoCard[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, VideoCard] =
        for {
            _      <- Scenario.eval(chat.send("Enter apartment floor", keyboard = Keyboard.Remove))
            input   <- Scenario.expect(text)
            videoCard <- Scenario.pure[F](VideoCard(input))
        } yield videoCard
}
