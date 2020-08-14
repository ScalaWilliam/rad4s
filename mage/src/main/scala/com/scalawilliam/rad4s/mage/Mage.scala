package com.scalawilliam.rad4s.mage

import scala.reflect.macros.whitebox

object Mage {

  /** Captures unary methods of TraitType, that return CaptureType, into a closure TraitType => CaptureType  */
  def mage[TraitType, CaptureType]: Map[String, TraitType => CaptureType] =
    macro impl[TraitType, CaptureType]

  def impl[Trait, CaptureType](c: whitebox.Context)(
      implicit t: c.WeakTypeTag[Trait],
      pt: c.WeakTypeTag[CaptureType]): c.universe.Tree = {
    import c.universe._
    val topClass     = weakTypeOf[Trait]
    val captureClass = weakTypeOf[CaptureType]

    if (!(topClass.typeSymbol.asClass.isTrait || topClass.typeSymbol.asClass.isClass)) {
      c.error(c.enclosingPosition, s"Expecting type to be a trait or a class")
    }
    val results = topClass.decls.flatMap { decl =>
      if (decl.isMethod && decl.typeSignature.finalResultType
            .weak_<:<(pt.tpe.finalResultType) && decl.typeSignature.paramLists.isEmpty && decl.typeSignature.typeArgs.isEmpty && decl.isPublic)
        Some {
          Literal(Constant(decl.name.toString)) -> q""" (x: $topClass) => x.${decl}"""
        } else None
    }

    q"Map[String, ${topClass} => ${captureClass}](..$results)"
  }

}
