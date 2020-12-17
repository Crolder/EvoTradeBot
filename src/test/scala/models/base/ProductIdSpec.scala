package models.base

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.util.UUID

class ProductIdSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "ProductId" should "convert ProductId object to string" in {
        forAll(uuid) { v: UUID =>
            ProductId.toString(ProductId(v)) shouldBe v.toString
        }
    }

    it should "convert string to ProductId object" in {
        forAll(uuid) { v: UUID =>
            ProductId.fromString(v.toString) shouldBe ProductId(v)
        }
    }
}
