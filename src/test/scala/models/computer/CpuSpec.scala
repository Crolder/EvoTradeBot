package models.computer

import models.computer.Cpu._
import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CpuSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks {
    "Cpu" should "convert Cpu object to integer" in {
        forAll(Gen.oneOf(Amd, Intel, Unknown)) { v: Cpu =>
            v match {
                case Amd => Cpu.toString(v) shouldBe "Amd"
                case Intel => Cpu.toString(v) shouldBe "Intel"
                case _ => Cpu.toString(v) shouldBe "Unknown"
            }
        }
    }

    it should "convert string to Cpu object" in {
        forAll(Gen.oneOf("Amd", "Intel", "Unknown")) { v: String =>
            v match {
                case "Amd" => Cpu.fromString(v) shouldBe Amd
                case "Intel" => Cpu.fromString(v) shouldBe Intel
                case "Unknown" => Cpu.fromString(v) shouldBe Unknown
            }
        }
    }
}
