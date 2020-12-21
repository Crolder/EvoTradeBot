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
import canoe.syntax.{command, _}
import cats.effect.{ExitCode, IO, IOApp}
import filters.Filter
import fs2.Stream
import services.{DbService, KeyboardService, ObjectFactory}
import services.ObjectFactory.objectFactory
import services.DbService.dbService
import main.scala.InputValidation._
import filters.AttributeFilter._
import main.scala.Templates._
import main.scala.DisplayContent._

/**
  * Example using compositional property of scenarios
  * by combining them into more complex registration process
  */
object TelegramBot extends IOApp {
    def run(args: List[String]): IO[ExitCode] = {
        Stream
          .resource(TelegramClient.global[IO](token))
          .flatMap { implicit client =>
              Bot.polling[IO].follow(start, add, products, actions, cancel)
          }
          .compile.drain.as(ExitCode.Success)
    }

//    def filterProducts[A <: Product, F[_] : TelegramClient](chat: Chat, products: List[A])(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
//        val filterKeyboard = KeyboardService.createKeyboard(true, "Yes", "No")
//
//        for {
//            _ <- Scenario.eval(chat.send("Would you like to filter computers?", keyboard = filterKeyboard))
//            answer <- Scenario.expect(text)
//            _ <- answer match {
//                case "Yes" => for {
//                    filteredProducts <- filter(chat,products)
//                    _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
//                    _ <- displayProduct(chat, filteredProducts, 0)
//                } yield ()
//                case "No" => for {
//                    _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
//                    _ <- displayProduct(chat, products, 0)
//                } yield ()
//            }
//        } yield ()
//    }


    def sendProduct[F[_] : TelegramClient](chat: Chat, product: Product): Scenario[F, Unit] = {

        val myProduct = chat.id == product.userId.id

        val keyboard = if(myProduct){
            KeyboardService.createKeyboard(true, "Previous", "Next", "Get Contact", "Delete", "Cancel")
        } else {
            KeyboardService.createKeyboard(true, "Previous", "Next", "Get Contact", "Cancel")
        }

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

    def displayAction[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val keyboard = KeyboardService.createKeyboard(true, "Add product", "Products", "Personal information")

        for {
            _ <- Scenario.eval(chat.send("Choose action", keyboard = keyboard))
            m <- Scenario.expect(text)
            _ <- m match {
                case "Products" => for {
                    _ <- displayProductOptions(chat)
                } yield ()
                case "Add product" => for {
                    _ <- addProductOptions(chat)
                    _ <- displayAction(chat)
                } yield ()
                case "Personal information" => for {
                    _ <- displayPersonalInformation(chat)
                    _ <- displayAction(chat)
                } yield()
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def actions[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("actions").chat)
            _ <- displayAction(chat).stopOn(command("actions").isDefinedAt)
        } yield ()
    }

    def cancel[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("cancel").chat)
            _ <- displayAction(chat).stopOn(command("cancel").isDefinedAt)
        } yield ()
    }

    def productsChat[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        for {
            products <- Scenario.eval(service.getProducts)
            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
            _ <- displayProduct(chat, products, 0)
            _ <- displayAction(chat)
        } yield ()
    }

    def products[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        for {
            chat <- Scenario.expect(command("products").chat)
            - <- productsChat(chat)
        } yield ()
    }

    def myProducts[F[_] : TelegramClient](implicit service: DbService[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("my").chat)
            detailedChat <- Scenario.eval(chat.details)
            products <- Scenario.eval(service.getProductsByUserId(UserId(detailedChat.id)))
            _ <- Scenario.eval(chat.send(s"My Products: $products"))
        } yield ()

    def start[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("start").chat)
            detailedChat <- Scenario.eval(chat.details)
            user <- Scenario.eval(service.getUserById(UserId(detailedChat.id)))
            userExists <- Scenario.pure[F](user.isDefined)
            _ <- if(!userExists) {
                for {
                    user <- Scenario.eval(objectFactory.createUser(UserId(detailedChat.id), detailedChat.firstName.getOrElse(""), detailedChat.lastName.getOrElse(""), detailedChat.username.getOrElse("")))
                    _ <- Scenario.eval(service.insertUserToDb(user))
                    _ <- Scenario.eval(chat.send(s"You was successfully registered!"))
                } yield ()
            } else Scenario.eval(chat.send(s"Welcome ${detailedChat.firstName.getOrElse("dear friend!")}"))
            _ <- displayAction(chat).stopOn(command("start").isDefinedAt)
        } yield ()

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

    def addProductOptions[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = for {
        detailedChat <- Scenario.eval(chat.details)
        user <- Scenario.eval(service.getUserById(UserId(detailedChat.id)))
        hasPhoneNumber <- Scenario.pure[F](user.get.phoneNumber.isDefined)
        _ <- if (hasPhoneNumber) addProduct(chat) else addPhoneNumber(chat)
    } yield ()

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

    def add[F[_] : TelegramClient](implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            chat <- Scenario.expect(command("add").chat)
            _ <- addProductOptions(chat)
        } yield ()
}