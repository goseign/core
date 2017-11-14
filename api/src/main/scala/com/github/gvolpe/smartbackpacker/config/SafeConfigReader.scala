package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class SafeConfigReader(config: Config) {

  private val log = LoggerFactory.getLogger(getClass)

  private def safeRead[A](f: String => A)(key: String): Option[A] =
    Try(f(key)) match {
      case Failure(error) =>
        log.warn(s"Key $key not found: ${error.getMessage}.")
        None
      case Success(value) =>
        Some(value)
    }

  def string(key: String): Option[String] = safeRead[String](config.getString)(key)

  def list(key: String): List[String] = {
    import scala.collection.JavaConverters._
    safeRead[java.util.List[String]](config.getStringList)(key).toList.flatMap(_.asScala)
  }

}