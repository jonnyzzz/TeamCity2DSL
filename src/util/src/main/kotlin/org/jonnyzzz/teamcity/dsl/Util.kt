package org.jonnyzzz.teamcity.dsl

import java.io.*
import kotlin.reflect.KMutableProperty1

inline fun <R, T : Any> R.setIfNull(p: KMutableProperty1<R, T?>, value: () -> T): T {
  val v = p.get(this)
  if (v == null) {
    val newValue = value()
    p.set(this, newValue)
    return newValue
  } else {
    return v
  }
}

operator fun File?.div(s : String) : File = File(this, s)
operator fun File.plus(s : String) : File = File(this.path + s)

fun File.deleteAll(): Boolean {
  val files = listFiles()
  if (files != null) {
    for (child in files) {
      if (!child.deleteAll()) return false;
    }
  }

  for (i in 1..10) {
    if (delete() || !exists() ) return true;
    Thread.sleep(10) ;
  }
  return false;
}

inline fun suppressing(action : () -> Unit) : Unit {
  try {
    action()
  } catch (t: Throwable) {
    //NOP
  }
}

inline fun <T: Closeable, Y> using(handler : T, action : T.() -> Y) : Y {
  try {
    return handler.action()
  } finally {
    closeIt(handler)
  }
}

fun closeIt(handler: Closeable) {
  try {
    handler.close()
  } catch (t: Throwable) {
    //NOP
  }
}


fun File.loadUTF() : String = using(BufferedReader(InputStreamReader(FileInputStream(this), "utf-8"))) {
    com.google.common.io.CharStreams.toString(this)!!
  }

fun File.writeUTF(text : String) : Unit {
  using(BufferedWriter(OutputStreamWriter(FileOutputStream(this), "utf-8"))) {
    write(text)
  }
}

inline fun File.writeUTF(builder : () -> String) {
  writeUTF(builder())
}

tailrec fun <T : Annotation, R> Class<R>.getAnnotationRec(ax: Class<T>): T? {
  val root = this.getAnnotation(ax)
  if (root != null) return root
  val sup = this.superclass ?: return null
  return sup.getAnnotationRec(ax)
}
