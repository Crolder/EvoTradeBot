package models.car

import models.car.Manufacturer._
import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ManufacturerSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks {
    "Manufacturer" should "convert Manufacturer object to integer" in {
        forAll(Gen.oneOf(Audi, Bmw, Volvo, Unknown)) { v: Manufacturer =>
          v match {
              case Audi => Manufacturer.toString(v) shouldBe "Audi"
              case Bmw => Manufacturer.toString(v) shouldBe "Bmw"
              case Volvo => Manufacturer.toString(v) shouldBe "Volvo"
              case _ => Manufacturer.toString(v) shouldBe "Unknown"
          }
        }
    }

    it should "convert string to Manufacturer object" in {
        forAll(Gen.oneOf("Audi", "Bmw", "Volvo", "Unknown")) { v: String =>
            v match {
                case "Audi" => Manufacturer.fromString(v) shouldBe Audi
                case "Bmw" => Manufacturer.fromString(v) shouldBe Bmw
                case "Volvo" => Manufacturer.fromString(v) shouldBe Volvo
                case "Unknown" => Manufacturer.fromString(v) shouldBe Unknown
            }
        }
    }
}
