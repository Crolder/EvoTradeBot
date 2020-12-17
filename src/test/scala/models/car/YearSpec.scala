package models.car

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class YearSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Year" should "allow being created with year between 1900 and 2021" in {
        forAll(choose(min = 1900, max = 2021)) { v: Int =>
            Year.of(v.toString).get.year shouldBe v
        }
    }

    it should "forbid being created with years outside [ 1900 - 2021 ] range" in {
        forAll { v: Int =>
            whenever(v < 1900 || v > 2021) {
                Year.of(v.toString) shouldBe None
            }
        }
    }

    it should "convert Year object to integer" in {
        forAll(choose(min = 1900, max = 2021)) { v: Int =>
            Year.toInt(Year.of(v.toString).get) shouldBe v
        }
    }

    it should "convert integer to Year object" in {
        forAll(choose(min = 1900, max = 2021)) { v: Int =>
            Year.fromInt(v) shouldBe Year.of(v.toString).get
        }
    }
}