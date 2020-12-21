package main.scala

import canoe.models.Chat
import models.Product
import models.base._
import models.car._
import models.apartment._
import models.computer._
import models.computer.Cpu._
import models.apartment.District._
import models.car.Manufacturer._
import canoe.api._
import canoe.syntax.{command, _}
import filters.Filter
import filters.Filter._
import services.{DbService, KeyboardService, ObjectFactory}
import utils.InputValidation._
import utils.DisplayContent._
import utils.CreateProducts._

object BotActions {
    def filterProducts[A <: Product, F[_] : TelegramClient](chat: Chat, products: List[A])(implicit service: DbService[F], objectFactory: ObjectFactory[F], filters: Filter[A, Product]): Scenario[F, Unit] = {
        val filterKeyboard = KeyboardService.createKeyboard(false, "Yes", "No")

        for {
            _ <- Scenario.eval(chat.send("Would you like to filter products?", keyboard = filterKeyboard))
            answer <- Scenario.expect(text)
            _ <- answer match {
                case "Yes" => for {
                    filteredProducts <- filter(chat, products)
                    _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
                    _ <- displayProduct(chat, filteredProducts, 0)
                } yield ()
                case "No" => for {
                    _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
            }
        } yield ()
    }

    def canAddProduct[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = for {
        detailedChat <- Scenario.eval(chat.details)
        user <- Scenario.eval(service.getUserById(UserId(detailedChat.id)))
        hasPhoneNumber <- Scenario.pure[F](user.get.phoneNumber.isDefined)
        _ <- if (hasPhoneNumber) addProduct(chat) else addPhoneNumber(chat)
    } yield ()

    def deleteProduct[F[_] : TelegramClient](chat: Chat, list: List[Product], position: Int)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val keyboard = KeyboardService.createKeyboard(true, "Yes", "No")

        for {
            _ <- Scenario.eval(chat.send("Are you sure you want to delete this product", keyboard = keyboard))
            m <- Scenario.expect(text)
            _ <- m match {
                case "Yes" => for {
                    _ <- Scenario.eval(service.deleteProductFromDb(list(position)))
                    _ <- Scenario.eval(chat.send("Product successfully deleted"))
                    _ <- displayAction(chat)
                } yield ()
                case "No" => for {
                    _ <- displayAction(chat)
                } yield ()
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def addProduct[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val categoryKeyboard = KeyboardService.createKeyboard(true, "Cars", "Apartments", "Computers", "Cancel")
        val carsKeyboard = KeyboardService.createKeyboard(true, "Audi", "Bmw", "Volvo", "Cancel")
        val districtKeyboard = KeyboardService.createKeyboard(true, "Imanta", "Center", "Purvciems", "Cancel")
        val cpuKeyboard = KeyboardService.createKeyboard(true, "Amd", "Intel", "Cancel")

        for {
            _ <- Scenario.eval(chat.send(content = "Choose category", keyboard = categoryKeyboard))
            category <- Scenario.expect(text)
            _ <- category match {
                case "Cars" => for {
                    - <- Scenario.eval(chat.send(content = "Choose manufacturer", keyboard = carsKeyboard))
                    manufacturer <- Scenario.expect(text)
                    _ <- manufacturer match {
                        case "Audi" => addCar(chat, Audi)
                        case "Bmw" => addCar(chat, Bmw)
                        case "Volvo" => addCar(chat, Volvo)
                        case _ => Scenario.eval(chat.send("No such manufacturer"))
                    }
                } yield ()
                case "Apartments" => for {
                    - <- Scenario.eval(chat.send(content = "Choose district", keyboard = districtKeyboard))
                    district <- Scenario.expect(text)
                    _ <- district match {
                        case "Imanta" => addApartment(chat, Imanta)
                        case "Center" => addApartment(chat, Center)
                        case "Purvciems" => addApartment(chat, Purvciems)
                        case _ => Scenario.eval(chat.send("No such district"))
                    }
                } yield ()
                case "Computers" => for {
                    _ <- Scenario.eval(chat.send(content = "Choose CPU", keyboard = cpuKeyboard))
                    cpu <- Scenario.expect(text)
                    _ <- cpu match {
                        case "Amd" => addComputer(chat, Amd)
                        case "Intel" => addComputer(chat, Intel)
                        case _ => Scenario.eval(chat.send("No such cpu"))
                    }
                } yield ()
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def addPhoneNumber[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val addPhoneNumberKeyboard = KeyboardService.createKeyboard(true, "Add phone number", "Back")

        for {
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(chat.send(
                content = "You didn't add your phone number. To add products you have to add phone number",
                keyboard = addPhoneNumberKeyboard
            ))
            choice <- Scenario.expect(text)
            _ <- choice match {
                case "Add phone number" => for {
                    phoneNumber <- providePhoneNumber(chat)
                    _ <- Scenario.eval(service.addPhoneNumberById(UserId(detailedChat.id), phoneNumber))
                    _ <- Scenario.eval(chat.send(content = "Phone number added successfully!")) >> displayAction(chat)
                } yield ()
                case "Back" => displayAction(chat)
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def addCar[F[_] : TelegramClient](chat: Chat, manufacturer: Manufacturer)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            car <- createCar(chat, manufacturer).stopOn(command("cancel").isDefinedAt)
            _ <- Scenario.eval(service.insertProductToDb(car))
            _ <- Scenario.eval(chat.send("Car added successfully!"))
        } yield ()

    def addApartment[F[_] : TelegramClient](chat: Chat, district: District)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            apartment <- createApartment(chat, district).stopOn(command("cancel").isDefinedAt)
            _ <- Scenario.eval(service.insertProductToDb(apartment))
            _ <- Scenario.eval(chat.send("Apartment added successfully!"))
        } yield ()

    def addComputer[F[_] : TelegramClient](chat: Chat, cpu: Cpu)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            computer <- createComputer(chat, cpu).stopOn(command("cancel").isDefinedAt)
            _ <- Scenario.eval(service.insertProductToDb(computer))
            _ <- Scenario.eval(chat.send("Computer added successfully!"))
        } yield ()

    def getImage[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F]): Scenario[F, ImageKey] = {
        val defaultImageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"

        for {
            _ <- Scenario.eval(chat.send(content = "Would you like to add images?", keyboard = KeyboardService.createKeyboard(false, "Yes", "No")))
            answer <- Scenario.expect(text)
            image <- answer match {
                case "Yes" => for {
                    image <- provideImage(chat)
                } yield image.fileId
                case _ => Scenario.pure[F](defaultImageKey)
            }
        } yield ImageKey(image)
    }
}
