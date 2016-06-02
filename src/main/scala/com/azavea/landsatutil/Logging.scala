package com.azavea.landsatutil

import com.typesafe.scalalogging._
import com.typesafe.scalalogging.slf4j._
import org.slf4j.LoggerFactory

trait Logging {
  @transient lazy val logger = Logger(LoggerFactory.getLogger(getClass.getName))
}