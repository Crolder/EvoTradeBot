package models.apartment

import models.apartment.District._
import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DistrictSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks {
    "District" should "convert District object to integer" in {
        forAll(Gen.oneOf(Imanta, Center, Purvciems, Unknown)) { v: District =>
          v match {
              case Imanta => District.toString(v) shouldBe "Imanta"
              case Center => District.toString(v) shouldBe "Center"
              case Purvciems => District.toString(v) shouldBe "Purvciems"
              case _ => District.toString(v) shouldBe "Unknown"
          }
        }
    }

    it should "convert string to District object" in {
        forAll(Gen.oneOf("Imanta", "Center", "Purvciems", "Unknown")) { v: String =>
            v match {
                case "Imanta" => District.fromString(v) shouldBe Imanta
                case "Center" => District.fromString(v) shouldBe Center
                case "Purvciems" => District.fromString(v) shouldBe Purvciems
                case "Unknown" => District.fromString(v) shouldBe Unknown
            }
        }
    }
}
