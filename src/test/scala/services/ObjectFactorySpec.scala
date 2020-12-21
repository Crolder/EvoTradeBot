package services

import cats.effect.IO
import models.Product._
import models.User
import models.apartment.District._
import models.apartment._
import models.computer.Cpu._
import models.computer._
import models.base._
import models.car.Manufacturer._
import models.car._
import models.user.PhoneNumber
import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.ObjectFactory.objectFactory._

import java.util.UUID

class ObjectFactorySpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks {
    "createUser" should "provide correct User object inside IO" in {
        val id = UserId(123)
        val firstname = "TestName"
        val lastname = "TestLastname"
        val username = "TestUsername"
        val phoneNumber = PhoneNumber.of("=37128877539")

        val manualUser = IO { User(id, firstname, lastname, username, phoneNumber) }

        val functionUser = createUser(id, firstname, lastname, username, phoneNumber)

        functionUser.unsafeRunSync() shouldBe manualUser.unsafeRunSync()
    }

    "createCar" should "provide correct Car object inside IO" in {
        val userId = UserId(123)
        val price = Price.of("123.56").get
        val manufacturer = Audi
        val year = Year.of("2007").get
        val mileage = Mileage.of("70000").get
        val description = Description("Test description")
        val imageKey = ImageKey("testKey")

        val functionCar = createCar(userId, price, manufacturer, year, mileage,description, imageKey)

        val manualCar = IO { Car(ProductId(UUID.randomUUID()), userId, price, manufacturer, year, mileage,description, imageKey) }

        functionCar.unsafeRunSync().userId shouldBe manualCar.unsafeRunSync().userId
        functionCar.unsafeRunSync().price shouldBe manualCar.unsafeRunSync().price
        functionCar.unsafeRunSync().manufacturer shouldBe manualCar.unsafeRunSync().manufacturer
        functionCar.unsafeRunSync().year shouldBe manualCar.unsafeRunSync().year
        functionCar.unsafeRunSync().mileage shouldBe manualCar.unsafeRunSync().mileage
        functionCar.unsafeRunSync().descripion shouldBe manualCar.unsafeRunSync().descripion
        functionCar.unsafeRunSync().imageKey shouldBe manualCar.unsafeRunSync().imageKey
    }

    "createApartment" should "provide correct Apartment object inside IO" in {
        val userId = UserId(123)
        val price = Price.of("123.56").get
        val district = Center
        val area = Area.of("60").get
        val roomAmount = RoomAmount.of("2").get
        val floor = Floor.of("7").get
        val description = Description("Test description")
        val imageKey = ImageKey("testKey")

        val functionApartment = createApartment(userId, price, district, area, roomAmount, floor, description, imageKey)

        val manualApartment = IO { Apartment(ProductId(UUID.randomUUID()), userId, price, district, area, roomAmount, floor, description, imageKey) }

        functionApartment.unsafeRunSync().userId shouldBe manualApartment.unsafeRunSync().userId
        functionApartment.unsafeRunSync().price shouldBe manualApartment.unsafeRunSync().price
        functionApartment.unsafeRunSync().district shouldBe manualApartment.unsafeRunSync().district
        functionApartment.unsafeRunSync().area shouldBe manualApartment.unsafeRunSync().area
        functionApartment.unsafeRunSync().roomAmount shouldBe manualApartment.unsafeRunSync().roomAmount
        functionApartment.unsafeRunSync().floor shouldBe manualApartment.unsafeRunSync().floor
        functionApartment.unsafeRunSync().descripion shouldBe manualApartment.unsafeRunSync().descripion
        functionApartment.unsafeRunSync().imageKey shouldBe manualApartment.unsafeRunSync().imageKey
    }

    "createComputer" should "provide correct Computer object inside IO" in {
        val userId = UserId(123)
        val price = Price.of("123.56").get
        val cpu = Amd
        val ram = Ram.of("16").get
        val videoCard = VideoCard("Test video card")
        val storage = Storage.of("1000").get
        val description = Description("Test description")
        val imageKey = ImageKey("testKey")

        val functionComputer = createComputer(userId, price, cpu, ram, videoCard, storage, description, imageKey)

        val manualComputer = IO { Computer(ProductId(UUID.randomUUID()), userId, price, cpu, ram, videoCard, storage, description, imageKey) }

        functionComputer.unsafeRunSync().userId shouldBe manualComputer.unsafeRunSync().userId
        functionComputer.unsafeRunSync().price shouldBe manualComputer.unsafeRunSync().price
        functionComputer.unsafeRunSync().cpu shouldBe manualComputer.unsafeRunSync().cpu
        functionComputer.unsafeRunSync().ram shouldBe manualComputer.unsafeRunSync().ram
        functionComputer.unsafeRunSync().videoCard shouldBe manualComputer.unsafeRunSync().videoCard
        functionComputer.unsafeRunSync().storage shouldBe manualComputer.unsafeRunSync().storage
        functionComputer.unsafeRunSync().descripion shouldBe manualComputer.unsafeRunSync().descripion
        functionComputer.unsafeRunSync().imageKey shouldBe manualComputer.unsafeRunSync().imageKey
    }
}
