package filters

import models.Product
import models.Product._
import models.computer._
import models.computer.Cpu._
import models.car._
import models.car.Manufacturer._
import models.apartment._
import models.apartment.District._
import canoe.api._
import canoe.syntax._
import canoe.api.models.Keyboard
import canoe.api.{Scenario, TelegramClient}
import canoe.models.Chat
import canoe.syntax.text
import main.scala.InputValidation._
import services.{DbService, KeyboardService}

object AttributeFilter {
    val lessMoreKeyboard: Keyboard.Reply = KeyboardService.createKeyboard(false, "Less", "More")

    def filterByPrice[F[_] : TelegramClient](chat: Chat, products: List[Product])(implicit service: DbService[F]): Scenario[F, List[Product]] = for {
        inputPrice <- providePrice(chat)
        _ <- Scenario.eval(chat.send(s"Display products with price LESS/MORE then ${inputPrice.price}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.price.price < inputPrice.price))
            case "More" => Scenario.pure[F](products.filter(product => product.price.price > inputPrice.price))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result


    def filterByManufacturer[F[_] : TelegramClient](chat: Chat, products: List[Car])(implicit service: DbService[F]): Scenario[F, List[Car]] = {
        val keyboard = KeyboardService.createKeyboard(true, "Audi", "Bmw", "Volvo")

        for {
            _ <- Scenario.eval(chat.send(s"Display only Audi/Bmw/Volvo", keyboard = keyboard))
            condition <- Scenario.expect(text)
            result <- condition match {
                case "Audi" => Scenario.pure[F](products.filter(product => product.manufacturer == Audi))
                case "Bmw" => Scenario.pure[F](products.filter(product => product.manufacturer == Bmw))
                case "Volvo" => Scenario.pure[F](products.filter(product => product.manufacturer == Volvo))
                case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
            }
        } yield result
    }

    def filterByYear[F[_] : TelegramClient](chat: Chat, products: List[Car])(implicit service: DbService[F]): Scenario[F, List[Car]] = for {
        inputYear <- provideYear(chat)
        _ <- Scenario.eval(chat.send(s"Display cars with year LESS/MORE then ${inputYear.year}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.year.year < inputYear.year))
            case "More" => Scenario.pure[F](products.filter(product => product.year.year > inputYear.year))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByMileage[F[_] : TelegramClient](chat: Chat, products: List[Car])(implicit service: DbService[F]): Scenario[F, List[Car]] = for {
        inputMileage <- provideMileage(chat)
        _ <- Scenario.eval(chat.send(s"Display cars with mileage LESS/MORE then ${inputMileage.mileage}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.mileage.mileage < inputMileage.mileage))
            case "More" => Scenario.pure[F](products.filter(product => product.mileage.mileage > inputMileage.mileage))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByDistrict[F[_] : TelegramClient](chat: Chat, products: List[Apartment])(implicit service: DbService[F]): Scenario[F, List[Apartment]] = {
        val keyboard = KeyboardService.createKeyboard(true, "Imanta", "Center", "Purvciems")

        for {
            _ <- Scenario.eval(chat.send(s"Display apartments located only in Imanta/Center/Purvciems", keyboard = keyboard))
            condition <- Scenario.expect(text)
            result <- condition match {
                case "Imanta" => Scenario.pure[F](products.filter(product => product.district == Imanta))
                case "Center" => Scenario.pure[F](products.filter(product => product.district == Center))
                case "Purvciems" => Scenario.pure[F](products.filter(product => product.district == Purvciems))
                case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
            }
        } yield result
    }

    def filterByArea[F[_] : TelegramClient](chat: Chat, products: List[Apartment])(implicit service: DbService[F]): Scenario[F, List[Apartment]] = for {
        inputArea <- provideArea(chat)
        _ <- Scenario.eval(chat.send(s"Display apartments with area LESS/MORE then ${inputArea.area}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.area.area < inputArea.area))
            case "More" => Scenario.pure[F](products.filter(product => product.area.area > inputArea.area))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByRoomAmount[F[_] : TelegramClient](chat: Chat, products: List[Apartment])(implicit service: DbService[F]): Scenario[F, List[Apartment]] = for {
        inputRoomAmount <- provideRoomAmount(chat)
        _ <- Scenario.eval(chat.send(s"Display apartments with amount of rooms LESS/MORE then ${inputRoomAmount.roomAmount}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.roomAmount.roomAmount < inputRoomAmount.roomAmount))
            case "More" => Scenario.pure[F](products.filter(product => product.roomAmount.roomAmount > inputRoomAmount.roomAmount))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByFloor[F[_] : TelegramClient](chat: Chat, products: List[Apartment])(implicit service: DbService[F]): Scenario[F, List[Apartment]] = for {
        inputFloor <- provideFloor(chat)
        _ <- Scenario.eval(chat.send(s"Display apartments with floor LESS/MORE then ${inputFloor.floor}", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.floor.floor < inputFloor.floor))
            case "More" => Scenario.pure[F](products.filter(product => product.floor.floor > inputFloor.floor))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByCpu[F[_] : TelegramClient](chat: Chat, products: List[Computer])(implicit service: DbService[F]): Scenario[F, List[Computer]] = {
        val keyboard = KeyboardService.createKeyboard(true, "Amd", "Intel")

        for {
            _ <- Scenario.eval(chat.send(s"Display computers only with Amd/Intel CPU", keyboard = keyboard))
            condition <- Scenario.expect(text)
            result <- condition match {
                case "Amd" => Scenario.pure[F](products.filter(product => product.cpu == Amd))
                case "Intel" => Scenario.pure[F](products.filter(product => product.cpu == Intel))
                case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
            }
        } yield result
    }

    def filterByRam[F[_] : TelegramClient](chat: Chat, products: List[Computer])(implicit service: DbService[F]): Scenario[F, List[Computer]] = for {
        inputRam <- provideRam(chat)
        _ <- Scenario.eval(chat.send(s"Display computers with RAM LESS/MORE then ${inputRam.ram} gb", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.ram.ram < inputRam.ram))
            case "More" => Scenario.pure[F](products.filter(product => product.ram.ram > inputRam.ram))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result

    def filterByStorage[F[_] : TelegramClient](chat: Chat, products: List[Computer])(implicit service: DbService[F]): Scenario[F, List[Computer]] = for {
        inputStorage <- provideStorage(chat)
        _ <- Scenario.eval(chat.send(s"Display computers with storage LESS/MORE then ${inputStorage.storage} gb", keyboard = lessMoreKeyboard))
        condition <- Scenario.expect(text)
        result <- condition match {
            case "Less" => Scenario.pure[F](products.filter(product => product.storage.storage < inputStorage.storage))
            case "More" => Scenario.pure[F](products.filter(product => product.storage.storage > inputStorage.storage))
            case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](products)
        }
    } yield result
}
