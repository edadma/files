package io.github.edadma.files

import io.github.edadma.cross_platform.*

import scala.language.postfixOps

@main def run(args: String*): Unit =
  val Seq(filesPath, output) = processArgs(args)

  if !readableFile(filesPath) then
    Console.err.println(s"'$filesPath' not readable")
    processExit(1)

  println(s"reading '$filesPath' ...")

  val files          = readFile(filesPath)
  var folder: String = null
  val buf            = new StringBuilder

  for lineRaw <- files.linesIterator do
    val line = lineRaw.trim

    if line.isEmpty then {} else if line.startsWith(";") then
      val comment = line drop 1 trim

      println(s"  ${if folder eq null then "" else "  "}comment \"$comment\"")
      buf ++=
        s"""
           |
           |+++++ COMMENT: $comment +++++
           |""".trim.stripMargin
    else if line.startsWith("#") then
      val section = line drop 1 trim

      println(s"  ${if folder eq null then "" else "  "}section \"$section\"")
      buf ++=
        s"""
           |
           |+++++ SECTION: $section +++++
           |""".trim.stripMargin
    else if line.startsWith("/") || line.startsWith("./") || line.startsWith("../") then
      val newFolder =
        if line.endsWith("/") then line dropRight 1
        else line

      println(s"  path: $newFolder")
      folder = line
      buf ++=
        s"""
           |
           |+++++ FOLDER: $folder +++++
           |""".trim.stripMargin
    else
      if folder eq null then
        Console.err.println(s"can't include '$line' because a path hasn't been set")
        processExit(1)

      println(s"    including '$line'")

      val file = readFile(s"$folder/$line")

      buf ++=
        s"""
           |
           |+++++ FILE: $line +++++
           |""".trim.stripMargin
      buf ++= file
      buf ++=
        s"""
           |+++++ END FILE +++++
           |""".trim.stripMargin
      if !file.endsWith("\n") then buf += '\n'
  end for

  println(s"writing '$output' ...")
  writeFile(output, buf.toString)
