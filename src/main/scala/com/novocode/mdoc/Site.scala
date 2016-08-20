package com.novocode.mdoc

import java.net.URI

import com.typesafe.config.Config
import org.commonmark.node._

class Site(val pages: Vector[Page], val toc: Vector[TocEntry]) {
  private[this] val pageMap: Map[String, Page] = pages.map(p => (p.uri.getPath, p)).toMap

  def getPageFor(uri: URI): Option[Page] =
    if(uri.getScheme == Util.siteRootURI.getScheme) pageMap.get(uri.getPath)
    else None
}

class Page(val sourceFileURI: Option[URI], val uri: URI, val suffix: String, val doc: Node, val config: Config,
           val section: PageSection, val extensions: Extensions) {
  override def toString: String = s"Page($uri)"

  def uriWithSuffix(ext: String): URI = Util.replaceSuffix(uri, suffix, ext)
}

sealed abstract class Section {
  def children: Vector[Section]
  def level: Int
  final def findFirstHeading: Option[HeadingSection] = {
    val it = allHeadings
    if(it.hasNext) Some(it.next) else None
  }
  def allHeadings: Iterator[HeadingSection] =
    children.iterator.flatMap(_.allHeadings)
}

final case class HeadingSection(id: String, level: Int, title: String, children: Vector[Section])(val heading: Heading) extends Section {
  override def allHeadings: Iterator[HeadingSection] =
    Iterator(this) ++ super.allHeadings
}

final case class UntitledSection(level: Int, children: Vector[Section]) extends Section

final case class PageSection(title: Option[String], children: Vector[Section]) extends Section {
  def level = 0
}

case class TocEntry(val page: Page, val title: String)
