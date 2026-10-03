package app.familygem

import app.familygem.util.Lineage
import org.folg.gedcom.model.ChildRef
import org.folg.gedcom.model.Family
import org.folg.gedcom.model.Gedcom
import org.folg.gedcom.model.ParentFamilyRef
import org.folg.gedcom.model.Person
import org.folg.gedcom.model.SpouseFamilyRef
import org.folg.gedcom.model.SpouseRef
import org.junit.Assert.assertEquals
import org.junit.Test

class LineageTest {

    private val gedcom = Gedcom()

    private fun person(id: String): Person {
        val person = Person()
        person.id = id
        gedcom.addPerson(person)
        return person
    }

    private fun family(id: String, husband: Person?, wife: Person?, vararg children: Person) {
        val family = Family()
        family.id = id
        husband?.let {
            family.addHusband(SpouseRef().apply { ref = it.id })
            it.addSpouseFamilyRef(SpouseFamilyRef().apply { ref = id })
        }
        wife?.let {
            family.addWife(SpouseRef().apply { ref = it.id })
            it.addSpouseFamilyRef(SpouseFamilyRef().apply { ref = id })
        }
        children.forEach {
            family.addChild(ChildRef().apply { ref = it.id })
            it.addParentFamilyRef(ParentFamilyRef().apply { ref = id })
        }
        gedcom.addFamily(family)
    }

    @Test
    fun `lineage includes only ancestors and descendants`() {
        val grandfather = person("GF")
        val grandmother = person("GM")
        val maternalGrandfather = person("MGF")
        val maternalGrandmother = person("MGM")
        val father = person("F")
        val mother = person("M")
        val uncle = person("U")
        val aunt = person("UW")
        val cousin = person("K")
        val fulcrum = person("X")
        val sibling = person("S")
        val wife = person("W")
        val secondWife = person("W2")
        val son = person("C1")
        val daughter = person("C2")
        val daughterInLaw = person("CW")
        val grandchild = person("G")
        family("F1", grandfather, grandmother, father, uncle)
        family("F7", maternalGrandfather, maternalGrandmother, mother)
        family("F2", father, mother, fulcrum, sibling)
        family("F5", uncle, aunt, cousin)
        family("F3", fulcrum, wife, son, daughter)
        family("F6", fulcrum, secondWife) // Marriage without children
        family("F4", son, daughterInLaw, grandchild)
        gedcom.createIndexes()

        val lineage = Lineage(fulcrum, gedcom)
        assertEquals(setOf("X", "F", "M", "GF", "GM", "MGF", "MGM", "C1", "C2", "G"), lineage.persons)
        assertEquals(setOf("F1", "F7", "F2", "F3", "F4"), lineage.families)

        // Seen from the grandchild, the line goes up through both parents and has no descendants
        val grandchildLineage = Lineage(grandchild, gedcom)
        assertEquals(setOf("G", "C1", "CW", "X", "W", "F", "M", "GF", "GM", "MGF", "MGM"), grandchildLineage.persons)
        assertEquals(setOf("F4", "F3", "F2", "F1", "F7"), grandchildLineage.families)
    }

    @Test
    fun `person alone has a lineage of one`() {
        val loner = person("L")
        gedcom.createIndexes()
        val lineage = Lineage(loner, gedcom)
        assertEquals(setOf("L"), lineage.persons)
        assertEquals(emptySet<String>(), lineage.families)
    }

    @Test
    fun `loop in the tree does not hang`() {
        val first = person("A")
        val second = person("B")
        family("F1", first, null, second) // A is parent of B
        family("F2", second, null, first) // B is parent of A
        gedcom.createIndexes()
        val lineage = Lineage(first, gedcom)
        assertEquals(setOf("A", "B"), lineage.persons)
        assertEquals(setOf("F1", "F2"), lineage.families)
    }
}
