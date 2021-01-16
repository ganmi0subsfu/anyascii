package com.anyascii.build

import com.ibm.icu.lang.UCharacter
import com.ibm.icu.lang.UCharacter.UnicodeBlock
import com.ibm.icu.lang.UScript
import com.ibm.icu.text.UnicodeSet

typealias CodePoint = Int

typealias Script = Int

typealias CharacterCategory = Byte

fun String.isPrintableAscii() = all { it.toInt().isPrintableAscii() }

fun String.isAscii() = all { it.toInt().isAscii() }

fun CodePoint.isPrintableAscii() = this in 0x20..0x7e

fun CodePoint.isAscii() = this in 0..127

fun CodePoint.asString(): String = Character.toString(this)

val CodePoint.name: String get() = UCharacter.getName(this)

val CodePoint.nameAlias: String get() = UCharacter.getNameAlias(this) ?: UCharacter.getName(this)

fun CodePoint(name: String): CodePoint = UCharacter.getCharFromName(name)

fun codePoint(name: String): CodePoint? = UCharacter.getCharFromName(name).takeUnless { it == -1 }

fun String.toCodePoint(): CodePoint = codePointAt(0).also { check(length == Character.charCount(it)) { this } }

fun String.lower(): String = UCharacter.toLowerCase(this)

fun CodePoint.lower(): CodePoint = UCharacter.toLowerCase(this)

fun String.upper(): String = UCharacter.toUpperCase(this)

fun CodePoint.upper(): CodePoint = UCharacter.toUpperCase(this)

fun CodePoint.isLower(): Boolean = UCharacter.isLowerCase(this)

fun CodePoint.isUpper(): Boolean = UCharacter.isUpperCase(this)

val CodePoint.intValue: Int get() = UCharacter.getNumericValue(this).also { check(it >= 0) }

val CodePoint.floatValue: Double get() = UCharacter.getUnicodeNumericValue(this).also { check(it != UCharacter.NO_NUMERIC_VALUE) }

val CodePoint.isDefined: Boolean get() = UCharacter.isDefined(this)

val CodePoint.script: Script get() = UScript.getScript(this)

val CodePoint.category: CharacterCategory get() = UCharacter.getType(this).toByte()

val CodePoint.block: UnicodeBlock get() = UnicodeBlock.of(this)

fun String.codePointsArray(): IntArray = codePoints().toArray()

fun UnicodeSet.codePoints(): Iterable<CodePoint> = Iterable {
    iterator {
        for (r in ranges()) {
            for (cp in r.codepoint..r.codepointEnd) {
                yield(cp)
            }
        }
    }
}

val ALL_CODE_POINTS = codePoints("[:Assigned:]")

fun codePoints(script: Script): Iterable<CodePoint> = codePoints("[:${UScript.getShortName(script)}:]")

fun UnicodeBlock.codePoints(): Iterable<CodePoint> = codePoints("[[:Block=$this:] & [:Assigned:]]")

fun codePoints(pattern: String): Iterable<CodePoint> = UnicodeSet(pattern).codePoints()

fun Iterable<CodePoint>.filterDefined(): Iterable<CodePoint> = filter { it.isDefined }

inline fun Iterable<CodePoint>.filterName(predicate: (String) -> Boolean) = filter { predicate(it.name) }

fun String.stripLeading(c: Char) = dropWhile { it == c }

fun String.remove(s: String) = replace(s, "")