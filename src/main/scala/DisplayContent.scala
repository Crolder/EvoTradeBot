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
import filters.Filter._
import main.scala.TelegramBot._

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
        val filterKeyboard = KeyboardService.createKeyboard(true, "Yes", "No")

        for {
            _ <- Scenario.eval(chat.send(s"Which products", keyboard = KeyboardService.createKeyboard(true, "All", "Cars", "Apartments", "Computers", "My", "Cancel")))
            m <- Scenario.expect(text)
            _ <- m match {
                case "All" => for {
                    products <- Scenario.eval(service.getProducts)
                    _ <- Scenario.eval(chat.send("Would you like to filter products by price?", keyboard = filterKeyboard))
                    answer <- Scenario.expect(text)
                    _ <- answer match {
                        case "Yes" => for {
                            filteredProducts <- filter(chat,products)
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
                            _ <- displayProduct(chat, filteredProducts, 0)
                        } yield ()
                        case "No" => for {
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                            _ <- displayProduct(chat, products, 0)
                        } yield ()
                    }
                } yield ()
                case "Cars" => for {
                    products <- Scenario.eval(service.getCars)
                    _ <- Scenario.eval(chat.send("Would you like to filter cars?", keyboard = filterKeyboard))
                    answer <- Scenario.expect(text)
                    _ <- answer match {
                        case "Yes" => for {
                            filteredProducts <- filter(chat,products)
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
                            _ <- displayProduct(chat, filteredProducts, 0)
                        } yield ()
                        case "No" => for {
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                            _ <- displayProduct(chat, products, 0)
                        } yield ()
                    }
                } yield ()
                case "Apartments" => for {
                    products <- Scenario.eval(service.getApartments)
                    _ <- Scenario.eval(chat.send("Would you like to filter apartments?", keyboard = filterKeyboard))
                    answer <- Scenario.expect(text)
                    _ <- answer match {
                        case "Yes" => for {
                            filteredProducts <- filter(chat,products)
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
                            _ <- displayProduct(chat, filteredProducts, 0)
                        } yield ()
                        case "No" => for {
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                            _ <- displayProduct(chat, products, 0)
                        } yield ()
                    }
                } yield ()
                case "Computers" => for {
                    products <- Scenario.eval(service.getComputers)
                    _ <- Scenario.eval(chat.send("Would you like to filter computers?", keyboard = filterKeyboard))
                    answer <- Scenario.expect(text)
                    _ <- answer match {
                        case "Yes" => for {
                            filteredProducts <- filter(chat,products)
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${filteredProducts.size}"))
                            _ <- displayProduct(chat, filteredProducts, 0)
                        } yield ()
                        case "No" => for {
                            _ <- Scenario.eval(chat.send(s"Amount of products: ${products.size}"))
                            _ <- displayProduct(chat, products, 0)
                        } yield ()
                    }
                } yield ()
                case "My" => for {
                    products <- Scenario.eval(service.getProductsByUserId(UserId(chat.id)))
                    _ <- if(products.nonEmpty) displayProduct(chat, products, 0) else for {
                        _ <- Scenario.eval(chat.send("You have no products to display"))
                        _ <- displayProductOptions(chat)
                    } yield ()
                } yield ()
                case "Cancel" => displayAction(chat)
                case _ => displayAction(chat)
            }
        } yield ()
    }

    def displayProduct[F[_] : TelegramClient](chat: Chat, list: List[Product], position: Int)(implicit service: DbService[F], objectFactory: ObjectFactory[F]): Scenario[F, Unit] = {
        val listSize = list.size - 1

        def expectation(chat: Chat, list: List[Product], position: Int)(implicit service: DbService[F]): Scenario[F, Unit] = for {
            m <- Scenario.expect(text)
            _ <- m match {
                case "Next" => if (position + 1 <= listSize) displayProduct(chat, list, position + 1) else displayProduct(chat, list, 0)
                case "Previous" => if (position - 1 >= 0) displayProduct(chat, list, position - 1) else displayProduct(chat, list, listSize)
                case "Cancel" => displayAction(chat)
                case "Delete" => if(chat.id == list(position).userId.id) deleteProduct(chat, list, position) else Scenario.eval(chat.send("You can delete only yours products")) >> expectation(chat, list, position)
                case "Get Contact" => displayContact(chat, list(position).userId) >> expectation(chat, list, position)
                case _ => displayAction(chat)
            }
        } yield ()

        if(list.isEmpty){
            Scenario.eval(chat.send("There are no products to display")) >> displayAction(chat)
        } else {
            for {
                _ <- sendProduct(chat, list(position))
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
}
