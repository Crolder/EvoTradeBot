package utils

import canoe.api.{Scenario, TelegramClient, chatApi}
import main.scala.BotActions._
import Templates.{apartmentTemplate, carTemplate, computerTemplate, contactTemplate, userTemplate}
import models.Product.{Apartment, Car, Computer}
import models.base.UserId
import canoe.models.Chat
import models.Product
import canoe.syntax._
import filters.Filter._
import services.{DbService, KeyboardService, ObjectFactory}

object DisplayContent {
    def displayPersonalInformation[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] =
        for {
            detailedChat <- Scenario.eval(chat.details)
            user <- Scenario.eval(service.getUserById(UserId(detailedChat.id)))
            products <- Scenario.eval(service.getProductsByUserId(UserId(detailedChat.id)))
            _ <- Scenario.eval(chat.send(userTemplate(detailedChat, user.get, products)))
        } yield ()

    def displayContact[F[_] : TelegramClient](chat: Chat, id: UserId)(implicit service: DbService[F]): Scenario[F, Unit] =
        for {
            user <- Scenario.eval(service.getUserById(id))
            _ <- user match {
                case None => Scenario.eval(chat.send("There is no users with this id"))
                case Some(user) => Scenario.eval(chat.send(contactTemplate(user, chat)))
            }
        } yield ()

    def displayProductOptions[F[_] : TelegramClient](chat: Chat)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        for {
            _ <- Scenario.eval(chat.send(s"Which products", keyboard = KeyboardService.createKeyboard(true, "All", "Cars", "Apartments", "Computers", "My", "Cancel")))
            m <- Scenario.expect(text)
            _ <- m match {
                case "All" => for {
                    products <- Scenario.eval(service.getProducts)
                    _ <- filterProducts[Product, F](chat, products)
                } yield ()
                case "Cars" => for {
                    products <- Scenario.eval(service.getCars)
                    _ <- filterProducts[Car, F](chat, products)
                } yield ()
                case "Apartments" => for {
                    products <- Scenario.eval(service.getApartments)
                    _ <- filterProducts[Apartment, F](chat, products)
                } yield ()
                case "Computers" => for {
                    products <- Scenario.eval(service.getComputers)
                    _ <- filterProducts[Computer, F](chat, products)
                } yield ()
                case "My" => for {
                    products <- Scenario.eval(service.getProductsByUserId(UserId(chat.id)))
                    _ <- if (products.nonEmpty) for {
                            _ <- Scenario.eval(chat.send(s"Amount of my products: ${products.size}"))
                            _ <- displayProduct(chat, products, 0)
                    } yield () else for {
                        _ <- Scenario.eval(chat.send("You have no products to display"))
                        _ <- displayProductOptions(chat)
                    } yield ()
                } yield ()
                case "Cancel" => displayAction(chat)
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def displayProductCard[F[_] : TelegramClient](chat: Chat, product: Product): Scenario[F, Unit] = {
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

    def displayProduct[F[_] : TelegramClient](chat: Chat, list: List[Product], position: Int)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val listSize = list.size - 1

        def expectation(chat: Chat, list: List[Product], position: Int)(implicit service: DbService[F]): Scenario[F, Unit] = for {
            m <- Scenario.expect(text)
            _ <- m match {
                case "Next" => if (position + 1 <= listSize) displayProduct(chat, list, position + 1) else displayProduct(chat, list, 0)
                case "Previous" => if (position - 1 >= 0) displayProduct(chat, list, position - 1) else displayProduct(chat, list, listSize)
                case "Cancel" => displayAction(chat)
                case "Delete" => if (chat.id == list(position).userId.id) deleteProduct(chat, list, position) else Scenario.eval(chat.send("You can delete only yours products")) >> expectation(chat, list, position)
                case "Get Contact" => displayContact(chat, list(position).userId) >> expectation(chat, list, position)
                case _ => displayAction(chat)
            }
        } yield ()

        if (list.isEmpty) {
            Scenario.eval(chat.send("There are no products to display")) >> displayAction(chat)
        } else {
            for {
                _ <- displayProductCard(chat, list(position))
                _ <- expectation(chat, list, position)
            } yield ()
        }
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
                    _ <- canAddProduct(chat)
                    _ <- displayAction(chat)
                } yield ()
                case "Personal information" => for {
                    _ <- displayPersonalInformation(chat)
                    _ <- displayAction(chat)
                } yield ()
                case _ => displayAction(chat)
            }
        } yield ()
    }
}
