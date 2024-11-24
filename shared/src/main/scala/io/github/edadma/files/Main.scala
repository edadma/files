package io.github.edadma.files

import io.github.edadma.cross_platform.*

import scala.language.postfixOps

@main def run(args: String*): Unit =
  println(s"Hello world - $platform")

  val Seq(filesPath, output) = processArgs(args)

  if !readableFile(filesPath) then
    Console.err.println(s"'$filesPath' not readable")
    processExit(1)

  println(s"reading '$filesPath' ...")

  val files        = readFile(filesPath)
  var path: String = null
  val buf          = new StringBuilder

  for lineRaw <- files.linesIterator do
    val line = lineRaw.trim

    if line.isEmpty then {
      //
    } else if line.startsWith(";") then
      val comment = line drop 1 trim

      println(s"  ${if path eq null then "" else "  "}comment \"$comment\"")
      buf ++=
        s"""
           |
           |//
           |// $comment
           |//
           |
           |""".trim.stripMargin
    else if line.startsWith("/") || line.startsWith("./") || line.startsWith("../") then
      val newPath =
        if line.endsWith("/") then line dropRight 1
        else line

      println(s"  path: $newPath")
      path = line
    else
      if path eq null then
        Console.err.println(s"can't include '$line' because a path hasn't been set")
        processExit(1)

      println(s"    including '$line'")

      val file = readFile(s"$path/$line")

      buf ++= file
      if !file.endsWith("\n") then buf += '\n'
  end for

  if !writableFile(output) then
    Console.err.println("'$output' is not writable")
    processExit(1)

  println(s"writing '$output' ...")
  writeFile(output, buf.toString)
