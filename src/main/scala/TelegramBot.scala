package main.scala

import adt.Product.{Apartment, Car, Computer}

import java.util.UUID
import canoe.models.outgoing.PhotoContent
import canoe.models.{Chat, Contact}
import adt.Product
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

import javax.print.attribute.standard.PrinterMoreInfoManufacturer
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

    def usersChat[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            users <- Scenario.eval(service.getUsers())
            _ <- Scenario.eval(chat.send(s"Users: $users"))
            _ <- displayAction(chat)
        } yield ()

    def users[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("users").chat)
            - <- usersChat(chat)
        } yield ()

    def getContact[F[_]: TelegramClient](chat: Chat, id: Long)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            users <- Scenario.eval(service.getUserById(id))
            _ <- users match {
                case Nil => for {
                    _ <- Scenario.eval(chat.send("There is no users with this id"))
                } yield ()
                case _ => for {
                    _ <- Scenario.eval(chat.send(ContactMessage(123,chat,123, Contact("+37121877531",users.head.firstname,Some(users.head.lastname),Some(users.head.id.toInt),None)).contact))
                } yield ()
            }
        } yield ()

    def productOptions[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            _ <- Scenario.eval(chat.send(s"Which products", keyboard = KeyboardService.createKeyboard(true, "All", "Cars","Apartments", "Computers", "My", "Cancel")))
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
                } yield()
                case "Cancel" => displayAction(chat)
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def sendProduct[F[_]: TelegramClient](chat: Chat, product: Product): Scenario[F, Unit] = {
        val keyboard = KeyboardService.createKeyboard(true, "Previous", "Next", "Get Contact", "Cancel")

        product match {
            case car: Car => for {
                _ <- Scenario.eval(chat.send(content = PhotoContent(Existing(car.imageKey),s"Price car: ${car.price}"),keyboard = keyboard))
            } yield ()
            case apartment: Apartment => for {
                _ <- Scenario.eval(chat.send(content = PhotoContent(Existing(apartment.imageKey),s"Price apar: ${apartment.price}"),keyboard = keyboard))
            } yield ()
            case computer: Computer => for {
                _ <- Scenario.eval(chat.send(content = PhotoContent(Existing(computer.imageKey),s"Price comp: ${computer.price}"),keyboard = keyboard))
            } yield ()
            case _ => for {
                _ <- Scenario.eval(chat.send("Something went wrong!"))
            } yield ()
        }
    }

    def displayProduct[F[_]: TelegramClient](chat: Chat, list: List[Product], position: Int)(implicit service: Service[F]): Scenario[F, Unit] = {
        val listSize = list.size - 1

        def expectation(chat: Chat, list: List[Product], position: Int)(implicit service: Service[F]): Scenario[F, Unit] = for {
            m <- Scenario.expect(text)
            _ <- m match {
                case "Next" => if(position + 1 <= listSize) displayProduct(chat, list,  position + 1) else displayProduct(chat, list, 0)
                case "Previous" => if(position - 1 >= 0) displayProduct(chat, list,  position - 1) else displayProduct(chat, list, listSize)
                case "Cancel" => displayAction(chat)
                case "Get Contact" => getContact(chat, list(position).userId) >> expectation(chat, list, position)
                case _ => displayAction(chat)
            }
        } yield ()

        for {
            _ <- sendProduct(chat,list(position))
            _ <- expectation(chat, list, position)
        } yield ()
    }

    def displayAction[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
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
                    _ <- addChat(chat)
                    _ <- displayAction(chat)
                } yield ()
                case "Users" => for {
                    _ <- usersChat(chat)
                    _ <- displayAction(chat)
                } yield()
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def images[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("images").chat)
            images <- Scenario.eval(service.getImages())
            _   <- Scenario.eval(chat.send(s"Images: $images"))
        } yield ()
    }

    def actions[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("actions").chat)
            _ <- displayAction(chat)
        } yield ()
    }

    def productsChat[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            products <- Scenario.eval(service.getProducts())
            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
            _ <- displayProduct(chat, products, 0)
            _ <- displayAction(chat)
        } yield ()
    }

    def products[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("products").chat)
            - <- productsChat(chat)
        } yield ()
    }

    def myProducts[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("my").chat)
            detailedChat <- Scenario.eval(chat.details)
            products <- Scenario.eval(service.getProductsByUserId(detailedChat.id))
            _ <- Scenario.eval(chat.send(s"My Products: $products"))
        } yield ()

    def start[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            userExists <- Scenario.eval(service.userExists(detailedChat.id))
            _   <- Scenario.eval(chat.send(s"Exist?: $userExists"))
            _   <- Scenario.eval(chat.send(s"Id: ${detailedChat.id}, Name: ${detailedChat.firstName}"))
            user   <- Scenario.eval(service.register(detailedChat.id, detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
            _ <- Scenario.eval(service.insertUserIfNotExist(user))
            - <- displayAction(chat)
        } yield ()

    def addChat[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, Unit] = {

        val categoryKeyboard = KeyboardService.createKeyboard(true, "Cars", "Apartments", "Computers")
        val carsKeyboard = KeyboardService.createKeyboard(true, "Audi", "BMW", "Volvo")
        val districtKeyboard = KeyboardService.createKeyboard(true, "Center", "Agenskalns", "Zolitude")
        val cpuKeyboard = KeyboardService.createKeyboard(true, "AMD", "INTEL")

        for {
            detailedChat <- Scenario.eval(chat.details)
            - <- Scenario.eval(chat.send(content = "Choose category", keyboard = categoryKeyboard))
            category <- Scenario.expect(text)
            _ <- category match {
                case "Cars" => for {
                    - <- Scenario.eval(chat.send(content = "Choose manufacturer", keyboard = carsKeyboard))
                    manufacturer <- Scenario.expect(text)
                    _ <- manufacturer match {
                        case "Audi" | "BMW" | "Volvo" => for {
                            _ <- addCar(chat, manufacturer)
                        } yield ()
                        case _ => for {
                            _ <- Scenario.eval(chat.send("No such manufacturer"))
                        } yield ()
                    }
                } yield ()
                case "Apartments" => for {
                    - <- Scenario.eval(chat.send(content = "Choose district", keyboard = districtKeyboard))
                    district <- Scenario.expect(text)
                    _ <- district match {
                        case "Center" | "Agenskalns" | "Zolitude" => for {
                            _ <- addApartment(chat, district)
                        } yield ()
                        case _ => for {
                            _ <- Scenario.eval(chat.send("No such district"))
                        } yield ()
                    }
                } yield ()
                case "Computers" => for {
                    _ <- Scenario.eval(chat.send(content = "Choose CPU", keyboard = cpuKeyboard))
                    cpu <- Scenario.expect(text)
                    _ <- cpu match {
                        case "AMD" | "INTEL" => for {
                            _ <- addComputer(chat, cpu)
                        } yield ()
                        case _ => for {
                            _ <- Scenario.eval(chat.send("No such cpu"))
                        } yield ()
                    }
                } yield ()
            }
            _ <- Scenario.eval(chat.send("Product added successfully!"))
        } yield ()
    }

    def addCar[F[_]: TelegramClient](chat: Chat, manufacturer: String)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            car <- createCar(chat, manufacturer)
            _ <- Scenario.eval(service.insertProductToDb(car))
            _ <- Scenario.eval(chat.send("Car added successfully!"))
        } yield ()

    def addApartment[F[_]: TelegramClient](chat: Chat, district: String)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            apartment <- createApartment(chat, district)
            _ <- Scenario.eval(service.insertProductToDb(apartment))
            _ <- Scenario.eval(chat.send("Apartment added successfully!"))
        } yield ()

    def addComputer[F[_]: TelegramClient](chat: Chat, cpu: String)(implicit service: Service[F]): Scenario[F, Unit] =
        for {
            computer <- createComputer(chat, cpu)
            _ <- Scenario.eval(service.insertProductToDb(computer))
            _ <- Scenario.eval(chat.send("Computer added successfully!"))
        } yield ()

    def createCar[F[_]: TelegramClient](chat: Chat, manufacturer: String)(implicit service: Service[F]): Scenario[F, Car] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(chat.send(content = "Enter car year", keyboard = Keyboard.Remove))
            year <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter car mileage in kilometres"))
            mileage <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter price"))
            price <- Scenario.expect(text)
            imageKey <- getImage(chat)
            car <- Scenario.eval(service.createCar(detailedChat.id, price.toInt, manufacturer, year.toInt, mileage.toInt, imageKey))
        } yield car

    def createApartment[F[_]: TelegramClient](chat: Chat, district: String)(implicit service: Service[F]): Scenario[F, Apartment] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(chat.send(content = "Enter apartment area", keyboard = Keyboard.Remove))
            area <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter apartment amount of rooms"))
            rooms <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter apartment floor"))
            floor <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter price"))
            price <- Scenario.expect(text)
            imageKey <- getImage(chat)
            apartment <- Scenario.eval(service.createApartment(detailedChat.id, price.toInt, district, area.toInt, rooms.toInt, floor.toInt, imageKey))
        } yield apartment

    def createComputer[F[_]: TelegramClient](chat: Chat, cpu: String)(implicit service: Service[F]): Scenario[F, Computer] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            _ <- Scenario.eval(chat.send(content = "Enter computer RAM", keyboard = Keyboard.Remove))
            ram <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter computer video card"))
            videoCard <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter computer storage"))
            storage <- Scenario.expect(text)
            _ <- Scenario.eval(chat.send("Enter price"))
            price <- Scenario.expect(text)
            imageKey <- getImage(chat)
            computer <- Scenario.eval(service.createComputer(detailedChat.id, price.toInt, cpu, ram.toInt, videoCard, storage.toInt, imageKey))
        } yield computer

    def getImage[F[_]: TelegramClient](chat: Chat)(implicit service: Service[F]): Scenario[F, String] = {

        val defaultImageKey = "AgACAgQAAxkBAAIGmV_XrqVeAAFW7af6-x90xzQwpb5c2AACybUxG7rfwFLx-iHEWkU-H8FrriddAAMBAAMCAANtAAMYQAMAAR4E"

        for {
            _ <- Scenario.eval(chat.send(content = "Would you like to add images?", keyboard = KeyboardService.createKeyboard(false,"Yes","No")))
            answer <- Scenario.expect(text)
            image = answer match {
                case "Yes" => for {
                    _ <- Scenario.eval(chat.send(content = "Enter product image", keyboard = Keyboard.Remove))
                    image <- Scenario.expect(photo)
                } yield image.fileId
                case _ => for {
                    _ <- Scenario.eval(chat.send(content = "Will be used default image"))
                } yield defaultImageKey
            }
            imageKey <- image
        } yield imageKey
    }

    def add[F[_]: TelegramClient](implicit service: Service[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("add").chat)
            _ <- addChat(chat)
        } yield ()

    def echos[F[_]: TelegramClient]: Scenario[F, Unit] =
        for {
            msg <- Scenario.expect(any)
            _   <- Scenario.eval(echoBack(msg))
        } yield ()

    def echoBack[F[_]: TelegramClient](msg: TelegramMessage): F[_] = msg match {
        case textMessage: TextMessage => msg.chat.send(textMessage.text)
        case animationMessage: AnimationMessage => msg.chat.send(animationMessage.animation)
        case stickerMessage: StickerMessage => msg.chat.send(stickerMessage.sticker)
        case photo: PhotoMessage => msg.chat.send(PhotoContent(Existing(photo.photo.head.fileId),photo.photo.head.fileId))
        case _ => msg.chat.send("Sorry! I can't echos that back.")
    }
}