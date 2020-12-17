package models.car

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class MileageSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Mileage" should "allow being created with mileage between 0 and 1000000" in {
        forAll(choose(min = 0, max = 1000000)) { v: Int =>
            Mileage.of(v.toString).get.mileage shouldBe v
        }
    }

    it should "forbid being created with mileage outside [ 0 - 1000000 ] range" in {
        forAll { v: Int =>
            whenever(v < 0 || v > 1000000) {
                Mileage.of(v.toString) shouldBe None
            }
        }
    }

    it should "convert Mileage object to integer" in {
        forAll(choose(min = 0, max = 1000000)) { v: Int =>
            Mileage.toInt(Mileage.of(v.toString).get) shouldBe v
        }
    }

    it should "convert integer to Mileage object" in {
        forAll(choose(min = 0, max = 1000000)) { v: Int =>
            Mileage.fromInt(v) shouldBe Mileage.of(v.toString).get
        }
    }
}