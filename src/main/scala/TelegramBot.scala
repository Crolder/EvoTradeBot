package main.scala

import models.Product.{Apartment, Car, Computer}

import java.util.UUID
import canoe.models.outgoing.PhotoContent
import canoe.models.{Chat, Contact, PhotoSize}
import models.Product
import models.base._
import models.car._
import models.apartment._
import models.computer._
import models.user._
import models.computer.Cpu._
import models.apartment.District._
import models.car.Manufacturer._
import canoe.models.messages.{AnimationMessage, ContactMessage, PhotoMessage, StickerMessage, TelegramMessage, TextMessage}
import main.scala.BotConfig.token
import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.InputFile.Existing
import canoe.syntax._
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import services.KeyboardService
import services.Service
import services.DbService.dbService
import main.scala.InputValidation._
import main.scala.Templates._

import java.util.stream.Collector.Characteristics
import scala.annotation.tailrec


/**
  * Example using compositional property of scenarios
  * by combining them into more complex registration process
  */
object TelegramBot extends IOApp {
    def run(args: List[String]): IO[ExitCode] = {
        Stream
          .resource(TelegramClient.global[IO](token))
          .flatMap { implicit client =>
              Bot.polling[IO].follow(start, users, add, products, myProducts, actions, images)
          }
          .compile.drain.as(ExitCode.Success)
    }

    def usersChat[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            users <- Scenario.eval(service.getUsers())
            _ <- Scenario.eval(chat.send(s"Users: $users"))
            _ <- displayAction(chat)
        } yield ()

