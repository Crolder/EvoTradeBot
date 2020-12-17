package models.base

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DescriptionSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Description" should "convert Description object to string" in {
        forAll(alphaUpperStr) { v: String =>
            Description.toString(Description(v)) shouldBe v
        }
    }

    it should "convert string to Description object" in {
        forAll(alphaUpperStr) { v: String =>
            Description.fromString(v) shouldBe Description(v)
        }
    }
}
