package com.github.gvolpe.smartbackpacker.scraper.parser

import cats.Functor
import cats.effect.Sync
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import com.github.gvolpe.smartbackpacker.scraper.model.VisaRestrictionsRanking
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.util.{Failure, Success, Try}

class VisaRestrictionsIndexParser[F[_] : Sync] extends AbstractVisaRestrictionsIndexParser[F] {

  override val htmlDocument: F[Document] = Sync[F].delay {
    val browser = new JsoupBrowser()
    browser.get("https://en.wikipedia.org/wiki/Travel_visa")
  }

}

abstract class AbstractVisaRestrictionsIndexParser[F[_] : Functor] {

  val CountriesOnIndex: Int = 104 // Number of countries that are part of the ranking

  def htmlDocument: F[Document]

  def parse: F[List[(CountryCode, VisaRestrictionsIndex)]] =
    htmlDocument.map { doc =>
      val wikiTable: List[Element] = doc >> elementList(".sortable")
      val result = wikiTable.flatMap(e => (e >> extractor(".collapsible td", wikiTableExtractor)).toList)

      val ranking = result.grouped(3).take(CountriesOnIndex).map {
        case List(Rank(r), Countries(c), PlacesCount(pc)) => VisaRestrictionsRanking(r, c, pc)
      }.toList

      for {
        code    <- ScraperConfiguration.countriesCode()
        names   = ScraperConfiguration.countryNames(code)
        index   <- ranking
        country <- index.countries
        if names.contains(country)
      } yield {
        val visaIndex = VisaRestrictionsIndex(
          rank = new Ranking(index.rank),
          count = new Count(index.count),
          sharing = new Sharing(index.countries.size)
        )
        (code, visaIndex)
      }
    }

  private val wikiTableExtractor: HtmlExtractor[Element, Iterable[VisaRestrictionsIndexValues]] = _.map { e =>
    Try(e.text.toInt) match {
      case Success(n) =>
        if (e.innerHtml.contains("#199502")) PlacesCount(n)
        else Rank(n)
      case Failure(_) =>
        val countries = e.text.split(",").toList.map(_.trim.noWhiteSpaces)
        Countries(countries)
    }
  }

}