    def users[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("users").chat)
            - <- usersChat(chat)
        } yield ()

    def getContact[F[_] : TelegramClient](chat: Chat, id: UserId)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            users <- Scenario.eval(service.getUserById(id))
            _ <- users match {
                case Nil => Scenario.eval(chat.send("There is no users with this id"))
                case _ => Scenario.eval(chat.send(
                    ContactMessage(
                        123,
                        chat,
                        123,
                        Contact(
                            users.head.phoneNumber.getOrElse("no phone number").toString,
                            users.head.firstname,
                            Some(users.head.lastname),
                            Some(users.head.id.id.toInt),
                            None)
                    ).contact
                ))
            }
        } yield ()

    def productOptions[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            _ <- Scenario.eval(chat.send(s"Which products", keyboard = KeyboardService.createKeyboard(true, "All", "Cars", "Apartments", "Computers", "My", "Cancel")))
            m <- Scenario.expect(text)
            _ <- m match {
                case "All" => for {
                    products <- Scenario.eval(service.getProducts())
                    _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
                case "Cars" => for {
                    products <- Scenario.eval(service.getCars())
                    _ <- Scenario.eval(chat.send(s"Amount of cars: ${products.size}"))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
                case "Apartments" => for {
                    products <- Scenario.eval(service.getApartments())
                    _ <- Scenario.eval(chat.send(s"Amount of apartments: ${products.size}"))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
                case "Computers" => for {
                    products <- Scenario.eval(service.getComputers())
                    _ <- Scenario.eval(chat.send(s"Amount of computers: ${products.size}"))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
                case "My" => for {
                    products <- Scenario.eval(service.getProductsByUserId(chat.id))
                    _ <- displayProduct(chat, products, 0)
                } yield ()
                case "Cancel" => displayAction(chat)
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def sendProduct[F[_] : TelegramClient](chat: Chat, product: Product): Scenario[F, Unit] = {
        val keyboard = KeyboardService.createKeyboard(true, "Previous", "Next", "Get Contact", "Cancel")

        product match {
            case car: Car => for {
                _ <- Scenario.eval(chat.send(
                    content = carTemplate(
                        car.manufacturer,
                        car.year,
                        car.mileage,
                        car.descripion,
                        car.price,
                        car.imageKey
                    ),
                    keyboard = keyboard
                ))
            } yield ()
            case apartment: Apartment => for {
                _ <- Scenario.eval(chat.send(
                    content = apartmentTemplate(
                        apartment.district,
                        apartment.area,
                        apartment.roomAmount,
                        apartment.floor,
                        apartment.descripion,
                        apartment.price,
                        apartment.imageKey
                    ),
                    keyboard = keyboard
                ))
            } yield ()
            case computer: Computer => for {
                _ <- Scenario.eval(chat.send(
                    content = computerTemplate(
                        computer.cpu,
                        computer.ram,
                        computer.videoCard,
                        computer.storage,
                        computer.descripion,
                        computer.price,
                        computer.imageKey
                    ),
                    keyboard = keyboard
                ))
            } yield ()
            case _ => for {
                _ <- Scenario.eval(chat.send("Something went wrong!"))
            } yield ()
        }
    }

    def displayProduct[F[_] : TelegramClient](chat: Chat, list: List[Product], position: Int)(implicit service: Service[F]): Scenario[F, Unit] = {
        val listSize = list.size - 1

        def expectation(chat: Chat, list: List[Product], position: Int)(implicit service: Service[F]): Scenario[F, Unit] = for {
            m <- Scenario.expect(text)
            _ <- m match {
                case "Next" => if (position + 1 <= listSize) displayProduct(chat, list, position + 1) else displayProduct(chat, list, 0)
                case "Previous" => if (position - 1 >= 0) displayProduct(chat, list, position - 1) else displayProduct(chat, list, listSize)
                case "Cancel" => displayAction(chat)
                case "Get Contact" => getContact(chat, list(position).userId) >> expectation(chat, list, position)
                case _ => displayAction(chat)
            }
        } yield ()

        for {
            _ <- sendProduct(chat, list(position))
            _ <- expectation(chat, list, position)
        } yield ()
    }

    def displayAction[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        val addProduct = "\u2795 Add product \u2795"

        val keyboard = KeyboardService.createKeyboard(true, addProduct, "Products", "Users")

        for {
            _ <- Scenario.eval(chat.send("Choose action", keyboard = keyboard))
            m <- Scenario.expect(text)
            _ <- m match {
                case "Products" => for {
                    _ <- productOptions(chat)
                } yield ()
                case `addProduct` => for {
                    _ <- addProductOptions(chat)
                    _ <- displayAction(chat)
                } yield ()
                case "Users" => for {
                    _ <- usersChat(chat)
                    _ <- displayAction(chat)
                } yield ()
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def images[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("images").chat)
            images <- Scenario.eval(service.getImages())
            _ <- Scenario.eval(chat.send(s"Images: $images"))
        } yield ()
    }

    def actions[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("actions").chat)
            _ <- displayAction(chat)
        } yield ()
    }

    def productsChat[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            products <- Scenario.eval(service.getProducts())
            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
            _ <- displayProduct(chat, products, 0)
            _ <- displayAction(chat)
        } yield ()
    }

    def products[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("products").chat)
            - <- productsChat(chat)
        } yield ()
    }

    def myProducts[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("my").chat)
            detailedChat <- Scenario.eval(chat.details)
            products <- Scenario.eval(service.getProductsByUserId(detailedChat.id))
            _ <- Scenario.eval(chat.send(s"My Products: $products"))
        } yield ()

    def start[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            userExists <- Scenario.eval(service.userExists(UserId(detailedChat.id)))
            _ <- Scenario.eval(chat.send(s"Exist?: $userExists"))
            _ <- Scenario.eval(chat.send(s"Id: ${detailedChat.id}, Name: ${detailedChat.firstName}"))
            user <- Scenario.eval(service.register(UserId(detailedChat.id), detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
            _ <- Scenario.eval(service.insertUserIfNotExist(user))
            - <- displayAction(chat)
        } yield ()

    def addProduct[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        val categoryKeyboard = KeyboardService.createKeyboard(true, "Cars", "Apartments", "Computers")
        val carsKeyboard = KeyboardService.createKeyboard(true, "Audi", "Bmw", "Volvo")
        val districtKeyboard = KeyboardService.createKeyboard(true, "Imanta", "Center", "Purvciems")
        val cpuKeyboard = KeyboardService.createKeyboard(true, "Amd", "Intel")

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
            }
        } yield ()
    }

    def addPhoneNumber[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
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

    def addProductOptions[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = for {
        detailedChat <- Scenario.eval(chat.details)
        hasPhoneNumber <- Scenario.eval(service.hasPhoneNumber(UserId(detailedChat.id)))
        _ <- if (hasPhoneNumber) addProduct(chat) else addPhoneNumber(chat)
    } yield ()

    def addCar[F[_] : TelegramClient](chat: Chat, manufacturer: Manufacturer)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            car <- createCar(chat, manufacturer)
            _ <- Scenario.eval(service.insertProductToDb(car))
            _ <- Scenario.eval(chat.send("Car added successfully!"))
        } yield ()

    def addApartment[F[_] : TelegramClient](chat: Chat, district: District)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            apartment <- createApartment(chat, district)
            _ <- Scenario.eval(service.insertProductToDb(apartment))
            _ <- Scenario.eval(chat.send("Apartment added successfully!"))
        } yield ()

    def addComputer[F[_] : TelegramClient](chat: Chat, cpu: Cpu)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            computer <- createComputer(chat, cpu)
            _ <- Scenario.eval(service.insertProductToDb(computer))
            _ <- Scenario.eval(chat.send("Computer added successfully!"))
        } yield ()

    def createCar[F[_] : TelegramClient](chat: Chat, manufacturer: Manufacturer)(implicit service: Service[F]): Scenario[F, Car] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            year <- provideYear(chat)
            mileage <- provideMileage(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            car <- Scenario.eval(service.createCar(
                UserId(detailedChat.id),
                price,
                manufacturer,
                year,
                mileage,
                description,
                imageKey
            ))
        } yield car

    def createApartment[F[_] : TelegramClient](chat: Chat, district: District)(implicit service: Service[F]): Scenario[F, Apartment] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            area <- provideArea(chat)
            roomAmount <- provideRoomAmount(chat)
            floor <- provideFloor(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            apartment <- Scenario.eval(service.createApartment(
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

    def createComputer[F[_] : TelegramClient](chat: Chat, cpu: Cpu)(implicit service: Service[F]): Scenario[F, Computer] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            ram <- provideRam(chat)
            videoCard <- provideVideoCard(chat)
            storage <- provideStorage(chat)
            price <- providePrice(chat)
            description <- provideDescription(chat)
            imageKey <- getImage(chat)
            computer <- Scenario.eval(service.createComputer(
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

    def getImage[F[_] : TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, ImageKey] = {
        val defaultImageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"

        for {
            _ <- Scenario.eval(chat.send(content = "Would you like to add images?", keyboard = KeyboardService.createKeyboard(false, "Yes", "No")))
            answer <- Scenario.expect(text)
            image <- answer match {
                case "Yes" => for {
                    image <- provideImage(chat)
                } yield image.fileId
                case _ => for {
                    _ <- Scenario.eval(chat.send(content = "Will be used default image"))
                } yield defaultImageKey
            }
        } yield ImageKey(image)
    }

    def add[F[_] : TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("add").chat)
            _ <- addProductOptions(chat)
        } yield ()
}