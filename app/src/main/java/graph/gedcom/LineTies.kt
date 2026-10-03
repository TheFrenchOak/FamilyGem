package graph.gedcom

import org.folg.gedcom.model.Family
import org.folg.gedcom.model.Person

/* This file is placed in the package of GedcomGraph library to read the package-private members of the lines,
   which tell what persons and family each line connects. */

/** The family a line belongs to: the origin family of the child for a curve line, the family of the partners for any other line. */
val Line.family: Family?
    get() = when (this) {
        is CurveLine -> origin.spouseFamily
        is HorizontalLine -> leftPerson.spouseFamily
        is VerticalLine -> bond.familyNode.spouseFamily
        is NextLine -> bond?.familyNode?.spouseFamily
        is BackLine -> node.spouseFamily
        else -> null
    }

/** The child at the lower end of a curve line. */
val CurveLine.child: Person
    get() = personNode.person

/** The partner at the start (x1, y1) of a horizontal line. */
val HorizontalLine.firstPartner: Person
    get() = leftPerson.person

/** The partner at the end (x2, y2) of a horizontal line. */
val HorizontalLine.secondPartner: Person
    get() = rightPerson.person

/** Horizontal center of the bond between the two partners, where the line towards the children starts. */
val HorizontalLine.bondCenterX: Float
    get() = leftPerson.familyNode.bond.centerX()

/** The following partner reached by a next line. */
val NextLine.nextPartner: Person
    get() = partner.person
