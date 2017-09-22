package com.github.gvolpe.smartbackpacker

object model {

  type Country = String

  sealed trait VisaCategory extends Product with Serializable
  case object VisaNotRequired extends VisaCategory
  case object VisaRequired extends VisaCategory
  case object ElectronicVisa extends VisaCategory
  case object VisaOnArrival extends VisaCategory
  case object ElectronicVisaPlusOnArrival extends VisaCategory
  case object UnknownVisaCategory extends VisaCategory

  object VisaCategory {
    def parse(value: String) = value.toLowerCase match {
      case v: String =>
        if (v.contains("visa not required")) VisaNotRequired
        else if (v.contains("visa required")) VisaRequired
        else if ((v.contains("e-visa") || v.contains("evisa") || v.contains("electronic"))
          && v.contains("on arrival")) ElectronicVisaPlusOnArrival
        else if (v.contains("e-visa") || v.contains("evisa") || v.contains("electronic")) ElectronicVisa
        else if (v.contains("on arrival")) VisaOnArrival
        else UnknownVisaCategory
      case _ => UnknownVisaCategory
    }
  }

  implicit class VisaCategoryOps(value: String) {
    def asVisaCategory: VisaCategory = VisaCategory.parse(value)
  }

  implicit class DescriptionOps(value: String) {
    def asDescription: String = {
      if (value.isEmpty) "No information available"
      else value
    }
  }

  implicit class CountryOps(value: String) {
    def asCountry: String = {
      value.dropWhile(_.toInt == 160) // Remove whitespaces at the start
    }
  }

  case class VisaRequirements(country: Country, visaCategory: VisaCategory, description: String)

}