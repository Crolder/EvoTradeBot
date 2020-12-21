package filters

import canoe.models.Chat
import models.Product
import models.Product._
import canoe.api._
import canoe.api.models.Keyboard
import canoe.syntax._
import AttributeFilter._
import services.{DbService, KeyboardService}

trait Filter[A, B] {
    def filter[F[_] : TelegramClient](chat: Chat, list: List[A])(implicit service: DbService[F]): Scenario[F, List[B]]
}
object Filter {
    def apply[A, B](implicit filter: Filter[A, B]): Filter[A, B] = filter

    def filter[A, B, F[_] : TelegramClient](chat: Chat, list: List[A])(implicit filter: Filter[A, B], service: DbService[F]): Scenario[F, List[B]] = filter.filter(chat,list)

    val carKeyboard: Keyboard.Reply = KeyboardService.createKeyboard(true, "Price", "Manufacturer", "Year", "Mileage", "Cancel")
    val computerKeyboard: Keyboard.Reply = KeyboardService.createKeyboard(true, "Price", "Cpu", "Ram", "Storage", "Cancel")
    val apartmentKeyboard: Keyboard.Reply = KeyboardService.createKeyboard(true, "Price","District", "Area", "Amount of rooms", "Floor", "Cancel")

    implicit val computerFilter: Filter[Computer, Product] =
        new Filter[Computer, Product] {
            def filter[F[_] : TelegramClient](chat: Chat, list: List[Computer])(implicit service: DbService[F]): Scenario[F, List[Product]] = for {
                _ <- Scenario.eval(chat.send("Choose filter", keyboard = computerKeyboard))
                filter   <- Scenario.expect(text)
                result <- filter match {
                    case "Price" => filterByPrice(chat,list)
                    case "Cpu" => filterByCpu(chat,list)
                    case "Ram" => filterByRam(chat,list)
                    case "Storage" => filterByStorage(chat,list)
                    case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](list)
                }
            } yield result
        }

    implicit val carFilter: Filter[Car, Product] =
        new Filter[Car, Product] {
            def filter[F[_] : TelegramClient](chat: Chat, cars: List[Car])(implicit service: DbService[F]): Scenario[F, List[Product]] =
                for {
                    _ <- Scenario.eval(chat.send("Choose filter", keyboard = carKeyboard))
                    filter   <- Scenario.expect(text)
                    result <- filter match {
                        case "Price" => filterByPrice(chat,cars)
                        case "Manufacturer" => filterByManufacturer(chat,cars)
                        case "Year" => filterByYear(chat,cars)
                        case "Mileage" => filterByYear(chat,cars)
                        case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](cars)
                    }
                } yield result
        }

    implicit val apartmentFilter: Filter[Apartment, Product] =
        new Filter[Apartment, Product] {
            def filter[F[_] : TelegramClient](chat: Chat, apartments: List[Apartment])(implicit service: DbService[F]): Scenario[F, List[Product]] =
                for {
                    _ <- Scenario.eval(chat.send("Choose filter", keyboard = apartmentKeyboard))
                    filter   <- Scenario.expect(text)
                    result <- filter match {
                        case "Price" => filterByPrice(chat,apartments)
                        case "District" => filterByDistrict(chat,apartments)
                        case "Area" => filterByArea(chat,apartments)
                        case "Amount of rooms" => filterByRoomAmount(chat,apartments)
                        case "Floor" => filterByFloor(chat,apartments)
                        case _ => Scenario.eval(chat.send("No such filter. Product will be shown without filter")) >> Scenario.pure[F](apartments)
                    }
                } yield result
        }

    implicit val productFilter: Filter[Product, Product] =
        new Filter[Product, Product] {
            def filter[F[_] : TelegramClient](chat: Chat, products: List[Product])(implicit service: DbService[F]): Scenario[F, List[Product]] =
                filterByPrice(chat, products)
        }
}
