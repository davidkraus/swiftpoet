/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet

/** A fully-qualified type name for top-level and member types.  */
class DeclaredTypeName internal constructor(
  names: List<String>
) : TypeName(), Comparable<DeclaredTypeName> {

  /**
   * Returns a class name created from the given parts. For example, calling this with module name
   * `"Swift"` and simple names `"Array"`, `"Iterator"` yields [Swift.Array.Iterator].
   */
  constructor(moduleName: String, simpleName: String, vararg simpleNames: String)
      : this(listOf(moduleName, simpleName, *simpleNames))

  /** From top to bottom. This will be `["Swift", "Array", "Iterator"]` for [Swift.Array.Iterator].  */
  private val names = names.toImmutableList()
  val canonicalName = if (names[0].isEmpty())
    names.subList(1, names.size).joinToString(".") else
    names.joinToString(".")

  /** Module name, like `"Swift"` for `Array.Iterator`.  */
  val moduleName get() = names[0]

  /** Simple name of this class, like `"Iterator"` for [Swift.Array.Iterator].  */
  val simpleName get() = names[names.size - 1]
  val simpleNames get() = names.subList(1, names.size)

  init {
    for (i in 1 until names.size) {
      require(names[i].isName) { "part ${names[i]} is keyword" }
    }
  }

  /**
   * Returns the enclosing class, like [Map] for `Map.Entry`. Returns null if this class
   * is not nested in another class.
   */
  fun enclosingTypeName(): DeclaredTypeName? {
    return if (names.size != 2)
      DeclaredTypeName(names.subList(0, names.size - 1)) else
      null
  }

  /**
   * Returns the top class in this nesting group. Equivalent to chained calls to
   * [DeclaredTypeName.enclosingClassName] until the result's enclosing class is null.
   */
  fun topLevelClassName() = DeclaredTypeName(names.subList(0, 2))

  /**
   * Returns a new [DeclaredTypeName] instance for the specified `name` as nested inside this
   * class.
   */
  fun nestedClass(name: String) = DeclaredTypeName(names + name)

  /**
   * Returns a class that shares the same enclosing package or class. If this class is enclosed by
   * another class, this is equivalent to `enclosingClassName().nestedClass(name)`. Otherwise
   * it is equivalent to `get(packageName(), name)`.
   */
  fun peerClass(name: String): DeclaredTypeName {
    val result = names.toMutableList()
    result[result.size - 1] = name
    return DeclaredTypeName(result)
  }

  override fun compareTo(other: DeclaredTypeName) = canonicalName.compareTo(other.canonicalName)

  override fun emit(out: CodeWriter) = out.emit(escapeKeywords(out.lookupName(this)))

  companion object {
    @JvmStatic fun typeName(qualifiedTypeName: String): DeclaredTypeName {
      val names = qualifiedTypeName.split('.')
      if (names.size < 2) {
        throw IllegalArgumentException("Type names MUST be qualified with their module name; to create a type for the 'current' module start the type with a '.' (e.g. '.MyType')")
      }
      return DeclaredTypeName(qualifiedTypeName.split('.'))
    }
  }
}

@JvmField val OPTIONAL = DeclaredTypeName.typeName("Swift.Optional")
@JvmField val ANY = DeclaredTypeName.typeName("Swift.Any")
@JvmField val VOID = DeclaredTypeName.typeName("Swift.Void")

@JvmField val INT = DeclaredTypeName.typeName("Swift.Int")
@JvmField val FLOAT = DeclaredTypeName.typeName("Swift.Float")
@JvmField val DOUBLE = DeclaredTypeName.typeName("Swift.Double")

@JvmField val INT8 = DeclaredTypeName.typeName("Swift.Int8")
@JvmField val UINT8 = DeclaredTypeName.typeName("Swift.UInt8")
@JvmField val INT16 = DeclaredTypeName.typeName("Swift.Int16")
@JvmField val UIN16 = DeclaredTypeName.typeName("Swift.UInt16")
@JvmField val INT32 = DeclaredTypeName.typeName("Swift.Int32")
@JvmField val UINT32 = DeclaredTypeName.typeName("Swift.UInt32")
@JvmField val INT64 = DeclaredTypeName.typeName("Swift.Int64")
@JvmField val UINT64 = DeclaredTypeName.typeName("Swift.UInt64")
@JvmField val FLOAT32 = DeclaredTypeName.typeName("Swift.Float32")
@JvmField val FLOAT64 = DeclaredTypeName.typeName("Swift.Float64")
@JvmField val FLOAT80 = DeclaredTypeName.typeName("Swift.Float80")

@JvmField val STRING = DeclaredTypeName.typeName("Swift.String")

@JvmField val BOOL = DeclaredTypeName.typeName("Swift.Bool")

@JvmField val ARRAY = DeclaredTypeName.typeName("Swift.Array")
@JvmField val SET = DeclaredTypeName.typeName("Swift.Set")
@JvmField val DICTIONARY = DeclaredTypeName.typeName("Swift.Dictionary")

@JvmField val CASE_ITERABLE = DeclaredTypeName.typeName("Swift.CaseIterable")